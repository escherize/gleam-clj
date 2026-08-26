(ns gleam.io
  (:refer-clojure :exclude [print println])
  (:require
   [gleam-ffi]))

(def ^{:malli/schema [:=> [:cat :string] :nil] :gleam/src "stdlib-src/src/gleam/io.gleam:14"} print gleam-ffi/print-stdout)

(def ^{:malli/schema [:=> [:cat :string] :nil] :gleam/src "stdlib-src/src/gleam/io.gleam:29"} print-error gleam-ffi/print-error)

(def ^{:malli/schema [:=> [:cat :string] :nil] :gleam/src "stdlib-src/src/gleam/io.gleam:42"} println gleam-ffi/println-stdout)

(def ^{:malli/schema [:=> [:cat :string] :nil] :gleam/src "stdlib-src/src/gleam/io.gleam:55"} println-error gleam-ffi/println-error)
