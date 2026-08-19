(ns sorting-algorithms-quicksort
  (:require
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.order :as order]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare quick-sort main)

(defn quick-sort [xs compare]
  (if (empty? xs)
    (list)
    (let [x (first xs) xs (rest xs)]
      (let [[left right] (list/separate xs
                                        (fn [y]
                                          (= (compare y x) (order/->Lt))))
            ql (quick-sort left compare)
            qr (quick-sort right compare)]
        (list/append (list/append ql (list x)) qr)))))

(defn main []
  (-> (list 31 4 1 5 9 2 6 5 3 5 8) (quick-sort int/cmp) p/echo))

(defn -main [& _]
  (main))
