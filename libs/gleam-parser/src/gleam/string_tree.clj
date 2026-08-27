(ns gleam.string-tree
  (:refer-clojure :exclude [concat replace reverse])
  (:require
   [gleam-ffi]
   [gleam.list :as list]))

(declare StringTree? StringTree-schema All? Direction? Direction-schema)

;; type StringTree
(defprotocol IStringTree)
(defn StringTree? "True if `v` is any StringTree value." [v] (instance? gleam.string_tree.IStringTree v))
(defn StringTree-schema
  "Malli schema for StringTree."
  []
  [:fn StringTree?])

;; type Direction
(defprotocol IDirection)
(defrecord All [] IDirection)
(defn All? "True if `v` is a All value." [v] (instance? All v))
(defn Direction? "True if `v` is any Direction value." [v] (instance? gleam.string_tree.IDirection v))
(defn Direction-schema
  "Malli schema for Direction."
  []
  [:fn All?])

(def ^{:malli/schema [:=> [:cat [:sequential :string]] (StringTree-schema)] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:69"} from-strings gleam-ffi/st-from-strings)

(defn new*
  "new() -> StringTree

   Create an empty `StringTree`. Useful as the start of a pipe chaining many
   trees together."
  {:malli/schema [:=> [:cat] (StringTree-schema)]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:24"}
  []
  (from-strings (list)))

(def ^{:malli/schema [:=> [:cat :string] (StringTree-schema)] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:85"} from-string gleam-ffi/st-from-string)

(def ^{:malli/schema [:=> [:cat (StringTree-schema) (StringTree-schema)] (StringTree-schema)] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:61"} append-tree gleam-ffi/st-append-tree)

(defn prepend
  "prepend(to tree: StringTree, prefix prefix: String) -> StringTree

   Prepends a `String` onto the start of some `StringTree`.

   Runs in constant time."
  {:malli/schema [:=> [:cat (StringTree-schema) :string] (StringTree-schema)]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:32"}
  [tree ^java.lang.String prefix]
  (append-tree (from-string prefix) tree))

(defn append
  "append(to tree: StringTree, suffix second: String) -> StringTree

   Appends a `String` onto the end of some `StringTree`.

   Runs in constant time."
  {:malli/schema [:=> [:cat (StringTree-schema) :string] (StringTree-schema)]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:40"}
  [tree ^java.lang.String second]
  (append-tree tree (from-string second)))

(defn prepend-tree
  "prepend_tree(to tree: StringTree, prefix prefix: StringTree) -> StringTree

   Prepends some `StringTree` onto the start of another.

   Runs in constant time."
  {:malli/schema [:=> [:cat (StringTree-schema) (StringTree-schema)]
                      (StringTree-schema)]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:48"}
  [tree prefix]
  (append-tree prefix tree))

(def ^{:malli/schema [:=> [:cat [:sequential (StringTree-schema)]] (StringTree-schema)] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:77"} concat gleam-ffi/st-concat)

(def ^{:malli/schema [:=> [:cat (StringTree-schema)] :string] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:94"} to-string gleam-ffi/st-to-string)

(def ^{:malli/schema [:=> [:cat (StringTree-schema)] :int] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:100"} byte-size gleam-ffi/st-byte-size)

(defn join
  "join(trees: List(StringTree), with sep: String) -> StringTree

   Joins the given trees into a new tree separated with the given string."
  {:malli/schema [:=> [:cat [:sequential (StringTree-schema)] :string]
                      (StringTree-schema)]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:104"}
  [trees ^java.lang.String sep]
  (-> trees (list/intersperse (from-string sep)) concat))

(def ^{:malli/schema [:=> [:cat (StringTree-schema)] (StringTree-schema)] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:115"} lowercase gleam-ffi/st-lowercase)

(def ^{:malli/schema [:=> [:cat (StringTree-schema)] (StringTree-schema)] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:122"} uppercase gleam-ffi/st-uppercase)

(def ^{:gleam/src "stdlib-src/src/gleam/string_tree.gleam:136"} do-to-graphemes gleam-ffi/st-to-graphemes)

(defn reverse
  "reverse(tree: StringTree) -> StringTree

   Converts a `StringTree` to a new one with the contents reversed."
  {:malli/schema [:=> [:cat (StringTree-schema)] (StringTree-schema)]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:127"}
  [tree]
  (-> tree to-string do-to-graphemes list/reverse from-strings))

(def ^{:gleam/src "stdlib-src/src/gleam/string_tree.gleam:150"} erl-split gleam-ffi/st-split)

(defn split
  "split(tree: StringTree, on pattern: String) -> List(StringTree)

   Splits a `StringTree` on a given pattern into a list of trees."
  {:malli/schema [:=> [:cat (StringTree-schema) :string]
                      [:sequential (StringTree-schema)]]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:145"}
  [tree ^java.lang.String pattern]
  (erl-split tree pattern (->All)))

(def ^{:malli/schema [:=> [:cat (StringTree-schema) :string :string] (StringTree-schema)] :gleam/src "stdlib-src/src/gleam/string_tree.gleam:156"} replace gleam-ffi/st-replace)

(defn is-equal
  "is_equal(a: StringTree, b: StringTree) -> Bool

   Compares two string trees to determine if they have the same textual
   content.

   Comparing two string trees using the `==` operator may return `False` even
   if they have the same content as they may have been built in different ways,
   so using this function is often preferred.

   ## Examples

   ```gleam
   assert string_tree.from_strings([\"a\", \"b\"]) != string_tree.from_string(\"ab\")
   ```

   ```gleam
   assert string_tree.is_equal(
   string_tree.from_strings([\"a\", \"b\"]),
   string_tree.from_string(\"ab\"),
   )
   ```"
  {:malli/schema [:=> [:cat (StringTree-schema) (StringTree-schema)] :boolean]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:183"}
  [a b]
  (= a b))

(defn is-empty
  "is_empty(tree: StringTree) -> Bool

   Inspects a `StringTree` to determine if it is equivalent to an empty string.

   ## Examples

   ```gleam
   assert !{ string_tree.from_string(\"ok\") |> string_tree.is_empty }
   ```

   ```gleam
   assert string_tree.from_string(\"\") |> string_tree.is_empty
   ```

   ```gleam
   assert string_tree.from_strings([]) |> string_tree.is_empty
   ```"
  {:malli/schema [:=> [:cat (StringTree-schema)] :boolean]
   :gleam/src "stdlib-src/src/gleam/string_tree.gleam:204"}
  [tree]
  (= (from-string "") tree))
