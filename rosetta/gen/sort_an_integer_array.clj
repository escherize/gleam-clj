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
      (list/sort int/compare)
      (list/map int/to-string)
      (string/join " ")
      io/println))

(defn -main [& _]
  (main))
