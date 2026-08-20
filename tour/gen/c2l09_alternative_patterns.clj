(ns c2l09-alternative-patterns
  (:require
   [gleam.int :as int]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [number (int/random 10)]
    (p/echo number "c2l09_alternative_patterns.gleam:5")
    (let [result (cond
                   (or (= number 2) (= number 4) (= number 6) (= number 8)) "This is an even number"
                   (or (= number 1) (= number 3) (= number 5) (= number 7)) "This is an odd number"
                   :else "I'm not sure")]
      (p/echo result "c2l09_alternative_patterns.gleam:12"))))

(defn -main [& _]
  (main))
