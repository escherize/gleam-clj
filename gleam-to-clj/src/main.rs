//! gleam-to-clj: parse a Gleam module with gleam-core and emit readable Clojure.
//!
//! v0: parse-only (untyped AST). Known limitations, each panics loudly:
//! - pipes assume first-argument insertion (matches `->`)
//! - labelled call args assumed to be in positional order
//! - single-subject case expressions only
//! - constructors limited to the prelude (Ok/Error/Nil/True/False)
//! - line comments are dropped (doc comments become docstrings)

use std::collections::HashMap;
use std::fmt::Write as _;

use gleam_core::ast::{
    ArgNames, AssignmentKind, BinOp, CallArg, Clause, Definition, Function, Pattern, Publicity,
    Statement, UntypedClauseGuard, UntypedExpr, UntypedModule, UntypedPattern,
    UntypedStatement,
};
use gleam_core::parse;
use gleam_core::warning::WarningEmitter;

const WIDTH: usize = 78;

/// java.lang simple names that a defrecord/import would collide with.
const JAVA_LANG: &[&str] = &[
    "Error", "String", "Integer", "Long", "Double", "Float", "Boolean", "Byte", "Short",
    "Character", "Object", "Class", "Thread", "Process", "Exception", "Number", "Iterable",
    "Comparable", "Runnable", "Math", "System", "Void", "Enum", "Record",
];

fn main() {
    let args: Vec<String> = std::env::args().collect();
    if args.len() < 2 {
        eprintln!("usage: gleam-to-clj <input.gleam> [output.clj]");
        std::process::exit(2);
    }
    let input = &args[1];
    let src = std::fs::read_to_string(input).expect("read input");
    let path = camino::Utf8PathBuf::from(input);
    let parsed =
        parse::parse_module(path.clone(), &src, &WarningEmitter::null()).unwrap_or_else(|e| {
            eprintln!("parse error: {e:?}");
            std::process::exit(1);
        });
    let stem = path.file_stem().expect("file stem").to_string();
    let file_name = path.file_name().expect("file name").to_string();
    let out = emit_module(&parsed.module, &stem, &file_name, &src);
    match args.get(2) {
        Some(out_path) => std::fs::write(out_path, out).expect("write output"),
        None => print!("{out}"),
    }
}

fn sp(n: usize) -> String {
    " ".repeat(n)
}

fn kebab(s: &str) -> String {
    s.replace('_', "-")
}

/// Where a constructor's record class lives.
enum Origin {
    Local,
    Prelude,
    Module(&'static str),
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
    "nthrest",
];

fn local_fn_name(kebab_name: &str) -> String {
    if SPECIAL_FORMS.contains(&kebab_name) {
        format!("{kebab_name}*")
    } else {
        user_var(kebab_name)
    }
}

fn user_var(kebab_name: &str) -> String {
    if CORE_SHADOW.contains(&kebab_name) {
        format!("{kebab_name}'")
    } else {
        kebab_name.to_string()
    }
}

struct Ctx {
    /// alias -> full gleam module name, e.g. "dict" -> "gleam/dict"
    aliases: HashMap<String, String>,
    /// constructor name -> (field names, origin)
    constructors: HashMap<String, (Vec<String>, Origin)>,
    /// kebab-case names of this module's top-level functions
    local_fns: std::collections::HashSet<String>,
    /// source file name, for echo location prefixes
    file: String,
    /// byte offset of each line start, for byte-offset -> line-number lookup
    line_starts: Vec<u32>,
}

impl Ctx {
    /// 1-based line number for a byte offset.
    fn line_of(&self, byte: u32) -> usize {
        self.line_starts.partition_point(|&s| s <= byte)
    }

    /// Module function reference, applying the clojure.core-collision rename table.
    fn module_fn(&self, alias: &str, label: &str) -> String {
        let module = self.aliases.get(alias).cloned().unwrap_or_default();
        match (module.as_str(), label) {
            ("gleam/int", "min") => "min".into(),
            ("gleam/int", "max") => "max".into(),
            ("gleam/int", "range") => format!("{alias}/fold-range"),
            ("gleam/int", "compare") => format!("{alias}/cmp"),
            ("gleam/float", "compare") => format!("{alias}/cmp"),
            ("gleam/list", "reduce") => format!("{alias}/reduce1"),
            ("gleam/list", "map") => format!("{alias}/map-over"),
            ("gleam/list", "filter") => format!("{alias}/keep-if"),
            ("gleam/list", "sort") => format!("{alias}/sort-with"),
            ("gleam/list", "max") => format!("{alias}/largest"),
            ("gleam/list", "first") => format!("{alias}/head"),
            ("gleam/list", "last") => format!("{alias}/final"),
            ("gleam/list", "count") => format!("{alias}/count-if"),
            ("gleam/list", "partition") => format!("{alias}/separate"),
            ("gleam/list", "reverse") => format!("{alias}/reversed"),
            ("gleam/dict", "get") => format!("{alias}/lookup"),
            ("gleam/string", "repeat") => format!("{alias}/repeat-str"),
            ("gleam/result", "map") => format!("{alias}/map-ok"),
            ("gleam/result", "try") => format!("{alias}/attempt"),
            ("gleam/io", "println") => format!("{alias}/print-line"),
            ("gleam/io", "print") => format!("{alias}/write"),
            ("gleam/io", "println_error") => format!("{alias}/print-line-error"),
            ("gleam/io", "print_error") => format!("{alias}/write-error"),
            _ => format!("{alias}/{}", kebab(label)),
        }
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

impl Ctx {
    /// Constructor function reference, e.g. `->Circle`, `p/->Ok`, `order/->Lt`.
    fn ctor_ref(&self, name: &str) -> String {
        match self.constructors.get(name) {
            Some((_, Origin::Local)) => format!("->{name}"),
            Some((_, Origin::Prelude)) => format!("p/->{name}"),
            Some((_, Origin::Module(m))) => {
                let alias = self
                    .aliases
                    .iter()
                    .find(|(_, v)| v.as_str() == *m)
                    .map(|(k, _)| k.clone())
                    .unwrap_or_else(|| panic!("constructor {name} needs an import of {m}"));
                format!("{alias}/->{name}")
            }
            None => panic!("unknown constructor (v0): {name}"),
        }
    }

    /// Record class reference for instance? checks.
    fn ctor_class(&self, name: &str) -> String {
        match self.constructors.get(name) {
            Some((_, Origin::Local)) => name.to_string(),
            Some((_, Origin::Prelude)) => class_ref(name),
            Some((_, Origin::Module(m))) => format!("{}.{name}", m.replace("/", ".")),
            None => panic!("unknown constructor in pattern (v0): {name}"),
        }
    }
}

fn emit_module(module: &UntypedModule, stem: &str, file_name: &str, src: &str) -> String {
    let mut line_starts = vec![0u32];
    for (i, b) in src.bytes().enumerate() {
        if b == b'\n' {
            line_starts.push(i as u32 + 1);
        }
    }
    let mut constructors = HashMap::new();
    let val = || vec!["value".to_string()];
    constructors.insert("Ok".to_string(), (val(), Origin::Prelude));
    constructors.insert("Error".to_string(), (val(), Origin::Prelude));
    for c in ["Lt", "Eq", "Gt"] {
        constructors.insert(c.to_string(), (vec![], Origin::Module("gleam/order")));
    }
    constructors.insert("Some".to_string(), (val(), Origin::Module("gleam/option")));
    constructors.insert("None".to_string(), (vec![], Origin::Module("gleam/option")));
    let mut local_fns = std::collections::HashSet::new();
    for def in &module.definitions {
        match &def.definition {
            Definition::CustomType(t) => {
                for c in &t.constructors {
                    let n = c.arguments.len();
                    let fields: Vec<String> = c
                        .arguments
                        .iter()
                        .enumerate()
                        .map(|(i, a)| match &a.label {
                            Some((_, l)) => kebab(l.as_str()),
                            None if n == 1 => "value".to_string(),
                            None => format!("f{i}"),
                        })
                        .collect();
                    constructors.insert(c.name.to_string(), (fields, Origin::Local));
                }
            }
            Definition::Function(f) => {
                if let Some((_, n)) = &f.name {
                    local_fns.insert(kebab(n.as_str()));
                }
            }
            _ => {}
        }
    }

    let mut ctx = Ctx {
        aliases: HashMap::new(),
        constructors,
        local_fns,
        file: file_name.to_string(),
        line_starts,
    };
    let mut requires: Vec<(String, String)> = Vec::new(); // (clj ns, alias)

    for def in &module.definitions {
        if let Definition::Import(import) = &def.definition {
            let alias = match import.used_name() {
                Some(a) => a.to_string(),
                None => continue,
            };
            ctx.aliases.insert(alias.clone(), import.module.to_string());
            requires.push((kebab(&import.module.replace("/", ".")), alias));
        }
    }
    requires.push(("gleam.prelude".into(), "p".into()));
    requires.sort();

    let mut out = String::new();
    let _ = writeln!(out, "(ns {}", kebab(stem));
    let _ = writeln!(out, "  (:require");
    for (i, (ns, alias)) in requires.iter().enumerate() {
        let end = if i + 1 == requires.len() { ")" } else { "" };
        let _ = writeln!(out, "   [{ns} :as {alias}]{end}");
    }
    let _ = writeln!(out, "  (:import (gleam.prelude Ok)))");

    for def in &module.definitions {
        if let Definition::CustomType(t) = &def.definition {
            let _ = write!(out, "\n;; type {}\n", t.name);
            for c in &t.constructors {
                if JAVA_LANG.contains(&c.name.as_str()) {
                    let _ = writeln!(out, "(ns-unmap *ns* '{})", c.name);
                }
                let (fields, _) = &ctx.constructors[c.name.as_str()];
                let _ = writeln!(out, "(defrecord {} [{}])", c.name, fields.join(" "));
            }
        }
    }

    // Gleam allows definitions in any order; Clojure needs forward declarations.
    let fn_names: Vec<String> = module
        .definitions
        .iter()
        .filter_map(|def| match &def.definition {
            Definition::Function(f) => {
                f.name.as_ref().map(|(_, n)| local_fn_name(&kebab(n.as_str())))
            }
            _ => None,
        })
        .collect();
    if fn_names.len() > 1 {
        let _ = writeln!(out, "\n(declare {})", fn_names.join(" "));
    }

    let mut has_pub_main = false;
    for def in &module.definitions {
        if let Definition::Function(f) = &def.definition {
            out.push('\n');
            out.push_str(&emit_function(&ctx, f));
            if f.publicity == Publicity::Public
                && f.name.as_ref().is_some_and(|(_, n)| n.as_str() == "main")
            {
                has_pub_main = true;
            }
        }
    }
    if has_pub_main {
        out.push_str("\n(defn -main [& _]\n  (main))\n");
    }
    out
}

fn emit_function(ctx: &Ctx, f: &Function<(), UntypedExpr>) -> String {
    let name = local_fn_name(&kebab(f.name.as_ref().expect("function name").1.as_str()));
    let defn = if f.publicity == Publicity::Public { "defn" } else { "defn-" };
    let args: Vec<String> = f
        .arguments
        .iter()
        .map(|a| match &a.names {
            ArgNames::Named { name, .. } | ArgNames::NamedLabelled { name, .. } => {
                user_var(&kebab(name.as_str()))
            }
            ArgNames::Discard { .. } | ArgNames::LabelledDiscard { .. } => "_".into(),
        })
        .collect();

    let mut out = format!("({defn} {name}");
    match &f.documentation {
        Some((_, doc)) => {
            let text: Vec<String> =
                doc.lines().map(|l| l.trim().replace('"', "\\\"")).collect();
            let _ = write!(out, "\n  \"{}\"", text.join("\n  ").trim_end());
            let _ = write!(out, "\n  [{}]\n  ", args.join(" "));
        }
        None => {
            let _ = write!(out, " [{}]\n  ", args.join(" "));
        }
    }
    let stmts: Vec<&UntypedStatement> = f.body.iter().collect();
    let tail = Tail { name: name.clone() };
    out.push_str(&emit_body_t(ctx, &stmts, 2, Some(&tail)));
    out.push_str(")\n");
    out
}

/// Tail-position context: the enclosing function, for self-tail-call -> recur.
struct Tail {
    name: String,
}

fn emit_body(ctx: &Ctx, stmts: &[&UntypedStatement], ind: usize) -> String {
    emit_body_t(ctx, stmts, ind, None)
}

/// Emit a statement sequence as one or more forms separated by newlines at `ind`.
/// `tail` is the enclosing function when the sequence ends in tail position.
fn emit_body_t(ctx: &Ctx, stmts: &[&UntypedStatement], ind: usize, tail: Option<&Tail>) -> String {
    if stmts.is_empty() {
        return "nil".into();
    }
    match stmts[0] {
        Statement::Assignment(a) => match &a.kind {
            AssignmentKind::Let | AssignmentKind::Generated => {
                // Gather a run of consecutive `let <irrefutable pattern> = ...`
                // into one binding vector, using Clojure destructuring.
                let mut binds: Vec<(String, String)> = Vec::new();
                let mut i = 0;
                while i < stmts.len() {
                    let Statement::Assignment(a2) = stmts[i] else { break };
                    if !matches!(a2.kind, AssignmentKind::Let | AssignmentKind::Generated) {
                        break;
                    }
                    let Some(form) = destructure_binding(ctx, &a2.pattern) else { break };
                    let val_ind = ind + 6 + form.len() + 1; // col of value in "(let [form "
                    binds.push((form, emit_expr(ctx, &a2.value, val_ind)));
                    i += 1;
                }
                if binds.is_empty() {
                    panic!(
                        "unsupported let pattern (refutable pattern in plain let?): {:?}",
                        stmts[0]
                    );
                }
                let rest = emit_body_t(ctx, &stmts[i..], ind + 2, tail);
                let bind_ind = ind + 6;
                let binds_str = binds
                    .iter()
                    .map(|(n, v)| format!("{n} {v}"))
                    .collect::<Vec<_>>()
                    .join(&format!("\n{}", sp(bind_ind)));
                format!("(let [{binds_str}]\n{}{rest})", sp(ind + 2))
            }
            AssignmentKind::Assert { message, .. } => {
                let (_, binds) = pattern_cond(ctx, &a.pattern, "v");
                if binds.is_empty() {
                    let form = emit_assert(ctx, &a.value, &a.pattern, message.as_ref(), ind);
                    if stmts.len() == 1 {
                        form
                    } else {
                        format!(
                            "{form}\n{}{}",
                            sp(ind),
                            emit_body_t(ctx, &stmts[1..], ind, tail)
                        )
                    }
                } else {
                    // Binding pattern: check, then destructure for the rest.
                    let (tests, binds) = pattern_cond(ctx, &a.pattern, "v");
                    let val = emit_expr(ctx, &a.value, ind + 8);
                    let msg = match message {
                        Some(m) => emit_expr(ctx, m, 0),
                        None => "\"let assert failed\"".into(),
                    };
                    let test = and_join(&tests);
                    let binds_str = binds
                        .iter()
                        .map(|(n, v)| format!("{n} {v}"))
                        .collect::<Vec<_>>()
                        .join(&format!("\n{}", sp(ind + 8)));
                    let rest = if stmts.len() == 1 {
                        "v".to_string()
                    } else {
                        emit_body_t(ctx, &stmts[1..], ind + 4, tail)
                    };
                    format!(
                        "(let [v {val}]\n{i2}(when-not {test}\n{i4}(throw (ex-info {msg} {{:value v}})))\n{i2}(let [{binds_str}]\n{i4}{rest}))",
                        i2 = sp(ind + 2),
                        i4 = sp(ind + 4),
                    )
                }
            }
        },
        Statement::Expression(e) => {
            if stmts.len() == 1 {
                emit_expr_t(ctx, e, ind, tail)
            } else {
                let s = emit_expr(ctx, e, ind);
                format!("{s}\n{}{}", sp(ind), emit_body_t(ctx, &stmts[1..], ind, tail))
            }
        }
        Statement::Use(_) => {
            // Collect consecutive `use` lines into one flat with-use form.
            let mut pairs: Vec<(String, String)> = Vec::new();
            let mut i = 0;
            let bind_ind = ind + 13; // col after "(p/with-use ["
            while i < stmts.len() {
                let Statement::Use(u) = stmts[i] else { break };
                let params: Vec<String> = u
                    .assignments
                    .iter()
                    .map(|a| {
                        destructure_binding(ctx, &a.pattern)
                            .unwrap_or_else(|| panic!("unsupported use pattern: {:?}", a.pattern))
                    })
                    .collect();
                let params = format!("[{}]", params.join(" "));
                let call = emit_expr(ctx, &u.call, bind_ind + params.len() + 1);
                pairs.push((params, call));
                i += 1;
            }
            let body = emit_body_t(ctx, &stmts[i..], ind + 2, None);
            let pairs_str = pairs
                .iter()
                .map(|(p, c)| format!("{p} {c}"))
                .collect::<Vec<_>>()
                .join(&format!("\n{}", sp(bind_ind)));
            format!("(p/with-use [{pairs_str}]\n{}{body})", sp(ind + 2))
        }
        Statement::Assert(a) => {
            let val = emit_expr(ctx, &a.value, ind + 10);
            let msg = match &a.message {
                Some(m) => emit_expr(ctx, m, 0),
                None => "\"assert failed\"".into(),
            };
            let form = format!(
                "(when-not {val}\n{}(throw (ex-info {msg} {{:gleam/assert true}})))",
                sp(ind + 2)
            );
            if stmts.len() == 1 {
                form
            } else {
                format!("{form}\n{}{}", sp(ind), emit_body_t(ctx, &stmts[1..], ind, tail))
            }
        }
    }
}

/// Clojure binding form for an irrefutable pattern (plain `let`), or None
/// when the pattern could fail to match.
fn destructure_binding(ctx: &Ctx, pattern: &UntypedPattern) -> Option<String> {
    match pattern {
        Pattern::Variable { name, .. } => Some(user_var(&kebab(name.as_str()))),
        Pattern::Discard { .. } => Some("_".into()),
        Pattern::Tuple { elements, .. } => {
            let parts: Option<Vec<String>> =
                elements.iter().map(|el| destructure_binding(ctx, el)).collect();
            Some(format!("[{}]", parts?.join(" ")))
        }
        // Single-variant custom type (Gleam checked exhaustiveness): map destructure.
        Pattern::Constructor { name, arguments, .. } => {
            let (fields, _) = ctx.constructors.get(name.as_str())?;
            if arguments.is_empty() {
                return Some("_".into());
            }
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
                parts.push(format!("{} :{field}", destructure_binding(ctx, &arg.value)?));
            }
            Some(format!("{{{}}}", parts.join(" ")))
        }
        _ => None,
    }
}

fn emit_assert(
    ctx: &Ctx,
    value: &UntypedExpr,
    pattern: &UntypedPattern,
    message: Option<&UntypedExpr>,
    ind: usize,
) -> String {
    // Fully-literal pattern -> compare against the constructed value, one line.
    if let Some(expected) = pattern_literal(ctx, pattern) {
        let head = "(p/let-assert ";
        let val = emit_expr(ctx, value, ind + head.len());
        let msg = match message {
            Some(m) => format!(" {}", emit_expr(ctx, m, 0)),
            None => String::new(),
        };
        let inline = format!("{head}{expected} {val}{msg})");
        if fits(&inline, ind) {
            return inline;
        }
        return format!(
            "{head}{expected}\n{}{val}{msg})",
            sp(ind + head.len())
        );
    }

    let (tests, binds) = pattern_cond(ctx, pattern, "v");
    if !binds.is_empty() {
        panic!("unsupported: let assert patterns that bind variables (v0)");
    }
    let val = emit_expr(ctx, value, ind + 8);
    let msg = match message {
        Some(m) => emit_expr(ctx, m, 0),
        None => "\"let assert failed\"".into(),
    };
    let test = and_join(&tests);
    format!(
        "(let [v {val}]\n{i2}(when-not {test}\n{i4}(throw (ex-info {msg} {{:value v}}))))",
        i2 = sp(ind + 2),
        i4 = sp(ind + 4),
    )
}

/// If the pattern is a pure literal (no bindings, no wildcards), the Clojure
/// expression that constructs its value.
fn pattern_literal(ctx: &Ctx, pattern: &UntypedPattern) -> Option<String> {
    match pattern {
        Pattern::Int { value, .. } => Some(int_lit(value)),
        Pattern::Float { value, .. } => Some(int_lit(value)),
        Pattern::String { value, .. } => Some(format!("\"{}\"", value.replace("\"", "\\\""))),
        Pattern::Constructor { name, arguments, spread, .. } => match name.as_str() {
            "Nil" => Some("nil".into()),
            "True" => Some("true".into()),
            "False" => Some("false".into()),
            n => {
                if spread.is_some() {
                    return None;
                }
                let (fields, _) = ctx.constructors.get(n)?;
                // Positional-only and fully saturated, so field order is right.
                if arguments.iter().any(|a| a.label.is_some())
                    || arguments.len() != fields.len()
                {
                    return None;
                }
                let ctor = ctx.ctor_ref(n);
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
        },
        Pattern::Tuple { elements, .. } => {
            let parts: Option<Vec<String>> =
                elements.iter().map(|el| pattern_literal(ctx, el)).collect();
            Some(format!("[{}]", parts?.join(" ")))
        }
        Pattern::List { elements, tail: None, .. } => {
            let parts: Option<Vec<String>> =
                elements.iter().map(|el| pattern_literal(ctx, el)).collect();
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

fn and_join(tests: &[String]) -> String {
    match tests.len() {
        0 => "true".into(),
        1 => tests[0].clone(),
        _ => format!("(and {})", tests.join(" ")),
    }
}

/// Compile a pattern against subject expr `subj` into (tests, bindings).
fn pattern_cond(
    ctx: &Ctx,
    pattern: &UntypedPattern,
    subj: &str,
) -> (Vec<String>, Vec<(String, String)>) {
    let mut tests = Vec::new();
    let mut binds = Vec::new();
    pattern_cond_inner(ctx, pattern, subj, &mut tests, &mut binds);
    (tests, binds)
}

fn pattern_cond_inner(
    ctx: &Ctx,
    pattern: &UntypedPattern,
    subj: &str,
    tests: &mut Vec<String>,
    binds: &mut Vec<(String, String)>,
) {
    match pattern {
        Pattern::Int { value, .. } => tests.push(format!("(= {subj} {})", int_lit(value))),
        Pattern::Float { value, .. } => tests.push(format!("(= {subj} {})", int_lit(value))),
        Pattern::String { value, .. } => tests.push(format!("(= {subj} \"{value}\")")),
        Pattern::Variable { name, .. } => {
            binds.push((user_var(&kebab(name.as_str())), subj.to_string()))
        }
        Pattern::Discard { .. } => {}
        Pattern::Tuple { elements, .. } => {
            for (i, el) in elements.iter().enumerate() {
                pattern_cond_inner(ctx, el, &format!("(nth {subj} {i})"), tests, binds);
            }
        }
        Pattern::Constructor { name, arguments, .. } => match name.as_str() {
            "Nil" => tests.push(format!("(nil? {subj})")),
            "True" => tests.push(subj.to_string()),
            "False" => tests.push(format!("(not {subj})")),
            n => {
                let Some((fields, _)) = ctx.constructors.get(n) else {
                    panic!("unknown constructor in pattern (v0): {n}");
                };
                tests.push(format!("(instance? {} {subj})", ctx.ctor_class(n)));
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
                    pattern_cond_inner(
                        ctx,
                        &arg.value,
                        &format!("(:{field} {subj})"),
                        tests,
                        binds,
                    );
                }
            }
        },
        Pattern::List { elements, tail, .. } => {
            let n = elements.len();
            match tail {
                None if n == 0 => tests.push(format!("(empty? {subj})")),
                None => {
                    tests.push(format!("(= (count {subj}) {n})"));
                }
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
        other => panic!("unsupported pattern (v0): {other:?}"),
    }
}

fn int_lit(value: &str) -> String {
    value.replace('_', "")
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

fn emit_expr(ctx: &Ctx, e: &UntypedExpr, ind: usize) -> String {
    emit_expr_t(ctx, e, ind, None)
}

fn emit_expr_t(ctx: &Ctx, e: &UntypedExpr, ind: usize, tail: Option<&Tail>) -> String {
    match e {
        UntypedExpr::Int { value, .. } => int_lit(value),
        UntypedExpr::Float { value, .. } => int_lit(value),
        UntypedExpr::String { value, .. } => format!("\"{}\"", value.replace("\"", "\\\"")),
        UntypedExpr::Var { name, .. } => match name.as_str() {
            "Nil" => "nil".into(),
            "True" => "true".into(),
            "False" => "false".into(),
            n if n.starts_with(char::is_uppercase) => match ctx.constructors.get(n) {
                // A zero-field variant used as a value IS the constructed value.
                Some((fields, _)) if fields.is_empty() => format!("({})", ctx.ctor_ref(n)),
                Some(_) => ctx.ctor_ref(n),
                None => panic!("unknown constructor (v0): {n}"),
            },
            n => {
                let k = kebab(n);
                if ctx.local_fns.contains(&k) { local_fn_name(&k) } else { user_var(&k) }
            }
        },
        UntypedExpr::FieldAccess { container, label, .. } => {
            if let UntypedExpr::Var { name, .. } = container.as_ref() {
                if ctx.aliases.contains_key(name.as_str()) {
                    return ctx.module_fn(name.as_str(), label.as_str());
                }
            }
            // record field access
            format!(
                "(:{} {})",
                kebab(label.as_str()),
                emit_expr(ctx, container, ind)
            )
        }
        UntypedExpr::Tuple { elements, .. } => {
            let parts: Vec<String> =
                elements.iter().map(|el| emit_expr(ctx, el, ind + 1)).collect();
            format!("[{}]", parts.join(" "))
        }
        UntypedExpr::TupleIndex { tuple, index, .. } => {
            format!("(nth {} {index})", emit_expr(ctx, tuple, ind))
        }
        UntypedExpr::List { elements, tail, .. } => {
            let parts: Vec<String> =
                elements.iter().map(|el| emit_expr(ctx, el, ind + 6)).collect();
            match tail {
                None if parts.is_empty() => "(list)".into(),
                None => format!("(list {})", parts.join(" ")),
                Some(t) => format!(
                    "(list* {} {})",
                    parts.join(" "),
                    emit_expr(ctx, t, ind)
                ),
            }
        }
        UntypedExpr::NegateInt { value, .. } => format!("(- {})", emit_expr(ctx, value, ind)),
        UntypedExpr::NegateBool { value, .. } => format!("(not {})", emit_expr(ctx, value, ind)),
        UntypedExpr::BinOp { operator, left, right, .. } => {
            let op = binop(operator);
            let l = emit_expr(ctx, left, ind);
            let r = emit_expr(ctx, right, ind);
            format!("({op} {l} {r})")
        }
        UntypedExpr::Fn { arguments, body, .. } => {
            let args: Vec<String> = arguments
                .iter()
                .map(|a| match &a.names {
                    ArgNames::Named { name, .. } | ArgNames::NamedLabelled { name, .. } => {
                        kebab(name.as_str())
                    }
                    _ => "_".into(),
                })
                .collect();
            let head = format!("(fn [{}] ", args.join(" "));
            let stmts: Vec<&UntypedStatement> = body.iter().collect();
            let inline_body = emit_body(ctx, &stmts, ind + head.len());
            let inline = format!("{head}{inline_body})");
            if fits(&inline, ind) {
                inline
            } else {
                let b = emit_body(ctx, &stmts, ind + 2);
                format!("(fn [{}]\n{}{b})", args.join(" "), sp(ind + 2))
            }
        }
        UntypedExpr::Call { fun, arguments, .. } => emit_call_t(ctx, fun, arguments, ind, tail),
        UntypedExpr::PipeLine { expressions } => {
            let exprs: Vec<&UntypedExpr> = expressions.iter().collect();
            let first = emit_expr(ctx, exprs[0], ind + 4);
            let steps: Vec<String> = exprs[1..]
                .iter()
                .map(|step| match step {
                    UntypedExpr::Call { fun, arguments, .. } => {
                        emit_call(ctx, fun, arguments, ind + 4)
                    }
                    other => emit_expr(ctx, other, ind + 4),
                })
                .collect();
            let inline = format!("(-> {first} {})", steps.join(" "));
            if fits(&inline, ind) {
                inline
            } else {
                format!("(-> {first}\n{}{})", sp(ind + 4), steps.join(&format!("\n{}", sp(ind + 4))))
            }
        }
        UntypedExpr::Case { subjects, clauses, .. } => {
            emit_case(ctx, subjects, clauses.as_deref().unwrap_or_default(), ind, tail)
        }
        UntypedExpr::Block { statements, .. } => {
            let stmts: Vec<&UntypedStatement> = statements.iter().collect();
            let body = emit_body_t(ctx, &stmts, ind, tail);
            // A block whose body reduces to a single form needs no `do`.
            if stmts.len() == 1 || body.starts_with("(let ") {
                body
            } else {
                let b = emit_body_t(ctx, &stmts, ind + 4, tail);
                format!("(do {b})")
            }
        }
        UntypedExpr::Echo { expression, message, location, .. } => {
            // `|> echo` (no expression) threads as a bare fn reference.
            let Some(expr) = expression else { return "p/echo".into() };
            let prefix = match message {
                Some(m) => emit_expr(ctx, m, ind),
                None => format!("\"{}:{}\"", ctx.file, ctx.line_of(location.start)),
            };
            let inner = emit_expr(ctx, expr, ind + 8);
            format!("(p/echo {inner} {prefix})")
        }
        UntypedExpr::Todo { message, .. } => {
            let msg = message
                .as_ref()
                .map(|m| emit_expr(ctx, m, ind))
                .unwrap_or_else(|| "\"todo\"".into());
            format!("(throw (ex-info {msg} {{:gleam/todo true}}))")
        }
        UntypedExpr::Panic { message, .. } => {
            let msg = message
                .as_ref()
                .map(|m| emit_expr(ctx, m, ind))
                .unwrap_or_else(|| "\"panic\"".into());
            format!("(throw (ex-info {msg} {{:gleam/panic true}}))")
        }
        other => panic!("unsupported expression (v0): {other:?}"),
    }
}

fn emit_call(ctx: &Ctx, fun: &UntypedExpr, arguments: &[CallArg<UntypedExpr>], ind: usize) -> String {
    emit_call_t(ctx, fun, arguments, ind, None)
}

fn emit_call_t(
    ctx: &Ctx,
    fun: &UntypedExpr,
    arguments: &[CallArg<UntypedExpr>],
    ind: usize,
    tail: Option<&Tail>,
) -> String {
    // Self-call in tail position -> recur (constant stack, matches BEAM TCO).
    if let (Some(t), UntypedExpr::Var { name, .. }) = (tail, fun) {
        if !name.starts_with(char::is_uppercase) && local_fn_name(&kebab(name.as_str())) == t.name
        {
            let args: Vec<String> = arguments
                .iter()
                .map(|a| emit_expr(ctx, &a.value, ind + 7))
                .collect();
            return format!("(recur {})", args.join(" "));
        }
    }
    let head = match fun {
        UntypedExpr::Var { name, .. } if name.starts_with(char::is_uppercase) => {
            ctx.ctor_ref(name.as_str())
        }
        _ => emit_expr(ctx, fun, ind),
    };
    let arg_ind = ind + 1 + head.len() + 1;
    let args: Vec<String> = arguments
        .iter()
        .map(|a| emit_expr(ctx, &a.value, arg_ind))
        .collect();
    let inline = format!("({head} {})", args.join(" "));
    if fits(&inline, ind) || args.is_empty() {
        if args.is_empty() {
            format!("({head})")
        } else {
            inline
        }
    } else {
        format!("({head} {})", args.join(&format!("\n{}", sp(arg_ind))))
    }
}

fn emit_case(
    ctx: &Ctx,
    subjects: &[UntypedExpr],
    clauses: &[Clause<UntypedExpr, ()>],
    ind: usize,
    tail: Option<&Tail>,
) -> String {
    // Complex subjects get bound to temporaries; the whole case wraps in a let.
    let mut subj_lets: Vec<(String, String)> = Vec::new();
    let mut subjs: Vec<String> = Vec::new();
    for (i, s) in subjects.iter().enumerate() {
        let e = emit_expr(ctx, s, ind + 2);
        if e.contains(' ') || e.contains('\n') {
            let name = if subjects.len() == 1 {
                "subject".to_string()
            } else {
                format!("s{i}")
            };
            subj_lets.push((name.clone(), e));
            subjs.push(name);
        } else {
            subjs.push(e);
        }
    }
    let ind = if subj_lets.is_empty() { ind } else { ind + 2 };

    // (test, bindings-used-by-body, body-expr)
    let mut branches: Vec<(String, Vec<(String, String)>, &UntypedExpr)> = Vec::new();
    for clause in clauses {
        let mut tests = Vec::new();
        let mut binds = Vec::new();
        for (pattern, subj) in clause.pattern.iter().zip(&subjs) {
            pattern_cond_inner(ctx, pattern, subj, &mut tests, &mut binds);
        }
        // `a | b` alternatives: OR the tests. Only bind-free alternatives are
        // supported — bindings could come from different positions per branch.
        if !clause.alternative_patterns.is_empty() {
            if !binds.is_empty() {
                panic!("unsupported: alternative patterns that bind variables");
            }
            let mut alts = vec![and_join(&tests)];
            for alt in &clause.alternative_patterns {
                let mut t = Vec::new();
                let mut b = Vec::new();
                for (pattern, subj) in alt.iter().zip(&subjs) {
                    pattern_cond_inner(ctx, pattern, subj, &mut t, &mut b);
                }
                if !b.is_empty() {
                    panic!("unsupported: alternative patterns that bind variables");
                }
                alts.push(and_join(&t));
            }
            tests = vec![format!("(or {})", alts.join(" "))];
        }
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

    let emit_branch_body =
        |used: &[(String, String)], then: &UntypedExpr, body_ind: usize| -> String {
            if used.is_empty() {
                emit_expr_t(ctx, then, body_ind, tail)
            } else {
                let binds_str = used
                    .iter()
                    .map(|(n, v)| format!("{n} {v}"))
                    .collect::<Vec<_>>()
                    .join(" ");
                let inner = emit_expr_t(ctx, then, body_ind + 2, tail);
                format!("(let [{binds_str}]\n{}{inner})", sp(body_ind + 2))
            }
        };

    // Two branches -> `if` (Gleam exhaustiveness means clause 2 covers the rest).
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
        let mut out = String::from("(cond\n");
        let n = branches.len();
        for (i, (test, used, then)) in branches.iter().enumerate() {
            let last = i + 1 == n;
            let test = if last && test == "true" { ":else".to_string() } else { test.clone() };
            let body = emit_branch_body(used, then, ind + 2 + test.len() + 1);
            let _ = write!(out, "{}{test} {}", sp(ind + 2), body);
            out.push_str(if last { ")" } else { "\n" });
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

fn emit_guard(ctx: &Ctx, guard: &UntypedClauseGuard, env: &HashMap<String, String>) -> String {
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
        G::Constant(c) => emit_constant(c),
        other => panic!("unsupported guard (v0): {other:?}"),
    }
}

fn emit_constant(c: &gleam_core::ast::Constant<()>) -> String {
    use gleam_core::ast::Constant as C;
    match c {
        C::Int { value, .. } => int_lit(value),
        C::Float { value, .. } => int_lit(value),
        C::String { value, .. } => format!("\"{value}\""),
        other => panic!("unsupported constant in guard (v0): {other:?}"),
    }
}

fn fits(s: &str, ind: usize) -> bool {
    !s.contains('\n') && ind + s.len() <= WIDTH
}

/// Does this expression reference variable `name` (in kebab form)?
fn expr_uses_var(e: &UntypedExpr, name: &str) -> bool {
    match e {
        UntypedExpr::Var { name: n, .. } => user_var(&kebab(n.as_str())) == name,
        UntypedExpr::Int { .. }
        | UntypedExpr::Float { .. }
        | UntypedExpr::String { .. }
        | UntypedExpr::Todo { .. }
        | UntypedExpr::Panic { .. } => false,
        UntypedExpr::Block { statements, .. } => {
            statements.iter().any(|s| stmt_uses_var(s, name))
        }
        UntypedExpr::Fn { body, .. } => body.iter().any(|s| stmt_uses_var(s, name)),
        UntypedExpr::List { elements, tail, .. } => {
            elements.iter().any(|el| expr_uses_var(el, name))
                || tail.as_ref().is_some_and(|t| expr_uses_var(t, name))
        }
        UntypedExpr::Call { fun, arguments, .. } => {
            expr_uses_var(fun, name)
                || arguments.iter().any(|a| expr_uses_var(&a.value, name))
        }
        UntypedExpr::BinOp { left, right, .. } => {
            expr_uses_var(left, name) || expr_uses_var(right, name)
        }
        UntypedExpr::PipeLine { expressions } => {
            expressions.iter().any(|x| expr_uses_var(x, name))
        }
        UntypedExpr::Case { subjects, clauses, .. } => {
            subjects.iter().any(|s| expr_uses_var(s, name))
                || clauses.as_ref().is_some_and(|cs| {
                    cs.iter().any(|c| expr_uses_var(&c.then, name))
                })
        }
        UntypedExpr::FieldAccess { container, .. } => expr_uses_var(container, name),
        UntypedExpr::Tuple { elements, .. } => {
            elements.iter().any(|el| expr_uses_var(el, name))
        }
        UntypedExpr::TupleIndex { tuple, .. } => expr_uses_var(tuple, name),
        UntypedExpr::NegateBool { value, .. } | UntypedExpr::NegateInt { value, .. } => {
            expr_uses_var(value, name)
        }
        // Conservative: unknown node shapes count as "uses it".
        _ => true,
    }
}

fn stmt_uses_var(s: &UntypedStatement, name: &str) -> bool {
    match s {
        Statement::Expression(e) => expr_uses_var(e, name),
        Statement::Assignment(a) => expr_uses_var(&a.value, name),
        _ => true,
    }
}
