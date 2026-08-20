(ns c2l01-case-expressions
  (:require
   [gleam.int :as int]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [x (int/random 5)]
    (p/echo x "c2l01_case_expressions.gleam:5")
    (let [result (cond
                   (= x 0) "Zero"
                   (= x 1) "One"
                   :else "Other")]
      (p/echo result "c2l01_case_expressions.gleam:15"))))

(defn -main [& _]
  (main))
