//! Typed emitter: TypedModule -> readable Clojure. Name resolution comes from
//! the type checker (ValueConstructor / ModuleSelect / PatternConstructor),
//! which replaces the untyped emitter's scope tracking, constructor registry,
//! and labelled-argument machinery entirely.

use std::collections::{HashMap, HashSet};
use std::fmt::Write as _;

use ecow::EcoString;
use gleam_core::ast::{
    ArgNames, AssignmentKind, BinOp, CallArg, Clause, FunctionLiteralKind, Pattern, Publicity,
    Statement, TypedAssert, TypedClauseGuard, TypedCustomType, TypedExpr, TypedFunction,
    TypedModuleConstant, TypedPattern, TypedStatement,
};
use gleam_core::type_::{PatternConstructor, ValueConstructor, ValueConstructorVariant};

use crate::analysis::AnalyzedModule;
use crate::{
    class_ref, clj_string, clojure_core_names, fits, int_lit, kebab, local_fn_name, sp, user_var,
    JAVA_LANG,
};

/// Constructor field names for every analyzed module, keyed (module, ctor).
pub struct TypedGlobal {
    pub ctor_fields: HashMap<(String, String), Vec<String>>,
    /// (module, type name) -> constructor names
    pub type_variants: HashMap<(String, String), Vec<String>>,
}

pub fn constructor_field_names(c: &gleam_core::ast::RecordConstructor<std::sync::Arc<gleam_core::type_::Type>>) -> Vec<String> {
    let n = c.arguments.len();
    c.arguments
        .iter()
        .enumerate()
        .map(|(i, a)| match &a.label {
            Some((_, l)) => kebab(l.as_str()),
            None if n == 1 => "value".to_string(),
            None => format!("f{i}"),
        })
        .collect()
}

pub fn build_typed_global(modules: &[AnalyzedModule]) -> TypedGlobal {
    let mut ctor_fields = HashMap::new();
    let _ = ctor_fields.insert(
        ("gleam".to_string(), "Ok".to_string()),
        vec!["value".to_string()],
    );
    let _ = ctor_fields.insert(
        ("gleam".to_string(), "Error".to_string()),
        vec!["value".to_string()],
    );
    let mut type_variants = HashMap::new();
    for m in modules {
        for t in &m.module.definitions.custom_types {
            let _ = type_variants.insert(
                (m.path.clone(), t.name.to_string()),
                t.constructors.iter().map(|c| c.name.to_string()).collect(),
            );
            for c in &t.constructors {
                let _ = ctor_fields.insert(
                    (m.path.clone(), c.name.to_string()),
                    constructor_field_names(c),
                );
            }
        }
    }
    TypedGlobal { ctor_fields, type_variants }
}

struct Emit<'a> {
    module_path: &'a str,
    /// modules referenced by schema predicates; they must be required even
    /// when nothing else in the body uses them
    schema_requires: std::cell::RefCell<HashSet<String>>,
    /// module -> alias used in this file
    aliases: HashMap<String, String>,
    global: &'a TypedGlobal,
    externals: &'a HashMap<(String, String), String>,
    is_dep: bool,
    file: String,
    line_starts: Vec<u32>,
}

struct Tail {
    name: String,
}

impl Emit<'_> {
    fn line_of(&self, byte: u32) -> usize {
        self.line_starts.partition_point(|&s| s <= byte)
    }

    fn alias_of(&self, module: &str) -> &str {
        self.aliases
            .get(module)
            .unwrap_or_else(|| panic!("{}: no import alias for module {module}", self.module_path))
    }

    fn fn_ref(&self, module: &str, name: &str) -> String {
        if module == self.module_path {
            local_fn_name(&kebab(name))
        } else {
            format!("{}/{}", self.alias_of(module), local_fn_name(&kebab(name)))
        }
    }

    fn ctor_fields(&self, module: &str, name: &str) -> &Vec<String> {
        self.global
            .ctor_fields
            .get(&(module.to_string(), name.to_string()))
            .unwrap_or_else(|| panic!("unknown constructor {module}.{name}"))
    }

    /// Value-position reference to a constructor. Zero-arity constructors ARE
    /// the constructed value.
    fn ctor_value(&self, module: &str, name: &str, arity: u16) -> String {
        match (module, name) {
            ("gleam", "True") => return "true".into(),
            ("gleam", "False") => return "false".into(),
            ("gleam", "Nil") => return "nil".into(),
            _ => {}
        }
        let f = self.ctor_fn(module, name);
        if arity == 0 {
            format!("({f})")
        } else {
            f
        }
    }

    fn ctor_fn(&self, module: &str, name: &str) -> String {
        if module == "gleam" {
            format!("p/->{name}")
        } else if module == self.module_path {
            format!("->{name}")
        } else {
            format!("{}/->{name}", self.alias_of(module))
        }
    }

    /// Reference to a variant's generated predicate fn (`Circle?`).
    fn pred_ref(&self, module: &str, name: &str) -> String {
        if module == "gleam" {
            let _ = self.schema_requires.borrow_mut().insert("gleam.prelude".into());
            return format!("p/{name}?");
        }
        if module == self.module_path {
            return format!("{name}?");
        }
        let ns = kebab(&module.replace("/", "."));
        let _ = self.schema_requires.borrow_mut().insert(ns.clone());
        match self.aliases.get(module) {
            Some(alias) => format!("{alias}/{name}?"),
            None => format!("{ns}/{name}?"),
        }
    }

    fn ctor_class(&self, module: &str, name: &str) -> String {
        if module == "gleam" {
            class_ref(name)
        } else if module == self.module_path {
            name.to_string()
        } else {
            format!("{}.{name}", module.replace("/", "."))
        }
    }
}

pub fn emit_module(
    am: &AnalyzedModule,
    global: &TypedGlobal,
    externals: &HashMap<(String, String), String>,
) -> String {
    let mut line_starts = vec![0u32];
    for (i, b) in am.src.bytes().enumerate() {
        if b == b'\n' {
            line_starts.push(i as u32 + 1);
        }
    }
    let defs = &am.module.definitions;
    let mut aliases = HashMap::new();
    let mut requires: Vec<(String, String)> = Vec::new();
    for import in &defs.imports {
        let Some(alias) = import.used_name() else { continue };
        let _ = aliases.insert(import.module.to_string(), alias.to_string());
        if import.module.as_str() != "gleam" {
            requires.push((kebab(&import.module.replace("/", ".")), alias.to_string()));
        }
    }
    let ctx = Emit {
        module_path: &am.path,
        schema_requires: std::cell::RefCell::new(HashSet::new()),
        aliases,
        global,
        externals,
        is_dep: am.is_dep,
        file: am.file_name.clone(),
        line_starts,
    };

    // Namespaces of externals used by this module.
    let mut external_nses: Vec<String> = defs
        .functions
        .iter()
        .filter_map(|f| {
            let name = f.name.as_ref().map(|(_, n)| n.to_string())?;
            if let Some(target) = externals.get(&(am.path.clone(), name)) {
                return target.split('/').next().map(String::from);
            }
            if ctx.is_dep {
                return None;
            }
            f.external_javascript.as_ref().map(|(m, _, _)| m.to_string())
        })
        .filter(|m| !m.contains('/'))
        .collect();
    external_nses.sort();
    external_nses.dedup();

    let mut require_entries: Vec<String> = requires
        .iter()
        .map(|(ns, alias)| format!("[{ns} :as {alias}]"))
        .collect();
    require_entries.push("[gleam.prelude :as p]".into());
    require_entries.extend(external_nses.iter().map(|ns| format!("[{ns}]")));
    require_entries.sort();
    require_entries.dedup();

    let mut excludes: Vec<String> = defs
        .functions
        .iter()
        .filter_map(|f| f.name.as_ref().map(|(_, n)| local_fn_name(&kebab(n.as_str()))))
        .chain(defs.constants.iter().map(|c| user_var(&kebab(c.name.as_str()))))
        .filter(|n| clojure_core_names().contains(n.as_str()))
        .collect();
    excludes.sort();
    excludes.dedup();

    let mut out = String::new();
    // All variant names in the module: a type predicate `<Type>?` is only
    // safe when no variant anywhere claims `<Type>` (else the two predicates
    // collide).
    let all_variant_names: std::collections::HashSet<&str> = defs
        .custom_types
        .iter()
        .flat_map(|t| t.constructors.iter().map(|c| c.name.as_str()))
        .collect();
    for t in &defs.custom_types {
        emit_custom_type(&mut out, t, &am.path, &all_variant_names);
    }

    // Definitions in call-dependency order (computed pre-analysis); a declare
    // only for mutual-recursion groups.
    let fn_by_name: HashMap<&str, &TypedFunction> = defs
        .functions
        .iter()
        .filter_map(|f| f.name.as_ref().map(|(_, n)| (n.as_str(), f)))
        .collect();
    let const_by_name: HashMap<&str, &TypedModuleConstant> =
        defs.constants.iter().map(|c| (c.name.as_str(), c)).collect();
    // Reachability from public fns and constants: private fns orphaned by
    // externals-map overrides are dead code (and clj-kondo agrees).
    let mut called: HashMap<String, Vec<String>> = HashMap::new();
    for f in &defs.functions {
        let Some((_, fname)) = &f.name else { continue };
        // An externals-overridden fn emits as a def alias; its Gleam body
        // never exists, so it contributes no call edges.
        if externals.contains_key(&(am.path.clone(), fname.to_string())) {
            let _ = called.insert(fname.to_string(), Vec::new());
            continue;
        }
        let mut uses = Vec::new();
        for stmt in &f.body {
            let walk = |e: &TypedExpr, uses: &mut Vec<String>| {
                visit_exprs_dyn(e, &mut |x| {
                    if let TypedExpr::Var { constructor, .. } = x {
                        if let ValueConstructorVariant::ModuleFn { module, name, .. } =
                            &constructor.variant
                        {
                            if module.as_str() == am.path {
                                uses.push(name.to_string());
                            }
                        }
                    }
                });
            };
            match stmt {
                Statement::Expression(e) => walk(e, &mut uses),
                Statement::Assignment(a) => walk(&a.value, &mut uses),
                Statement::Use(u) => walk(&u.call, &mut uses),
                Statement::Assert(a) => walk(&a.value, &mut uses),
            }
        }
        let _ = called.insert(fname.to_string(), uses);
    }
    let mut reachable: std::collections::HashSet<String> = defs
        .functions
        .iter()
        .filter(|f| !matches!(f.publicity, Publicity::Private))
        .filter_map(|f| f.name.as_ref().map(|(_, n)| n.to_string()))
        .collect();
    // Constants can hold fn references; they always emit, so those fns are
    // roots too.
    for c in &defs.constants {
        collect_constant_fn_refs(&c.value, &am.path, &mut reachable);
    }
    let mut frontier: Vec<String> = reachable.iter().cloned().collect();
    while let Some(name) = frontier.pop() {
        for callee in called.get(&name).cloned().unwrap_or_default() {
            if reachable.insert(callee.clone()) {
                frontier.push(callee);
            }
        }
    }

    // Library builds set GLEAM_CLJ_NO_MAIN: the module is required, not run,
    // so `main` (typically a BEAM-side self-check) and its `-main` wrapper are
    // dropped entirely rather than shipped as dead entry-point code.
    let suppress_main = std::env::var_os("GLEAM_CLJ_NO_MAIN").is_some();
    let mut has_pub_main = false;
    for group in &am.dependency_order {
        if group.len() > 1 {
            let names: Vec<String> = group
                .iter()
                .map(|n| local_fn_name(&kebab(n.as_str())))
                .collect();
            let _ = writeln!(out, "\n(declare {})", names.join(" "));
        }
        for name in group {
            if let Some(f) = fn_by_name.get(name.as_str()) {
                if matches!(f.publicity, Publicity::Private)
                    && !reachable.contains(name.as_str())
                {
                    continue;
                }
                let is_main =
                    !matches!(f.publicity, Publicity::Private) && name.as_str() == "main";
                if is_main && suppress_main {
                    continue;
                }
                out.push('\n');
                out.push_str(&emit_function(&ctx, f));
                if is_main {
                    has_pub_main = true;
                }
            } else if let Some(c) = const_by_name.get(name.as_str()) {
                let private = if matches!(c.publicity, Publicity::Private) { "^:private " } else { "" };
                let _ = write!(
                    out,
                    "\n(def {private}{} {})\n",
                    user_var(&kebab(c.name.as_str())),
                    emit_constant(&ctx, &c.value)
                );
            }
        }
    }
    if has_pub_main {
        out.push_str("\n(defn -main [& _]\n  (main))\n");
    }

    // Header last: requires and imports are emitted only when the body
    // actually uses them (clj-kondo: unused namespaces / imports).
    let body = out;
    let mut header = String::new();
    let _ = writeln!(header, "(ns {}", kebab(&am.path.replace("/", ".")));
    // Gleam module doc (`////` lines) becomes the namespace docstring.
    let module_doc = am.module_doc.trim();
    if !module_doc.is_empty() {
        let escaped = module_doc.replace('\\', "\\\\").replace('"', "\\\"");
        // Continuation lines align under the docstring's text column (one
        // column past the opening quote at indent 2 => column 3).
        let indented = escaped.replace('\n', "\n   ");
        let _ = writeln!(header, "  \"{indented}\"");
    }
    if !excludes.is_empty() {
        let _ = writeln!(header, "  (:refer-clojure :exclude [{}])", excludes.join(" "));
    }
    let schema_requires = ctx.schema_requires.borrow();
    let needs_prelude = body.contains("p/")
        || body.contains("gleam.prelude.")
        || schema_requires.contains("gleam.prelude");
    let self_ns = kebab(&am.path.replace("/", "."));
    let extra_requires: Vec<String> = {
        let mut v: Vec<String> = schema_requires
            .iter()
            .filter(|ns| {
                ns.as_str() != "gleam.prelude"
                    && ns.as_str() != self_ns
                    && !require_entries.iter().any(|e| e.contains(&format!("[{ns} ")))
            })
            .map(|ns| format!("[{ns}]"))
            .collect();
        v.sort();
        v
    };
    let kept: Vec<&String> = require_entries
        .iter()
        .filter(|entry| {
            if entry.as_str() == "[gleam.prelude :as p]" {
                return needs_prelude;
            }
            if let Some((ns, alias)) = entry
                .trim_start_matches('[')
                .trim_end_matches(']')
                .split_once(" :as ")
            {
                body.contains(&format!("{alias}/")) || body.contains(&format!("{ns}."))
            } else {
                true
            }
        })
        .collect();
    let kept: Vec<&String> = kept.into_iter().chain(extra_requires.iter()).collect();
    if !kept.is_empty() {
        let _ = writeln!(header, "  (:require");
        for (i, entry) in kept.iter().enumerate() {
            let end = if i + 1 == kept.len() { ")" } else { "" };
            // Required only so its record classes exist at load time?
            // clj-kondo can't see class usage as ns usage; say so explicitly.
            let class_only = entry
                .trim_start_matches('[')
                .trim_end_matches(']')
                .split_once(" :as ")
                .is_some_and(|(ns, alias)| {
                    !body.contains(&format!("{alias}/")) && body.contains(&format!("{ns}."))
                });
            if class_only {
                let _ = writeln!(header, "   #_{{:clj-kondo/ignore [:unused-namespace]}}");
            }
            let _ = writeln!(header, "   {entry}{end}");
        }
    }
    let needs_ok = body.contains("(instance? Ok ") || body.contains("instance? Ok)");
    if needs_ok {
        let _ = writeln!(header, "  (:import (gleam.prelude Ok))");
    }
    // close the ns form
    let header = {
        let mut h = header.trim_end().to_string();
        h.push_str(")\n");
        h
    };
    format!("{header}{body}")
}

/// Gleam type -> malli schema (as emitted Clojure code; custom types become
/// instance? predicates over their variant record classes).
fn malli_type(ctx: &Emit, t: &gleam_core::type_::Type) -> String {
    use gleam_core::type_::{Type, TypeVar};
    match t {
        Type::Var { type_ } => match &*type_.borrow() {
            TypeVar::Link { type_ } => malli_type(ctx, type_),
            _ => ":any".into(),
        },
        Type::Tuple { elements } => {
            let parts: Vec<String> = elements.iter().map(|e| malli_type(ctx, e)).collect();
            format!("[:tuple {}]", parts.join(" "))
        }
        Type::Fn { arguments, return_ } => {
            let parts: Vec<String> = arguments.iter().map(|a| malli_type(ctx, a)).collect();
            let cat = if parts.is_empty() {
                "[:cat]".to_string()
            } else {
                format!("[:cat {}]", parts.join(" "))
            };
            format!("[:=> {cat} {}]", malli_type(ctx, return_))
        }
        Type::Named { module, name, arguments, .. } => {
            match (module.as_str(), name.as_str()) {
                ("gleam", "Int") => ":int".into(),
                ("gleam", "Float") => ":double".into(),
                ("gleam", "String") => ":string".into(),
                ("gleam", "Bool") => ":boolean".into(),
                ("gleam", "Nil") => ":nil".into(),
                ("gleam", "BitArray") => "[:vector :int]".into(),
                ("gleam", "UtfCodepoint") => ":int".into(),
                ("gleam", "List") => {
                    let inner = arguments
                        .first()
                        .map(|a| malli_type(ctx, a))
                        .unwrap_or_else(|| ":any".into());
                    format!("[:sequential {inner}]")
                }
                ("gleam", "Result") => {
                    // Typed payloads: (p/result-of ok err) builds the
                    // variant-plus-:value schema in the prelude. The pred_ref
                    // calls still run for their schema_requires side effect.
                    let _ = ctx.pred_ref("gleam", "Ok");
                    let ok = arguments
                        .first()
                        .map(|a| malli_type(ctx, a))
                        .unwrap_or_else(|| ":any".into());
                    let err = arguments
                        .get(1)
                        .map(|a| malli_type(ctx, a))
                        .unwrap_or_else(|| ":any".into());
                    format!("(p/result-of {ok} {err})")
                }
                ("gleam/dict", "Dict") => {
                    let k = arguments
                        .first()
                        .map(|a| malli_type(ctx, a))
                        .unwrap_or_else(|| ":any".into());
                    let v = arguments
                        .get(1)
                        .map(|a| malli_type(ctx, a))
                        .unwrap_or_else(|| ":any".into());
                    format!("[:map-of {k} {v}]")
                }
                (m, n) => {
                    let Some(variants) = ctx
                        .global
                        .type_variants
                        .get(&(m.to_string(), n.to_string()))
                    else {
                        return ":any".into();
                    };
                    // A multi-variant type whose name no variant claims has a
                    // single `<Type>?` interface predicate — reference that
                    // instead of or-ing every variant.
                    let has_type_pred =
                        variants.len() > 1 && variants.iter().all(|v| v != n);
                    if has_type_pred {
                        // A java.lang-colliding type name (e.g. glance's
                        // Error) never gets a `Name?` type predicate; its
                        // marker protocol interface always exists, so check
                        // that class directly. pred_ref is still called for
                        // its schema_requires side effect.
                        if JAVA_LANG.contains(&n) {
                            let _ = ctx.pred_ref(m, n);
                            let class_ns = kebab(&m.replace('/', ".")).replace('-', "_");
                            return format!("[:fn (fn [v] (instance? {class_ns}.I{n} v))]");
                        }
                        return format!("[:fn {}]", ctx.pred_ref(m, n));
                    }
                    let checks: Vec<String> = variants
                        .iter()
                        .map(|v| format!("[:fn {}]", ctx.pred_ref(m, v)))
                        .collect();
                    if checks.len() == 1 {
                        checks.into_iter().next().expect("one variant")
                    } else {
                        format!("[:or {}]", checks.join(" "))
                    }
                }
            }
        }
    }
}

fn emit_custom_type(
    out: &mut String,
    t: &TypedCustomType,
    module_path: &str,
    all_variant_names: &std::collections::HashSet<&str>,
) {
    let _ = write!(out, "\n;; type {}\n", t.name);

    // A marker protocol gives the type's variants a shared Clojure identity —
    // the sum type as a real thing, not just a comment. Each variant record
    // implements it, so the type predicate is an O(1) `instance?` on the
    // protocol's generated Java interface (as fast as any variant check, NOT
    // a slow `satisfies?`).
    //
    // The protocol interface name is always suffixed with `Type` so it never
    // collides with a variant record of the same name (`type S { S F }`). The
    // type predicate `<Type>?` is emitted only when no variant already claims
    // that name — otherwise `S?` would be ambiguous with the S variant, so we
    // leave the variant predicate owning it and rely on the interface for the
    // rare "is it any S" question.
    let type_name = t.name.as_str();
    // Marker protocol named with the Clojure `I<Name>` interface convention;
    // it can only collide with a Gleam variant literally named `I<Type>`,
    // which is vanishingly unlikely for UpperCamel variant names.
    let proto = format!("I{type_name}");
    // A variant literally named I<Type> would clobber the marker protocol
    // (defrecord IX after defprotocol IX) and fail at load with an opaque
    // Clojure error. Refuse at build time instead.
    if t.constructors.iter().any(|c| c.name.as_str() == proto) {
        panic!(
            "module {module_path} type {type_name}: variant {proto} collides with the \
             type's generated marker protocol {proto} — rename the variant"
        );
    }
    let variant_names_type = all_variant_names.contains(type_name);
    let _ = writeln!(out, "(defprotocol {proto})");

    for c in &t.constructors {
        if JAVA_LANG.contains(&c.name.as_str()) {
            let _ = writeln!(out, "(ns-unmap *ns* '{})", c.name);
        }
        let fields: Vec<String> = constructor_field_names(c)
            .iter()
            .zip(&c.arguments)
            .map(|(n, a)| match jvm_hint(&a.type_) {
                Some(h) => format!("{h} {n}"),
                None => n.clone(),
            })
            .collect();
        let _ = writeln!(out, "(defrecord {} [{}] {proto})", c.name, fields.join(" "));
        let _ = writeln!(
            out,
            "(defn {}? \"True if `v` is a {} value.\" [v] (instance? {} v))",
            c.name, c.name, c.name
        );
    }

    if !variant_names_type && !JAVA_LANG.contains(&type_name) {
        // `defprotocol` binds a var to the protocol map; the generated Java
        // interface is a class named <munged-ns>.<Proto>. `instance?` needs
        // the class, and Clojure munges `-` to `_` in class names, so build
        // the class ns from the raw path (kebab for the var, underscores for
        // the class).
        let class_ns = kebab(&module_path.replace("/", ".")).replace('-', "_");
        let _ = writeln!(
            out,
            "(defn {type_name}? \"True if `v` is any {type_name} value.\" [v] (instance? {class_ns}.{proto} v))"
        );
    }
}

/// Malli function schema for a fn's checked signature, as (input, output).
fn fn_schema_parts(ctx: &Emit, f: &TypedFunction) -> (String, String) {
    let args: Vec<String> = f.arguments.iter().map(|a| malli_type(ctx, &a.type_)).collect();
    let cat = if args.is_empty() {
        "[:cat]".to_string()
    } else {
        format!("[:cat {}]", args.join(" "))
    };
    (cat, malli_type(ctx, &f.return_type))
}

fn fn_schema(ctx: &Emit, f: &TypedFunction) -> String {
    let (cat, ret) = fn_schema_parts(ctx, f);
    format!("[:=> {cat} {ret}]")
}

/// `file.gleam:line` of a fn's definition, for :gleam/src metadata.
fn gleam_src(ctx: &Emit, f: &TypedFunction) -> String {
    format!("{}:{}", ctx.file, ctx.line_of(f.location.start))
}

/// The defn attr-map: the Gleam source location, plus the function schema for
/// public fns — wrapped when it would overflow the line (the return schema
/// aligns under the argument schema, :gleam/src under :malli/schema).
fn fn_attr(ctx: &Emit, f: &TypedFunction, ind: usize, public: bool) -> String {
    let src_entry = format!(":gleam/src \"{}\"", gleam_src(ctx, f));
    if !public {
        return format!("{{{src_entry}}}");
    }
    let (cat, ret) = fn_schema_parts(ctx, f);
    let inline = format!("{{:malli/schema [:=> {cat} {ret}] {src_entry}}}");
    if fits(&inline, ind) {
        return inline;
    }
    let sep = format!("\n{}", sp(ind + 1));
    let schema_line = format!("{{:malli/schema [:=> {cat} {ret}]");
    if fits(&schema_line, ind) {
        return format!("{schema_line}{sep}{src_entry}}}");
    }
    let pad = ind + "{:malli/schema [:=> ".len();
    format!("{{:malli/schema [:=> {cat}\n{}{ret}]{sep}{src_entry}}}", sp(pad))
}

/// One-line Gleam signature for docstrings: `name(x: Int, y: Float) -> Bool`.
fn gleam_signature(f: &TypedFunction, gleam_name: &str) -> String {
    let mut printer = gleam_core::type_::pretty::Printer::new();
    let mut one_line = |t: &gleam_core::type_::Type| {
        printer.pretty_print(t, 0).split_whitespace().collect::<Vec<_>>().join(" ")
    };
    let args: Vec<String> = f
        .arguments
        .iter()
        .map(|a| {
            let t = one_line(&a.type_);
            match &a.names {
                ArgNames::Named { name, .. } | ArgNames::Discard { name, .. } => {
                    format!("{name}: {t}")
                }
                ArgNames::NamedLabelled { label, name, .. }
                | ArgNames::LabelledDiscard { label, name, .. } => {
                    format!("{label} {name}: {t}")
                }
            }
        })
        .collect();
    let ret = one_line(&f.return_type);
    format!("{gleam_name}({}) -> {ret}", args.join(", "))
}

/// JVM type hint for a checked Gleam type. Int is deliberately unhinted:
/// Gleam Int is arbitrary-precision (the promoting +'/-'/*' ops), so ^long
/// would be wrong the moment a value overflows into BigInt.
fn jvm_hint(t: &gleam_core::type_::Type) -> Option<&'static str> {
    use gleam_core::type_::{Type, TypeVar};
    match t {
        Type::Var { type_ } => match &*type_.borrow() {
            TypeVar::Link { type_ } => jvm_hint(type_),
            _ => None,
        },
        Type::Named { module, name, .. } => match (module.as_str(), name.as_str()) {
            ("gleam", "Float") => Some("^double"),
            // Fully qualified: a Gleam variant named `String` ns-unmaps the
            // java.lang default, after which a bare ^String cannot resolve.
            ("gleam", "String") => Some("^java.lang.String"),
            _ => None,
        },
        _ => None,
    }
}

fn emit_function(ctx: &Emit, f: &TypedFunction) -> String {
    let gleam_name = f.name.as_ref().expect("function name").1.to_string();
    let name = local_fn_name(&kebab(&gleam_name));
    let public = !matches!(f.publicity, Publicity::Private);
    let def_meta = if public {
        format!("^{{:malli/schema {} :gleam/src \"{}\"}} ", fn_schema(ctx, f), gleam_src(ctx, f))
    } else {
        format!("^{{:gleam/src \"{}\"}} ", gleam_src(ctx, f))
    };
    if let Some(target) = ctx.externals.get(&(ctx.module_path.to_string(), gleam_name.clone())) {
        return format!("(def {def_meta}{name} {target})\n");
    }
    if let Some((module, fun, _)) = &f.external_javascript {
        let looks_clojure = !module.contains('/') && !module.ends_with(".mjs");
        if looks_clojure && !ctx.is_dep {
            return format!("(def {def_meta}{name} {module}/{fun})\n");
        }
        if f.body.is_empty() {
            panic!(
                "module {} fn {name} requires a native external ({module}/{fun}) and has \
                 no Gleam fallback body — supply a Clojure implementation via \
                 clojure-externals.txt",
                ctx.module_path
            );
        }
    }
    if f.body.is_empty() {
        panic!(
            "fn {name} in {} has no body and no usable external for this backend",
            ctx.module_path
        );
    }
    let defn = if matches!(f.publicity, Publicity::Private) { "defn-" } else { "defn" };

    // Body first: a primitive arg hint is a compile error when a boxed
    // expression reaches its `recur` slot, so self-recursive fns keep
    // object args. Primitive fn interfaces also stop at 4 params.
    let stmts: Vec<&TypedStatement> = f.body.iter().collect();
    let tail = Tail { name: name.clone() };
    let body = emit_body(ctx, &stmts, 2, Some(&tail));
    let allow_prim_args = f.arguments.len() <= 4 && !body.contains("(recur");
    let args: Vec<String> = f
        .arguments
        .iter()
        .map(|a| {
            let n = arg_name(a);
            match jvm_hint(&a.type_) {
                Some(h) if h != "^double" || allow_prim_args => format!("{h} {n}"),
                _ => n,
            }
        })
        .collect();
    let arg_vec = {
        let inner = args.join(" ");
        match jvm_hint(&f.return_type) {
            Some("^double") if f.arguments.len() > 4 => format!("[{inner}]"),
            Some(h) => format!("{h} [{inner}]"),
            None => format!("[{inner}]"),
        }
    };

    // Docstring: the checked Gleam signature, then the doc comment if any.
    let mut raw = gleam_signature(f, &gleam_name);
    if let Some((_, doc)) = &f.documentation {
        raw.push_str("\n\n");
        raw.push_str(doc.trim_end());
    }
    let mut rendered = String::new();
    for (i, l) in raw.lines().enumerate() {
        let e = l.trim().replace('\\', "\\\\").replace('"', "\\\"");
        if i > 0 {
            rendered.push('\n');
            if !e.is_empty() {
                rendered.push_str("   ");
            }
        }
        rendered.push_str(&e);
    }

    let mut out = format!("({defn} {name}");
    let _ = write!(out, "\n  \"{rendered}\"");
    let _ = write!(out, "\n  {}", fn_attr(ctx, f, 2, public));
    let _ = write!(out, "\n  {arg_vec}\n  ");
    out.push_str(&body);
    out.push_str(")\n");
    out
}

fn arg_name(a: &gleam_core::ast::TypedArg) -> String {
    match &a.names {
        ArgNames::Named { name, .. } | ArgNames::NamedLabelled { name, .. } => {
            user_var(&kebab(name.as_str()))
        }
        ArgNames::Discard { .. } | ArgNames::LabelledDiscard { .. } => "_".into(),
    }
}

fn emit_body(ctx: &Emit, stmts: &[&TypedStatement], ind: usize, tail: Option<&Tail>) -> String {
    if stmts.is_empty() {
        return "nil".into();
    }
    match stmts[0] {
        Statement::Assignment(a) => match &a.kind {
            AssignmentKind::Let | AssignmentKind::Generated => {
                let mut binds: Vec<(String, String)> = Vec::new();
                let mut i = 0;
                while i < stmts.len() {
                    let Statement::Assignment(a2) = stmts[i] else { break };
                    if !matches!(a2.kind, AssignmentKind::Let | AssignmentKind::Generated) {
                        break;
                    }
                    let Some(form) = destructure_binding(ctx, &a2.pattern) else { break };
                    let val_ind = ind + 6 + form.len() + 1;
                    binds.push((form, emit_expr(ctx, &a2.value, val_ind, None)));
                    i += 1;
                }
                if binds.is_empty() {
                    panic!("unsupported let pattern: {:?}", a.pattern);
                }
                let rest = emit_body(ctx, &stmts[i..], ind + 2, tail);
                let bind_ind = ind + 6;
                let binds_str = binds
                    .iter()
                    .map(|(n, v)| format!("{n} {v}"))
                    .collect::<Vec<_>>()
                    .join(&format!("\n{}", sp(bind_ind)));
                merged_let(&binds_str, rest, ind)
            }
            AssignmentKind::Assert { message, .. } => {
                emit_let_assert(ctx, a, message.as_ref(), stmts, ind, tail)
            }
        },
        Statement::Expression(e) => {
            if stmts.len() == 1 {
                emit_expr(ctx, e, ind, tail)
            } else {
                let s = emit_expr(ctx, e, ind, None);
                format!("{s}\n{}{}", sp(ind), emit_body(ctx, &stmts[1..], ind, tail))
            }
        }
        Statement::Use(u) => emit_use(ctx, u, ind, tail),
        Statement::Assert(a) => emit_bool_assert(ctx, a, stmts, ind, tail),
    }
}

fn emit_bool_assert(
    ctx: &Emit,
    a: &TypedAssert,
    stmts: &[&TypedStatement],
    ind: usize,
    tail: Option<&Tail>,
) -> String {
    let val = emit_expr(ctx, &a.value, ind + 10, None);
    let msg = match &a.message {
        Some(m) => emit_expr(ctx, m, 0, None),
        None => "\"assert failed\"".into(),
    };
    let form = format!(
        "(when-not {val}\n{}(throw (ex-info {msg} {{:gleam/assert true}})))",
        sp(ind + 2)
    );
    if stmts.len() == 1 {
        form
    } else {
        format!("{form}\n{}{}", sp(ind), emit_body(ctx, &stmts[1..], ind, tail))
    }
}

/// `use a, b <- f(x)`: the typed AST holds the fully desugared call with the
/// callback as its final argument. Reconstruct the flat with-use form.
fn emit_use(ctx: &Emit, u: &gleam_core::ast::TypedUse, ind: usize, tail: Option<&Tail>) -> String {
    let bind_ind = ind + 13;
    let mut pairs: Vec<(String, String)> = Vec::new();
    let mut current: &gleam_core::ast::TypedUse = u;
    loop {
        let TypedExpr::Call { fun, arguments, .. } = current.call.as_ref() else {
            panic!("use call is not a call: {:?}", current.call);
        };
        let (callback, rhs_args) = arguments.split_last().expect("use callback");
        let TypedExpr::Fn { body, arguments: cb_args, .. } = &callback.value else {
            panic!("use callback is not a fn");
        };
        // The typed desugar moves the use patterns onto the callback fn.
        let params: Vec<String> = cb_args.iter().map(arg_name).collect();
        let params = format!("[{}]", params.join(" "));
        let call = emit_call(ctx, fun, rhs_args, bind_ind + params.len() + 1, None);
        pairs.push((params, call));
        let body_stmts: Vec<&TypedStatement> = body.iter().collect();
        if let [Statement::Use(inner)] = body_stmts.as_slice() {
            current = inner;
            continue;
        }
        let body_str = emit_body(ctx, &body_stmts, ind + 2, None);
        let pairs_str = pairs
            .iter()
            .map(|(p, c)| format!("{p} {c}"))
            .collect::<Vec<_>>()
            .join(&format!("\n{}", sp(bind_ind)));
        let _ = tail;
        return format!("(p/with-use [{pairs_str}]\n{}{body_str})", sp(ind + 2));
    }
}

fn emit_let_assert(
    ctx: &Emit,
    a: &gleam_core::ast::TypedAssignment,
    message: Option<&TypedExpr>,
    stmts: &[&TypedStatement],
    ind: usize,
    tail: Option<&Tail>,
) -> String {
    let msg = match message {
        Some(m) => emit_expr(ctx, m, 0, None),
        None => "\"let assert failed\"".into(),
    };
    if let Some(expected) = pattern_literal(ctx, &a.pattern) {
        let head = "(p/let-assert ";
        let val = emit_expr(ctx, &a.value, ind + head.len(), None);
        let msg_arg = if msg == "\"let assert failed\"" { String::new() } else { format!(" {msg}") };
        let inline = format!("{head}{expected} {val}{msg_arg})");
        let form = if fits(&inline, ind) {
            inline
        } else {
            format!("{head}{expected}\n{}{val}{msg_arg})", sp(ind + head.len()))
        };
        return if stmts.len() == 1 {
            form
        } else {
            format!("{form}\n{}{}", sp(ind), emit_body(ctx, &stmts[1..], ind, tail))
        };
    }
    let (tests, binds) = pattern_cond(ctx, &a.pattern, "v");
    let val = emit_expr(ctx, &a.value, ind + 8, None);
    let test = and_join(&tests);
    if binds.is_empty() {
        let form = format!(
            "(let [v {val}]\n{i2}(when-not {test}\n{i4}(throw (ex-info {msg} {{:value v}}))))",
            i2 = sp(ind + 2),
            i4 = sp(ind + 4),
        );
        if stmts.len() == 1 {
            form
        } else {
            format!("{form}\n{}{}", sp(ind), emit_body(ctx, &stmts[1..], ind, tail))
        }
    } else {
        let binds_str = order_binds(binds)
            .iter()
            .map(|(n, v)| format!("{n} {v}"))
            .collect::<Vec<_>>()
            .join(&format!("\n{}", sp(ind + 8)));
        let rest = if stmts.len() == 1 {
            "v".to_string()
        } else {
            emit_body(ctx, &stmts[1..], ind + 4, tail)
        };
        // merged_let collapses a rest that is itself a let into this one's
        // binding vector, instead of emitting a redundant nested let.
        let inner_let = merged_let(&binds_str, rest, ind + 2);
        format!(
            "(let [v {val}]\n{i2}(when-not {test}\n{i4}(throw (ex-info {msg} {{:value v}})))\n{i2}{inner_let})",
            i2 = sp(ind + 2),
            i4 = sp(ind + 4),
        )
    }
}

fn and_join(tests: &[String]) -> String {
    match tests.len() {
        0 => "true".into(),
        1 => tests[0].clone(),
        _ => format!("(and {})", tests.join(" ")),
    }
}

/// A bind that shadows a name other binds' accessors reference must come last.
fn order_binds(binds: Vec<(String, String)>) -> Vec<(String, String)> {
    let mentions = |expr: &str, name: &str| {
        expr.contains(&format!("{name} ")) || expr.contains(&format!("{name})"))
    };
    let orig = binds.clone();
    let mut out = binds;
    out.sort_by_key(|(n, _)| orig.iter().any(|(n2, v2)| n2 != n && mentions(v2, n)));
    out
}

/// Wrap `inner` in a let; when `inner` is itself a let, splice its bindings
/// into ours instead of nesting (clj-kondo: redundant let).
fn merged_let(binds_str: &str, inner: String, body_ind: usize) -> String {
    if let Some(rest) = inner.strip_prefix("(let [") {
        // Find the closing bracket of the inner binding vector. The scan is
        // string-aware: brackets inside emitted string literals (with \"
        // escapes) must not move the depth. Runs off the end → don't merge.
        let mut depth = 1usize;
        let mut i = 0;
        let mut in_str = false;
        let mut esc = false;
        let bytes = rest.as_bytes();
        while depth > 0 && i < bytes.len() {
            let b = bytes[i];
            if in_str {
                if esc {
                    esc = false;
                } else if b == b'\\' {
                    esc = true;
                } else if b == b'"' {
                    in_str = false;
                }
            } else {
                match b {
                    b'"' => in_str = true,
                    b'[' => depth += 1,
                    b']' => depth -= 1,
                    _ => {}
                }
            }
            i += 1;
        }
        if depth > 0 {
            return format!("(let [{binds_str}]\n{}{inner})", sp(body_ind + 2));
        }
        let inner_binds = &rest[..i - 1];
        let mut body = rest[i..].trim_start_matches('\n').to_string();
        // body was indented two deeper than our target; shift it left
        let from = format!("\n{}", sp(body_ind + 4));
        let to = format!("\n{}", sp(body_ind + 2));
        body = body.replace(&from, &to);
        let body = body.trim_start();
        // inner bindings were indented relative to the inner let; normalize
        let inner_binds = inner_binds
            .lines()
            .map(|l| l.trim())
            .collect::<Vec<_>>()
            .join(" ");
        return format!(
            "(let [{binds_str} {inner_binds}]\n{}{body}",
            sp(body_ind + 2)
        );
    }
    format!("(let [{binds_str}]\n{}{inner})", sp(body_ind + 2))
}

fn destructure_binding(ctx: &Emit, pattern: &TypedPattern) -> Option<String> {
    match pattern {
        Pattern::Assign { name, pattern, .. } => {
            let inner = destructure_binding(ctx, pattern)?;
            let name = user_var(&kebab(name.as_str()));
            if let Some(body) = inner.strip_suffix(']') {
                Some(format!("{body} :as {name}]"))
            } else if let Some(body) = inner.strip_suffix('}') {
                Some(format!("{body} :as {name}}}"))
            } else if inner == "_" {
                Some(name)
            } else {
                panic!("unsupported `as` binding over non-destructuring pattern");
            }
        }
        Pattern::Variable { name, .. } => Some(user_var(&kebab(name.as_str()))),
        Pattern::Discard { .. } => Some("_".into()),
        Pattern::Tuple { elements, .. } => {
            let parts: Option<Vec<String>> =
                elements.iter().map(|e| destructure_binding(ctx, e)).collect();
            Some(format!("[{}]", parts?.join(" ")))
        }
        Pattern::Constructor { arguments, constructor, .. } => {
            // Zero-arity first: prelude constructors like Nil have no field
            // table, and none is needed to bind nothing.
            if arguments.is_empty() {
                return Some("_".into());
            }
            let pc = known(constructor);
            let fields = ctx.ctor_fields(pc.module.as_str(), pc.name.as_str());
            let mut parts = Vec::new();
            let mut pos = 0;
            for arg in arguments {
                let field = match &arg.label {
                    Some(l) => kebab(l.as_str()),
                    None => {
                        let f = fields.get(pos)?.clone();
                        pos += 1;
                        f
                    }
                };
                if matches!(arg.value, Pattern::Discard { .. }) {
                    continue;
                }
                parts.push(format!("{} :{field}", destructure_binding(ctx, &arg.value)?));
            }
            if parts.is_empty() {
                return Some("_".into());
            }
            Some(format!("{{{}}}", parts.join(" ")))
        }
        _ => None,
    }
}

fn known(c: &gleam_core::analyse::Inferred<PatternConstructor>) -> &PatternConstructor {
    match c {
        gleam_core::analyse::Inferred::Known(pc) => pc,
        gleam_core::analyse::Inferred::Unknown => panic!("unresolved pattern constructor"),
    }
}

fn pattern_cond(ctx: &Emit, pattern: &TypedPattern, subj: &str) -> (Vec<String>, Vec<(String, String)>) {
    let mut tests = Vec::new();
    let mut binds = Vec::new();
    pattern_cond_inner(ctx, pattern, subj, &mut tests, &mut binds);
    (tests, binds)
}

fn pattern_cond_inner(
    ctx: &Emit,
    pattern: &TypedPattern,
    subj: &str,
    tests: &mut Vec<String>,
    binds: &mut Vec<(String, String)>,
) {
    match pattern {
        Pattern::Int { value, .. } => tests.push(format!("(= {subj} {})", int_lit(value))),
        Pattern::Float { value, .. } => tests.push(format!("(= {subj} {})", int_lit(value))),
        Pattern::String { value, .. } => {
            tests.push(format!("(= {subj} \"{}\")", clj_string(value)))
        }
        Pattern::Variable { name, .. } => {
            binds.push((user_var(&kebab(name.as_str())), subj.to_string()))
        }
        Pattern::Discard { .. } => {}
        Pattern::Assign { name, pattern, .. } => {
            binds.push((user_var(&kebab(name.as_str())), subj.to_string()));
            pattern_cond_inner(ctx, pattern, subj, tests, binds);
        }
        Pattern::Tuple { elements, .. } => {
            for (i, el) in elements.iter().enumerate() {
                pattern_cond_inner(ctx, el, &format!("(nth {subj} {i})"), tests, binds);
            }
        }
        Pattern::List { elements, tail, .. } => {
            let n = elements.len();
            match tail {
                None if n == 0 => tests.push(format!("(empty? {subj})")),
                None => tests.push(format!("(= (count {subj}) {n})")),
                Some(_) if n == 1 => tests.push(format!("(seq {subj})")),
                Some(_) => tests.push(format!("(<= {n} (count {subj}))")),
            }
            for (i, el) in elements.iter().enumerate() {
                let access = if i == 0 {
                    format!("(first {subj})")
                } else {
                    format!("(nth {subj} {i})")
                };
                pattern_cond_inner(ctx, el, &access, tests, binds);
            }
            if let Some(t) = tail {
                let access = if n == 1 {
                    format!("(rest {subj})")
                } else {
                    format!("(nthrest {subj} {n})")
                };
                pattern_cond_inner(ctx, &t.pattern, &access, tests, binds);
            }
        }
        Pattern::StringPrefix {
            left_side_string,
            left_side_assignment,
            right_side_assignment,
            ..
        } => {
            let prefix = clj_string(left_side_string);
            tests.push(format!("(.startsWith ^String {subj} \"{prefix}\")"));
            if let Some((name, _)) = left_side_assignment {
                binds.push((user_var(&kebab(name.as_str())), format!("\"{prefix}\"")));
            }
            if let gleam_core::ast::AssignName::Variable(name) = right_side_assignment {
                binds.push((
                    user_var(&kebab(name.as_str())),
                    format!("(subs {subj} {})", crate::gleam_str_len(left_side_string)),
                ));
            }
        }
        Pattern::Constructor { arguments, constructor, .. } => {
            let pc = known(constructor);
            match (pc.module.as_str(), pc.name.as_str()) {
                ("gleam", "Nil") => tests.push(format!("(nil? {subj})")),
                ("gleam", "True") => tests.push(subj.to_string()),
                ("gleam", "False") => tests.push(format!("(not {subj})")),
                (module, n) => {
                    tests.push(format!("(instance? {} {subj})", ctx.ctor_class(module, n)));
                    let fields = ctx.ctor_fields(module, n).clone();
                    let mut pos = 0;
                    for arg in arguments {
                        let field = match &arg.label {
                            Some(l) => kebab(l.as_str()),
                            None => {
                                let f = fields
                                    .get(pos)
                                    .unwrap_or_else(|| panic!("too many pattern args for {n}"))
                                    .clone();
                                pos += 1;
                                f
                            }
                        };
                        pattern_cond_inner(ctx, &arg.value, &format!("(:{field} {subj})"), tests, binds);
                    }
                }
            }
        }
        // The empty bit array <<>>: a plain emptiness test on the byte
        // vector. Segmented bit-array patterns stay unsupported (loud panic).
        Pattern::BitArray { segments, .. } if segments.is_empty() => {
            tests.push(format!("(= [] {subj})"));
        }
        other => panic!("unsupported pattern: {other:?}"),
    }
}

fn pattern_literal(ctx: &Emit, pattern: &TypedPattern) -> Option<String> {
    match pattern {
        Pattern::Int { value, .. } => Some(int_lit(value)),
        Pattern::Float { value, .. } => Some(int_lit(value)),
        Pattern::String { value, .. } => Some(format!("\"{}\"", clj_string(value))),
        Pattern::Constructor { arguments, constructor, spread, .. } => {
            let pc = known(constructor);
            let name = &pc.name;
            match (pc.module.as_str(), pc.name.as_str()) {
                ("gleam", "Nil") => return Some("nil".into()),
                ("gleam", "True") => return Some("true".into()),
                ("gleam", "False") => return Some("false".into()),
                _ => {}
            }
            if spread.is_some() {
                return None;
            }
            let fields = ctx
                .global
                .ctor_fields
                .get(&(pc.module.to_string(), name.to_string()))?;
            if arguments.iter().any(|a| a.label.is_some()) || arguments.len() != fields.len() {
                return None;
            }
            let ctor = ctx.ctor_fn(pc.module.as_str(), name.as_str());
            let mut parts = Vec::new();
            for arg in arguments {
                parts.push(pattern_literal(ctx, &arg.value)?);
            }
            if parts.is_empty() {
                Some(format!("({ctor})"))
            } else {
                Some(format!("({ctor} {})", parts.join(" ")))
            }
        }
        Pattern::Tuple { elements, .. } => {
            let parts: Option<Vec<String>> =
                elements.iter().map(|e| pattern_literal(ctx, e)).collect();
            Some(format!("[{}]", parts?.join(" ")))
        }
        Pattern::List { elements, tail: None, .. } => {
            let parts: Option<Vec<String>> =
                elements.iter().map(|e| pattern_literal(ctx, e)).collect();
            let parts = parts?;
            if parts.is_empty() {
                Some("(list)".into())
            } else {
                Some(format!("(list {})", parts.join(" ")))
            }
        }
        _ => None,
    }
}

/// Ops whose left-nested chains may flatten into one variadic call.
fn flattenable(op: &BinOp) -> bool {
    matches!(
        op,
        BinOp::Concatenate
            | BinOp::AddInt
            | BinOp::AddFloat
            | BinOp::SubInt
            | BinOp::SubFloat
            | BinOp::MultInt
            | BinOp::MultFloat
            | BinOp::DivFloat
            | BinOp::And
            | BinOp::Or
    )
}

fn binop(op: &BinOp) -> &'static str {
    match op {
        BinOp::And => "and",
        BinOp::Or => "or",
        BinOp::Eq => "=",
        BinOp::NotEq => "not=",
        BinOp::LtInt | BinOp::LtFloat => "<",
        BinOp::LtEqInt | BinOp::LtEqFloat => "<=",
        BinOp::GtInt | BinOp::GtFloat => ">",
        BinOp::GtEqInt | BinOp::GtEqFloat => ">=",
        BinOp::AddInt => "+'",
        BinOp::AddFloat => "+",
        BinOp::SubInt => "-'",
        BinOp::SubFloat => "-",
        BinOp::MultInt => "*'",
        BinOp::MultFloat => "*",
        BinOp::DivInt => "quot",
        BinOp::DivFloat => "/",
        BinOp::RemainderInt => "rem",
        BinOp::Concatenate => "str",
    }
}

fn var_ref(ctx: &Emit, name: &str, constructor: &ValueConstructor) -> String {
    match &constructor.variant {
        ValueConstructorVariant::LocalVariable { .. } => user_var(&kebab(name)),
        ValueConstructorVariant::ModuleFn { module, name: fn_name, .. } => {
            ctx.fn_ref(module.as_str(), fn_name.as_str())
        }
        ValueConstructorVariant::ModuleConstant { module, name: c_name, .. } => {
            if module.as_str() == ctx.module_path {
                user_var(&kebab(c_name.as_str()))
            } else {
                format!("{}/{}", ctx.alias_of(module.as_str()), user_var(&kebab(c_name.as_str())))
            }
        }
        ValueConstructorVariant::Record { module, name: r_name, arity, .. } => {
            ctx.ctor_value(module.as_str(), r_name.as_str(), *arity)
        }
    }
}

fn emit_expr(ctx: &Emit, e: &TypedExpr, ind: usize, tail: Option<&Tail>) -> String {
    match e {
        TypedExpr::Int { value, .. } => int_lit(value),
        TypedExpr::Float { value, .. } => int_lit(value),
        TypedExpr::String { value, .. } => format!("\"{}\"", clj_string(value)),
        TypedExpr::Var { name, constructor, .. } => var_ref(ctx, name.as_str(), constructor),
        TypedExpr::ModuleSelect { label, module_name, constructor, .. } => {
            use gleam_core::type_::ModuleValueConstructor as M;
            match constructor {
                M::Fn { module, name, .. } => ctx.fn_ref(module.as_str(), name.as_str()),
                M::Record { name, arity, .. } => {
                    ctx.ctor_value(module_name.as_str(), name.as_str(), *arity)
                }
                M::Constant { .. } => {
                    if module_name.as_str() == ctx.module_path {
                        user_var(&kebab(label.as_str()))
                    } else {
                        format!(
                            "{}/{}",
                            ctx.alias_of(module_name.as_str()),
                            user_var(&kebab(label.as_str()))
                        )
                    }
                }
            }
        }
        TypedExpr::RecordAccess { record, label, .. } => {
            format!("(:{} {})", kebab(label.as_str()), emit_expr(ctx, record, ind, None))
        }
        TypedExpr::PositionalAccess { record, index, .. } => {
            let fields = record_fields_of(ctx, record);
            let field = fields
                .get(*index as usize)
                .unwrap_or_else(|| panic!("positional access {index} out of range"))
                .clone();
            format!("(:{field} {})", emit_expr(ctx, record, ind, None))
        }
        TypedExpr::Tuple { elements, .. } => {
            let parts: Vec<String> =
                elements.iter().map(|el| emit_expr(ctx, el, ind + 1, None)).collect();
            format!("[{}]", parts.join(" "))
        }
        TypedExpr::TupleIndex { tuple, index, .. } => {
            format!("(nth {} {index})", emit_expr(ctx, tuple, ind, None))
        }
        TypedExpr::List { elements, tail: t, .. } => {
            let parts: Vec<String> =
                elements.iter().map(|el| emit_expr(ctx, el, ind + 6, None)).collect();
            match t {
                None if parts.is_empty() => "(list)".into(),
                None => format!("(list {})", parts.join(" ")),
                Some(t) => format!("(list* {} {})", parts.join(" "), emit_expr(ctx, t, ind, None)),
            }
        }
        TypedExpr::NegateInt { value, .. } => format!("(- {})", emit_expr(ctx, value, ind, None)),
        TypedExpr::NegateBool { value, .. } => {
            format!("(not {})", emit_expr(ctx, value, ind, None))
        }
        TypedExpr::BinOp { operator, left, right, .. } => {
            // A left-nested chain of the same operator flattens into one
            // variadic call: `a <> b <> c` emits (str a b c), not
            // (str (str a b) c). This is an identity rewrite — Clojure's
            // variadic ops reduce left, exactly matching Gleam's
            // left-associative grammar — so it is safe even for float ops
            // where true associativity fails. quot/rem stay binary: not
            // variadic in Clojure.
            let mut operands = vec![right.as_ref()];
            let mut head = left.as_ref();
            if flattenable(operator) {
                while let TypedExpr::BinOp { operator: op2, left: l2, right: r2, .. } = head {
                    if op2 != operator {
                        break;
                    }
                    operands.push(r2.as_ref());
                    head = l2.as_ref();
                }
            }
            operands.push(head);
            operands.reverse();
            let parts: Vec<String> =
                operands.iter().map(|e| emit_expr(ctx, e, ind, None)).collect();
            format!("({} {})", binop(operator), parts.join(" "))
        }
        TypedExpr::Fn { arguments, body, .. } => {
            let args: Vec<String> = arguments.iter().map(arg_name).collect();
            let head = format!("(fn [{}] ", args.join(" "));
            let stmts: Vec<&TypedStatement> = body.iter().collect();
            let inline_body = emit_body(ctx, &stmts, ind + head.len(), None);
            let inline = format!("{head}{inline_body})");
            if fits(&inline, ind) {
                inline
            } else {
                let b = emit_body(ctx, &stmts, ind + 2, None);
                format!("(fn [{}]\n{}{b})", args.join(" "), sp(ind + 2))
            }
        }
        TypedExpr::Call { fun, arguments, .. } => emit_call(ctx, fun, arguments, ind, tail),
        TypedExpr::Pipeline { first_value, assignments, finally, .. } => {
            emit_pipeline(ctx, first_value, assignments, finally, ind, tail)
        }
        TypedExpr::Case { subjects, clauses, .. } => {
            emit_case(ctx, subjects, clauses, ind, tail)
        }
        TypedExpr::Block { statements, .. } => {
            let stmts: Vec<&TypedStatement> = statements.iter().collect();
            let body = emit_body(ctx, &stmts, ind, tail);
            if stmts.len() == 1 || body.starts_with("(let ") || body.starts_with("(p/with-use ") {
                body
            } else {
                let b = emit_body(ctx, &stmts, ind + 4, tail);
                format!("(do {b})")
            }
        }
        TypedExpr::Todo { message, .. } => {
            let msg = message
                .as_ref()
                .map(|m| emit_expr(ctx, m, ind, None))
                .unwrap_or_else(|| "\"todo\"".into());
            format!("(throw (ex-info {msg} {{:gleam/todo true}}))")
        }
        TypedExpr::Panic { message, .. } => {
            let msg = message
                .as_ref()
                .map(|m| emit_expr(ctx, m, ind, None))
                .unwrap_or_else(|| "\"panic\"".into());
            format!("(throw (ex-info {msg} {{:gleam/panic true}}))")
        }
        TypedExpr::Echo { expression, message, location, .. } => {
            let Some(expr) = expression else { return "p/echo".into() };
            let prefix = match message {
                Some(m) => emit_expr(ctx, m, ind, None),
                None => format!("\"{}:{}\"", ctx.file, ctx.line_of(location.start)),
            };
            let inner = emit_expr(ctx, expr, ind + 8, None);
            format!("(p/echo {inner} {prefix})")
        }
        TypedExpr::BitArray { segments, .. } => {
            use gleam_core::ast::BitArrayOption as Opt;
            let parts: Vec<String> = segments
                .iter()
                .map(|seg| {
                    let val = emit_expr(ctx, &seg.value, ind + 2, None);
                    let utf8 = seg.options.iter().any(|o| matches!(o, Opt::Utf8 { .. }));
                    let raw = seg
                        .options
                        .iter()
                        .any(|o| matches!(o, Opt::Bits { .. } | Opt::Bytes { .. }));
                    let size = seg.options.iter().find_map(|o| match o {
                        Opt::Size { value, .. } => Some(emit_expr(ctx, value, ind + 2, None)),
                        _ => None,
                    });
                    if utf8 {
                        format!("(p/ba-utf8 {val})")
                    } else if raw {
                        val
                    } else {
                        format!("(p/ba-int {val} {})", size.unwrap_or_else(|| "8".into()))
                    }
                })
                .collect();
            format!("(p/bit-array {})", parts.join(" "))
        }
        TypedExpr::RecordUpdate {
            updated_record,
            updated_record_assigned_name,
            constructor,
            arguments,
            ..
        } => {
            let ctor = emit_expr(ctx, constructor, ind, None);
            let args: Vec<String> = arguments
                .iter()
                .map(|a| emit_expr(ctx, &a.value, ind + 2, None))
                .collect();
            let call = format!("({ctor} {})", args.join(" "));
            match updated_record_assigned_name {
                Some(name) => {
                    let rec = emit_expr(ctx, updated_record, ind + 8, None);
                    format!(
                        "(let [{} {rec}]\n{}{call})",
                        user_var(&kebab(name.as_str())),
                        sp(ind + 2)
                    )
                }
                None => call,
            }
        }
        TypedExpr::Invalid { .. } => panic!("invalid expression reached codegen"),
    }
}

/// Field names of the record a PositionalAccess reads from, via its type.
fn record_fields_of<'a>(ctx: &'a Emit, record: &TypedExpr) -> &'a Vec<String> {
    let type_ = record.type_();
    let (name, module) = type_
        .named_type_name()
        .unwrap_or_else(|| panic!("positional access on non-named type"));
    // Single-constructor types only (the type checker guarantees this for
    // record updates); constructor name == type name for these.
    ctx.global
        .ctor_fields
        .get(&(name.to_string(), module.to_string()))
        .or_else(|| ctx.global.ctor_fields.get(&(module.to_string(), name.to_string())))
        .unwrap_or_else(|| panic!("unknown record type {module}.{name}"))
}

fn emit_call(
    ctx: &Emit,
    fun: &TypedExpr,
    arguments: &[CallArg<TypedExpr>],
    ind: usize,
    tail: Option<&Tail>,
) -> String {
    // Tail self-call -> recur. Resolution is exact now: a ModuleFn var naming
    // this module's current fn.
    if let (Some(t), TypedExpr::Var { constructor, .. }) = (tail, fun) {
        if let ValueConstructorVariant::ModuleFn { module, name, .. } = &constructor.variant {
            if module.as_str() == ctx.module_path && local_fn_name(&kebab(name.as_str())) == t.name
            {
                let args: Vec<String> = arguments
                    .iter()
                    .map(|a| emit_expr(ctx, &a.value, ind + 7, None))
                    .collect();
                return format!("(recur {})", args.join(" "));
            }
        }
    }
    let head = match fun {
        TypedExpr::Var { name, constructor, .. } => match &constructor.variant {
            ValueConstructorVariant::Record { module, name: r_name, .. } => {
                ctx.ctor_fn(module.as_str(), r_name.as_str())
            }
            _ => var_ref(ctx, name.as_str(), constructor),
        },
        TypedExpr::ModuleSelect { constructor, module_name, .. } => {
            use gleam_core::type_::ModuleValueConstructor as M;
            match constructor {
                M::Record { name, .. } => ctx.ctor_fn(module_name.as_str(), name.as_str()),
                _ => emit_expr(ctx, fun, ind, None),
            }
        }
        _ => emit_expr(ctx, fun, ind, None),
    };
    let arg_ind = ind + 1 + head.len() + 1;
    let args: Vec<String> = arguments
        .iter()
        .map(|a| emit_expr(ctx, &a.value, arg_ind, None))
        .collect();
    if args.is_empty() {
        return format!("({head})");
    }
    let inline = format!("({head} {})", args.join(" "));
    if fits(&inline, ind) {
        inline
    } else {
        format!("({head} {})", args.join(&format!("\n{}", sp(arg_ind))))
    }
}

/// Rebuild `->` threading from the desugared pipeline when every step is a
/// call whose first argument is the previous step's variable; otherwise fall
/// back to a let chain (rare: echo steps, function-returning pipes).
fn emit_pipeline(
    ctx: &Emit,
    first: &gleam_core::ast::TypedPipelineAssignment,
    assignments: &[(gleam_core::ast::TypedPipelineAssignment, gleam_core::ast::PipelineAssignmentKind)],
    finally: &TypedExpr,
    ind: usize,
    tail: Option<&Tail>,
) -> String {
    let mut steps: Vec<&TypedExpr> = Vec::new();
    let mut prev_name: &EcoString = &first.name;
    let mut threadable = true;
    for (a, _) in assignments {
        steps.push(&a.value);
        if !first_arg_is_var(&a.value, prev_name) {
            threadable = false;
        }
        prev_name = &a.name;
    }
    if !first_arg_is_var(finally, prev_name) {
        threadable = false;
    }
    if threadable {
        let head = emit_expr(ctx, &first.value, ind + 4, None);
        let mut rendered: Vec<String> = Vec::new();
        for step in steps.iter().chain(std::iter::once(&finally)) {
            let TypedExpr::Call { fun, arguments, .. } = *step else { unreachable!() };
            rendered.push(match fun.as_ref() {
                f @ (TypedExpr::Var { .. } | TypedExpr::ModuleSelect { .. })
                    if arguments.len() == 1 =>
                {
                    emit_expr(ctx, f, ind + 4, None)
                }
                f => emit_call(ctx, f, &arguments[1..], ind + 4, None),
            });
        }
        let inline = format!("(-> {head} {})", rendered.join(" "));
        if fits(&inline, ind) {
            let _ = tail;
            return inline;
        }
        return format!(
            "(-> {head}\n{}{})",
            sp(ind + 4),
            rendered.join(&format!("\n{}", sp(ind + 4)))
        );
    }

    // Fallback: honest let chain over the desugared assignments.
    let mut binds: Vec<(String, String)> = Vec::new();
    let n0 = user_var(&kebab(first.name.as_str()));
    binds.push((n0.clone(), emit_expr(ctx, &first.value, ind + 6, None)));
    for (a, _) in assignments {
        let n = user_var(&kebab(a.name.as_str()));
        binds.push((n, emit_expr(ctx, &a.value, ind + 6, None)));
    }
    let bind_ind = ind + 6;
    let binds_str = binds
        .iter()
        .map(|(n, v)| format!("{n} {v}"))
        .collect::<Vec<_>>()
        .join(&format!("\n{}", sp(bind_ind)));
    let fin = emit_expr(ctx, finally, ind + 2, tail);
    format!("(let [{binds_str}]\n{}{fin})", sp(ind + 2))
}

fn first_arg_is_var(e: &TypedExpr, name: &EcoString) -> bool {
    let TypedExpr::Call { arguments, .. } = e else { return false };
    matches!(
        arguments.first().map(|a| &a.value),
        Some(TypedExpr::Var { name: n, .. }) if n == name
    )
}

fn emit_case(
    ctx: &Emit,
    subjects: &[TypedExpr],
    clauses: &[Clause<TypedExpr, std::sync::Arc<gleam_core::type_::Type>>],
    ind: usize,
    tail: Option<&Tail>,
) -> String {
    let mut subj_lets: Vec<(String, String)> = Vec::new();
    let mut subjs: Vec<String> = Vec::new();
    for (i, s) in subjects.iter().enumerate() {
        let e = emit_expr(ctx, s, ind + 2, None);
        if e.contains(' ') || e.contains('\n') {
            let name = if subjects.len() == 1 { "subject".to_string() } else { format!("s{i}") };
            subj_lets.push((name.clone(), e));
            subjs.push(name);
        } else {
            subjs.push(e);
        }
    }
    let ind = if subj_lets.is_empty() { ind } else { ind + 2 };

    let mut branches: Vec<(String, Vec<(String, String)>, &TypedExpr)> = Vec::new();
    for clause in clauses {
        let mut pattern_sets: Vec<&Vec<TypedPattern>> = vec![&clause.pattern];
        pattern_sets.extend(clause.alternative_patterns.iter());
        let mut expanded: Vec<(Vec<String>, Vec<(String, String)>)> = Vec::new();
        for patterns in &pattern_sets {
            let mut tests = Vec::new();
            let mut binds = Vec::new();
            for (pattern, subj) in patterns.iter().zip(&subjs) {
                pattern_cond_inner(ctx, pattern, subj, &mut tests, &mut binds);
            }
            expanded.push((tests, binds));
        }
        if expanded.len() > 1 && expanded.iter().all(|(_, b)| b.is_empty()) {
            let alts: Vec<String> = expanded.iter().map(|(t, _)| and_join(t)).collect();
            expanded = vec![(vec![format!("(or {})", alts.join(" "))], vec![])];
        }
        for (mut tests, binds) in expanded {
            let env: HashMap<String, String> = binds.iter().cloned().collect();
            if let Some(guard) = &clause.guard {
                tests.push(emit_guard(ctx, guard, &env));
            }
            let used: Vec<(String, String)> = binds
                .into_iter()
                .filter(|(n, _)| expr_uses_var(&clause.then, n))
                .collect();
            branches.push((and_join(&tests), used, &clause.then));
        }
    }

    let emit_branch_body = |used: &[(String, String)], then: &TypedExpr, body_ind: usize| -> String {
        if used.is_empty() {
            emit_expr(ctx, then, body_ind, tail)
        } else {
            let used = order_binds(used.to_vec());
            let binds_str = used
                .iter()
                .map(|(n, v)| format!("{n} {v}"))
                .collect::<Vec<_>>()
                .join(" ");
            let inner = if let TypedExpr::Block { statements, .. } = then {
                let stmts: Vec<&TypedStatement> = statements.iter().collect();
                emit_body(ctx, &stmts, body_ind + 2, tail)
            } else {
                emit_expr(ctx, then, body_ind + 2, tail)
            };
            merged_let(&binds_str, inner, body_ind)
        }
    };

    let core = if branches.len() == 2 {
        let (t, used1, then1) = &branches[0];
        let (_, used2, then2) = &branches[1];
        let b1 = emit_branch_body(used1, then1, ind + 2);
        let b2 = emit_branch_body(used2, then2, ind + 2);
        let inline = format!("(if {t} {b1} {b2})");
        if fits(&inline, ind) {
            inline
        } else {
            format!("(if {t}\n{i2}{b1}\n{i2}{b2})", i2 = sp(ind + 2))
        }
    } else {
        let n = branches.len();
        let tests: Vec<String> = branches
            .iter()
            .enumerate()
            .map(|(i, (test, _, _))| {
                if i + 1 == n && test == "true" { ":else".to_string() } else { test.clone() }
            })
            .collect();
        // Compact when every `test body` pair fits on one line; otherwise
        // stack: test on its own line, body at a fixed indent, blank line
        // between branches.
        let compact_bodies: Vec<String> = branches
            .iter()
            .zip(&tests)
            .map(|((_, used, then), test)| emit_branch_body(used, then, ind + 2 + test.len() + 1))
            .collect();
        let compact_ok = compact_bodies
            .iter()
            .zip(&tests)
            .all(|(body, test)| !body.contains('\n') && fits(&format!("{test} {body}"), ind + 2));
        let mut out = String::from("(cond\n");
        if compact_ok {
            for (i, (test, body)) in tests.iter().zip(&compact_bodies).enumerate() {
                let _ = write!(out, "{}{test} {}", sp(ind + 2), body);
                out.push_str(if i + 1 == n { ")" } else { "\n" });
            }
        } else {
            for (i, ((_, used, then), test)) in branches.iter().zip(&tests).enumerate() {
                let body = emit_branch_body(used, then, ind + 2);
                let _ = write!(out, "{}{test}\n{}{body}", sp(ind + 2), sp(ind + 2));
                out.push_str(if i + 1 == n { ")" } else { "\n\n" });
            }
        }
        out
    };

    if subj_lets.is_empty() {
        core
    } else {
        let binds_str = subj_lets
            .iter()
            .map(|(n, v)| format!("{n} {v}"))
            .collect::<Vec<_>>()
            .join(&format!("\n{}", sp(ind + 4)));
        format!("(let [{binds_str}]\n{}{core})", sp(ind))
    }
}

fn emit_guard(ctx: &Emit, guard: &TypedClauseGuard, env: &HashMap<String, String>) -> String {
    use gleam_core::ast::ClauseGuard as G;
    match guard {
        G::Block { value, .. } => emit_guard(ctx, value, env),
        G::Not { expression, .. } => format!("(not {})", emit_guard(ctx, expression, env)),
        G::BinaryOperator { operator, left, right, .. } => format!(
            "({} {} {})",
            binop(operator),
            emit_guard(ctx, left, env),
            emit_guard(ctx, right, env)
        ),
        G::Var { name, .. } => {
            let n = user_var(&kebab(name.as_str()));
            env.get(&n).cloned().unwrap_or(n)
        }
        G::TupleIndex { tuple, index, .. } => {
            format!("(nth {} {index})", emit_guard(ctx, tuple, env))
        }
        G::FieldAccess { container, label, .. } => {
            format!("(:{} {})", kebab(label.as_str()), emit_guard(ctx, container, env))
        }
        G::ModuleSelect { module_name, label, .. } => {
            if module_name.as_str() == ctx.module_path {
                user_var(&kebab(label.as_str()))
            } else {
                format!("{}/{}", ctx.alias_of(module_name.as_str()), user_var(&kebab(label.as_str())))
            }
        }
        G::Constant(c) => emit_constant(ctx, c),
        other => panic!("unsupported guard: {other:?}"),
    }
}

fn emit_constant(ctx: &Emit, c: &gleam_core::ast::Constant<std::sync::Arc<gleam_core::type_::Type>>) -> String {
    use gleam_core::ast::Constant as C;
    match c {
        C::Int { value, .. } => int_lit(value),
        C::Float { value, .. } => int_lit(value),
        C::String { value, .. } => format!("\"{}\"", clj_string(value)),
        C::Tuple { elements, .. } => {
            let parts: Vec<String> = elements.iter().map(|e| emit_constant(ctx, e)).collect();
            format!("[{}]", parts.join(" "))
        }
        C::List { elements, .. } => {
            let parts: Vec<String> = elements.iter().map(|e| emit_constant(ctx, e)).collect();
            if parts.is_empty() {
                "(list)".into()
            } else {
                format!("(list {})", parts.join(" "))
            }
        }
        C::Record { module, name, arguments, record_constructor, .. } => {
            // Resolve the defining module via the constructor when available.
            let module_path: String = record_constructor
                .as_ref()
                .and_then(|rc| match &rc.variant {
                    ValueConstructorVariant::Record { module, .. } => Some(module.to_string()),
                    _ => None,
                })
                .or_else(|| {
                    module.as_ref().and_then(|(alias, _)| {
                        ctx.aliases
                            .iter()
                            .find(|(_, a)| a.as_str() == alias.as_str())
                            .map(|(m, _)| m.clone())
                    })
                })
                .unwrap_or_else(|| {
                    match name.as_str() {
                        "Ok" | "Error" | "Nil" | "True" | "False" => "gleam".to_string(),
                        _ => ctx.module_path.to_string(),
                    }
                });
            match (module_path.as_str(), name.as_str()) {
                ("gleam", "Nil") => return "nil".into(),
                ("gleam", "True") => return "true".into(),
                ("gleam", "False") => return "false".into(),
                _ => {}
            }
            let head = ctx.ctor_fn(&module_path, name.as_str());
            match arguments {
                None => format!("({head})"),
                Some(args) if args.is_empty() => format!("({head})"),
                Some(args) => {
                    let parts: Vec<String> =
                        args.iter().map(|a| emit_constant(ctx, &a.value)).collect();
                    format!("({head} {})", parts.join(" "))
                }
            }
        }
        C::Var { name, module, constructor, .. } => {
            if let Some(vc) = constructor {
                if let ValueConstructorVariant::ModuleFn { module, name, .. } = &vc.variant {
                    return ctx.fn_ref(module.as_str(), name.as_str());
                }
            }
            match module {
                Some((alias, _)) => {
                    let m = ctx
                        .aliases
                        .iter()
                        .find(|(_, a)| a.as_str() == alias.as_str())
                        .map(|(m, _)| m.clone())
                        .unwrap_or_else(|| panic!("unknown alias {alias}"));
                    format!("{}/{}", ctx.alias_of(&m), user_var(&kebab(name.as_str())))
                }
                None => user_var(&kebab(name.as_str())),
            }
        }
        other => panic!("unsupported constant: {other:?}"),
    }
}

fn expr_uses_var(e: &TypedExpr, name: &str) -> bool {
    let mut found = false;
    visit_exprs(e, &mut |x| {
        if let TypedExpr::Var { name: n, constructor, .. } = x {
            if matches!(constructor.variant, ValueConstructorVariant::LocalVariable { .. })
                && user_var(&kebab(n.as_str())) == name
            {
                found = true;
            }
        }
    });
    found
}

fn visit_exprs(e: &TypedExpr, f: &mut impl FnMut(&TypedExpr)) {
    f(e);
    let visit_stmts = |stmts: &vec1::Vec1<TypedStatement>, f: &mut dyn FnMut(&TypedExpr)| {
        for s in stmts {
            match s {
                Statement::Expression(e) => visit_exprs_dyn(e, f),
                Statement::Assignment(a) => visit_exprs_dyn(&a.value, f),
                Statement::Use(u) => visit_exprs_dyn(&u.call, f),
                Statement::Assert(a) => visit_exprs_dyn(&a.value, f),
            }
        }
    };
    match e {
        TypedExpr::Block { statements, .. } => visit_stmts(statements, f),
        TypedExpr::Fn { body, .. } => visit_stmts(body, f),
        TypedExpr::Pipeline { first_value, assignments, finally, .. } => {
            visit_exprs(&first_value.value, f);
            for (a, _) in assignments {
                visit_exprs(&a.value, f);
            }
            visit_exprs(finally, f);
        }
        TypedExpr::Case { subjects, clauses, .. } => {
            for s in subjects {
                visit_exprs(s, f);
            }
            for c in clauses {
                visit_exprs(&c.then, f);
            }
        }
        TypedExpr::Call { fun, arguments, .. } => {
            visit_exprs(fun, f);
            for a in arguments {
                visit_exprs(&a.value, f);
            }
        }
        TypedExpr::BinOp { left, right, .. } => {
            visit_exprs(left, f);
            visit_exprs(right, f);
        }
        TypedExpr::List { elements, tail, .. } => {
            for el in elements {
                visit_exprs(el, f);
            }
            if let Some(t) = tail {
                visit_exprs(t, f);
            }
        }
        TypedExpr::Tuple { elements, .. } => {
            for el in elements {
                visit_exprs(el, f);
            }
        }
        TypedExpr::TupleIndex { tuple, .. } => visit_exprs(tuple, f),
        TypedExpr::RecordAccess { record, .. } => visit_exprs(record, f),
        TypedExpr::PositionalAccess { record, .. } => visit_exprs(record, f),
        TypedExpr::NegateBool { value, .. } | TypedExpr::NegateInt { value, .. } => {
            visit_exprs(value, f)
        }
        TypedExpr::Echo { expression, .. } => {
            if let Some(x) = expression {
                visit_exprs(x, f);
            }
        }
        TypedExpr::RecordUpdate { updated_record, constructor, arguments, .. } => {
            visit_exprs(updated_record, f);
            visit_exprs(constructor, f);
            for a in arguments {
                visit_exprs(&a.value, f);
            }
        }
        TypedExpr::BitArray { segments, .. } => {
            for s in segments {
                visit_exprs(&s.value, f);
            }
        }
        _ => {}
    }
}

fn collect_constant_fn_refs(
    c: &gleam_core::ast::Constant<std::sync::Arc<gleam_core::type_::Type>>,
    module_path: &str,
    out: &mut std::collections::HashSet<String>,
) {
    use gleam_core::ast::Constant as C;
    match c {
        C::Var { constructor, .. } => {
            if let Some(vc) = constructor {
                if let ValueConstructorVariant::ModuleFn { module, name, .. } = &vc.variant {
                    if module.as_str() == module_path {
                        let _ = out.insert(name.to_string());
                    }
                }
            }
        }
        C::Tuple { elements, .. } | C::List { elements, .. } => {
            for e in elements {
                collect_constant_fn_refs(e, module_path, out);
            }
        }
        C::Record { arguments, .. } => {
            for a in arguments.iter().flatten() {
                collect_constant_fn_refs(&a.value, module_path, out);
            }
        }
        _ => {}
    }
}

fn visit_exprs_dyn(e: &TypedExpr, f: &mut dyn FnMut(&TypedExpr)) {
    visit_exprs(e, &mut |x| f(x));
}

// Unused-import silencer for items brought in for signatures.
#[allow(unused)]
fn _unused(_: &FunctionLiteralKind, _: &HashSet<String>) {}
