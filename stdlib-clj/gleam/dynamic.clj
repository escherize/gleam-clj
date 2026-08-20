(ns gleam.dynamic
  (:refer-clojure :exclude [cast float int])
  (:require
   [gleam-ffi]
   [gleam.dict :as dict]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Dynamic

(def classify gleam-ffi/classify)

(def bool gleam-ffi/identity1)

(def string gleam-ffi/identity1)

(def float gleam-ffi/identity1)

(def int gleam-ffi/identity1)

(def bit-array gleam-ffi/identity1)

(def list' gleam-ffi/identity1)

(def array gleam-ffi/identity1)

(def cast gleam-ffi/identity1)

(defn properties
  "Create a dynamic value made of an unordered series of keys and values, where
  the keys are unique.
  
  On Erlang this will be a map, on JavaScript this will be a Gleam dict
  object."
  [entries]
  (cast (dict/from-list entries)))

(defn nil'
  "A dynamic value representing nothing.
  
  On Erlang this will be the atom `nil`, on JavaScript this will be
  `undefined`."
  []
  (cast nil))
