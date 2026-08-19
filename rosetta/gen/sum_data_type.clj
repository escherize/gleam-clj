(ns sum-data-type
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Tree
(defrecord Empty [])
(defrecord Leaf [value])
(defrecord Node [f0 f1])

(defn main []
  (let [t1 (->Node (->Leaf 1) (->Node (->Leaf 2) (->Leaf 3)))]
    nil))

(defn -main [& _]
  (main))
