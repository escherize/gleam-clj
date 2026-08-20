(ns gleam.function
  (:refer-clojure :exclude [identity])
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn identity
  "Takes a single argument and always returns its input value."
  [x]
  x)
