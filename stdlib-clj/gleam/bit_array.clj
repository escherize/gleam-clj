(ns gleam.bit-array
  (:refer-clojure :exclude [compare concat])
  (:require
   [gleam-ffi]
   [gleam.int :as int]
   [gleam.order :as order]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(def from-string gleam-ffi/ba-from-string)

(def bit-size gleam-ffi/ba-bit-size)

(def byte-size gleam-ffi/ba-byte-size)

(def pad-to-bytes gleam-ffi/identity1)

(def concat gleam-ffi/ba-concat)

(defn append
  "Creates a new bit array by joining two bit arrays.
  
  ## Examples
  
  ```gleam
  assert bit_array.append(
  to: bit_array.from_string(\"butter\"),
  suffix: bit_array.from_string(\"fly\"),
  )
  == bit_array.from_string(\"butterfly\")
  ```"
  [first' second]
  (concat (list first' second)))

(def slice gleam-ffi/ba-slice)

(def is-utf8-loop gleam-ffi/ba-is-utf8)

(def is-utf8 gleam-ffi/ba-is-utf8)

(def unsafe-to-string gleam-ffi/ba-unsafe-to-string)

(defn to-string
  "Converts a bit array to a string.
  
  Returns an error if the bit array is invalid UTF-8 data."
  [bits]
  (let [subject (is-utf8 bits)]
    (if subject (p/->Ok (unsafe-to-string bits)) (p/->Error nil))))

(def base64-encode gleam-ffi/ba-base64-encode)

(def decode64 gleam-ffi/ba-decode64)

(defn base64-decode
  "Decodes a base 64 encoded string into a `BitArray`."
  [encoded]
  (let [padded (let [subject (rem (byte-size (from-string encoded)) 4)]
                 (if (= subject 0)
                   encoded
                   (let [n subject]
                     (string/append encoded (string/repeat "=" (-' 4 n))))))]
    (decode64 padded)))

(defn base64-url-encode
  "Encodes a `BitArray` into a base 64 encoded string with URL and filename
  safe alphabet.
  
  If the bit array does not contain a whole number of bytes then it is padded
  with zero bits prior to being encoded."
  [input padding]
  (-> input
      (base64-encode padding)
      (string/replace "+" "-")
      (string/replace "/" "_")))

(defn base64-url-decode
  "Decodes a base 64 encoded string with URL and filename safe alphabet into a
  `BitArray`."
  [encoded]
  (-> encoded (string/replace "-" "+") (string/replace "_" "/") base64-decode))

(def base16-encode gleam-ffi/ba-base16-encode)

(def base16-decode gleam-ffi/ba-base16-decode)

(def inspect-loop gleam-ffi/ba-inspect)

(def inspect gleam-ffi/ba-inspect)

(def bit-array-to-int-and-size gleam-ffi/ba-to-int-and-size)

(def compare gleam-ffi/ba-compare)

(def starts-with gleam-ffi/ba-starts-with)

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'append [:=> [:cat [:vector :int] [:vector :int]] [:vector :int]]
   'base16-decode [:=> [:cat :string] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'base16-encode [:=> [:cat [:vector :int]] :string]
   'base64-decode [:=> [:cat :string] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'base64-encode [:=> [:cat [:vector :int] :boolean] :string]
   'base64-url-decode [:=> [:cat :string] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'base64-url-encode [:=> [:cat [:vector :int] :boolean] :string]
   'bit-size [:=> [:cat [:vector :int]] :int]
   'byte-size [:=> [:cat [:vector :int]] :int]
   'compare [:=> [:cat [:vector :int] [:vector :int]] [:or [:fn (partial instance? gleam.order.Lt)] [:fn (partial instance? gleam.order.Eq)] [:fn (partial instance? gleam.order.Gt)]]]
   'concat [:=> [:cat [:sequential [:vector :int]]] [:vector :int]]
   'from-string [:=> [:cat :string] [:vector :int]]
   'inspect [:=> [:cat [:vector :int]] :string]
   'is-utf8 [:=> [:cat [:vector :int]] :boolean]
   'pad-to-bytes [:=> [:cat [:vector :int]] [:vector :int]]
   'slice [:=> [:cat [:vector :int] :int :int] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'starts-with [:=> [:cat [:vector :int] [:vector :int]] :boolean]
   'to-string [:=> [:cat [:vector :int]] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]})
