(ns c1l04-function-captures
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- add [a b]
  (+' a b))

(defn main []
  (let [add-one-v1 (fn [x] (add 1 x))
        add-one-v2 (fn [-capture] (add 1 -capture))]
    (p/echo (add-one-v1 10) "c1l04_function_captures.gleam:6")
    (p/echo (add-one-v2 10) "c1l04_function_captures.gleam:7")))

(defn -main [& _]
  (main))
