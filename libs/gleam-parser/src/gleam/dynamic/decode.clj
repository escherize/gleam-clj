(ns gleam.dynamic.decode
  "The `Dynamic` type is used to represent dynamically typed data. That is, data
   that we don't know the precise type of yet, so we need to introspect the data to
   see if it is of the desired type before we can use it. Typically data like this
   would come from user input or from untyped languages such as Erlang or JavaScript.

   This module provides the `Decoder` type and associated functions, which provides
   a type-safe and composable way to convert dynamic data into some desired type,
   or into errors if the data doesn't have the desired structure.

   The `Decoder` type is generic and has 1 type parameter, which is the type that
   it attempts to decode. A `Decoder(String)` can be used to decode strings, and a
   `Decoder(Option(Int))` can be used to decode `Option(Int)`s

   Decoders work using _runtime reflection_ and the data structures of the target
   platform. Differences between Erlang and JavaScript data structures may impact
   your decoders, so it is important to test your decoders on all supported
   platforms.

   The decoding technique used by this module was inspired by Juraj Petráš'
   [Toy](https://github.com/Hackder/toy), Go's `encoding/json`, and Elm's
   `Json.Decode`. Thank you to them!

   # Generating decoders

   The language server has the \"generate dynamic decoder\" code action, which
   will generate a decoder function when run on a custom type definition.
   This generated decoder function can be a convenient shortcut when creating
   your own decoders, and you can edit the generated function to suit your needs.

   # Examples

   Dynamic data may come from various sources and so many different syntaxes could
   be used to describe or construct them. In these examples a pseudocode
   syntax is used to describe the data.

   ## Simple types

   This module defines decoders for simple data types such as [`string`](#string),
   [`int`](#int), [`float`](#float), [`bit_array`](#bit_array), and [`bool`](#bool).

   ```gleam
   // Data:
   // \"Hello, Joe!\"

   let result = decode.run(data, decode.string)
   assert result == Ok(\"Hello, Joe!\")
   ```

   ## Lists

   The [`list`](#list) decoder decodes `List`s. To use it you must construct it by
   passing in another decoder into the `list` function, which is the decoder that
   is to be used for the elements of the list, type checking both the list and its
   elements.

   ```gleam
   // Data:
   // [1, 2, 3, 4]

   let result = decode.run(data, decode.list(decode.int))
   assert result == Ok([1, 2, 3, 4])
   ```

   On Erlang this decoder can decode from lists, and on JavaScript it can
   decode from lists as well as JavaScript arrays.

   ## Options

   The [`optional`](#optional) decoder is used to decode values that may or may not
   be present. In other environments these might be called \"nullable\" values.

   Like the `list` decoder, the `optional` decoder takes another decoder,
   which is used to decode the value if it is present.

   ```gleam
   // Data:
   // 12.45

   let result = decode.run(data, decode.optional(decode.float))
   assert result == Ok(option.Some(12.45))
   ```
   ```gleam
   // Data:
   // null

   let result = decode.run(data, decode.optional(decode.int))
   assert result == Ok(option.None)
   ```

   This decoder knows how to handle multiple different runtime representations of
   absent values, including `Nil`, `None`, `null`, and `undefined`.

   ## Dicts

   The [`dict`](#dict) decoder decodes `Dicts` and contains two other decoders, one
   for the keys, one for the values.

   ```gleam
   // Data:
   // { \"Lucy\" -> 10, \"Nubi\" -> 20 }

   let result = decode.run(data, decode.dict(decode.string, decode.int))
   assert result
   == Ok(
   dict.from_list([
   #(\"Lucy\", 10),
   #(\"Nubi\", 20),
   ]),
   )
   ```

   ## Indexing objects

   The [`at`](#at) decoder can be used to decode a value that is nested within
   key-value containers such as Gleam dicts, Erlang maps, or JavaScript objects.

   ```gleam
   // Data:
   // { \"one\" -> { \"two\" -> 123 } }

   let result = decode.run(data, decode.at([\"one\", \"two\"], decode.int))
   assert result == Ok(123)
   ```

   ## Indexing arrays

   If you use ints as keys then the [`at`](#at) decoder can be used to index into
   array-like containers such as Gleam or Erlang tuples, or JavaScript arrays.

   ```gleam
   // Data:
   // [\"one\", \"two\", \"three\"]

   let result = decode.run(data, decode.at([1], decode.string))
   assert result == Ok(\"two\")
   ```

   ## Records

   Decoding records from dynamic data is more complex and requires combining a
   decoder for each field and a special constructor that builds your records with
   the decoded field values.

   ```gleam
   // Data:
   // {
   //   \"score\" -> 180,
   //   \"name\" -> \"Mel Smith\",
   //   \"is-admin\" -> false,
   //   \"enrolled\" -> true,
   //   \"colour\" -> \"Red\",
   // }

   let decoder = {
   use name <- decode.field(\"name\", decode.string)
   use score <- decode.field(\"score\", decode.int)
   use colour <- decode.field(\"colour\", decode.string)
   use enrolled <- decode.field(\"enrolled\", decode.bool)
   decode.success(Player(name:, score:, colour:, enrolled:))
   }

   let result = decode.run(data, decoder)
   assert result == Ok(Player(\"Mel Smith\", 180, \"Red\", True))
   ```

   ## Enum variants

   Imagine you have a custom type where all the variants do not contain any values.

   ```gleam
   pub type PocketMonsterType {
   Fire
   Water
   Grass
   Electric
   }
   ```

   You might choose to encode these variants as strings, `\"fire\"` for `Fire`,
   `\"water\"` for `Water`, and so on. To decode them you'll need to decode the dynamic
   data as a string, but then you'll need to decode it further still as not all
   strings are valid values for the enum. This can be done with the `then`
   function, which enables running a second decoder after the first one
   succeeds.

   ```gleam
   let decoder = {
   use decoded_string <- decode.then(decode.string)
   case decoded_string {
   // Return succeeding decoders for valid strings
   \"fire\" -> decode.success(Fire)
   \"water\" -> decode.success(Water)
   \"grass\" -> decode.success(Grass)
   \"electric\" -> decode.success(Electric)
   // Return a failing decoder for any other strings
   _ -> decode.failure(Fire, expected: \"PocketMonsterType\")
   }
   }

   let result = decode.run(dynamic.string(\"water\"), decoder)
   assert result == Ok(Water)

   let result = decode.run(dynamic.string(\"wobble\"), decoder)
   assert result == Error([DecodeError(\"PocketMonsterType\", \"String\", [])])
   ```

   ## Record variants

   Decoding type variants that contain other values is done by combining the
   techniques from the \"enum variants\" and \"records\" examples. Imagine you have
   this custom type that you want to decode:

   ```gleam
   pub type PocketMonsterPerson {
   Trainer(name: String, badge_count: Int)
   GymLeader(name: String, speciality: PocketMonsterType)
   }
   ```
   And you would like to be able to decode these from dynamic data like this:
   ```erlang
   {
   \"type\" -> \"trainer\",
   \"name\" -> \"Ash\",
   \"badge-count\" -> 1,
   }
   ```
   ```erlang
   {
   \"type\" -> \"gym-leader\",
   \"name\" -> \"Misty\",
   \"speciality\" -> \"water\",
   }
   ```

   Notice how both documents have a `\"type\"` field, which is used to indicate which
   variant the data is for.

   First, define decoders for each of the variants:

   ```gleam
   let trainer_decoder = {
   use name <- decode.field(\"name\", decode.string)
   use badge_count <- decode.field(\"badge-count\", decode.int)
   decode.success(Trainer(name, badge_count))
   }

   let gym_leader_decoder = {
   use name <- decode.field(\"name\", decode.string)
   use speciality <- decode.field(\"speciality\", pocket_monster_type_decoder)
   decode.success(GymLeader(name, speciality))
   }
   ```

   A third decoder can be used to extract and decode the `\"type\"` field, and the
   expression can evaluate to whichever decoder is suitable for the document.

   ```gleam
   // Data:
   // {
   //   \"type\" -> \"gym-leader\",
   //   \"name\" -> \"Misty\",
   //   \"speciality\" -> \"water\",
   // }

   let decoder = {
   use tag <- decode.field(\"type\", decode.string)
   case tag {
   \"gym-leader\" -> gym_leader_decoder
   _ -> trainer_decoder
   }
   }

   let result = decode.run(data, decoder)
   assert result == Ok(GymLeader(\"Misty\", Water))
   ```"
  (:refer-clojure :exclude [cast float int map])
  (:require
   [gleam-ffi]
   [gleam.dict :as dict]
   [gleam.dynamic :as dynamic]
   [gleam.float :as float]
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.option :as option]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type DecodeError
(defprotocol IDecodeError)
(defrecord DecodeError [^java.lang.String expected ^java.lang.String found path] IDecodeError)
(defn DecodeError? "True if `v` is a DecodeError value." [v] (instance? DecodeError v))
(defn DecodeError-schema
  "Malli schema for DecodeError."
  []
  [:and [:fn DecodeError?] [:map [:expected :string] [:found :string] [:path [:sequential :string]]]])

;; type Decoder
(defprotocol IDecoder)
(defrecord Decoder [function] IDecoder)
(alter-meta! #'->Decoder assoc :private true)
(alter-meta! #'map->Decoder assoc :private true)
(defn Decoder? "True if `v` is a Decoder value." [v] (instance? Decoder v))
(defn Decoder-schema
  "Malli schema for Decoder(t)."
  [t]
  [:and [:fn Decoder?] [:map [:function [:=> [:cat (dynamic/Dynamic-schema)] [:tuple t [:sequential (DecodeError-schema)]]]]]])

(defn- decode-dynamic
  "decode_dynamic(data: Dynamic) -> #(Dynamic, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:757"}
  [data]
  [data (list)])

(def dynamic (->Decoder decode-dynamic))

(defn run
  "run(data: Dynamic, decoder: Decoder(a)) -> Result(a, List(DecodeError))

   Run a decoder on a `Dynamic` value, decoding the value if it is of the
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
  {:malli/schema [:=> [:cat (dynamic/Dynamic-schema) (Decoder-schema :any)]
                      (p/result-of :any [:sequential (DecodeError-schema)])]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:371"}
  [data decoder]
  (let [[maybe-invalid-data errors] ((:function decoder) data)]
    (if (empty? errors) (p/->Ok maybe-invalid-data) (p/->Error errors))))

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:744"} dynamic-float gleam-ffi/dynamic-float)

(defn- run-dynamic-function
  "run_dynamic_function(data: Dynamic, name: String, f: fn(Dynamic) -> Result(a, a)) -> #(a, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:632"}
  [data ^java.lang.String name f]
  (let [subject (f data)]
    (if (instance? Ok subject)
      (let [data (:value subject)]
        [data (list)])
      (let [placeholder (:value subject)]
        [placeholder (list (->DecodeError name (dynamic/classify data) (list)))]))))

(defn- decode-float
  "decode_float(data: Dynamic) -> #(Float, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:738"}
  [data]
  (run-dynamic-function data "Float" dynamic-float))

(def float (->Decoder decode-float))

(defn map
  "map(decoder: Decoder(a), transformer: fn(a) -> b) -> Decoder(b)

   Apply a transformation function to any value decoded by the decoder.

   ## Examples

   ```gleam
   let decoder = decode.int |> decode.map(int.to_string)
   let result = decode.run(dynamic.int(1000), decoder)
   assert result == Ok(\"1000\")
   ```"
  {:malli/schema [:=> [:cat (Decoder-schema :any) [:=> [:cat :any] :any]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:917"}
  [decoder transformer]
  (->Decoder (fn [d]
               (let [[data errors] ((:function decoder) d)]
                 [(transformer data) errors]))))

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:718"} dynamic-int gleam-ffi/dynamic-int)

(defn- decode-int
  "decode_int(data: Dynamic) -> #(Int, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:712"}
  [data]
  (run-dynamic-function data "Int" dynamic-int))

(def int (->Decoder decode-int))

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:778"} dynamic-bit-array gleam-ffi/dynamic-bit-array)

(defn- decode-bit-array
  "decode_bit_array(data: Dynamic) -> #(BitArray, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:772"}
  [data]
  (run-dynamic-function data "BitArray" dynamic-bit-array))

(def bit-array (->Decoder decode-bit-array))

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:661"} dynamic-string gleam-ffi/dynamic-string)

(defn- decode-string
  "decode_string(data: Dynamic) -> #(String, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:656"}
  [data]
  (run-dynamic-function data "String" dynamic-string))

(def string (->Decoder decode-string))

(defn- run-decoders
  "run_decoders(data: Dynamic, failure: #(a, List(DecodeError)), decoders: List(Decoder(a))) -> #(a, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:1007"}
  [data failure decoders]
  (if (empty? decoders)
    failure
    (let [decoder (first decoders) decoders (rest decoders) [_ errors :as layer] ((:function decoder) data)]
      (if (empty? errors) layer (recur data failure decoders)))))

(defn one-of
  "one_of(first: Decoder(a), or alternatives: List(Decoder(a))) -> Decoder(a)

   Create a new decoder from several other decoders. Each of the inner
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
  {:malli/schema [:=> [:cat (Decoder-schema :any) [:sequential (Decoder-schema :any)]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:994"}
  [first' alternatives]
  (->Decoder (fn [dynamic-data]
               (let [[_ errors :as layer] ((:function first') dynamic-data)]
                 (if (empty? errors)
                   layer
                   (run-decoders dynamic-data layer alternatives))))))

(defn- path-segment-to-string
  "path_segment_to_string(key: Dynamic) -> String"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:468"}
  ^java.lang.String [key]
  (let [decoder (one-of string
                        (list (-> int (map int/to-string)) (-> float (map float/to-string)))) subject (run key decoder)]
    (if (instance? Ok subject)
      (let [key (:value subject)]
        key)
      (str "<" (dynamic/classify key) ">"))))

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:1127"} cast gleam-ffi/identity1)

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:802"} decode-list gleam-ffi/decode-list)

(declare push-path list')

(defn- push-path
  "push_path(layer: #(a, List(DecodeError)), path: List(b)) -> #(a, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:456"}
  [layer path]
  (let [path (list/map path (fn [key] (-> key cast path-segment-to-string)))
        errors (list/map (nth layer 1)
                         (fn [error]
                           (->DecodeError (:expected error) (:found error) (list/append path (:path error)))))]
    [(nth layer 0) errors]))

(defn list'
  "list(of inner: Decoder(a)) -> Decoder(List(a))

   A decoder that decodes lists where all elements are decoded with a given
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
  {:malli/schema [:=> [:cat (Decoder-schema :any)]
                      (Decoder-schema [:sequential :any])]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:794"}
  [inner]
  (->Decoder (fn [data]
               (decode-list data
                            (:function inner)
                            (fn [p k] (push-path p (list k)))
                            0
                            (list)))))

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:454"} bare-index gleam-ffi/bare-index)

(defn- index
  "index(path: List(a), position: List(a), inner: fn(Dynamic) -> #(b, List(DecodeError)), data: Dynamic, handle_miss: fn(Dynamic, List(a)) -> #(b, List(DecodeError))) -> #(b, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:420"}
  [path position inner data handle-miss]
  (if (empty? path)
    (-> data inner (push-path (list/reverse position)))
    (let [key (first path) path (rest path) subject (bare-index data key)]
      (cond
        (and (instance? Ok subject) (instance? gleam.option.Some (:value subject)))
        (let [data (:value (:value subject))]
          (recur path (list* key position) inner data handle-miss))

        (and (instance? Ok subject) (instance? gleam.option.None (:value subject)))
        (handle-miss data (list* key position))

        (instance? gleam.prelude.Error subject)
        (let [kind (:value subject) [default _] (inner data)]
          (-> [default (list (->DecodeError kind (dynamic/classify data) (list)))]
              (push-path (list/reverse position))))))))

(defn subfield
  "subfield(field_path: List(a), field_decoder: Decoder(b), next: fn(b) -> Decoder(c)) -> Decoder(c)

   The same as [`field`](#field), except taking a path to the value rather
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
  {:malli/schema [:=> [:cat [:sequential :any] (Decoder-schema :any) [:=> [:cat :any] (Decoder-schema :any)]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:339"}
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
  "at(path: List(a), inner: Decoder(b)) -> Decoder(b)

   A decoder that decodes a value that is nested within other values. For
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
  {:malli/schema [:=> [:cat [:sequential :any] (Decoder-schema :any)]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:410"}
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
  "success(data: a) -> Decoder(a)

   Finalise a decoder having successfully extracted a value.

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
  {:malli/schema [:=> [:cat :any] (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:501"}
  [data]
  (->Decoder (fn [_] [data (list)])))

(defn decode-error
  "decode_error(expected expected: String, found found: Dynamic) -> List(DecodeError)

   Construct a decode error for some unexpected dynamic data."
  {:malli/schema [:=> [:cat :string (dynamic/Dynamic-schema)]
                      [:sequential (DecodeError-schema)]]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:507"}
  [^java.lang.String expected found]
  (list (->DecodeError expected (dynamic/classify found) (list))))

(defn field
  "field(field_name: a, field_decoder: Decoder(b), next: fn(b) -> Decoder(c)) -> Decoder(c)

   Run a decoder on a field of a `Dynamic` value, decoding the value if it is
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
  {:malli/schema [:=> [:cat :any (Decoder-schema :any) [:=> [:cat :any] (Decoder-schema :any)]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:547"}
  [field-name field-decoder next]
  (subfield (list field-name) field-decoder next))

(defn optional-field
  "optional_field(key: a, default: b, field_decoder: Decoder(b), next: fn(b) -> Decoder(c)) -> Decoder(c)

   Run a decoder on a field of a `Dynamic` value, decoding the value if it is
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
  {:malli/schema [:=> [:cat :any :any (Decoder-schema :any) [:=> [:cat :any] (Decoder-schema :any)]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:581"}
  [key default field-decoder next]
  (->Decoder (fn [data]
               (let [[out errors1] (-> (let [subject (bare-index data key)]
                                         (cond
                                           (and (instance? Ok subject) (instance? gleam.option.Some (:value subject)))
                                           (let [data (:value (:value subject))]
                                             ((:function field-decoder) data))

                                           (and (instance? Ok subject) (instance? gleam.option.None (:value subject)))
                                           [default (list)]

                                           (instance? gleam.prelude.Error subject)
                                           (let [kind (:value subject)]
                                             [default (list (->DecodeError kind
                                                                   (dynamic/classify data)
                                                                   (list)))])))
                                       (push-path (list key)))
                     [out errors2] ((:function (next out)) data)]
                 [out (list/append errors1 errors2)]))))

(defn optionally-at
  "optionally_at(path: List(a), default: b, inner: Decoder(b)) -> Decoder(b)

   A decoder that decodes a value that is nested within other values. For
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
  {:malli/schema [:=> [:cat [:sequential :any] :any (Decoder-schema :any)]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:622"}
  [path default inner]
  (->Decoder (fn [data]
               (index path
                      (list)
                      (:function inner)
                      data
                      (fn [_ _] [default (list)])))))

(defn- decode-bool
  "decode_bool(data: Dynamic) -> #(Bool, List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:683"}
  [data]
  (let [subject (= (cast true) data)]
    (if subject
      [true (list)]
      (let [subject (= (cast false) data)]
        (if subject [false (list)] [false (decode-error "Bool" data)])))))

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:874"} decode-dict gleam-ffi/decode-dict)

(declare fold-dict dict)

(defn- fold-dict
  "fold_dict(acc: #(Dict(a, b), List(DecodeError)), key: Dynamic, value: Dynamic, key_decoder: fn(Dynamic) -> #(a, List(DecodeError)), value_decoder: fn(Dynamic) -> #(b, List(DecodeError))) -> #(Dict(a, b), List(DecodeError))"
  {:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:846"}
  [acc key value key-decoder value-decoder]
  (let [subject (key-decoder key)]
    (if (empty? (nth subject 1))
      (let [key-decoded (nth subject 0) subject (value-decoder value)]
        (if (empty? (nth subject 1))
          (let [value (nth subject 0) dict (dict/insert (nth acc 0) key-decoded value)]
            [dict (nth acc 1)])
          (let [errors (nth subject 1) key-identifier (path-segment-to-string key)]
            (push-path [(dict/new*) errors] (list key-identifier)))))
      (let [errors (nth subject 1)]
        (push-path [(dict/new*) errors] (list "keys"))))))

(defn dict
  "dict(key: Decoder(a), value: Decoder(b)) -> Decoder(Dict(a, b))

   A decoder that decodes dicts where all keys and values are decoded with
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
  {:malli/schema [:=> [:cat (Decoder-schema :any) (Decoder-schema :any)]
                      (Decoder-schema [:map-of :any :any])]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:826"}
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

(def ^{:gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:1131"} is-null gleam-ffi/is-null)

(defn optional
  "optional(inner: Decoder(a)) -> Decoder(Option(a))

   A decoder that decodes nullable values of a type decoded by with a given
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
  {:malli/schema [:=> [:cat (Decoder-schema :any)]
                      (Decoder-schema (option/Option-schema :any))]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:895"}
  [inner]
  (->Decoder (fn [data]
               (let [subject (is-null data)]
                 (if subject
                   [(option/->None) (list)]
                   (let [[data errors] ((:function inner) data)]
                     [(option/->Some data) errors]))))))

(defn map-errors
  "map_errors(decoder: Decoder(a), transformer: fn(List(DecodeError)) -> List(DecodeError)) -> Decoder(a)

   Apply a transformation function to any errors returned by the decoder."
  {:malli/schema [:=> [:cat (Decoder-schema :any) [:=> [:cat [:sequential (DecodeError-schema)]] [:sequential (DecodeError-schema)]]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:926"}
  [decoder transformer]
  (->Decoder (fn [d]
               (let [[data errors] ((:function decoder) d)]
                 [data (transformer errors)]))))

(defn collapse-errors
  "collapse_errors(decoder: Decoder(a), name: String) -> Decoder(a)

   Replace all errors produced by a decoder with one single error for a named
   expected type.

   This function may be useful if you wish to simplify errors before
   presenting them to a user, particularly when using the `one_of` function.

   ## Examples

   ```gleam
   let decoder = decode.string |> decode.collapse_errors(\"MyThing\")
   let result = decode.run(dynamic.int(1000), decoder)
   assert result == Error([DecodeError(\"MyThing\", \"Int\", [])])
   ```"
  {:malli/schema [:=> [:cat (Decoder-schema :any) :string]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:950"}
  [decoder ^java.lang.String name]
  (->Decoder (fn [dynamic-data]
               (let [[data errors :as layer] ((:function decoder) dynamic-data)]
                 (if (empty? errors)
                   layer
                   [data (decode-error name dynamic-data)])))))

(defn then
  "then(decoder: Decoder(a), next: fn(a) -> Decoder(b)) -> Decoder(b)

   Create a new decoder based upon the value of a previous decoder.

   This may be useful to run one previous decoder to use in further decoding."
  {:malli/schema [:=> [:cat (Decoder-schema :any) [:=> [:cat :any] (Decoder-schema :any)]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:964"}
  [decoder next]
  (->Decoder (fn [dynamic-data]
               (let [[data errors] ((:function decoder) dynamic-data)
                     decoder (next data)
                     [data _ :as layer] ((:function decoder) dynamic-data)]
                 (if (empty? errors) layer [data errors])))))

(defn failure
  "failure(placeholder: a, expected name: String) -> Decoder(a)

   Define a decoder that always fails.

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
  {:malli/schema [:=> [:cat :any :string] (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:1040"}
  [placeholder ^java.lang.String name]
  (->Decoder (fn [d] [placeholder (decode-error name d)])))

(defn new-primitive-decoder
  "new_primitive_decoder(name: String, decoding_function: fn(Dynamic) -> Result(a, a)) -> Decoder(a)

   Create a decoder for a new data type from a decoding function.

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
  {:malli/schema [:=> [:cat :string [:=> [:cat (dynamic/Dynamic-schema)] (p/result-of :any :any)]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:1081"}
  [^java.lang.String name decoding-function]
  (->Decoder (fn [d]
               (let [subject (decoding-function d)]
                 (if (instance? Ok subject)
                   (let [t (:value subject)]
                     [t (list)])
                   (let [placeholder (:value subject)]
                     [placeholder (list (->DecodeError name (dynamic/classify d) (list)))]))))))

(defn recursive
  "recursive(inner: fn() -> Decoder(a)) -> Decoder(a)

   Create a decoder that can refer to itself, useful for decoding deeply
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
  {:malli/schema [:=> [:cat [:=> [:cat] (Decoder-schema :any)]]
                      (Decoder-schema :any)]
   :gleam/src "stdlib-src/src/gleam/dynamic/decode.gleam:1118"}
  [inner]
  (->Decoder (fn [data]
               (let [decoder (inner)]
                 ((:function decoder) data)))))

(def bool (->Decoder decode-bool))
