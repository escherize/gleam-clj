(ns array-concatenation
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> (list/append (list 1 2 3) (list 4 5 6))
      (list/map int/to-string)
      (string/join " ")
      io/println))

(defn -main [& _]
  (main))
