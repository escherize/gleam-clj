(ns sort-an-integer-array
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> (list 2 1 5 3 4)
      (list/sort-with int/cmp)
      (list/map-over int/to-string)
      (string/join " ")
      io/print-line))

(defn -main [& _]
  (main))
