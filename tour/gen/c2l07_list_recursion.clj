(ns c2l07-list-recursion
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- sum-list [list' total]
  (if (seq list')
    (let [first' (first list') rest' (rest list')]
      (recur rest' (+' total first')))
    total))

(defn main []
  (let [sum (sum-list (list 18 56 35 85 91) 0)]
    (p/echo sum "c2l07_list_recursion.gleam:3")))

(defn -main [& _]
  (main))
