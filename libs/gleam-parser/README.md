# gleam-parser

Parse Gleam source code from Clojure. The parser is
[glance](https://hexdocs.pm/glance/) — Gleam's own parser library, written
in Gleam — compiled to Clojure by [gleam-clj](../..). It parses its own
source as a test.

```clojure
;; deps.edn
{:deps {io.github.escherize/gleam-parser
        {:git/url "https://github.com/escherize/gleam-clj"
         :git/sha "..." :deps/root "libs/gleam-parser"}}}
```

```clojure
(require 'gleam-parser)

(gleam-parser/defs (gleam-parser/parse (slurp "src/thing.gleam")))
;; => ({:kind :function, :name "main", :publicity "Public", :parameters 0}
;;     {:kind :custom-type, :name "Shape", :publicity "Public",
;;      :variants ["Circle" "Rect" "Point"]}
;;     ...)
```

`parse` returns the full glance `Module` record (`:functions`
`:custom-types` `:constants` `:imports` `:type-aliases` — Clojure records
all the way down, keyword access everywhere) or nil; `parse!` throws
ex-info with the glance error record. Every public fn carries
`{:malli/schema ...}` metadata derived from the Gleam types.

`src/` is a build artifact (compiled glance + its deps + the gleam-clj
runtime, self-contained); regenerate with `./build.sh`. The public wrapper
lives in `wrapper/`. Run tests: `clojure -M:test -m gleam-parser-test`.

Compiled-in dependencies and their licences: glance, glexer, splitter, and
gleam_stdlib are Apache-2.0 (© their authors; sources on hex.pm). The
splitter/glexer native externals are reimplemented in Clojure here.
