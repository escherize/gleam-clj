(ns c0l06-floats
  (:require
   [gleam.float :as float]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (p/echo (+ 1.0 1.5) "c0l06_floats.gleam:5")
  (p/echo (- 5.0 1.5) "c0l06_floats.gleam:6")
  (p/echo (/ 5.0 2.5) "c0l06_floats.gleam:7")
  (p/echo (* 3.0 3.5) "c0l06_floats.gleam:8")
  (let [one 1.0]
    (p/echo (> 2.2 one) "c0l06_floats.gleam:12")
    (p/echo (< 2.2 one) "c0l06_floats.gleam:13")
    (p/echo (>= 2.2 one) "c0l06_floats.gleam:14")
    (p/echo (<= 2.2 one) "c0l06_floats.gleam:15")
    (p/echo (= 3.0 (* 1.5 2.0)) "c0l06_floats.gleam:18")
    (p/echo (= 2.1 (+ 1.2 1.0)) "c0l06_floats.gleam:19")
    (p/echo (/ 3.14 0.0) "c0l06_floats.gleam:22")
    (p/echo (float/max' 2.0 9.5) "c0l06_floats.gleam:25")
    (p/echo (float/ceiling 5.4) "c0l06_floats.gleam:26")))

(defn -main [& _]
  (main))
