#!/bin/bash
# Full verification: emitter build + snapshots, fixtures on the JVM, both
# corpora with the pass-count ratchet. Any failure is loud and fatal.
set -euo pipefail
cd "$(dirname "$0")"

echo "== cargo build + snapshot tests"
(cd gleam-to-clj && cargo build -q && cargo test -q)

echo "== fixtures"
for f in coin_change shapes sum_to jellyfish ffi_demo; do
  ./gleam-to-clj/target/debug/gleam-to-clj "gleam-src/$f.gleam" "gen/$f.clj"
done
for m in coin-change shapes sum-to jellyfish ffi-demo; do
  clojure -A:gen -M -m "$m" >/dev/null 2>&1 || { echo "FIXTURE FAILED: $m"; exit 1; }
done
echo "fixtures ok"

suite() {
  if [ -z "$(ls "$1/tasks"/*.gleam 2>/dev/null)" ]; then
    echo "== $1 corpus: no tasks (see $1/scrape.py or README), skipping"
    return 0
  fi
  echo "== $1 corpus"
  python3 rosetta/run.py "$1" 2>/dev/null | sed -n '/== totals/,/^$/p'
}
suite rosetta
suite tour

echo "ALL GREEN"
