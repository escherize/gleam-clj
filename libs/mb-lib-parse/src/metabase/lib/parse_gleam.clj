(ns metabase.lib.parse-gleam
  "Drop-in replacement for metabase.lib.parse whose parser is written in
  Gleam (type-checked, exhaustively pattern-matched) and compiled to Clojure
  by gleam-clj. Same signatures, same output shapes, same error behavior."
  (:require [mb-lib-parse :as impl]
            [gleam.prelude :as p])
  (:import (gleam.prelude Ok)
           (mb_lib_parse Literal Param Optional
                         Unterminated InvalidParamName EmptyParam
                         OptionalWithoutParam)))

(defn- fragment->clj [f]
  (condp instance? f
    Literal (:value f)
    Param {:type :metabase.lib.parse/param, :name (:value f)}
    Optional {:type :metabase.lib.parse/optional
              :contents (mapv fragment->clj (:value f))}))

(def ^:private error->message
  {Unterminated "Invalid query: found \"[[\" or \"{{\" with no matching \"]]\" or \"}}\""
   InvalidParamName "Invalid '{{...}}' clause: expected a param name"
   EmptyParam "'{{...}}' clauses cannot be empty."
   OptionalWithoutParam "[[...]] clauses must contain at least one '{{...}}' clause."})

(defn parse
  "Parse parameters in string `s`. Returns a sequence of string fragments
  interposed with maps for params and optional clauses; throws ex-info with
  {:type (:parse-error-type opts)} on invalid input (matching the behavior
  of metabase.lib.parse)."
  ([opts s]
   (parse opts s true))
  ([opts s handle-sql-comments]
   (let [strict (some? (:parse-error-type opts))
         result (impl/parse s handle-sql-comments strict)]
     (if (instance? Ok result)
       (mapv fragment->clj (:value result))
       (throw (ex-info (error->message (class (:value result)) "parse error")
                       {:type (:parse-error-type opts)}))))))
