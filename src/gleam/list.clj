(ns gleam.list
  "Shims for gleam/list. Gleam lists are represented as eager Clojure seqs.
  Gleam names that collide with clojure.core are renamed (codegen rename
  table): reduce -> reduce1, map -> map-over."
  (:require [gleam.prelude :as p])
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
