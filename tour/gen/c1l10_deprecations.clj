(ns c1l10-deprecations
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- new-function []
  nil)

(defn- old-function []
  nil)

(defn main []
  (p/echo (old-function) "c1l10_deprecations.gleam:2")
  (p/echo (new-function) "c1l10_deprecations.gleam:3"))

(defn -main [& _]
  (main))
