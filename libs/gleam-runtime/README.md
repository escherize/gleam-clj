# gleam-runtime

The gleam-clj runtime as a standalone dep: `gleam.prelude`, the `gleam-ffi`
native core, and the self-hosted compiled `gleam_stdlib` (Apache-2.0, see
../../stdlib-src/LICENCE). Depend on this when you vendor compiled Gleam
modules directly into your own source tree:

    {:git/url "https://github.com/escherize/gleam-clj"
     :git/sha "..." :deps/root "libs/gleam-runtime"}

`src/` is a build artifact; regenerate with `./build.sh`.
