(ns numeric-separator-syntax
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (p/echo 1000000 "numeric_separator_syntax.gleam:2")
  (p/echo 10000.01 "numeric_separator_syntax.gleam:3"))

(defn -main [& _]
  (main))
