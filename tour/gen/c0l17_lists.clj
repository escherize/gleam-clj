(ns c0l17-lists
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [ints (list 1 2 3)]
    (p/echo ints "c0l17_lists.gleam:4")
    (p/echo (list* -1 0 ints) "c0l17_lists.gleam:7")
    (p/echo ints "c0l17_lists.gleam:13")))

(defn -main [& _]
  (main))
