#!/bin/bash
# Snapshot the gleam-clj runtime (prelude + FFI core + compiled stdlib) as a
# standalone consumable library. src/ is a build artifact.
set -euo pipefail
cd "$(dirname "$0")"
ROOT=../..
rm -rf src
mkdir -p src/gleam
cp "$ROOT"/src/*.clj src/
cp "$ROOT"/src/gleam/*.clj src/gleam/
cp -r "$ROOT"/stdlib-clj/gleam/. src/gleam/
mkdir -p src/clj-kondo.exports/io.github.escherize/gleam-runtime
cp -r "$ROOT"/libs/gleam-parser/.clj-kondo/. src/clj-kondo.exports/io.github.escherize/gleam-runtime/
echo "built $(find src -name '*.clj' | wc -l | tr -d ' ') files"
