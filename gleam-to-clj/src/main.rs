//! gleam-to-clj: parse a Gleam module with gleam-core and emit readable Clojure.
//!
//! v0: parse-only (untyped AST). Known limitations, each panics loudly:
//! - pipes assume first-argument insertion (matches `->`)
//! - labelled call args assumed to be in positional order
//! - single-subject case expressions only
//! - constructors limited to the prelude (Ok/Error/Nil/True/False)
//! - line comments are dropped (doc comments become docstrings)

use std::collections::HashMap;

use gleam_core::ast::{Definition, UntypedModule};
use gleam_core::parse;
use gleam_core::warning::WarningEmitter;

const WIDTH: usize = 78;

/// java.lang simple names that a defrecord/import would collide with.
const JAVA_LANG: &[&str] = &[
    "Error", "String", "Integer", "Long", "Double", "Float", "Boolean", "Byte", "Short",
    "Character", "Object", "Class", "Thread", "Process", "Exception", "Number", "Iterable",
    "Comparable", "Runnable", "Math", "System", "Void", "Enum", "Record",
];

mod analysis;
mod emit;

fn main() {
    let args: Vec<String> = std::env::args().collect();
    let usage = "usage: gleam-to-clj <input.gleam> [output.clj]\n       gleam-to-clj build <project-dir> <out-dir> [stdlib-src-dir]\n       gleam-to-clj typecheck <project-dir> [stdlib-src-dir]";
    if args.len() < 2 {
        eprintln!("{usage}");
        std::process::exit(2);
    }
    match args[1].as_str() {
        "typecheck" => {
            if args.len() < 3 {
                eprintln!("{usage}");
                std::process::exit(2);
            }
            let stdlib = args.get(3).map(String::as_str).unwrap_or("stdlib-src");
            typecheck(&args[2], stdlib);
        }
        "build" | "buildt" => {
            if args.len() < 4 {
                eprintln!("{usage}");
                std::process::exit(2);
            }
            let stdlib = args.get(4).map(String::as_str).unwrap_or("stdlib-src");
            build_typed(&args[2], &args[3], stdlib);
        }
        "typed" => {
            let stdlib = args.get(4).map(String::as_str).unwrap_or("stdlib-src");
            typed_single(&args[2], args.get(3).map(String::as_str), stdlib);
        }
        _ => {
            let stdlib = std::env::var("GLEAM_CLJ_STDLIB").unwrap_or_else(|_| "stdlib-src".into());
            typed_single(&args[1], args.get(2).map(String::as_str), &stdlib);
        }
    }
}

/// Analyze stdlib + project sources and report: the typecheck gate.
fn typecheck(proj: &str, stdlib_dir: &str) {
    let sources = gather_typed_sources(proj, stdlib_dir);
    let n = sources.len();
    let analyzed = analysis::analyze(sources);
    for m in &analyzed {
        if m.emit {
            eprintln!("typed {} ({} functions)", m.path, m.module.definitions.functions.len());
        }
    }
    println!("TYPECHECKED {n} modules");
}





/// Definitions active on this backend: untargeted or erlang-targeted (BEAM
/// semantics are the reference); javascript-only definitions are dropped.
fn active_defs(module: &UntypedModule) -> impl Iterator<Item = &gleam_core::ast::TargetedDefinition> {
    module.definitions.iter().filter(|d| {
        d.target.is_none() || d.target == Some(gleam_core::build::Target::Erlang)
    })
}



fn collect_gleam_files(dir: &std::path::Path, base: &std::path::Path, out: &mut Vec<(String, std::path::PathBuf)>) {
    for entry in std::fs::read_dir(dir).expect("read src dir") {
        let path = entry.expect("dir entry").path();
        if path.is_dir() {
            collect_gleam_files(&path, base, out);
        } else if path.extension().is_some_and(|e| e == "gleam") {
            let rel = path.strip_prefix(base).expect("strip prefix");
            let module = rel.with_extension("");
            out.push((module.to_string_lossy().to_string(), path));
        }
    }
}


fn load_externals(proj: &str) -> HashMap<(String, String), String> {
    let mut externals: HashMap<(String, String), String> = HashMap::new();
    let map_path = std::path::Path::new(proj).join("clojure-externals.txt");
    if map_path.exists() {
        for line in std::fs::read_to_string(&map_path).expect("read externals map").lines() {
            let line = line.trim();
            if line.is_empty() || line.starts_with('#') {
                continue;
            }
            let parts: Vec<&str> = line.split_whitespace().collect();
            let [module, fun, target] = parts[..] else {
                panic!("bad clojure-externals.txt line (want `module fn ns/var`): {line}");
            };
            let _ = externals.insert((module.to_string(), fun.to_string()), target.to_string());
        }
    }
    externals
}

/// Gather sources for the typed pipeline: our stdlib (analyze-only), the
/// project's src/ and test/, and vendored pure-Gleam deps discovered by the
/// import graph. gleeunit is analyzed for its interface but never emitted
/// (a Clojure shim provides the runtime).
fn gather_typed_sources(proj: &str, stdlib_dir: &str) -> Vec<analysis::SourceModule> {
    let stdlib_root = std::path::Path::new(stdlib_dir).join("src");
    let emit_stdlib = std::fs::canonicalize(proj).ok()
        == std::fs::canonicalize(stdlib_dir).ok();
    let mut files: Vec<(String, std::path::PathBuf, bool, bool)> = Vec::new(); // (path, file, is_dep, emit)
    if stdlib_root.exists() {
        let mut v = Vec::new();
        collect_gleam_files(&stdlib_root, &stdlib_root, &mut v);
        files.extend(v.into_iter().map(|(m, f)| (m, f, true, emit_stdlib)));
    }
    if !emit_stdlib {
        for root in ["src", "test"] {
            let dir = std::path::Path::new(proj).join(root);
            if dir.exists() {
                let mut v = Vec::new();
                collect_gleam_files(&dir, &dir, &mut v);
                files.extend(v.into_iter().map(|(m, f)| (m, f, false, true)));
            }
        }
    }
    let mut seen: std::collections::HashSet<String> =
        files.iter().map(|(m, _, _, _)| m.clone()).collect();

    // Import-graph BFS into vendored packages.
    let packages_dir = std::path::Path::new(proj).join("build/packages");
    let dep_roots: Vec<std::path::PathBuf> = if packages_dir.exists() {
        std::fs::read_dir(&packages_dir)
            .expect("read packages dir")
            .filter_map(|e| {
                let p = e.expect("dir entry").path().join("src");
                p.exists().then_some(p)
            })
            .collect()
    } else {
        Vec::new()
    };
    let emitter = WarningEmitter::null();
    let mut queue: Vec<std::path::PathBuf> = files.iter().map(|(_, f, _, _)| f.clone()).collect();
    while let Some(file) = queue.pop() {
        let src = std::fs::read_to_string(&file).expect("read module");
        let path = camino::Utf8PathBuf::from(file.to_string_lossy().to_string());
        let Ok(parsed) = parse::parse_module(path, &src, &emitter) else { continue };
        for def in active_defs(&parsed.module) {
            if let Definition::Import(import) = &def.definition {
                let m = import.module.to_string();
                if m == "gleam" || m.starts_with("gleam/") || seen.contains(&m) {
                    continue;
                }
                let Some(dep_file) = dep_roots
                    .iter()
                    .map(|root| root.join(format!("{m}.gleam")))
                    .find(|p| p.exists())
                else {
                    panic!(
                        "module {m} not found in src/, test/, or any vendored package — \
                         run `gleam build` first or add the package"
                    );
                };
                let _ = seen.insert(m.clone());
                // gleeunit: interface only, runtime comes from the shim.
                let emit = !(m == "gleeunit" || m.starts_with("gleeunit/"));
                files.push((m, dep_file.clone(), true, emit));
                queue.push(dep_file);
            }
        }
    }

    files
        .into_iter()
        .map(|(path, file, is_dep, emit)| analysis::SourceModule {
            file_name: file.to_string_lossy().to_string(),
            src: std::fs::read_to_string(&file).expect("read module"),
            path,
            is_dep,
            emit,
        })
        .collect()
}

fn build_typed(proj: &str, out_dir: &str, stdlib_dir: &str) {
    let sources = gather_typed_sources(proj, stdlib_dir);
    let analyzed = analysis::analyze(sources);
    let global = emit::build_typed_global(&analyzed);
    let mut externals = load_externals(proj);
    if std::fs::canonicalize(proj).ok() != std::fs::canonicalize(stdlib_dir).ok() {
        // Stdlib overrides apply when its modules get analyzed for interfaces
        // but are never emitted here, so only project map entries matter; the
        // stdlib map is still needed when emitting the stdlib itself.
    } else {
        externals.extend(load_externals(stdlib_dir));
    }
    for m in &analyzed {
        if !m.emit {
            continue;
        }
        let code = emit::emit_module(m, &global, &externals);
        let out_path = std::path::Path::new(out_dir).join(format!("{}.clj", m.path));
        std::fs::create_dir_all(out_path.parent().expect("parent")).expect("mkdir");
        std::fs::write(&out_path, code).expect("write output");
        eprintln!("emitted {}", out_path.display());
    }
}

fn typed_single(input: &str, output: Option<&str>, stdlib_dir: &str) {
    let stdlib_root = std::path::Path::new(stdlib_dir).join("src");
    let mut files: Vec<(String, std::path::PathBuf, bool, bool)> = Vec::new();
    if stdlib_root.exists() {
        let mut v = Vec::new();
        collect_gleam_files(&stdlib_root, &stdlib_root, &mut v);
        files.extend(v.into_iter().map(|(m, f)| (m, f, true, false)));
    }
    let path = std::path::Path::new(input);
    let stem = path.file_stem().expect("file stem").to_string_lossy().to_string();
    files.push((stem.clone(), path.to_path_buf(), false, true));
    let sources: Vec<analysis::SourceModule> = files
        .into_iter()
        .map(|(p, file, is_dep, emit)| analysis::SourceModule {
            file_name: file
                .file_name()
                .expect("file name")
                .to_string_lossy()
                .to_string(),
            src: std::fs::read_to_string(&file).expect("read module"),
            path: p,
            is_dep,
            emit,
        })
        .collect();
    let analyzed = analysis::analyze(sources);
    let global = emit::build_typed_global(&analyzed);
    let externals = HashMap::new();
    let m = analyzed.iter().find(|m| m.emit).expect("target module");
    let code = emit::emit_module(m, &global, &externals);
    match output {
        Some(out) => std::fs::write(out, code).expect("write output"),
        None => print!("{code}"),
    }
}


fn sp(n: usize) -> String {
    " ".repeat(n)
}

fn kebab(s: &str) -> String {
    // A leading underscore is Gleam's "intentionally unused" marker and a
    // fine Clojure convention too — keep it, kebab the rest.
    match s.strip_prefix('_') {
        Some(rest) => format!("_{}", rest.replace('_', "-")),
        None => s.replace('_', "-"),
    }
}


/// Clojure special forms: a top-level Gleam fn with one of these names gets
/// a `*` suffix, since e.g. `(new 1)` would hit the special form, not the var.
const SPECIAL_FORMS: &[&str] = &[
    "new", "do", "if", "let", "fn", "def", "loop", "recur", "quote", "var", "throw", "try",
    "catch", "finally",
];

/// clojure.core names the emitter itself emits bare (list literals, pattern
/// accessors, int.min/max). A user variable with one of these names would
/// shadow them, so it gets a `'` suffix everywhere.
const CORE_SHADOW: &[&str] = &[
    "list", "min", "max", "nth", "first", "rest", "count", "seq", "str", "quot", "rem", "abs",
    "nthrest", "nil", "true", "false",
];

fn local_fn_name(kebab_name: &str) -> String {
    if SPECIAL_FORMS.contains(&kebab_name) {
        format!("{kebab_name}*")
    } else {
        user_var(kebab_name)
    }
}

/// Public names of clojure.core, for :refer-clojure :exclude emission.
fn clojure_core_names() -> &'static std::collections::HashSet<&'static str> {
    static NAMES: std::sync::OnceLock<std::collections::HashSet<&'static str>> =
        std::sync::OnceLock::new();
    NAMES.get_or_init(|| include_str!("clojure_core_names.txt").lines().collect())
}

fn user_var(kebab_name: &str) -> String {
    match kebab_name {
        // clj-kondo's reader splits nil'/true'/false' at the quote; use an
        // underscore suffix for these three.
        "nil" | "true" | "false" => format!("{kebab_name}_"),
        k if CORE_SHADOW.contains(&k) => format!("{k}'"),
        k => k.to_string(),
    }
}



/// Class reference for a prelude variant, fully qualified when the simple
/// name collides with an auto-imported java.lang class.
fn class_ref(name: &str) -> String {
    if JAVA_LANG.contains(&name) {
        format!("gleam.prelude.{name}")
    } else {
        name.to_string()
    }
}



fn int_lit(value: &str) -> String {
    let v = value.replace('_', "");
    // Gleam radix prefixes -> Clojure reader syntax (0x works as-is).
    if let Some(rest) = v.strip_prefix("0b") {
        format!("2r{rest}")
    } else if let Some(rest) = v.strip_prefix("-0b") {
        format!("-2r{rest}")
    } else if let Some(rest) = v.strip_prefix("0o") {
        format!("8r{rest}")
    } else if let Some(rest) = v.strip_prefix("-0o") {
        format!("-8r{rest}")
    } else {
        v
    }
}

/// Translate a Gleam string literal body (raw source text, escapes included)
/// into a Clojure string literal body. Gleam and Clojure share \\n \\r \\t
/// \\\\ \\\"; Gleam adds \\e, \\f and \\u{...} which Clojure spells differently.
fn clj_string(value: &str) -> String {
    // Unescape Gleam syntax to real chars.
    let mut chars = value.chars().peekable();
    let mut real = String::new();
    while let Some(c) = chars.next() {
        if c != '\\' {
            real.push(c);
            continue;
        }
        match chars.next() {
            Some('n') => real.push('\n'),
            Some('r') => real.push('\r'),
            Some('t') => real.push('\t'),
            Some('f') => real.push('\u{000C}'),
            Some('e') => real.push('\u{001B}'),
            Some('\\') => real.push('\\'),
            Some('"') => real.push('"'),
            Some('u') => {
                // \u{HEX}
                let hex: String = chars
                    .by_ref()
                    .skip_while(|&c| c == '{')
                    .take_while(|&c| c != '}')
                    .collect();
                let cp = u32::from_str_radix(&hex, 16).expect("unicode escape");
                real.push(char::from_u32(cp).expect("valid codepoint"));
            }
            other => panic!("unknown string escape: \\{other:?}"),
        }
    }
    // Re-escape for Clojure.
    let mut out = String::new();
    for c in real.chars() {
        match c {
            '"' => out.push_str("\\\""),
            '\\' => out.push_str("\\\\"),
            '\n' => out.push_str("\\n"),
            '\r' => out.push_str("\\r"),
            '\t' => out.push_str("\\t"),
            '\u{000C}' => out.push_str("\\f"),
            c if (c as u32) < 0x20 => out.push_str(&format!("\\u{:04x}", c as u32)),
            c => out.push(c),
        }
    }
    out
}

/// Character count of a Gleam string literal after unescaping (for StringPrefix).
fn gleam_str_len(value: &str) -> usize {
    let translated = clj_string(value);
    // Count chars of the *unescaped* value: redo minimal unescape on the
    // Clojure body (every backslash pair is one char).
    let mut n = 0;
    let mut chars = translated.chars();
    while let Some(c) = chars.next() {
        if c == '\\' {
            if let Some(e) = chars.next() {
                if e == 'u' {
                    for _ in 0..4 {
                        chars.next();
                    }
                }
            }
        }
        n += 1;
    }
    n
}








fn fits(s: &str, ind: usize) -> bool {
    !s.contains('\n') && ind + s.len() <= WIDTH
}


