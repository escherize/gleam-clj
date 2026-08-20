(ns c1l02-higher-order-functions
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- add-one [argument]
  (+' argument 1))

(defn- twice [argument passed-function]
  (passed-function (passed-function argument)))

(defn main []
  (p/echo (twice 1 add-one) "c1l02_higher_order_functions.gleam:3")
  (let [my-function add-one]
    (p/echo (my-function 100) "c1l02_higher_order_functions.gleam:7")))

(defn -main [& _]
  (main))
