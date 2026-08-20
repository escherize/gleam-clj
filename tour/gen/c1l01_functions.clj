(ns c1l01-functions
  (:refer-clojure :exclude [double])
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- multiply [a b]
  (*' a b))

(defn- double [a]
  (multiply a 2))

(defn main []
  (p/echo (double 10) "c1l01_functions.gleam:2"))

(defn -main [& _]
  (main))
