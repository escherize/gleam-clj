(ns gleam.list
  "Shims for gleam/list. Gleam lists are represented as eager Clojure seqs.
  Gleam names that collide with clojure.core are renamed (codegen rename
  table): reduce -> reduce1, map -> map-over, filter -> keep-if,
  sort -> sort-with, max -> largest, first -> head, last -> final,
  count -> count-if."
  (:require [gleam.order :as order]
            [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn fold [lst initial fun]
  (reduce fun initial lst))

(defn map-over
  "Shim for gleam/list.map (eager)."
  [lst fun]
  (doall (map fun lst)))

(defn filter-map
  "Keep the unwrapped values of Ok results."
  [lst fun]
  (doall
   (for [x lst
         :let [r (fun x)]
         :when (instance? Ok r)]
     (:value r))))

(defn reduce1
  "Shim for gleam/list.reduce: fold without an initial value.
  Error(Nil) on empty list."
  [lst fun]
  (if (empty? lst)
    (p/->Error nil)
    (p/->Ok (reduce fun lst))))

(defn length [lst] (count lst))

(defn append [a b] (doall (concat a b)))

(defn prepend [lst x] (cons x lst))

(defn each [lst fun]
  (doseq [x lst] (fun x)))

(defn keep-if
  "Shim for gleam/list.filter."
  [lst pred]
  (doall (filter pred lst)))

(defn flat-map [lst fun]
  (doall (mapcat fun lst)))

(defn count-if
  "Shim for gleam/list.count: count elements satisfying the predicate."
  [lst pred]
  (count (filter pred lst)))

(defn sort-with
  "Shim for gleam/list.sort: comparator returns a gleam.order value."
  [lst cmp]
  (sort (fn [a b] (order/to-int (cmp a b))) lst))

(defn largest
  "Shim for gleam/list.max."
  [lst cmp]
  (if (empty? lst)
    (p/->Error nil)
    (p/->Ok (reduce (fn [a b] (if (instance? gleam.order.Lt (cmp a b)) b a)) lst))))

(defn head
  "Shim for gleam/list.first."
  [lst]
  (if (empty? lst) (p/->Error nil) (p/->Ok (first lst))))

(defn final
  "Shim for gleam/list.last."
  [lst]
  (if (empty? lst) (p/->Error nil) (p/->Ok (last lst))))

(defn chunk
  "gleam/list.chunk: split into runs by key function."
  [lst f]
  (doall (map doall (partition-by f lst))))

(defn sized-chunk [lst n]
  (doall (map doall (partition-all n lst))))

(defn separate
  "Shim for gleam/list.partition: tuple of (satisfying, not-satisfying)."
  [lst pred]
  [(doall (filter pred lst)) (doall (remove pred lst))])

(defn window-by-2
  "Adjacent pairs as tuples."
  [lst]
  (mapv vec (partition 2 1 lst)))
