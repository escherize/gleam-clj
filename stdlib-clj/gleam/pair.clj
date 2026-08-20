(ns gleam.pair
  (:refer-clojure :exclude [second])
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn first'
  "Returns the first element in a pair.
  
  ## Examples
  
  ```gleam
  assert pair.first(#(1, 2)) == 1
  ```"
  [pair]
  (let [[a _] pair]
    a))

(defn second
  "Returns the second element in a pair.
  
  ## Examples
  
  ```gleam
  assert pair.second(#(1, 2)) == 2
  ```"
  [pair]
  (let [[_ a] pair]
    a))

(defn swap
  "Returns a new pair with the elements swapped.
  
  ## Examples
  
  ```gleam
  assert pair.swap(#(1, 2)) == #(2, 1)
  ```"
  [pair]
  (let [[a b] pair]
    [b a]))

(defn map-first
  "Returns a new pair with the first element having had `with` applied to
  it.
  
  ## Examples
  
  ```gleam
  assert #(1, 2) |> pair.map_first(fn(n) { n * 2 }) == #(2, 2)
  ```"
  [pair fun]
  (let [[a b] pair]
    [(fun a) b]))

(defn map-second
  "Returns a new pair with the second element having had `with` applied to
  it.
  
  ## Examples
  
  ```gleam
  assert #(1, 2) |> pair.map_second(fn(n) { n * 2 }) == #(1, 4)
  ```"
  [pair fun]
  (let [[a b] pair]
    [a (fun b)]))

(defn new*
  "Returns a new pair with the given elements. This can also be done using the dedicated
  syntax instead: `new(1, 2) == #(1, 2)`.
  
  ## Examples
  
  ```gleam
  assert pair.new(1, 2) == #(1, 2)
  ```"
  [first' second]
  [first' second])

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'first' [:=> [:cat [:tuple :any :any]] :any]
   'map-first [:=> [:cat [:tuple :any :any] [:=> [:cat :any] :any]] [:tuple :any :any]]
   'map-second [:=> [:cat [:tuple :any :any] [:=> [:cat :any] :any]] [:tuple :any :any]]
   'new* [:=> [:cat :any :any] [:tuple :any :any]]
   'second [:=> [:cat [:tuple :any :any]] :any]
   'swap [:=> [:cat [:tuple :any :any]] [:tuple :any :any]]})
