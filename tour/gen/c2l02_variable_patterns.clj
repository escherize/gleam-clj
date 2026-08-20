(ns c2l02-variable-patterns
  (:require
   [gleam.int :as int]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [result (let [subject (int/random 5)]
                 (cond
                   (= subject 0) "Zero"
                   (= subject 1) "One"
                   :else (let [other subject]
                           (str "It is " (int/to-string other)))))]
    (p/echo result "c2l02_variable_patterns.gleam:12")))

(defn -main [& _]
  (main))
