(ns c3l00-tuples
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [triple [1 2.2 "three"]]
    (p/echo triple "c3l00_tuples.gleam:3")
    (let [[a _ _] triple]
      (p/echo a "c3l00_tuples.gleam:6")
      (p/echo (nth triple 1) "c3l00_tuples.gleam:7"))))

(defn -main [& _]
  (main))
