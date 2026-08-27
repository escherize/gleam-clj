#!/bin/bash
# Wild-code sweep: fuzz gleam-to-clj against real hex packages and GitHub
# Gleam repos. Each target is fetched, its deps resolved with `gleam deps
# download`, compiled with the current emitter build, and classified:
#
#   OK         compiled every reachable module
#   NEEDS-FFI  refused loudly at a missing JS/Erlang external (by design)
#   PANIC      emitter bug: investigate
#   GLEAM-ERR  Gleam's own diagnostic (usually a dep or syntax mismatch)
#
# Usage: tools/wild-sweep.sh            (sweep dir defaults to ~/.cache/gleam-clj-wild)
#        WILD_DIR=/tmp/sweep tools/wild-sweep.sh
#
# Requires: curl, the gleam CLI, a built gleam-to-clj (cargo build).
# Network-dependent; not part of check.sh.
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SWEEP="${WILD_DIR:-$HOME/.cache/gleam-clj-wild}"
BIN="$ROOT/gleam-to-clj/target/debug/gleam-to-clj"
[ -x "$BIN" ] || { echo "build the compiler first: (cd gleam-to-clj && cargo build)"; exit 1; }
mkdir -p "$SWEEP"
cd "$SWEEP"

PACKAGES="nibble party gap tote glearray iv gleam_yielder gleam_community_maths gleam_community_colour gleam_community_ansi rank prng gsv justin filepath gleam_http glint birl gleam_json simplifile"
REPOS="TanklesXL/gladvent:gladvent giacomocavalieri/advent:advent schurhammer/advent-of-code-2022:aoc2022"

fetch_hex() {
  local p=$1
  [ -d "$p/src" ] && return 0
  local ver
  ver=$(curl -s "https://hex.pm/api/packages/$p" | python3 -c 'import json,sys; print(json.load(sys.stdin)["latest_stable_version"])' 2>/dev/null)
  [ -z "$ver" ] && return 1
  mkdir -p "$p" && cd "$p"
  curl -s "https://repo.hex.pm/tarballs/$p-$ver.tar" -o pkg.tar \
    && tar -xf pkg.tar contents.tar.gz \
    && tar -xzf contents.tar.gz \
    && rm -f pkg.tar contents.tar.gz VERSION CHECKSUM metadata.config
  cd ..
  [ -d "$p/src" ]
}

run_one() {
  local name=$1 dir=$2
  (cd "$dir" && gleam deps download >/dev/null 2>&1)
  local err rc
  err=$("$BIN" build "$dir" "$dir/out-clj" "$ROOT/stdlib-src" 2>&1 >/dev/null); rc=$?
  if [ $rc -eq 0 ]; then
    echo "OK        $name"
  elif echo "$err" | grep -qi 'native external\|no usable external\|clojure-externals'; then
    echo "NEEDS-FFI $name :: $(echo "$err" | grep -im1 'external' | cut -c1-120)"
  elif echo "$err" | grep -q 'panicked'; then
    echo "PANIC     $name :: $(echo "$err" | grep -m1 -A1 'panicked' | tail -1 | cut -c1-120)"
  elif echo "$err" | grep -q '^error:'; then
    echo "GLEAM-ERR $name :: $(echo "$err" | grep -m1 '^error:' | cut -c1-120)"
  else
    echo "FAIL      $name :: $(echo "$err" | head -1 | cut -c1-120)"
  fi
}

echo "== hex packages =="
for p in $PACKAGES; do
  fetch_hex "$p" >/dev/null 2>&1 || { echo "FETCHFAIL $p"; continue; }
  run_one "$p" "$SWEEP/$p"
done

echo
echo "== github repos =="
for spec in $REPOS; do
  url=${spec%%:*}; name=${spec##*:}
  [ -d "$name" ] || git clone -q --depth 1 "https://github.com/$url" "$name" 2>/dev/null || { echo "FETCHFAIL $name"; continue; }
  [ -f "$name/gleam.toml" ] || { echo "SKIP $name (no gleam.toml)"; continue; }
  run_one "$name" "$SWEEP/$name"
done
echo "== sweep done =="
