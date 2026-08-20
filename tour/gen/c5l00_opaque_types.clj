(ns c5l00-opaque-types
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
    (p/echo (to-int positive) "c5l00_opaque_types.gleam:6")
    (p/echo (to-int zero) "c5l00_opaque_types.gleam:7")
    (p/echo (to-int negative) "c5l00_opaque_types.gleam:8")))

(defn -main [& _]
  (main))
