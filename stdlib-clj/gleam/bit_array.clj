(ns gleam.bit-array
  (:refer-clojure :exclude [compare concat])
  (:require
   [gleam-ffi]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [gleam.order :as order]
   [gleam.prelude :as p]
   [gleam.string :as string]))

(def ^{:malli/schema [:=> [:cat :string] [:vector :int]]} from-string gleam-ffi/ba-from-string)

(def ^{:malli/schema [:=> [:cat [:vector :int]] :int]} bit-size gleam-ffi/ba-bit-size)

(def ^{:malli/schema [:=> [:cat [:vector :int]] :int]} byte-size gleam-ffi/ba-byte-size)

(def ^{:malli/schema [:=> [:cat [:vector :int]] [:vector :int]]} pad-to-bytes gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat [:sequential [:vector :int]]] [:vector :int]]} concat gleam-ffi/ba-concat)

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
  {:malli/schema [:=> [:cat [:vector :int] [:vector :int]] [:vector :int]]}
  [first' second]
  (concat (list first' second)))

(def ^{:malli/schema [:=> [:cat [:vector :int] :int :int] [:or [:fn (partial instance? gleam.prelude.Ok)] [:fn (partial instance? gleam.prelude.Error)]]]} slice gleam-ffi/ba-slice)

(def ^{:malli/schema [:=> [:cat [:vector :int]] :boolean]} is-utf8 gleam-ffi/ba-is-utf8)

(def unsafe-to-string gleam-ffi/ba-unsafe-to-string)

(defn to-string
  "Converts a bit array to a string.
  
  Returns an error if the bit array is invalid UTF-8 data."
  {:malli/schema [:=> [:cat [:vector :int]] [:or [:fn (partial instance? gleam.prelude.Ok)] [:fn (partial instance? gleam.prelude.Error)]]]}
  [bits]
  (let [subject (is-utf8 bits)]
    (if subject (p/->Ok (unsafe-to-string bits)) (p/->Error nil))))

(def ^{:malli/schema [:=> [:cat [:vector :int] :boolean] :string]} base64-encode gleam-ffi/ba-base64-encode)

(def decode64 gleam-ffi/ba-decode64)

(defn base64-decode
  "Decodes a base 64 encoded string into a `BitArray`."
  {:malli/schema [:=> [:cat :string] [:or [:fn (partial instance? gleam.prelude.Ok)] [:fn (partial instance? gleam.prelude.Error)]]]}
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
  {:malli/schema [:=> [:cat [:vector :int] :boolean] :string]}
  [input padding]
  (-> input
      (base64-encode padding)
      (string/replace "+" "-")
      (string/replace "/" "_")))

(defn base64-url-decode
  "Decodes a base 64 encoded string with URL and filename safe alphabet into a
  `BitArray`."
  {:malli/schema [:=> [:cat :string] [:or [:fn (partial instance? gleam.prelude.Ok)] [:fn (partial instance? gleam.prelude.Error)]]]}
  [encoded]
  (-> encoded (string/replace "-" "+") (string/replace "_" "/") base64-decode))

(def ^{:malli/schema [:=> [:cat [:vector :int]] :string]} base16-encode gleam-ffi/ba-base16-encode)

(def ^{:malli/schema [:=> [:cat :string] [:or [:fn (partial instance? gleam.prelude.Ok)] [:fn (partial instance? gleam.prelude.Error)]]]} base16-decode gleam-ffi/ba-base16-decode)

(def ^{:malli/schema [:=> [:cat [:vector :int]] :string]} inspect gleam-ffi/ba-inspect)

(def ^{:malli/schema [:=> [:cat [:vector :int] [:vector :int]] [:or [:fn (partial instance? gleam.order.Lt)] [:fn (partial instance? gleam.order.Eq)] [:fn (partial instance? gleam.order.Gt)]]]} compare gleam-ffi/ba-compare)

(def ^{:malli/schema [:=> [:cat [:vector :int] [:vector :int]] :boolean]} starts-with gleam-ffi/ba-starts-with)
