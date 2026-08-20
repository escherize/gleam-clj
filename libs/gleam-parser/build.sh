#!/bin/bash
# Regenerate the library's src/ from sources: compile glance (+ deps) with
# gleam-to-clj and snapshot the runtime. src/ is a build artifact — edit the
# wrapper in wrapper/, the runtime in the repo root, never src/ directly.
set -euo pipefail
cd "$(dirname "$0")"
ROOT=../..

(cd "$ROOT/gleam-to-clj" && cargo build -q)
(cd project && gleam build 2>/dev/null >/dev/null)

rm -rf src
mkdir -p src
"$ROOT/gleam-to-clj/target/debug/gleam-to-clj" build project src "$ROOT/stdlib-src"

# Runtime snapshot: prelude + FFI cores + compiled stdlib.
cp "$ROOT"/src/*.clj src/
mkdir -p src/gleam
cp "$ROOT"/src/gleam/*.clj src/gleam/
cp -r "$ROOT"/stdlib-clj/gleam/. src/gleam/

# The wrapper is the public API.
cp wrapper/*.clj src/

# Exported clj-kondo config (with-use hook) so consumers' linting works.
mkdir -p src/clj-kondo.exports/io.github.escherize/gleam-parser
cp -r .clj-kondo/. src/clj-kondo.exports/io.github.escherize/gleam-parser/

echo "built $(find src -name '*.clj' | wc -l | tr -d ' ') files into src/"
