# mb-lib-parse

`metabase.lib.parse` — the parser for `{{param}}` and `[[optional]]`
clauses in Metabase native queries — reimplemented in Gleam (typed sum
types, exhaustive matching, Result instead of exception-based
backtracking) and compiled to Clojure by [gleam-clj](../..).

The wrapper ns `metabase.lib.parse-gleam` is signature- and
behavior-compatible: same fn arity, same output shapes
(`{:type :metabase.lib.parse/param, :name ...}` etc.), same ex-info error
behavior including the lenient no-error-type mode.

Verified two ways:
- the entire upstream test table (48 assertions) ported and passing
- differential fuzzing: 5000 random token soups x 4 modes = 20,000
  comparisons against the original implementation (loaded at test time
  from a local Metabase checkout; skipped when absent) — zero divergence,
  including which error type is thrown

The port found one real gleam-clj stdlib bug on day one (erl-split
first-vs-all semantics in string.split_once) — parity suites earn their
keep. `src/` is a build artifact; regenerate with `./build.sh`; the source
of truth is `project/src/mb_lib_parse.gleam` (also runs on the BEAM:
`cd project && gleam run` executes its self-checks).
