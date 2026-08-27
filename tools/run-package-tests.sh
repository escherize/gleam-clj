#!/bin/bash
# Run a Gleam package's own gleeunit test suite on the JVM.
#
# The package's test/ directory compiles along with src/; every emitted
# *_test namespace is required and the gleeunit shim runs every public
# *-test fn. Upstream authors' assertions become the evidence.
#
# Usage: tools/run-package-tests.sh <project-dir>
#        (project-dir needs gleam.toml; deps are downloaded if missing)
#
# Exit code: gleeunit's (non-zero on any failing test).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BIN="$ROOT/gleam-to-clj/target/debug/gleam-to-clj"
PROJ="${1:?usage: run-package-tests.sh <project-dir>}"
[ -x "$BIN" ] || { echo "build the compiler first: (cd gleam-to-clj && cargo build)"; exit 1; }
[ -f "$PROJ/gleam.toml" ] || { echo "$PROJ has no gleam.toml"; exit 1; }
[ -d "$PROJ/test" ] || { echo "$PROJ has no test/ directory"; exit 1; }

(cd "$PROJ" && gleam deps download >/dev/null 2>&1) || true
OUT="$PROJ/out-clj"
"$BIN" build "$PROJ" "$OUT" "$ROOT/stdlib-src"

# BEAM gleeunit runs every module in test/, whatever it is named; derive
# the namespaces from the test sources (path -> dotted kebab ns).
NSES=$(cd "$PROJ/test" && find . -name '*.gleam' \
  | sed -e 's|^\./||' -e 's|\.gleam$||' -e 's|/|.|g' -e 's|_|-|g')
[ -n "$NSES" ] || { echo "no test modules found"; exit 1; }
REQUIRES=$(echo "$NSES" | sed -e 's|.*|(require (quote &))|')
NSLIST=$(echo "$NSES" | sed -e "s|.*|(quote &)|" | tr '\n' ' ')

clojure -Sdeps "{:paths [\"$ROOT/src\" \"$ROOT/stdlib-clj\" \"$OUT\"]}" \
  -M -e "$REQUIRES (gleeunit/main (list $NSLIST))"
