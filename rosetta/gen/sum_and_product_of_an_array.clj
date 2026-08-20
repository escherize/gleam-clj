(ns sum-and-product-of-an-array
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [list' (list 1 2 3 4 5)
        sum (int/sum list')
        product (int/product list')]
    (io/println (str "sum = " (int/to-string sum)))
    (io/println (str "product = " (int/to-string product)))))

(defn -main [& _]
  (main))
