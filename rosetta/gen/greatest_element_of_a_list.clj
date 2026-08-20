(ns greatest-element-of-a-list
  (:require
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> (list 1 99 136 4 3 22 111) (list/max' int/compare) (p/echo)))

(defn -main [& _]
  (main))
