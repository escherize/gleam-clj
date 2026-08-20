(ns c1l08-label-shorthand-syntax
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- calculate-total-cost [quantity price discount]
  (let [subtotal (* quantity price)
        discount (* subtotal discount)]
    (- subtotal discount)))

(defn main []
  (let [quantity 5.0
        unit-price 10.0
        discount 0.2]
    (p/echo (calculate-total-cost quantity unit-price discount) "c1l08_label_shorthand_syntax.gleam:7")
    (p/echo (calculate-total-cost quantity unit-price discount) "c1l08_label_shorthand_syntax.gleam:14")))

(defn -main [& _]
  (main))
