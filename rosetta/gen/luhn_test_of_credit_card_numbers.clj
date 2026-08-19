(ns luhn-test-of-credit-card-numbers
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare main is-luhn is-luhn-loop)

(defn main []
  (let [_ (p/echo (is-luhn 49927398716) "luhn_test_of_credit_card_numbers.gleam:2")
        _ (p/echo (is-luhn 49927398717) "luhn_test_of_credit_card_numbers.gleam:3")
        _ (p/echo (is-luhn 1234567812345678) "luhn_test_of_credit_card_numbers.gleam:4")
        _ (p/echo (is-luhn 1234567812345670) "luhn_test_of_credit_card_numbers.gleam:5")]
    nil))

(defn is-luhn [n]
  (is-luhn-loop n 0 true))

(defn- is-luhn-loop [n sum even-digit]
  (cond
    (= n 0) (= (rem sum 10) 0)
    even-digit (recur (quot n 10) (+' sum (rem n 10)) false)
    (not even-digit) (let [x (rem n 10)
                           y (let [subject (< x 5)]
                               (if subject (+' x x) (-' (+' x x) 9)))]
                       (recur (quot n 10) (+' sum y) true))))

(defn -main [& _]
  (main))
