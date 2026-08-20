(ns array-length
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> (list "apple" "orange") list/length int/to-string io/println))

(defn -main [& _]
  (main))
