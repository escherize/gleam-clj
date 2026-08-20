(ns gleam.string-tree
  (:refer-clojure :exclude [concat replace reverse])
  (:require
   [gleam-ffi]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type StringTree

;; type Direction
(defrecord All [])

(def from-strings gleam-ffi/st-from-strings)

(defn new*
  "Create an empty `StringTree`. Useful as the start of a pipe chaining many
  trees together."
  []
  (from-strings (list)))

(def from-string gleam-ffi/st-from-string)

(def append-tree gleam-ffi/st-append-tree)

(defn prepend
  "Prepends a `String` onto the start of some `StringTree`.
  
  Runs in constant time."
  [tree prefix]
  (append-tree (from-string prefix) tree))

(defn append
  "Appends a `String` onto the end of some `StringTree`.
  
  Runs in constant time."
  [tree second]
  (append-tree tree (from-string second)))

(defn prepend-tree
  "Prepends some `StringTree` onto the start of another.
  
  Runs in constant time."
  [tree prefix]
  (append-tree prefix tree))

(def concat gleam-ffi/st-concat)

(def to-string gleam-ffi/st-to-string)

(def byte-size gleam-ffi/st-byte-size)

(defn join
  "Joins the given trees into a new tree separated with the given string."
  [trees sep]
  (-> trees (list/intersperse (from-string sep)) concat))

(def lowercase gleam-ffi/st-lowercase)

(def uppercase gleam-ffi/st-uppercase)

(def do-to-graphemes gleam-ffi/st-to-graphemes)

(defn reverse
  "Converts a `StringTree` to a new one with the contents reversed."
  [tree]
  (-> tree to-string do-to-graphemes list/reverse from-strings))

(def erl-split gleam-ffi/st-split)

(defn split
  "Splits a `StringTree` on a given pattern into a list of trees."
  [tree pattern]
  (erl-split tree pattern (->All)))

(def replace gleam-ffi/st-replace)

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
  [tree]
  (= (from-string "") tree))

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'append [:=> [:cat [:or ] :string] [:or ]]
   'append-tree [:=> [:cat [:or ] [:or ]] [:or ]]
   'byte-size [:=> [:cat [:or ]] :int]
   'concat [:=> [:cat [:sequential [:or ]]] [:or ]]
   'from-string [:=> [:cat :string] [:or ]]
   'from-strings [:=> [:cat [:sequential :string]] [:or ]]
   'is-empty [:=> [:cat [:or ]] :boolean]
   'is-equal [:=> [:cat [:or ] [:or ]] :boolean]
   'join [:=> [:cat [:sequential [:or ]] :string] [:or ]]
   'lowercase [:=> [:cat [:or ]] [:or ]]
   'new* [:=> [:cat] [:or ]]
   'prepend [:=> [:cat [:or ] :string] [:or ]]
   'prepend-tree [:=> [:cat [:or ] [:or ]] [:or ]]
   'replace [:=> [:cat [:or ] :string :string] [:or ]]
   'reverse [:=> [:cat [:or ]] [:or ]]
   'split [:=> [:cat [:or ] :string] [:sequential [:or ]]]
   'to-string [:=> [:cat [:or ]] :string]
   'uppercase [:=> [:cat [:or ]] [:or ]]})
