(ns coin-change
  (:require
   [gleam.dict :as dict]
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare min-coins step main)

(defn min-coins
  "Fewest coins summing to `amount`. Error(Nil) if unreachable."
  [coins amount]
  (cond
    (= amount 0) (p/->Ok 0)
    (< amount 0) (p/->Error nil)
    :else (-> (int/fold-range 1
                              (+ amount 1)
                              (dict/from-list (list [0 0]))
                              (fn [table a] (step coins table a)))
              (dict/lookup amount))))

(defn- step
  [coins table a]
  (p/echo ["step" "a: " a ", table: " table] "coin_change.gleam:20")
  (let [best (-> coins
                 (list/filter-map (fn [c] (dict/lookup table (- a c))))
                 (list/reduce1 min))]
    (if (instance? Ok best)
      (let [b (:value best)]
        (dict/insert table a (+ b 1)))
      table)))

(defn main
  []
  (let [v (min-coins (list 1 5 10) 0)]
    (when-not (and (instance? Ok v) (= (:value v) 0))
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (min-coins (list 1 5 10) 10)]
    (when-not (and (instance? Ok v) (= (:value v) 1))
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (min-coins (list 1 5 10) 15)]
    (when-not (and (instance? Ok v) (= (:value v) 2))
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (min-coins (list 1 5 10) 13)]
    (when-not (and (instance? Ok v) (= (:value v) 4))
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (min-coins (list 1 3 4) 6)]
    (when-not (and (instance? Ok v) (= (:value v) 2))
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (min-coins (list 5 10) 3)]
    (when-not (and (instance? gleam.prelude.Error v) (nil? (:value v)))
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (min-coins (list) 7)]
    (when-not (and (instance? gleam.prelude.Error v) (nil? (:value v)))
      (throw (ex-info "let assert failed" {:value v})))))

(defn -main [& _]
  (main))
