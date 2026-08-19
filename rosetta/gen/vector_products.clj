(ns vector-products
  (:require
   [gleam.float :as float]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Vector3
(defrecord Vector3 [f0 f1 f2])

(declare main dot-product cross-product scalar-triple-product vector-triple-product to-string)

(defn main []
  (let [a (->Vector3 3.0 4.0 5.0)
        b (->Vector3 4.0 3.0 5.0)
        c (->Vector3 -5.0 -12.0 -13.0)]
    (io/print-line (str "dot_product(a, b) = " (-> (dot-product a b) float/to-string)))
    (io/print-line (str "cross_product(a, b) = " (-> (cross-product a b) to-string)))
    (io/print-line (str "scalar_triple_product(a, b) = " (-> (scalar-triple-product a b c) float/to-string)))
    (io/print-line (str "vector_triple_product(a, b) = " (-> (vector-triple-product a b c) to-string)))))

(defn dot-product [u v]
  (let [{a :f0 b :f1 c :f2} u
        {x :f0 y :f1 z :f2} v]
    (+ (+ (* a x) (* b y)) (* c z))))

(defn cross-product [u v]
  (let [{a :f0 b :f1 c :f2} u
        {x :f0 y :f1 z :f2} v]
    (->Vector3 (- (* b z) (* c y)) (- (* c x) (* a z)) (- (* a y) (* b x)))))

(defn scalar-triple-product [u v w]
  (dot-product u (cross-product v w)))

(defn vector-triple-product [u v w]
  (cross-product u (cross-product v w)))

(defn to-string [v]
  (let [{a :f0 b :f1 c :f2} v]
    (str (str (str (str (str (str "(" (float/to-string a)) ", ") (float/to-string b)) ", ") (float/to-string c)) ")")))

(defn -main [& _]
  (main))
