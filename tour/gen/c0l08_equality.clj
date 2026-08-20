(ns c0l08-equality
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (p/echo (= 100 (+' 50 50)) "c0l08_equality.gleam:2")
  (p/echo (not= 1.5 (* 0.1 10.0)) "c0l08_equality.gleam:3"))

(defn -main [& _]
  (main))
