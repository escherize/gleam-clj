(ns water-collected-between-towers
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn water-area [towers]
  (-> towers
      (list/scan 0 max)
      (list/map2 (-> towers list/reversed (list/scan 0 max) list/reversed)
                 min)
      (list/map2 towers int/subtract)
      int/sum))

(defn main []
  (let [cases (list (list 1 5 3 7 2) (list 5 3 7 2 6 4 5 9 1 2) (list 2 6 3 5 2 8 1 4 2 2 5 3 5 7 4 1) (list 5 5 5 5) (list 5 6 7 8) (list 8 7 7 6) (list 6 7 10 7 6))]
    (p/with-use [[towers] (list/each cases)]
      (-> towers water-area int/to-string io/print-line))))

(defn -main [& _]
  (main))
