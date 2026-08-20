(ns c3l01-custom-types
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Season
(defrecord Spring [])
(defrecord Summer [])
(defrecord Autumn [])
(defrecord Winter [])

(defn- weather [season]
  (cond
    (instance? Spring season) "Mild"
    (instance? Summer season) "Hot"
    (instance? Autumn season) "Windy"
    (instance? Winter season) "Cold"))

(defn main []
  (p/echo (weather (->Spring)) "c3l01_custom_types.gleam:9")
  (p/echo (weather (->Autumn)) "c3l01_custom_types.gleam:10"))

(defn -main [& _]
  (main))
