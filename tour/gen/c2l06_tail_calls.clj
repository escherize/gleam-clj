(ns c2l06-tail-calls
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- factorial-loop [x accumulator]
  (cond
    (= x 0) accumulator
    (= x 1) accumulator
    :else (recur (-' x 1) (*' accumulator x))))

(defn factorial [x]
  (factorial-loop x 1))

(defn main []
  (p/echo (factorial 5) "c2l06_tail_calls.gleam:2")
  (p/echo (factorial 7) "c2l06_tail_calls.gleam:3"))

(defn -main [& _]
  (main))
