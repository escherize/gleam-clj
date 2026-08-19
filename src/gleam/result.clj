(ns gleam.result
  "Shims for gleam/result."
  (:require [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn unwrap
  "Ok value, or the default."
  [r default]
  (if (instance? Ok r) (:value r) default))

(defn attempt
  "Shim for gleam/result.try (rename: try is a Clojure special form)."
  [r fun]
  (if (instance? Ok r) (fun (:value r)) r))

(defn map-ok
  "Shim for gleam/result.map (rename: map collides with clojure.core)."
  [r fun]
  (if (instance? Ok r) (p/->Ok (fun (:value r))) r))
