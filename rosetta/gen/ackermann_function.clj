(ns ackermann-function
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare ackermann main)

(defn ackermann [m n]
  (cond
    (= m 0) (let [n n]
              (+' n 1))
    (= n 0) (let [m m]
              (recur (-' m 1) 1))
    :else (let [m m n n]
            (recur (-' m 1) (ackermann m (-' n 1))))))

(defn main []
  (p/echo (ackermann 0 0) "ackermann_function.gleam:10")
  (p/echo (ackermann 0 4) "ackermann_function.gleam:11")
  (p/echo (ackermann 1 0) "ackermann_function.gleam:12")
  (p/echo (ackermann 1 1) "ackermann_function.gleam:13")
  (p/echo (ackermann 2 1) "ackermann_function.gleam:14")
  (p/echo (ackermann 2 2) "ackermann_function.gleam:15")
  (p/echo (ackermann 3 1) "ackermann_function.gleam:16")
  (p/echo (ackermann 3 3) "ackermann_function.gleam:17")
  (p/echo (ackermann 4 0) "ackermann_function.gleam:18")
  (p/echo (ackermann 4 1) "ackermann_function.gleam:19"))

(defn -main [& _]
  (main))
