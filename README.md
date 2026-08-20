# gleam-clj

Gleam -> Clojure compiler, v0.

## Pipeline

```
gleam-src/foo.gleam
      |  gleam-to-clj (Rust bin; parses with gleam-core from ../gleam)
      v
gen/foo.clj  ->  runs on the JVM against the shim library in src/gleam/
```

## Generate and run

```bash
cd gleam-to-clj
cargo run -q -- ../gleam-src/coin_change.gleam ../gen/coin_change.clj
cd ..
clojure -A:gen -M -m coin-change   # exit 0 = all `let assert`s passed
```

Reference check: the same `.gleam` runs under real Gleam (`gleam new` a
scratch project, drop the file in `src/`, `gleam run`).

## Layout

- `gleam-to-clj/` — the emitter. Parse-only (untyped AST) in v0; see the
  limitation list at the top of `src/main.rs`.
- `src/gleam/` — runtime shims: `prelude` (Ok/Error records), `list`, `dict`,
  `int`. Gleam stdlib names that collide with clojure.core are renamed
  (rename table lives in `Ctx::module_fn` in the emitter and in each shim's
  ns docstring): `int.range -> fold-range`, `list.reduce -> reduce1`,
  `dict.get -> lookup`. `int.min`/`int.max` emit `clojure.core/min`/`max`
  directly.
- `golden/coin_change.clj` — hand-written target output (the readability
  spec); run with `clojure -A:golden -M -m coin-change`. Not hand-edited
  going forward except to update the spec.
- `gen/` — generated output. Never hand-edit.

## Representation

| Gleam | Clojure |
|---|---|
| Int / Float / Bool / String | long / double / boolean / String |
| Nil | nil |
| List | eager seq (`(list ...)`) |
| Tuple | vector |
| Dict | persistent map |
| Ok / Error | `gleam.prelude.Ok` / `.Error` defrecords, field `:value` |
| fn | Clojure fn |
| `\|>` | `->` (first-arg insertion assumed) |

Variant classes whose simple name collides with java.lang (e.g. `Error`)
are `ns-unmap`ped at definition and referenced fully qualified.

## Tail calls

Self-calls in tail position emit `recur` (constant stack, matches BEAM TCO);
non-tail self-calls stay plain calls. Mutual recursion is stack-bounded on
the JVM — deliberately not trampolined, since that would poison every return
value at the interop boundary.

## Multi-module builds

`gleam-to-clj build <project-dir> <out-dir>` compiles every module under
`<project-dir>/src` into one Clojure namespace each, in call-graph
dependency order. Cross-module constructors work qualified (`g.Mouse(...)`)
and via unqualified imports (`import mod.{Mouse}`). Top-level names that
would shadow clojure.core get `(:refer-clojure :exclude [...])`.

Labelled call arguments are verified against a signature registry (local
fns, project modules, and a table generated from gleam_stdlib) and
reordered; calls whose labels cannot be verified fail the build loudly —
nothing is silently assumed positional.

## Clojure FFI

`@external(javascript, "clojure.string", "upper-case")` is interpreted as
the Clojure binding: the fn emits as `(def shout clojure.string/upper-case)`
and the namespace is required. (A proper `clojure` external target keyword
awaits the typed-AST fork.)

## Tests

`cargo test` in `gleam-to-clj/` snapshot-checks emitter output against the
checked-in `gen/*.clj`. On intentional output changes, regenerate the gen
files and commit them.
