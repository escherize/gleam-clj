(ns c3l03-record-accessors
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type SchoolPerson
(defrecord Teacher [name subject])
(defrecord Student [name])

(defn main []
  (let [teacher (->Teacher "Mr Schofield" "Physics")
        student (->Student "Koushiar")]
    (p/echo (:name teacher) "c3l03_record_accessors.gleam:10")
    (p/echo (:name student) "c3l03_record_accessors.gleam:11")))

(defn -main [& _]
  (main))
