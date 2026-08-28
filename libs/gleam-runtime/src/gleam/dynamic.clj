(ns gleam.dynamic
  (:refer-clojure :exclude [cast float int])
  (:require
   [gleam-ffi]
   [gleam.dict :as dict]))

;; type Dynamic
(defprotocol IDynamic)
(defn Dynamic? "True if `v` is any Dynamic value." [v] (instance? gleam.dynamic.IDynamic v))
(defn Dynamic-schema
  "Malli schema for Dynamic."
  []
  [:fn Dynamic?])

(def ^{:malli/schema [:=> [:cat (Dynamic-schema)] :string] :gleam/src "stdlib-src/src/gleam/dynamic.gleam:29"} classify gleam-ffi/classify)

(def ^{:malli/schema [:=> [:cat :boolean] (Dynamic-schema)] :gleam/src "stdlib-src/src/gleam/dynamic.gleam:35"} bool gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat :string] (Dynamic-schema)] :gleam/src "stdlib-src/src/gleam/dynamic.gleam:43"} string gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat :double] (Dynamic-schema)] :gleam/src "stdlib-src/src/gleam/dynamic.gleam:49"} float gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat :int] (Dynamic-schema)] :gleam/src "stdlib-src/src/gleam/dynamic.gleam:55"} int gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat [:vector :int]] (Dynamic-schema)] :gleam/src "stdlib-src/src/gleam/dynamic.gleam:61"} bit-array gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat [:sequential (Dynamic-schema)]] (Dynamic-schema)] :gleam/src "stdlib-src/src/gleam/dynamic.gleam:67"} list' gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat [:sequential (Dynamic-schema)]] (Dynamic-schema)] :gleam/src "stdlib-src/src/gleam/dynamic.gleam:76"} array gleam-ffi/identity1)

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic.gleam:99"} cast gleam-ffi/identity1)

(defn properties
  "properties(entries: List(#(Dynamic, Dynamic))) -> Dynamic

   Create a dynamic value made of an unordered series of keys and values, where
   the keys are unique.

   On Erlang this will be a map, on JavaScript this will be a Gleam dict
   object."
  {:malli/schema [:=> [:cat [:sequential [:tuple (Dynamic-schema) (Dynamic-schema)]]]
                      (Dynamic-schema)]
   :gleam/src "stdlib-src/src/gleam/dynamic.gleam:84"}
  [entries]
  (cast (dict/from-list entries)))

(defn nil_
  "nil() -> Dynamic

   A dynamic value representing nothing.

   On Erlang this will be the atom `nil`, on JavaScript this will be
   `undefined`."
  {:malli/schema [:=> [:cat] (Dynamic-schema)]
   :gleam/src "stdlib-src/src/gleam/dynamic.gleam:93"}
  []
  (cast nil))
