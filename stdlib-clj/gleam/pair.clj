(ns gleam.pair
  (:refer-clojure :exclude [second]))

(defn first'
  "Returns the first element in a pair.
  
  ## Examples
  
  ```gleam
  assert pair.first(#(1, 2)) == 1
  ```"
  {:malli/schema [:=> [:cat [:tuple :any :any]] :any]}
  [pair]
  (let [[a _] pair]
    a))

(defn second
  "Returns the second element in a pair.
  
  ## Examples
  
  ```gleam
  assert pair.second(#(1, 2)) == 2
  ```"
  {:malli/schema [:=> [:cat [:tuple :any :any]] :any]}
  [pair]
  (let [[_ a] pair]
    a))

(defn swap
  "Returns a new pair with the elements swapped.
  
  ## Examples
  
  ```gleam
  assert pair.swap(#(1, 2)) == #(2, 1)
  ```"
  {:malli/schema [:=> [:cat [:tuple :any :any]] [:tuple :any :any]]}
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
  {:malli/schema [:=> [:cat [:tuple :any :any] [:=> [:cat :any] :any]] [:tuple :any :any]]}
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
  {:malli/schema [:=> [:cat [:tuple :any :any] [:=> [:cat :any] :any]] [:tuple :any :any]]}
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
  {:malli/schema [:=> [:cat :any :any] [:tuple :any :any]]}
  [first' second]
  [first' second])
