(ns c3l08-results
  (:require
   [gleam.int :as int]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type PurchaseError
(defrecord NotEnoughMoney [required])
(defrecord NotLuckyEnough [])

(defn- buy-pastry [money]
  (let [subject (>= money 5)]
    (if subject
      (let [subject (= (int/random 4) 0)]
        (if subject (p/->Error (->NotLuckyEnough)) (p/->Ok (-' money 5))))
      (p/->Error (->NotEnoughMoney 5)))))

(defn main []
  (let [_ (p/echo (buy-pastry 10) "c3l08_results.gleam:4")
        _ (p/echo (buy-pastry 8) "c3l08_results.gleam:5")
        _ (p/echo (buy-pastry 5) "c3l08_results.gleam:6")
        _ (p/echo (buy-pastry 3) "c3l08_results.gleam:7")]
    nil))

(defn -main [& _]
  (main))
