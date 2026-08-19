(ns gleam.dict
  "Shims for gleam/dict. Gleam Dict is a Clojure persistent map.
  Gleam names that collide with clojure.core are renamed (codegen rename
  table): get -> lookup."
  (:require [gleam.prelude :as p]))

(defn from-list
  "Pairs arrive as 2-tuples, i.e. vectors."
  [pairs]
  (into {} pairs))

(defn lookup
  "Shim for gleam/dict.get: Ok(value) or Error(Nil)."
  [d k]
  (if-let [e (find d k)]
    (p/->Ok (val e))
    (p/->Error nil)))

(defn insert [d k v]
  (assoc d k v))

(defn new [] {})

(defn has-key [d k]
  (contains? d k))
