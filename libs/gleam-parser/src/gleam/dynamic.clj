(ns gleam.dynamic
  (:refer-clojure :exclude [cast float int])
  (:require
   [gleam-ffi]
   [gleam.dict :as dict]))

;; type Dynamic
(defprotocol IDynamic)
(defn Dynamic? "True if `v` is any Dynamic value." [v] (instance? gleam.dynamic.IDynamic v))

(def ^{:malli/schema [:=> [:cat [:or ]] :string]} classify gleam-ffi/classify)

(def ^{:malli/schema [:=> [:cat :boolean] [:or ]]} bool gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat :string] [:or ]]} string gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat :double] [:or ]]} float gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat :int] [:or ]]} int gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat [:vector :int]] [:or ]]} bit-array gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat [:sequential [:or ]]] [:or ]]} list' gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat [:sequential [:or ]]] [:or ]]} array gleam-ffi/identity1)

(def cast gleam-ffi/identity1)

(defn properties
  "Create a dynamic value made of an unordered series of keys and values, where
   the keys are unique.
   
   On Erlang this will be a map, on JavaScript this will be a Gleam dict
   object."
  {:malli/schema [:=> [:cat [:sequential [:tuple [:or ] [:or ]]]] [:or ]]}
  [entries]
  (cast (dict/from-list entries)))

(defn nil_
  "A dynamic value representing nothing.
   
   On Erlang this will be the atom `nil`, on JavaScript this will be
   `undefined`."
  {:malli/schema [:=> [:cat] [:or ]]}
  []
  (cast nil))
