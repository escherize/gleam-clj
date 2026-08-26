(ns coin-change
  (:require
   [gleam.dict :as dict]
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- step
  "step(coins: List(Int), table: Dict(Int, Int), a: Int) -> Dict(Int, Int)"
  {:gleam/src "coin_change.gleam:19"}
  [coins table a]
  (p/echo ["step" "a: " a ", table: " table] "coin_change.gleam:20")
  (let [best (-> coins
                 (list/filter-map (fn [c] (dict/get table (-' a c))))
                 (list/reduce int/min'))]
    (if (instance? Ok best)
      (let [b (:value best)]
        (dict/insert table a (+' b 1)))
      table)))

(defn min-coins
  "min_coins(coins: List(Int), amount: Int) -> Result(Int, Nil)

   Fewest coins summing to `amount`. Error(Nil) if unreachable."
  {:malli/schema [:=> [:cat [:sequential :int] :int]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "coin_change.gleam:6"}
  [coins amount]
  (cond
    (= amount 0)
    (p/->Ok 0)

    (< amount 0)
    (p/->Error nil)

    :else
    (-> (int/range 1
                   (+' amount 1)
                   (dict/from-list (list [0 0]))
                   (fn [table a] (step coins table a)))
        (dict/get amount))))

(defn main
  "main() -> Result(Int, Nil)"
  {:malli/schema [:=> [:cat] [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "coin_change.gleam:31"}
  []
  (p/let-assert (p/->Ok 0) (min-coins (list 1 5 10) 0))
  (p/let-assert (p/->Ok 1) (min-coins (list 1 5 10) 10))
  (p/let-assert (p/->Ok 2) (min-coins (list 1 5 10) 15))
  (p/let-assert (p/->Ok 4) (min-coins (list 1 5 10) 13))
  (p/let-assert (p/->Ok 2) (min-coins (list 1 3 4) 6))
  (p/let-assert (p/->Error nil) (min-coins (list 5 10) 3))
  (p/let-assert (p/->Error nil) (min-coins (list) 7)))

(defn -main [& _]
  (main))
