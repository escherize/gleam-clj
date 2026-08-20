# gleam-clj

A [Gleam](https://gleam.run) -> Clojure/JVM compiler. Not an official Gleam
project.

**Status: experimental.** Parse-only frontend (the typed-AST integration is
planned); everything unsupported fails loudly at build time, nothing is
silently wrong on purpose. Verified by stdout-parity corpora against real
`gleam run`: 54/54 runnable Rosetta Code tasks, 58/58 runnable language-tour
lessons, gleam_stdlib self-hosted, real hex packages (snag, glance) running
byte-identical on the JVM.

Known approximations: dict/set iteration is key-sorted (matches BEAM small
maps), mutual recursion is JVM-stack-bounded (self tail calls become
`recur`), int arithmetic auto-promotes via `+'` (bignum parity, boxed).

## Licences and provenance

- This project: Apache-2.0.
- `stdlib-src/` vendors [gleam_stdlib](https://github.com/gleam-lang/stdlib)
  (Apache-2.0); `stdlib-clj/` is compiled from it.
- `tour/tasks/` vendors lesson code from
  [gleam-lang/language-tour](https://github.com/gleam-lang/language-tour)
  (Apache-2.0).
- Rosetta Code task content is GFDL and is **not** redistributed here; run
  `python3 rosetta/scrape.py` to fetch it locally.
- Depends on [gleam-core](https://github.com/gleam-lang/gleam) via a fork
  carrying a one-line patch (`pub mod call_graph`).

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

## Self-hosted stdlib

`stdlib-clj/` is gleam_stdlib itself, compiled by this compiler from the
vendored sources in `stdlib-src/` (Apache-2.0, see stdlib-src/LICENCE) over
a ~110-fn native core in `src/gleam_ffi.clj`, wired by
`stdlib-src/clojure-externals.txt`. Regenerate after emitter changes:

    ./gleam-to-clj/target/debug/gleam-to-clj build stdlib-src stdlib-clj

Stdlib fns keep their Gleam names (`list/map`, `dict/get`); names that
would shadow clojure.core get `(:refer-clojure :exclude ...)` in the
generated ns, and names the emitter itself emits bare (`first`, `rest`,
`count`...) carry a `'` suffix (`list/first'`). The hand-written rename
table is gone. Graphemes are real (BreakIterator). A few bit-level fns are
whole-fn overrides in the externals map (sub-byte bit patterns).

## Dependencies and tests

`build` walks `src/` and `test/`, then follows the import graph into the
package sources `gleam build` vendors under `build/packages/*/src` — pure-
Gleam hex deps compile right along (verified: snag, byte-identical output).
Dependency fns whose externals have no Gleam fallback body fail the build
loudly with the module and fn named. gleeunit is shimmed: the compiled test
module's `main` discovers and runs every `*-test` fn, nonzero exit on
failure.

`./check.sh` runs everything: emitter build, snapshot tests, fixtures, both
corpora. Corpus runs enforce a ratchet — passing fewer tasks than the last
full run fails.

## Clojure FFI

`@external(javascript, "clojure.string", "upper-case")` is interpreted as
the Clojure binding: the fn emits as `(def shout clojure.string/upper-case)`
and the namespace is required. (A proper `clojure` external target keyword
awaits the typed-AST fork.)

## Tests

`cargo test` in `gleam-to-clj/` snapshot-checks emitter output against the
checked-in `gen/*.clj`. On intentional output changes, regenerate the gen
files and commit them.
