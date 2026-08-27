(ns gleam.order
  (:refer-clojure :exclude [compare reverse]))

;; type Order
(defprotocol IOrder)
(defrecord Lt [] IOrder)
(defn Lt? "True if `v` is a Lt value." [v] (instance? Lt v))
(defrecord Eq [] IOrder)
(defn Eq? "True if `v` is a Eq value." [v] (instance? Eq v))
(defrecord Gt [] IOrder)
(defn Gt? "True if `v` is a Gt value." [v] (instance? Gt v))
(defn Order? "True if `v` is any Order value." [v] (instance? gleam.order.IOrder v))
(defn Order-schema
  "Malli schema for Order."
  []
  [:or
   [:fn Lt?]
   [:fn Eq?]
   [:fn Gt?]])

(defn negate
  "negate(order: Order) -> Order

   Inverts an order, so less-than becomes greater-than and greater-than
   becomes less-than.

   ## Examples

   ```gleam
   assert order.negate(Lt) == Gt
   ```

   ```gleam
   assert order.negate(Eq) == Eq
   ```

   ```gleam
   assert order.negate(Gt) == Lt
   ```"
  {:malli/schema [:=> [:cat (Order-schema)] (Order-schema)]
   :gleam/src "stdlib-src/src/gleam/order.gleam:32"}
  [order]
  (cond
    (instance? Lt order) (->Gt)
    (instance? Eq order) (->Eq)
    (instance? Gt order) (->Lt)))

(defn to-int
  "to_int(order: Order) -> Int

   Produces a numeric representation of the order.

   ## Examples

   ```gleam
   assert order.to_int(Lt) == -1
   ```

   ```gleam
   assert order.to_int(Eq) == 0
   ```

   ```gleam
   assert order.to_int(Gt) == 1
   ```"
  {:malli/schema [:=> [:cat (Order-schema)] :int]
   :gleam/src "stdlib-src/src/gleam/order.gleam:56"}
  [order]
  (cond
    (instance? Lt order) -1
    (instance? Eq order) 0
    (instance? Gt order) 1))

(defn compare
  "compare(a: Order, with b: Order) -> Order

   Compares two `Order` values to one another, producing a new `Order`.

   ## Examples

   ```gleam
   assert order.compare(Eq, with: Lt) == Gt
   ```"
  {:malli/schema [:=> [:cat (Order-schema) (Order-schema)] (Order-schema)]
   :gleam/src "stdlib-src/src/gleam/order.gleam:72"}
  [a b]
  (cond
    (= a b) (->Eq)
    (or (instance? Lt a) (and (instance? Eq a) (instance? Gt b))) (->Lt)
    :else (->Gt)))

(defn reverse
  "reverse(orderer: fn(a, a) -> Order) -> fn(a, a) -> Order

   Inverts an ordering function, so less-than becomes greater-than and greater-than
   becomes less-than.

   ## Examples

   ```gleam
   import gleam/int
   import gleam/list

   assert list.sort([1, 5, 4], by: order.reverse(int.compare)) == [5, 4, 1]
   ```"
  {:malli/schema [:=> [:cat [:=> [:cat :any :any] (Order-schema)]]
                      [:=> [:cat :any :any] (Order-schema)]]
   :gleam/src "stdlib-src/src/gleam/order.gleam:92"}
  [orderer]
  (fn [a b] (orderer b a)))

(defn break-tie
  "break_tie(in order: Order, with other: Order) -> Order

   Return a fallback `Order` in case the first argument is `Eq`.

   ## Examples

   ```gleam
   import gleam/int

   assert order.break_tie(in: int.compare(1, 1), with: Lt) == Lt
   ```

   ```gleam
   import gleam/int

   assert order.break_tie(in: int.compare(1, 0), with: Eq) == Gt
   ```"
  {:malli/schema [:=> [:cat (Order-schema) (Order-schema)] (Order-schema)]
   :gleam/src "stdlib-src/src/gleam/order.gleam:112"}
  [order other]
  (if (or (instance? Lt order) (instance? Gt order)) order other))

(defn lazy-break-tie
  "lazy_break_tie(in order: Order, with comparison: fn() -> Order) -> Order

   Invokes a fallback function returning an `Order` in case the first argument
   is `Eq`.

   This can be useful when the fallback comparison might be expensive and it
   needs to be delayed until strictly necessary.

   ## Examples

   ```gleam
   import gleam/int

   assert order.lazy_break_tie(in: int.compare(1, 1), with: fn() { Lt }) == Lt
   ```

   ```gleam
   import gleam/int

   assert order.lazy_break_tie(in: int.compare(1, 0), with: fn() { Eq }) == Gt
   ```"
  {:malli/schema [:=> [:cat (Order-schema) [:=> [:cat] (Order-schema)]]
                      (Order-schema)]
   :gleam/src "stdlib-src/src/gleam/order.gleam:139"}
  [order comparison]
  (if (or (instance? Lt order) (instance? Gt order)) order (comparison)))
