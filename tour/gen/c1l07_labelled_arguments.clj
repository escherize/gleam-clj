(ns c1l07-labelled-arguments
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- calculate [value addend multiplier]
  (+' (*' value multiplier) addend))

(defn main []
  (p/echo (calculate 1 2 3) "c1l07_labelled_arguments.gleam:3")
  (p/echo (calculate 1 2 3) "c1l07_labelled_arguments.gleam:6")
  (p/echo (calculate 1 3 2) "c1l07_labelled_arguments.gleam:9"))

(defn -main [& _]
  (main))
