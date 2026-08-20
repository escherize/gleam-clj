(ns c1l05-generic-functions
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- twice [argument my-function]
  (my-function (my-function argument)))

(defn main []
  (let [add-one (fn [x] (+' x 1))
        exclaim (fn [x] (str x "!"))]
    (p/echo (twice 10 add-one) "c1l05_generic_functions.gleam:9")
    (p/echo (twice "Hello" exclaim) "c1l05_generic_functions.gleam:12")))

(defn -main [& _]
  (main))
