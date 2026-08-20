(ns increment-a-numerical-string
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.result :as result])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> "12349"
      int/parse
      (result/unwrap -1)
      (int/add 1)
      int/to-string
      io/println))

(defn -main [& _]
  (main))
