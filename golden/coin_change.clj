(ns coin-change
  "Golden file: hand-written target output for gleam-src/coin_change.gleam.
  The Clojure backend should emit code semantically identical to this."
  (:require [gleam.dict :as dict]
            [gleam.int :as int]
            [gleam.list :as list]
            [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- step [coins table a]
  (let [best (-> coins
                 (list/filter-map (fn [c] (dict/lookup table (- a c))))
                 (list/reduce1 min))]
    (if (instance? Ok best)
      (let [b (:value best)]
        (dict/insert table a (+ b 1)))
      table)))

(defn min-coins [coins amount]
  (cond
    (= amount 0) (p/->Ok 0)
    (< amount 0) (p/->Error nil)
    :else (-> (int/fold-range 1 (+ amount 1)
                              (dict/from-list (list [0 0]))
                              (fn [table a] (step coins table a)))
              (dict/lookup amount))))

(defn- let-assert [expected actual]
  (when-not (= expected actual)
    (throw (ex-info "let assert failed"
                    {:expected expected :actual actual}))))

(defn main []
  (let-assert (p/->Ok 0) (min-coins (list 1 5 10) 0))
  (let-assert (p/->Ok 1) (min-coins (list 1 5 10) 10))
  (let-assert (p/->Ok 2) (min-coins (list 1 5 10) 15))
  (let-assert (p/->Ok 4) (min-coins (list 1 5 10) 13))
  ;; greedy trap: greedy picks 4+1+1 = 3 coins, optimal is 3+3 = 2
  (let-assert (p/->Ok 2) (min-coins (list 1 3 4) 6))
  (let-assert (p/->Error nil) (min-coins (list 5 10) 3))
  (let-assert (p/->Error nil) (min-coins (list) 7)))

(defn -main [& _]
  (main)
  (println "coin-change: all let asserts passed"))
