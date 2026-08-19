(ns filter
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> (list 1 2 3 4 5 6 7 8 9 10)
      (list/keep-if (fn [x] (= (rem x 2) 0)))
      p/echo))

(defn -main [& _]
  (main))
