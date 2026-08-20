#!/bin/bash
# Regenerate src/ (build artifact): compile the Gleam parser with gleam-to-clj
# and snapshot the runtime. Edit wrapper/, never src/.
set -euo pipefail
cd "$(dirname "$0")"
ROOT=../..

(cd "$ROOT/gleam-to-clj" && cargo build -q)
(cd project && gleam build 2>/dev/null >/dev/null)

rm -rf src
mkdir -p src
"$ROOT/gleam-to-clj/target/debug/gleam-to-clj" build project src "$ROOT/stdlib-src"

cp "$ROOT"/src/*.clj src/
mkdir -p src/gleam
cp "$ROOT"/src/gleam/*.clj src/gleam/
cp -r "$ROOT"/stdlib-clj/gleam/. src/gleam/
cp -r wrapper/. src/

mkdir -p src/clj-kondo.exports/io.github.escherize/mb-lib-parse
cp -r "$ROOT"/libs/gleam-parser/.clj-kondo/. src/clj-kondo.exports/io.github.escherize/mb-lib-parse/

echo "built $(find src -name '*.clj' | wc -l | tr -d ' ') files into src/"
