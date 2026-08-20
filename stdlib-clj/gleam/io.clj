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
