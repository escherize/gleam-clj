(ns dot-product
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- dot-product-loop [u v sum]
  (cond
    (and (empty? u) (seq v)) (p/->Error "Inputs must have the same length.")
    (and (seq u) (empty? v)) (p/->Error "Inputs must have the same length.")
    (and (seq u) (seq v)) (let [x (first u) rest-u (rest u) y (first v) rest-v (rest v)]
                            (recur rest-u rest-v (+' (*' x y) sum)))
    (and (empty? u) (empty? v)) (p/->Ok sum)))

(defn dot-product [u v]
  (dot-product-loop u v 0))

(defn main []
  (let [_ (p/echo (dot-product (list 1 3 -5) (list 4 -2 -1)) "dot_product.gleam:2")
        _ (p/echo (dot-product (list 1 3 -5) (list 4 -2)) "dot_product.gleam:3")]
    nil))

(defn -main [& _]
  (main))
