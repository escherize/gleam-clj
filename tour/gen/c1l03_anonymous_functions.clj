(ns c1l03-anonymous-functions
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- twice [argument my-function]
  (my-function (my-function argument)))

(defn main []
  (let [add-one (fn [a] (+' a 1))]
    (p/echo (twice 1 add-one) "c1l03_anonymous_functions.gleam:4")
    (p/echo (twice 1 (fn [a] (*' a 2))) "c1l03_anonymous_functions.gleam:7")
    (let [secret-number 42
          secret (fn [] secret-number)]
      (p/echo (secret) "c1l03_anonymous_functions.gleam:12"))))

(defn -main [& _]
  (main))
