(ns gleam.io
  (:refer-clojure :exclude [print println])
  (:require
   [gleam-ffi]))

(def ^{:malli/schema [:=> [:cat :string] :nil]} print gleam-ffi/print-stdout)

(def ^{:malli/schema [:=> [:cat :string] :nil]} print-error gleam-ffi/print-error)

(def ^{:malli/schema [:=> [:cat :string] :nil]} println gleam-ffi/println-stdout)

(def ^{:malli/schema [:=> [:cat :string] :nil]} println-error gleam-ffi/println-error)
