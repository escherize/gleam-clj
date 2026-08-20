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
for f in coin_change shapes sum_to jellyfish ffi_demo; do
  ./gleam-to-clj/target/debug/gleam-to-clj "gleam-src/$f.gleam" "gen/$f.clj"
done
for m in coin-change shapes sum-to jellyfish ffi-demo; do
  clojure -A:gen -M -m "$m" >/dev/null 2>&1 || { echo "FIXTURE FAILED: $m"; exit 1; }
done
echo "fixtures ok (5/5)"

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

suite rosetta "Gleam solutions scraped from Rosetta Code (GFDL, fetched locally via rosetta/scrape.py, not redistributed)"
suite tour "all 63 example programs from the official Gleam language tour"

echo "ALL GREEN"
