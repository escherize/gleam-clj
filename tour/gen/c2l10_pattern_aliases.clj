(ns c2l10-pattern-aliases
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- get-first-non-empty [lists]
  (cond
    (and (seq lists) (seq (first lists))) (let [first' (first lists)]
                                            first')
    (seq lists) (let [rest' (rest lists)]
                  (recur rest'))
    (empty? lists) (list)))

(defn main []
  (p/echo (get-first-non-empty (list (list) (list 1 2 3) (list 4 5))) "c2l10_pattern_aliases.gleam:2")
  (p/echo (get-first-non-empty (list (list 1 2) (list 3 4 5) (list))) "c2l10_pattern_aliases.gleam:3")
  (p/echo (get-first-non-empty (list (list) (list) (list))) "c2l10_pattern_aliases.gleam:4"))

(defn -main [& _]
  (main))
