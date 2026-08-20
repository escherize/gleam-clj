(ns gleam.string-tree
  (:refer-clojure :exclude [concat replace reverse])
  (:require
   [gleam-ffi]
   [gleam.list :as list]))

;; type StringTree

;; type Direction
(defrecord All [])
(defn All? [v] (instance? All v))

(def ^{:malli/schema [:=> [:cat [:sequential :string]] [:or ]]} from-strings gleam-ffi/st-from-strings)

(defn new*
  "Create an empty `StringTree`. Useful as the start of a pipe chaining many
  trees together."
  {:malli/schema [:=> [:cat] [:or ]]}
  []
  (from-strings (list)))

(def ^{:malli/schema [:=> [:cat :string] [:or ]]} from-string gleam-ffi/st-from-string)

(def ^{:malli/schema [:=> [:cat [:or ] [:or ]] [:or ]]} append-tree gleam-ffi/st-append-tree)

(defn prepend
  "Prepends a `String` onto the start of some `StringTree`.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:or ] :string] [:or ]]}
  [tree prefix]
  (append-tree (from-string prefix) tree))

(defn append
  "Appends a `String` onto the end of some `StringTree`.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:or ] :string] [:or ]]}
  [tree second]
  (append-tree tree (from-string second)))

(defn prepend-tree
  "Prepends some `StringTree` onto the start of another.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:or ] [:or ]] [:or ]]}
  [tree prefix]
  (append-tree prefix tree))

(def ^{:malli/schema [:=> [:cat [:sequential [:or ]]] [:or ]]} concat gleam-ffi/st-concat)

(def ^{:malli/schema [:=> [:cat [:or ]] :string]} to-string gleam-ffi/st-to-string)

(def ^{:malli/schema [:=> [:cat [:or ]] :int]} byte-size gleam-ffi/st-byte-size)

(defn join
  "Joins the given trees into a new tree separated with the given string."
  {:malli/schema [:=> [:cat [:sequential [:or ]] :string] [:or ]]}
  [trees sep]
  (-> trees (list/intersperse (from-string sep)) concat))

(def ^{:malli/schema [:=> [:cat [:or ]] [:or ]]} lowercase gleam-ffi/st-lowercase)

(def ^{:malli/schema [:=> [:cat [:or ]] [:or ]]} uppercase gleam-ffi/st-uppercase)

(def do-to-graphemes gleam-ffi/st-to-graphemes)

(defn reverse
  "Converts a `StringTree` to a new one with the contents reversed."
  {:malli/schema [:=> [:cat [:or ]] [:or ]]}
  [tree]
  (-> tree to-string do-to-graphemes list/reverse from-strings))

(def erl-split gleam-ffi/st-split)

(defn split
  "Splits a `StringTree` on a given pattern into a list of trees."
  {:malli/schema [:=> [:cat [:or ] :string] [:sequential [:or ]]]}
  [tree pattern]
  (erl-split tree pattern (->All)))

(def ^{:malli/schema [:=> [:cat [:or ] :string :string] [:or ]]} replace gleam-ffi/st-replace)

(defn is-equal
  "Compares two string trees to determine if they have the same textual
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
  {:malli/schema [:=> [:cat [:or ] [:or ]] :boolean]}
  [a b]
  (= a b))

(defn is-empty
  "Inspects a `StringTree` to determine if it is equivalent to an empty string.
  
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
  {:malli/schema [:=> [:cat [:or ]] :boolean]}
  [tree]
  (= (from-string "") tree))
