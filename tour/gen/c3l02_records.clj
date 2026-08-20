(ns c3l02-records
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Person
(defrecord Person [name age needs-glasses])

(defn main []
  (let [amy (->Person "Amy" 26 true)
        jared (->Person "Jared" 31 true)
        tom (->Person "Tom" 28 false)
        friends (list amy jared tom)]
    (p/echo friends "c3l02_records.gleam:11")))

(defn -main [& _]
  (main))
