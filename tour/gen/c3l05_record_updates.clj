(ns c3l05-record-updates
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type SchoolPerson
(defrecord Teacher [name subject floor room])

(defn main []
  (let [teacher1 (->Teacher "Mr Dodd" "ICT" 2 2)
        teacher2 (assoc teacher1 :subject "PE" :room 6)]
    (p/echo teacher1 "c3l05_record_updates.gleam:11")
    (p/echo teacher2 "c3l05_record_updates.gleam:12")))

(defn -main [& _]
  (main))
