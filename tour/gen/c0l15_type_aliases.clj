(ns c0l15-type-aliases
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [one 1
        two 2]
    (p/echo (= one two) "c0l15_type_aliases.gleam:9")))

(defn -main [& _]
  (main))
