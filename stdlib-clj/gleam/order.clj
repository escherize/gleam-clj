(ns gleam.order
  (:refer-clojure :exclude [compare reverse]))

;; type Order
(defrecord Lt [])
(defn Lt? [v] (instance? Lt v))
(defrecord Eq [])
(defn Eq? [v] (instance? Eq v))
(defrecord Gt [])
(defn Gt? [v] (instance? Gt v))

(defn negate
  "Inverts an order, so less-than becomes greater-than and greater-than
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
  {:malli/schema [:=> [:cat [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]
                      [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]}
  [order]
  (cond
    (instance? Lt order) (->Gt)
    (instance? Eq order) (->Eq)
    (instance? Gt order) (->Lt)))

(defn to-int
  "Produces a numeric representation of the order.
  
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
  {:malli/schema [:=> [:cat [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]] :int]}
  [order]
  (cond
    (instance? Lt order) -1
    (instance? Eq order) 0
    (instance? Gt order) 1))

(defn compare
  "Compares two `Order` values to one another, producing a new `Order`.
  
  ## Examples
  
  ```gleam
  assert order.compare(Eq, with: Lt) == Gt
  ```"
  {:malli/schema [:=> [:cat [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]] [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]
                      [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]}
  [a b]
  (cond
    (= a b) (->Eq)
    (or (instance? Lt a) (and (instance? Eq a) (instance? Gt b))) (->Lt)
    :else (->Gt)))

(defn reverse
  "Inverts an ordering function, so less-than becomes greater-than and greater-than
  becomes less-than.
  
  ## Examples
  
  ```gleam
  import gleam/int
  import gleam/list
  
  assert list.sort([1, 5, 4], by: order.reverse(int.compare)) == [5, 4, 1]
  ```"
  {:malli/schema [:=> [:cat [:=> [:cat :any :any] [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]]
                      [:=> [:cat :any :any] [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]]}
  [orderer]
  (fn [a b] (orderer b a)))

(defn break-tie
  "Return a fallback `Order` in case the first argument is `Eq`.
  
  ## Examples
  
  ```gleam
  import gleam/int
  
  assert order.break_tie(in: int.compare(1, 1), with: Lt) == Lt
  ```
  
  ```gleam
  import gleam/int
  
  assert order.break_tie(in: int.compare(1, 0), with: Eq) == Gt
  ```"
  {:malli/schema [:=> [:cat [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]] [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]
                      [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]}
  [order other]
  (if (or (instance? Lt order) (instance? Gt order)) order other))

(defn lazy-break-tie
  "Invokes a fallback function returning an `Order` in case the first argument
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
  {:malli/schema [:=> [:cat [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]] [:=> [:cat] [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]]
                      [:or [:fn Lt?] [:fn Eq?] [:fn Gt?]]]}
  [order comparison]
  (if (or (instance? Lt order) (instance? Gt order)) order (comparison)))
