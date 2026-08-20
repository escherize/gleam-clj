(ns abstract-type
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type PositiveInt
(defrecord PositiveInt [inner])

(defn to-int [i]
  (:inner i))

(defn new* [i]
  (let [subject (>= i 0)]
    (if subject (->PositiveInt i) (->PositiveInt 0))))

(defn main []
  (let [positive (new* 1)
        zero (new* 0)
        negative (new* -1)]
    (p/echo (to-int positive) "abstract_type.gleam:6")
    (p/echo (to-int zero) "abstract_type.gleam:7")
    (p/echo (to-int negative) "abstract_type.gleam:8")))

(defn -main [& _]
  (main))
