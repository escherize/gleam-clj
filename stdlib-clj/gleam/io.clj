(ns gleam.io
  (:refer-clojure :exclude [print println])
  (:require
   [gleam-ffi]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(def print gleam-ffi/print-stdout)

(def print-error gleam-ffi/print-error)

(def println gleam-ffi/println-stdout)

(def println-error gleam-ffi/println-error)

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'print [:=> [:cat :string] :nil]
   'print-error [:=> [:cat :string] :nil]
   'println [:=> [:cat :string] :nil]
   'println-error [:=> [:cat :string] :nil]})
