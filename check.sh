#!/bin/bash
# Full verification: emitter build + snapshots, fixtures on the JVM, both
# corpora with the pass-count ratchet. Any failure is loud and fatal.
set -euo pipefail
cd "$(dirname "$0")"

cat <<'BANNER'
================================================================================
gleam-clj verification

The claim under test: Gleam programs compiled to Clojure by gleam-to-clj
behave identically to the same programs run by the real Gleam compiler.

Method, per corpus task:
  1. oracle   — the .gleam source runs on the BEAM via the official `gleam`
                compiler; its stdout is captured as the expected output
  2. compile  — gleam-to-clj translates the same source to a Clojure namespace
  3. run      — that namespace executes on the JVM (against the compiled
                gleam_stdlib in stdlib-clj/, itself built by this compiler)
  4. compare  — stdout must be byte-identical; programs that crash on purpose
                (todo/panic/assert demos) must also crash, with matching stdout

The ratchet: a run that passes fewer tasks than the last committed run fails.
================================================================================
BANNER

echo "== cargo build + snapshot tests (emitter output locked against gen/)"
(cd gleam-to-clj && cargo build -q && cargo test -q)

echo "== fixtures: hand-written programs, compiled and executed on the JVM"
for f in coin_change shapes sum_to jellyfish ffi_demo permissions regressions bit_patterns dynamics; do
  ./gleam-to-clj/target/debug/gleam-to-clj "gleam-src/$f.gleam" "gen/$f.clj"
done
for m in coin-change shapes sum-to jellyfish ffi-demo permissions regressions bit-patterns dynamics; do
  clojure -A:gen -M -m "$m" >/dev/null 2>&1 || { echo "FIXTURE FAILED: $m"; exit 1; }
done
echo "fixtures ok (9/9)"

suite() {
  if [ -z "$(ls "$1/tasks"/*.gleam 2>/dev/null)" ]; then
    echo "== $1 corpus: no tasks present — $2 — skipping"
    return 0
  fi
  echo "== $1 corpus: $2"
  python3 rosetta/run.py "$1" 2>/dev/null | sed -n '/== totals/,$p'
  python3 - "$1" <<'EOF'
import json, sys
s = json.load(open(f"{sys.argv[1]}/status.json"))
n = lambda k: sum(1 for v in s.values() if v["status"] == k)
runnable = len(s) - n("excluded") - n("ref_fail")
print(f"SUMMARY [{sys.argv[1]}]: {n('pass')}/{runnable} runnable tasks produce "
      f"byte-identical stdout on the JVM"
      + (f"; {n('excluded')} excluded (BEAM/JS-native FFI, out of scope)" if n("excluded") else "")
      + (f"; {n('ref_fail')} have no oracle (real gleam cannot run them)" if n("ref_fail") else ""))
if n("diff") or n("clj_fail") or n("emit_fail"):
    sys.exit(1)
EOF
}
if command -v clj-kondo >/dev/null; then
  echo "== clj-kondo over all generated Clojure (zero findings required)"
  clj-kondo --lint gen stdlib-clj src 2>/dev/null | tail -1
  clj-kondo --lint gen stdlib-clj src >/dev/null 2>&1 || { echo "LINT FAILED"; exit 1; }
else
  echo "== clj-kondo not installed, skipping lint"
fi

echo "== REPL loader (compile + load Gleam from Clojure)"
clojure -Sdeps '{:paths ["src" "stdlib-clj" "test"]}' -M -m gleam-clj.load-test >/dev/null 2>&1 \
  && echo "loader ok (require-gleam + eval-gleam)" \
  || { echo "LOADER FAILED"; exit 1; }

echo "== try alias (one-shot compile + call)"
clojure -M:try >/dev/null 2>&1 && echo "try ok" || { echo "TRY FAILED"; exit 1; }

echo "== gleam-parser library (compiled glance): build, tests, lint"
if [ -d libs/gleam-parser/project/build ]; then
  ./libs/gleam-parser/build.sh >/dev/null
  (cd libs/gleam-parser && clojure -M:test -m gleam-parser-test >/dev/null 2>&1)     || { echo "GLEAM-PARSER TESTS FAILED"; exit 1; }
  if command -v clj-kondo >/dev/null; then
    (cd libs/gleam-parser && clj-kondo --lint src test >/dev/null 2>&1)       || { echo "GLEAM-PARSER LINT FAILED"; exit 1; }
  fi
  echo "gleam-parser ok (glance parses its own source on the JVM)"
else
  echo "gleam-parser skipped (run: cd libs/gleam-parser/project && gleam build)"
fi

echo "== mb-lib-parse library (metabase.lib.parse in Gleam): build, parity, lint"
if [ -d libs/mb-lib-parse/project/build ]; then
  ./libs/mb-lib-parse/build.sh >/dev/null
  (cd libs/mb-lib-parse && clojure -M:test -m parse-gleam-test >/dev/null 2>&1)     || { echo "MB-LIB-PARSE PARITY FAILED"; exit 1; }
  (cd libs/mb-lib-parse && clojure -M:test -m differential-test >/dev/null 2>&1)     || { echo "MB-LIB-PARSE DIFFERENTIAL FAILED"; exit 1; }
  if command -v clj-kondo >/dev/null; then
    (cd libs/mb-lib-parse && clj-kondo --lint src test >/dev/null 2>&1)       || { echo "MB-LIB-PARSE LINT FAILED"; exit 1; }
  fi
  echo "mb-lib-parse ok (48 ported assertions + differential fuzz vs original when present)"
else
  echo "mb-lib-parse skipped (run: cd libs/mb-lib-parse/project && gleam build)"
fi

suite rosetta "Gleam solutions scraped from Rosetta Code (GFDL, fetched locally via rosetta/scrape.py, not redistributed)"
suite tour "all 63 example programs from the official Gleam language tour"

echo "ALL GREEN"
