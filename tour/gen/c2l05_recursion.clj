(ns c2l05-recursion
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- step-towards-zero [x]
  (let [subject (>= x 0)]
    (if subject (-' x 1) (+' x 1))))

(defn factorial [x]
  (cond
    (= x 0) 1
    (= x 1) 1
    :else (*' x (factorial (step-towards-zero x)))))

(defn main []
  (p/echo (factorial 5) "c2l05_recursion.gleam:2")
  (p/echo (factorial 7) "c2l05_recursion.gleam:3"))

(defn -main [& _]
  (main))
