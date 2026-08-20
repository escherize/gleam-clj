(ns c0l18-constants
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(def ^:private floats (list 1.1 2.2 3.3))

(def ^:private ints (list 1 2 3))

(defn main []
  (p/echo ints "c0l18_constants.gleam:6")
  (p/echo (= ints (list 1 2 3)) "c0l18_constants.gleam:7")
  (p/echo floats "c0l18_constants.gleam:9")
  (p/echo (= floats (list 1.1 2.2 3.3)) "c0l18_constants.gleam:10"))

(defn -main [& _]
  (main))
