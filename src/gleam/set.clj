(ns gleam.set
  "Shims for gleam/set. Gleam Set is a Clojure persistent set."
  (:require [clojure.set :as cset]))

(defn new [] #{})

(defn insert [s x] (conj s x))

(defn delete [s x] (disj s x))

(defn contains [s x] (contains? s x))

(defn from-list [lst] (set lst))

(defn to-list
  "Order is unspecified in Gleam; sort when comparable for determinism."
  [s]
  (try (sort s)
       (catch ClassCastException _ (seq s))))

(defn union [a b] (cset/union a b))
(defn intersection [a b] (cset/intersection a b))
(defn difference [a b] (cset/difference a b))

(defn symmetric-difference [a b]
  (cset/union (cset/difference a b) (cset/difference b a)))

(defn is-subset [a b] (cset/subset? a b))

(defn size [s] (count s))
