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
