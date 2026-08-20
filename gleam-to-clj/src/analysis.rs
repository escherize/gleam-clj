//! Typed analysis pipeline: run gleam-core's ModuleAnalyzer over a set of
//! module sources (stdlib + project + vendored deps), in import-topological
//! order, producing TypedModules. This replicates what gleam's package
//! compiler does internally, without the build system.

use std::collections::{HashMap, HashSet};

use camino::Utf8PathBuf;
use ecow::EcoString;
use gleam_core::analyse::ModuleAnalyzerConstructor;
use gleam_core::ast::{Definition, TypedModule};
use gleam_core::build::{Origin, Target};
use gleam_core::config::PackageConfig;
use src_span::LineNumbers;
use gleam_core::parse;
use gleam_core::type_::build_prelude;
use gleam_core::warning::TypeWarningEmitter;
use gleam_core::uid::UniqueIdGenerator;
use gleam_core::warning::WarningEmitter;

pub struct SourceModule {
    /// gleam module path, e.g. "gleam/dict"
    pub path: String,
    pub file_name: String,
    pub src: String,
    pub is_dep: bool,
    /// analyze-only modules (stdlib, gleeunit) are not re-emitted
    pub emit: bool,
}

pub struct AnalyzedModule {
    pub path: String,
    pub file_name: String,
    pub src: String,
    pub is_dep: bool,
    pub emit: bool,
    pub module: TypedModule,
    /// SCC groups of fn/const names in call-dependency order
    pub dependency_order: Vec<Vec<EcoString>>,
}

/// Analyze all sources. Panics loudly on parse or type errors — the input is
/// expected to be a valid Gleam project (it compiles under real gleam).
pub fn analyze(sources: Vec<SourceModule>) -> Vec<AnalyzedModule> {
    // Parse everything first so imports drive a topological order.
    let emitter = WarningEmitter::null();
    let mut parsed: Vec<(SourceModule, gleam_core::ast::UntypedModule)> = sources
        .into_iter()
        .map(|s| {
            let p = parse::parse_module(
                Utf8PathBuf::from(&s.file_name),
                &s.src,
                &emitter,
            )
            .unwrap_or_else(|e| panic!("parse error in {}: {e:?}", s.path));
            let mut ast = p.module;
            ast.name = EcoString::from(s.path.as_str());
            (s, ast)
        })
        .collect();

    // Kahn topo-sort by imports (only edges to modules in this set).
    let index: HashMap<String, usize> = parsed
        .iter()
        .enumerate()
        .map(|(i, (s, _))| (s.path.clone(), i))
        .collect();
    let mut deps_of: Vec<HashSet<usize>> = parsed
        .iter()
        .map(|(_, ast)| {
            ast.definitions
                .iter()
                .filter_map(|d| match &d.definition {
                    Definition::Import(i) => index.get(i.module.as_str()).copied(),
                    _ => None,
                })
                .collect()
        })
        .collect();
    let mut order: Vec<usize> = Vec::with_capacity(parsed.len());
    let mut done: HashSet<usize> = HashSet::new();
    while order.len() < parsed.len() {
        let before = order.len();
        for i in 0..parsed.len() {
            if !done.contains(&i) && deps_of[i].iter().all(|d| done.contains(d)) {
                order.push(i);
                let _ = done.insert(i);
                deps_of[i].clear();
            }
        }
        if order.len() == before {
            let stuck: Vec<&str> = (0..parsed.len())
                .filter(|i| !done.contains(i))
                .map(|i| parsed[i].0.path.as_str())
                .collect();
            panic!("import cycle among modules: {stuck:?}");
        }
    }

    // Analyze in order, accumulating module interfaces.
    let ids = UniqueIdGenerator::new();
    let mut interfaces = im::HashMap::new();
    let _ = interfaces.insert(
        EcoString::from(gleam_core::type_::PRELUDE_MODULE_NAME),
        build_prelude(&ids),
    );
    let mut config = PackageConfig::default();
    config.name = "gleam_clj_build".into();
    let direct_dependencies: HashMap<EcoString, ()> = HashMap::new();
    let dev_dependencies: HashSet<EcoString> = HashSet::new();
    let type_warnings = TypeWarningEmitter::null();

    let mut out: Vec<Option<AnalyzedModule>> = (0..parsed.len()).map(|_| None).collect();
    for i in order {
        let (source, ast) = std::mem::replace(
            &mut parsed[i],
            (
                SourceModule {
                    path: String::new(),
                    file_name: String::new(),
                    src: String::new(),
                    is_dep: false,
                    emit: false,
                },
                gleam_core::ast::UntypedModule {
                    name: "".into(),
                    documentation: vec![],
                    type_info: (),
                    definitions: vec![],
                    names: Default::default(),
                    unused_definition_positions: Default::default(),
                },
            ),
        );
        // Dependency order comes from the untyped defs (the typed module
        // regroups definitions by kind and loses source order).
        let mut functions = Vec::new();
        let mut constants = Vec::new();
        for def in crate::active_defs(&ast) {
            match &def.definition {
                Definition::Function(f) => functions.push(f.clone()),
                Definition::ModuleConstant(c) => constants.push(c.clone()),
                _ => {}
            }
        }
        let dependency_order: Vec<Vec<EcoString>> =
            gleam_core::call_graph::into_dependency_order(functions, constants)
                .unwrap_or_else(|e| panic!("call graph error in {}: {e:?}", source.path))
                .into_iter()
                .map(|group| {
                    group
                        .into_iter()
                        .map(|node| match node {
                            gleam_core::call_graph::CallGraphNode::Function(f) => {
                                f.name.expect("fn name").1
                            }
                            gleam_core::call_graph::CallGraphNode::ModuleConstant(c) => c.name,
                        })
                        .collect()
                })
                .collect();
        let line_numbers = LineNumbers::new(&source.src);
        let typed = ModuleAnalyzerConstructor::<()> {
            target: Target::Erlang,
            ids: &ids,
            origin: Origin::Src,
            importable_modules: &interfaces,
            warnings: &type_warnings,
            direct_dependencies: &direct_dependencies,
            dev_dependencies: &dev_dependencies,
            target_support: gleam_core::analyse::TargetSupport::NotEnforced,
            package_config: &config,
        }
        .infer_module(
            ast,
            line_numbers,
            Utf8PathBuf::from(&source.file_name),
        )
        .into_result()
        .unwrap_or_else(|e| panic!("type error in {}: {e:?}", source.path));
        let _ = interfaces.insert(
            EcoString::from(source.path.as_str()),
            typed.type_info.clone(),
        );
        out[i] = Some(AnalyzedModule {
            path: source.path,
            file_name: source.file_name,
            src: source.src,
            is_dep: source.is_dep,
            emit: source.emit,
            module: typed,
            dependency_order,
        });
    }
    out.into_iter().map(|m| m.expect("analyzed")).collect()
}
