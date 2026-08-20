(ns gleam.dynamic.decode
  (:refer-clojure :exclude [cast float int map])
  (:require
   [gleam-ffi]
   [gleam.bit-array :as bit_array]
   [gleam.dict :as dict]
   [gleam.dynamic :as dynamic]
   [gleam.float :as float]
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.option :as option]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type DecodeError
(defrecord DecodeError [expected found path])

;; type Decoder
(defrecord Decoder [function])

(defn- decode-dynamic [data]
  [data (list)])

(def dynamic (->Decoder decode-dynamic))

(defn run
  "Run a decoder on a `Dynamic` value, decoding the value if it is of the
  desired type, or returning errors.
  
  ## Examples
  
  ```gleam
  let decoder = {
  use name <- decode.field(\"name\", decode.string)
  use email <- decode.field(\"email\", decode.string)
  decode.success(SignUp(name: name, email: email))
  }
  
  decode.run(data, decoder)
  ```"
  [data decoder]
  (let [[maybe-invalid-data errors] ((:function decoder) data)]
    (if (empty? errors) (p/->Ok maybe-invalid-data) (p/->Error errors))))

(def dynamic-float gleam-ffi/dynamic-float)

(defn- run-dynamic-function [data name f]
  (let [subject (f data)]
    (if (instance? Ok subject)
      (let [data (:value subject)]
        [data (list)])
      (let [placeholder (:value subject)]
        [placeholder (list (->DecodeError name (dynamic/classify data) (list)))]))))

(defn- decode-float [data]
  (run-dynamic-function data "Float" dynamic-float))

(def float (->Decoder decode-float))

(defn map
  "Apply a transformation function to any value decoded by the decoder.
  
  ## Examples
  
  ```gleam
  let decoder = decode.int |> decode.map(int.to_string)
  let result = decode.run(dynamic.int(1000), decoder)
  assert result == Ok(\"1000\")
  ```"
  [decoder transformer]
  (->Decoder (fn [d]
               (let [[data errors] ((:function decoder) d)]
                 [(transformer data) errors]))))

(def dynamic-int gleam-ffi/dynamic-int)

(defn- decode-int [data]
  (run-dynamic-function data "Int" dynamic-int))

(def int (->Decoder decode-int))

(def dynamic-bit-array gleam-ffi/dynamic-bit-array)

(defn- decode-bit-array [data]
  (run-dynamic-function data "BitArray" dynamic-bit-array))

(def bit-array (->Decoder decode-bit-array))

(defn- dynamic-string [data]
  (let [subject (dynamic-bit-array data)]
    (if (instance? Ok subject)
      (let [data (:value subject)]
        (let [subject (bit_array/to-string data)]
          (if (instance? Ok subject)
            (let [string (:value subject)]
              (p/->Ok string))
            (p/->Error ""))))
      (p/->Error ""))))

(defn- decode-string [data]
  (run-dynamic-function data "String" dynamic-string))

(def string (->Decoder decode-string))

(defn- run-decoders [data failure decoders]
  (if (empty? decoders)
    failure
    (let [decoder (first decoders) decoders (rest decoders)]
      (let [[_ errors :as layer] ((:function decoder) data)]
        (if (empty? errors) layer (recur data failure decoders))))))

(defn one-of
  "Create a new decoder from several other decoders. Each of the inner
  decoders is run in turn, and the value from the first to succeed is used.
  
  If no decoder succeeds then the errors from the first decoder are used.
  If you wish for different errors then you may wish to use the
  `collapse_errors` or `map_errors` functions.
  
  ## Examples
  
  ```gleam
  let decoder =
  decode.one_of(decode.string, or: [
  decode.int |> decode.map(int.to_string),
  decode.float |> decode.map(float.to_string),
  ])
  assert decode.run(dynamic.int(1000), decoder) == Ok(\"1000\")
  ```"
  [first' alternatives]
  (->Decoder (fn [dynamic-data]
               (let [[_ errors :as layer] ((:function first') dynamic-data)]
                 (if (empty? errors)
                   layer
                   (run-decoders dynamic-data layer alternatives))))))

(defn- path-segment-to-string [key]
  (let [decoder (one-of string
                        (list (-> int (map int/to-string)) (-> float (map float/to-string))))]
    (let [subject (run key decoder)]
      (if (instance? Ok subject)
        (let [key (:value subject)]
          key)
        (str (str "<" (dynamic/classify key)) ">")))))

(def cast gleam-ffi/identity1)

(def decode-list gleam-ffi/decode-list)

(declare push-path list')

(defn- push-path [layer path]
  (let [path (list/map path (fn [key] (-> key cast path-segment-to-string)))
        errors (list/map (nth layer 1)
                         (fn [error]
                           (->DecodeError (:expected error) (:found error) (list/append path (:path error)))))]
    [(nth layer 0) errors]))

(defn list'
  "A decoder that decodes lists where all elements are decoded with a given
  decoder.
  
  ## Examples
  
  ```gleam
  let result =
  [1, 2, 3]
  |> list.map(dynamic.int)
  |> dynamic.list
  |> decode.run(decode.list(of: decode.int))
  assert result == Ok([1, 2, 3])
  ```"
  [inner]
  (->Decoder (fn [data]
               (decode-list data
                            (:function inner)
                            (fn [p k] (push-path p (list k)))
                            0
                            (list)))))

(def bare-index gleam-ffi/bare-index)

(defn- index [path position inner data handle-miss]
  (if (empty? path)
    (-> data inner (push-path (list/reverse position)))
    (let [key (first path) path (rest path)]
      (let [subject (bare-index data key)]
        (cond
          (and (instance? Ok subject) (instance? gleam.option.Some (:value subject))) (let [data (:value (:value subject))]
                                                                                        (recur path (list* key position) inner data handle-miss))
          (and (instance? Ok subject) (instance? gleam.option.None (:value subject))) (handle-miss data
                                                                                                   (list* key position))
          (instance? gleam.prelude.Error subject) (let [kind (:value subject)]
                                                    (let [[default _] (inner data)]
                                                      (-> [default (list (->DecodeError kind
                                                                                (dynamic/classify data)
                                                                                (list)))]
                                                          (push-path (list/reverse position))))))))))

(defn subfield
  "The same as [`field`](#field), except taking a path to the value rather
  than a field name.
  
  This function will index into dictionaries with any key type, and if the key is
  an int then it'll also index into Erlang tuples and JavaScript arrays, and
  the first eight elements of Gleam lists.
  
  ## Examples
  
  ```gleam
  let data =
  dynamic.properties([
  #(
  dynamic.string(\"data\"),
  dynamic.properties([
  #(dynamic.string(\"email\"), dynamic.string(\"lucy@example.com\")),
  #(dynamic.string(\"name\"), dynamic.string(\"Lucy\")),
  ]),
  ),
  ])
  
  let decoder = {
  use name <- decode.subfield([\"data\", \"name\"], decode.string)
  use email <- decode.subfield([\"data\", \"email\"], decode.string)
  decode.success(SignUp(name: name, email: email))
  }
  let result = decode.run(data, decoder)
  assert result == Ok(SignUp(name: \"Lucy\", email: \"lucy@example.com\"))
  ```"
  [field-path field-decoder next]
  (->Decoder (fn [data]
               (let [[out errors1] (index field-path
                                          (list)
                                          (:function field-decoder)
                                          data
                                          (fn [data position]
                                            (let [[default _] ((:function field-decoder) data)]
                                              (-> [default (list (->DecodeError "Field"
                                                                        "Nothing"
                                                                        (list)))]
                                                  (push-path (list/reverse position))))))
                     [out errors2] ((:function (next out)) data)]
                 [out (list/append errors1 errors2)]))))

(defn at
  "A decoder that decodes a value that is nested within other values. For
  example, decoding a value that is within some deeply nested JSON objects.
  
  This function will index into dictionaries with any key type, and if the key is
  an int then it'll also index into Erlang tuples and JavaScript arrays, and
  the first eight elements of Gleam lists.
  
  ## Examples
  
  ```gleam
  let decoder = decode.at([\"one\", \"two\"], decode.int)
  
  let data =
  dynamic.properties([
  #(
  dynamic.string(\"one\"),
  dynamic.properties([
  #(dynamic.string(\"two\"), dynamic.int(1000)),
  ]),
  ),
  ])
  
  assert decode.run(data, decoder) == Ok(1000)
  ```
  
  ```gleam
  assert dynamic.nil()
  |> decode.run(decode.optional(decode.int))
  == Ok(option.None)
  ```"
  [path inner]
  (->Decoder (fn [data]
               (index path
                      (list)
                      (:function inner)
                      data
                      (fn [data position]
                        (let [[default _] ((:function inner) data)]
                          (-> [default (list (->DecodeError "Field" "Nothing" (list)))]
                              (push-path (list/reverse position)))))))))

(defn success
  "Finalise a decoder having successfully extracted a value.
  
  ## Examples
  
  ```gleam
  let data =
  dynamic.properties([
  #(dynamic.string(\"email\"), dynamic.string(\"lucy@example.com\")),
  #(dynamic.string(\"name\"), dynamic.string(\"Lucy\")),
  ])
  
  let decoder = {
  use name <- decode.field(\"name\", string)
  use email <- decode.field(\"email\", string)
  decode.success(SignUp(name: name, email: email))
  }
  
  let result = decode.run(data, decoder)
  assert result == Ok(SignUp(name: \"Lucy\", email: \"lucy@example.com\"))
  ```"
  [data]
  (->Decoder (fn [_] [data (list)])))

(defn decode-error
  "Construct a decode error for some unexpected dynamic data."
  [expected found]
  (list (->DecodeError expected (dynamic/classify found) (list))))

(defn field
  "Run a decoder on a field of a `Dynamic` value, decoding the value if it is
  of the desired type, or returning errors. An error is returned if there is
  no field for the specified key.
  
  This function will index into dictionaries with any key type, and if the key is
  an int then it'll also index into Erlang tuples and JavaScript arrays, and
  the first eight elements of Gleam lists.
  
  ## Examples
  
  ```gleam
  let data =
  dynamic.properties([
  #(dynamic.string(\"email\"), dynamic.string(\"lucy@example.com\")),
  #(dynamic.string(\"name\"), dynamic.string(\"Lucy\")),
  ])
  
  let decoder = {
  use name <- decode.field(\"name\", string)
  use email <- decode.field(\"email\", string)
  decode.success(SignUp(name: name, email: email))
  }
  
  let result = decode.run(data, decoder)
  assert result == Ok(SignUp(name: \"Lucy\", email: \"lucy@example.com\"))
  ```
  
  If you wish to decode a value that is more deeply nested within the dynamic
  data, see [`subfield`](#subfield) and [`at`](#at).
  
  If you wish to return a default in the event that a field is not present,
  see [`optional_field`](#optional_field) and / [`optionally_at`](#optionally_at)."
  [field-name field-decoder next]
  (subfield (list field-name) field-decoder next))

(defn optional-field
  "Run a decoder on a field of a `Dynamic` value, decoding the value if it is
  of the desired type, or returning errors. The given default value is
  returned if there is no field for the specified key.
  
  This function will index into dictionaries with any key type, and if the key is
  an int then it'll also index into Erlang tuples and JavaScript arrays, and
  the first eight elements of Gleam lists.
  
  ## Examples
  
  ```gleam
  let data =
  dynamic.properties([
  #(dynamic.string(\"name\"), dynamic.string(\"Lucy\")),
  ])
  
  let decoder = {
  use name <- decode.field(\"name\", string)
  use email <- decode.optional_field(\"email\", \"n/a\", string)
  decode.success(SignUp(name: name, email: email))
  }
  
  let result = decode.run(data, decoder)
  assert result == Ok(SignUp(name: \"Lucy\", email: \"n/a\"))
  ```"
  [key default field-decoder next]
  (->Decoder (fn [data]
               (let [[out errors1] (-> (let [subject (bare-index data key)]
                                         (cond
                                           (and (instance? Ok subject) (instance? gleam.option.Some (:value subject))) (let [data (:value (:value subject))]
                                                                                                                         ((:function field-decoder) data))
                                           (and (instance? Ok subject) (instance? gleam.option.None (:value subject))) [default (list)]
                                           (instance? gleam.prelude.Error subject) (let [kind (:value subject)]
                                                                                     [default (list (->DecodeError kind
                                                                                                           (dynamic/classify data)
                                                                                                           (list)))])))
                                       (push-path (list key)))
                     [out errors2] ((:function (next out)) data)]
                 [out (list/append errors1 errors2)]))))

(defn optionally-at
  "A decoder that decodes a value that is nested within other values. For
  example, decoding a value that is within some deeply nested JSON objects.
  
  This function will index into dictionaries with any key type, and if the key is
  an int then it'll also index into Erlang tuples and JavaScript arrays, and
  the first eight elements of Gleam lists.
  
  ## Examples
  
  ```gleam
  let decoder = decode.optionally_at([\"one\", \"two\"], 100, decode.int)
  
  let data =
  dynamic.properties([
  #(dynamic.string(\"one\"), dynamic.properties([])),
  ])
  
  assert decode.run(data, decoder) == Ok(100)
  ```"
  [path default inner]
  (->Decoder (fn [data]
               (index path
                      (list)
                      (:function inner)
                      data
                      (fn [_ _] [default (list)])))))

(defn- decode-bool [data]
  (let [subject (= (cast true) data)]
    (if subject
      [true (list)]
      (let [subject (= (cast false) data)]
        (if subject [false (list)] [false (decode-error "Bool" data)])))))

(def decode-dict gleam-ffi/decode-dict)

(declare fold-dict dict)

(defn- fold-dict [acc key value key-decoder value-decoder]
  (let [subject (key-decoder key)]
    (if (empty? (nth subject 1))
      (let [key-decoded (nth subject 0)]
        (let [subject (value-decoder value)]
          (if (empty? (nth subject 1))
            (let [value (nth subject 0)]
              (let [dict (dict/insert (nth acc 0) key-decoded value)]
                [dict (nth acc 1)]))
            (let [errors (nth subject 1)]
              (let [key-identifier (path-segment-to-string key)]
                (push-path [(dict/new*) errors] (list key-identifier)))))))
      (let [errors (nth subject 1)]
        (push-path [(dict/new*) errors] (list "keys"))))))

(defn dict
  "A decoder that decodes dicts where all keys and values are decoded with
  given decoders.
  
  ## Examples
  
  ```gleam
  let values =
  dynamic.properties([
  #(dynamic.string(\"one\"), dynamic.int(1)),
  #(dynamic.string(\"two\"), dynamic.int(2)),
  ])
  
  let result = decode.run(values, decode.dict(decode.string, decode.int))
  assert result == Ok(values)
  ```"
  [key value]
  (->Decoder (fn [data]
               (let [subject (decode-dict data)]
                 (if (instance? gleam.prelude.Error subject)
                   [(dict/new*) (decode-error "Dict" data)]
                   (let [dict (:value subject)]
                     (dict/fold dict
                                [(dict/new*) (list)]
                                (fn [a k v]
                                  (let [subject (nth a 1)]
                                    (if (empty? subject)
                                      (fold-dict a
                                                 k
                                                 v
                                                 (:function key)
                                                 (:function value))
                                      a))))))))))

(def is-null gleam-ffi/is-null)

(defn optional
  "A decoder that decodes nullable values of a type decoded by with a given
  decoder.
  
  This function can handle common representations of null on all runtimes, such as
  `nil`, `null`, and `undefined` on Erlang, and `undefined` and `null` on
  JavaScript.
  
  ## Examples
  
  ```gleam
  let result = decode.run(dynamic.int(100), decode.optional(decode.int))
  assert result == Ok(option.Some(100))
  ```
  
  ```gleam
  let result = decode.run(dynamic.nil(), decode.optional(decode.int))
  assert result == Ok(option.None)
  ```"
  [inner]
  (->Decoder (fn [data]
               (let [subject (is-null data)]
                 (if subject
                   [(option/->None) (list)]
                   (let [[data errors] ((:function inner) data)]
                     [(option/->Some data) errors]))))))

(defn map-errors
  "Apply a transformation function to any errors returned by the decoder."
  [decoder transformer]
  (->Decoder (fn [d]
               (let [[data errors] ((:function decoder) d)]
                 [data (transformer errors)]))))

(defn collapse-errors
  "Replace all errors produced by a decoder with one single error for a named
  expected type.
  
  This function may be useful if you wish to simplify errors before
  presenting them to a user, particularly when using the `one_of` function.
  
  ## Examples
  
  ```gleam
  let decoder = decode.string |> decode.collapse_errors(\"MyThing\")
  let result = decode.run(dynamic.int(1000), decoder)
  assert result == Error([DecodeError(\"MyThing\", \"Int\", [])])
  ```"
  [decoder name]
  (->Decoder (fn [dynamic-data]
               (let [[data errors :as layer] ((:function decoder) dynamic-data)]
                 (if (empty? errors)
                   layer
                   [data (decode-error name dynamic-data)])))))

(defn then
  "Create a new decoder based upon the value of a previous decoder.
  
  This may be useful to run one previous decoder to use in further decoding."
  [decoder next]
  (->Decoder (fn [dynamic-data]
               (let [[data errors] ((:function decoder) dynamic-data)
                     decoder (next data)
                     [data _ :as layer] ((:function decoder) dynamic-data)]
                 (if (empty? errors) layer [data errors])))))

(defn failure
  "Define a decoder that always fails.
  
  The first parameter is a \"placeholder\" value, which is some default value that the
  decoder uses internally in place of the value that would have been produced
  if the decoder was successful. It doesn't matter what this value is, it is
  never returned by the decoder or shown to the user, so pick some arbitrary
  value. If it is an int you might pick `0`, if it is a list you might pick
  `[]`.
  
  The second parameter is the name of the type that has failed to decode.
  
  ```gleam
  decode.failure(User(name: \"\", score: 0, tags: []), expected: \"User\")
  ```"
  [placeholder name]
  (->Decoder (fn [d] [placeholder (decode-error name d)])))

(defn new-primitive-decoder
  "Create a decoder for a new data type from a decoding function.
  
  This function is used for new primitive types. For example, you might
  define a decoder for Erlang's pid type.
  
  A default \"placeholder\" value is also required to make a decoder. When this
  decoder is used as part of a larger decoder this placeholder value is used
  so that the rest of the decoder can continue to run and
  collect all decoding errors. It doesn't matter what this value is, it is
  never returned by the decoder or shown to the user, so pick some arbitrary
  value. If it is an int you might pick `0`, if it is a list you might pick
  `[]`.
  
  If you were to make a decoder for the `Int` type (rather than using the
  built-in `Int` decoder) you would define it like so:
  
  ```gleam
  pub fn int_decoder() -> decode.Decoder(Int) {
  let default = \"\"
  decode.new_primitive_decoder(\"Int\", int_from_dynamic)
  }
  
  @external(erlang, \"my_module\", \"int_from_dynamic\")
  fn int_from_dynamic(data: Int) -> Result(Int, Int)
  ```
  
  ```erlang
  -module(my_module).
  -export([int_from_dynamic/1]).
  
  int_from_dynamic(Data) ->
  case is_integer(Data) of
  true -> {ok, Data};
  false -> {error, 0}
  end.
  ```"
  [name decoding-function]
  (->Decoder (fn [d]
               (let [subject (decoding-function d)]
                 (if (instance? Ok subject)
                   (let [t (:value subject)]
                     [t (list)])
                   (let [placeholder (:value subject)]
                     [placeholder (list (->DecodeError name (dynamic/classify d) (list)))]))))))

(defn recursive
  "Create a decoder that can refer to itself, useful for decoding deeply
  nested data.
  
  Attempting to create a recursive decoder without this function could result
  in an infinite loop. If you are using `field` or other `use`able functions
  then you may not need to use this function.
  
  ## Examples
  
  ```gleam
  type Nested {
  Nested(List(Nested))
  Value(String)
  }
  
  fn nested_decoder() -> decode.Decoder(Nested) {
  use <- decode.recursive
  decode.one_of(decode.string |> decode.map(Value), [
  decode.list(nested_decoder()) |> decode.map(Nested),
  ])
  }
  ```"
  [inner]
  (->Decoder (fn [data]
               (let [decoder (inner)]
                 ((:function decoder) data)))))

(def bool (->Decoder decode-bool))

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'at [:=> [:cat [:sequential :any] [:fn (partial instance? gleam.dynamic.decode.Decoder)]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'collapse-errors [:=> [:cat [:fn (partial instance? gleam.dynamic.decode.Decoder)] :string] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'decode-error [:=> [:cat :string [:or ]] [:sequential [:fn (partial instance? gleam.dynamic.decode.DecodeError)]]]
   'dict [:=> [:cat [:fn (partial instance? gleam.dynamic.decode.Decoder)] [:fn (partial instance? gleam.dynamic.decode.Decoder)]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'failure [:=> [:cat :any :string] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'field [:=> [:cat :any [:fn (partial instance? gleam.dynamic.decode.Decoder)] [:=> [:cat :any] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'list' [:=> [:cat [:fn (partial instance? gleam.dynamic.decode.Decoder)]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'map [:=> [:cat [:fn (partial instance? gleam.dynamic.decode.Decoder)] [:=> [:cat :any] :any]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'map-errors [:=> [:cat [:fn (partial instance? gleam.dynamic.decode.Decoder)] [:=> [:cat [:sequential [:fn (partial instance? gleam.dynamic.decode.DecodeError)]]] [:sequential [:fn (partial instance? gleam.dynamic.decode.DecodeError)]]]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'new-primitive-decoder [:=> [:cat :string [:=> [:cat [:or ]] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'one-of [:=> [:cat [:fn (partial instance? gleam.dynamic.decode.Decoder)] [:sequential [:fn (partial instance? gleam.dynamic.decode.Decoder)]]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'optional [:=> [:cat [:fn (partial instance? gleam.dynamic.decode.Decoder)]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'optional-field [:=> [:cat :any :any [:fn (partial instance? gleam.dynamic.decode.Decoder)] [:=> [:cat :any] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'optionally-at [:=> [:cat [:sequential :any] :any [:fn (partial instance? gleam.dynamic.decode.Decoder)]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'recursive [:=> [:cat [:=> [:cat] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'run [:=> [:cat [:or ] [:fn (partial instance? gleam.dynamic.decode.Decoder)]] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'subfield [:=> [:cat [:sequential :any] [:fn (partial instance? gleam.dynamic.decode.Decoder)] [:=> [:cat :any] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'success [:=> [:cat :any] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]
   'then [:=> [:cat [:fn (partial instance? gleam.dynamic.decode.Decoder)] [:=> [:cat :any] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]] [:fn (partial instance? gleam.dynamic.decode.Decoder)]]})
