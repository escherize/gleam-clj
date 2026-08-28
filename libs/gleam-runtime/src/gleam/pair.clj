(ns gleam.pair
  (:refer-clojure :exclude [second]))

(defn first'
  "first(pair: #(a, b)) -> a

   Returns the first element in a pair.

   ## Examples

   ```gleam
   assert pair.first(#(1, 2)) == 1
   ```"
  {:malli/schema [:=> [:cat [:tuple :any :any]] :any]
   :gleam/src "stdlib-src/src/gleam/pair.gleam:9"}
  [pair]
  (let [[a _] pair]
    a))

(defn second
  "second(pair: #(a, b)) -> b

   Returns the second element in a pair.

   ## Examples

   ```gleam
   assert pair.second(#(1, 2)) == 2
   ```"
  {:malli/schema [:=> [:cat [:tuple :any :any]] :any]
   :gleam/src "stdlib-src/src/gleam/pair.gleam:22"}
  [pair]
  (let [[_ a] pair]
    a))

(defn swap
  "swap(pair: #(a, b)) -> #(b, a)

   Returns a new pair with the elements swapped.

   ## Examples

   ```gleam
   assert pair.swap(#(1, 2)) == #(2, 1)
   ```"
  {:malli/schema [:=> [:cat [:tuple :any :any]] [:tuple :any :any]]
   :gleam/src "stdlib-src/src/gleam/pair.gleam:35"}
  [pair]
  (let [[a b] pair]
    [b a]))

(defn map-first
  "map_first(of pair: #(a, b), with fun: fn(a) -> c) -> #(c, b)

   Returns a new pair with the first element having had `with` applied to
   it.

   ## Examples

   ```gleam
   assert #(1, 2) |> pair.map_first(fn(n) { n * 2 }) == #(2, 2)
   ```"
  {:malli/schema [:=> [:cat [:tuple :any :any] [:=> [:cat :any] :any]]
                      [:tuple :any :any]]
   :gleam/src "stdlib-src/src/gleam/pair.gleam:49"}
  [pair fun]
  (let [[a b] pair]
    [(fun a) b]))

(defn map-second
  "map_second(of pair: #(a, b), with fun: fn(b) -> c) -> #(a, c)

   Returns a new pair with the second element having had `with` applied to
   it.

   ## Examples

   ```gleam
   assert #(1, 2) |> pair.map_second(fn(n) { n * 2 }) == #(1, 4)
   ```"
  {:malli/schema [:=> [:cat [:tuple :any :any] [:=> [:cat :any] :any]]
                      [:tuple :any :any]]
   :gleam/src "stdlib-src/src/gleam/pair.gleam:63"}
  [pair fun]
  (let [[a b] pair]
    [a (fun b)]))

(defn new*
  "new(first: a, second: b) -> #(a, b)

   Returns a new pair with the given elements. This can also be done using the dedicated
   syntax instead: `new(1, 2) == #(1, 2)`.

   ## Examples

   ```gleam
   assert pair.new(1, 2) == #(1, 2)
   ```"
  {:malli/schema [:=> [:cat :any :any] [:tuple :any :any]]
   :gleam/src "stdlib-src/src/gleam/pair.gleam:77"}
  [first' second]
  [first' second])
