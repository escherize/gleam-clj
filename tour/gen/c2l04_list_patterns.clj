(ns c2l04-list-patterns
  (:require
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [x (list/repeated (int/random 5) (int/random 3))]
    (p/echo x "c2l04_list_patterns.gleam:6")
    (let [result (cond
                   (empty? x) "Empty list"
                   (and (= (count x) 1) (= (first x) 1)) "List of just 1"
                   (and (seq x) (= (first x) 4)) "List starting with 4"
                   (= (count x) 2) "List of 2 elements"
                   :else "Some other list")]
      (p/echo result "c2l04_list_patterns.gleam:15"))))

(defn -main [& _]
  (main))
