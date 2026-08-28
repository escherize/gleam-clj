(ns gleam.bit-array
  "BitArrays are a sequence of binary data of any length."
  (:refer-clojure :exclude [compare concat])
  (:require
   [gleam-ffi]
   [gleam.order :as order]
   [gleam.prelude :as p]
   [gleam.string :as string]))

(def ^{:malli/schema [:=> [:cat :string] [:vector :int]] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:14"} from-string gleam-ffi/ba-from-string)

(def ^{:malli/schema [:=> [:cat [:vector :int]] :int] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:20"} bit-size gleam-ffi/ba-bit-size)

(def ^{:malli/schema [:=> [:cat [:vector :int]] :int] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:26"} byte-size gleam-ffi/ba-byte-size)

(def ^{:malli/schema [:=> [:cat [:vector :int]] [:vector :int]] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:30"} pad-to-bytes gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat [:sequential [:vector :int]]] [:vector :int]] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:124"} concat gleam-ffi/ba-concat)

(defn append
  "append(to first: BitArray, suffix second: BitArray) -> BitArray

   Creates a new bit array by joining two bit arrays.

   ## Examples

   ```gleam
   assert bit_array.append(
   to: bit_array.from_string(\"butter\"),
   suffix: bit_array.from_string(\"fly\"),
   )
   == bit_array.from_string(\"butterfly\")
   ```"
  {:malli/schema [:=> [:cat [:vector :int] [:vector :int]] [:vector :int]]
   :gleam/src "stdlib-src/src/gleam/bit_array.gleam:52"}
  [first' second]
  (concat (list first' second)))

(def ^{:malli/schema [:=> [:cat [:vector :int] :int :int] (p/result-of [:vector :int] :nil)] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:66"} slice gleam-ffi/ba-slice)

(def ^{:malli/schema [:=> [:cat [:vector :int]] :boolean] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:74"} is-utf8 gleam-ffi/ba-is-utf8)

(def ^{:gleam/src "stdlib-src/src/gleam/bit_array.gleam:108"} unsafe-to-string gleam-ffi/ba-unsafe-to-string)

(defn to-string
  "to_string(bits: BitArray) -> Result(String, Nil)

   Converts a bit array to a string.

   Returns an error if the bit array is invalid UTF-8 data."
  {:malli/schema [:=> [:cat [:vector :int]] (p/result-of :string :nil)]
   :gleam/src "stdlib-src/src/gleam/bit_array.gleam:100"}
  [bits]
  (let [subject (is-utf8 bits)]
    (if subject (p/->Ok (unsafe-to-string bits)) (p/->Error nil))))

(def ^{:malli/schema [:=> [:cat [:vector :int] :boolean] :string] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:133"} base64-encode gleam-ffi/ba-base64-encode)

(def ^{:gleam/src "stdlib-src/src/gleam/bit_array.gleam:147"} decode64 gleam-ffi/ba-decode64)

(defn base64-decode
  "base64_decode(encoded: String) -> Result(BitArray, Nil)

   Decodes a base 64 encoded string into a `BitArray`."
  {:malli/schema [:=> [:cat :string] (p/result-of [:vector :int] :nil)]
   :gleam/src "stdlib-src/src/gleam/bit_array.gleam:137"}
  [^java.lang.String encoded]
  (let [padded (let [subject (rem (byte-size (from-string encoded)) 4)]
                 (if (= subject 0)
                   encoded
                   (let [n subject]
                     (string/append encoded (string/repeat "=" (-' 4 n))))))]
    (decode64 padded)))

(defn base64-url-encode
  "base64_url_encode(input: BitArray, padding: Bool) -> String

   Encodes a `BitArray` into a base 64 encoded string with URL and filename
   safe alphabet.

   If the bit array does not contain a whole number of bytes then it is padded
   with zero bits prior to being encoded."
  {:malli/schema [:=> [:cat [:vector :int] :boolean] :string]
   :gleam/src "stdlib-src/src/gleam/bit_array.gleam:155"}
  ^java.lang.String [input padding]
  (-> input
      (base64-encode padding)
      (string/replace "+" "-")
      (string/replace "/" "_")))

(defn base64-url-decode
  "base64_url_decode(encoded: String) -> Result(BitArray, Nil)

   Decodes a base 64 encoded string with URL and filename safe alphabet into a
   `BitArray`."
  {:malli/schema [:=> [:cat :string] (p/result-of [:vector :int] :nil)]
   :gleam/src "stdlib-src/src/gleam/bit_array.gleam:165"}
  [^java.lang.String encoded]
  (-> encoded (string/replace "-" "+") (string/replace "_" "/") base64-decode))

(def ^{:malli/schema [:=> [:cat [:vector :int]] :string] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:179"} base16-encode gleam-ffi/ba-base16-encode)

(def ^{:malli/schema [:=> [:cat :string] (p/result-of [:vector :int] :nil)] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:185"} base16-decode gleam-ffi/ba-base16-decode)

(def ^{:malli/schema [:=> [:cat [:vector :int]] :string] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:202"} inspect gleam-ffi/ba-inspect)

(def ^{:malli/schema [:=> [:cat [:vector :int] [:vector :int]] (order/Order-schema)] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:248"} compare gleam-ffi/ba-compare)

(def ^{:malli/schema [:=> [:cat [:vector :int] [:vector :int]] :boolean] :gleam/src "stdlib-src/src/gleam/bit_array.gleam:288"} starts-with gleam-ffi/ba-starts-with)
