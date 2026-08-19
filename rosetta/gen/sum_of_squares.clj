(ns sum-of-squares
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> (list/fold (list 3 1 4 1 5 9) 0 (fn [acc e] (+' (*' e e) acc)))
      int/to-string
      io/print-line))

(defn -main [& _]
  (main))
