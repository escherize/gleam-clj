(ns repeat-a-string
  (:require
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn main []
  (p/echo (string/repeat "hello" 3) "repeat_a_string.gleam:4"))

(defn -main [& _]
  (main))
