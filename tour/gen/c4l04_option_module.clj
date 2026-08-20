(ns c4l04-option-module
  (:require
   [gleam.option :as option]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Person
(defrecord Person [name pet])

(defn main []
  (let [person-with-pet (->Person "Al" (option/->Some "Nubi"))
        person-without-pet (->Person "Maria" (option/->None))]
    (p/echo person-with-pet "c4l04_option_module.gleam:11")
    (p/echo person-without-pet "c4l04_option_module.gleam:12")))

(defn -main [& _]
  (main))
