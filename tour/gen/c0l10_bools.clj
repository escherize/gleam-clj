(ns c0l10-bools
  (:require
   [gleam.bool :as bool]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (p/echo (and true false) "c0l10_bools.gleam:5")
  (p/echo (and true true) "c0l10_bools.gleam:6")
  (p/echo (or false false) "c0l10_bools.gleam:7")
  (p/echo (or false true) "c0l10_bools.gleam:8")
  (p/echo (bool/to-string true) "c0l10_bools.gleam:11"))

(defn -main [& _]
  (main))
