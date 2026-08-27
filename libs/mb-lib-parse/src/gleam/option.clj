(ns gleam.option
  (:refer-clojure :exclude [flatten map or reverse])
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Option
(defprotocol IOption)
(defrecord Some [value] IOption)
(defn Some? "True if `v` is a Some value." [v] (instance? Some v))
(defrecord None [] IOption)
(defn None? "True if `v` is a None value." [v] (instance? None v))
(defn Option? "True if `v` is any Option value." [v] (instance? gleam.option.IOption v))
(defn Option-schema
  "Malli schema for Option(a)."
  [a]
  [:or
   [:and [:fn Some?] [:map [:value a]]]
   [:fn None?]])

(defn- reverse-and-prepend
  "reverse_and_prepend(list prefix: List(a), to suffix: List(a)) -> List(a)"
  {:gleam/src "stdlib-src/src/gleam/option.gleam:57"}
  [prefix suffix]
  (if (empty? prefix)
    suffix
    (let [first' (first prefix) rest' (rest prefix)]
      (recur rest' (list* first' suffix)))))

(defn- reverse
  "reverse(list: List(a)) -> List(a)"
  {:gleam/src "stdlib-src/src/gleam/option.gleam:53"}
  [list']
  (reverse-and-prepend list' (list)))

(defn- all-loop
  "all_loop(list: List(Option(a)), acc: List(a)) -> Option(List(a))"
  {:gleam/src "stdlib-src/src/gleam/option.gleam:42"}
  [list' acc]
  (cond
    (empty? list')
    (->Some (reverse acc))

    (and (seq list') (instance? None (first list')))
    (->None)

    (and (seq list') (instance? Some (first list')))
    (let [first' (:value (first list')) rest' (rest list')]
      (recur rest' (list* first' acc)))))

(defn all
  "all(list: List(Option(a))) -> Option(List(a))

   Combines a list of `Option`s into a single `Option`.
   If all elements in the list are `Some` then returns a `Some` holding the list of values.
   If any element is `None` then returns `None`.

   ## Examples

   ```gleam
   assert option.all([Some(1), Some(2)]) == Some([1, 2])
   ```

   ```gleam
   assert option.all([Some(1), None]) == None
   ```"
  {:malli/schema [:=> [:cat [:sequential (Option-schema :any)]]
                      (Option-schema [:sequential :any])]
   :gleam/src "stdlib-src/src/gleam/option.gleam:38"}
  [list']
  (all-loop list' (list)))

(defn is-some
  "is_some(option: Option(a)) -> Bool

   Checks whether the `Option` is a `Some` value.

   ## Examples

   ```gleam
   assert option.is_some(Some(1))
   ```

   ```gleam
   assert !option.is_some(None)
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any)] :boolean]
   :gleam/src "stdlib-src/src/gleam/option.gleam:76"}
  [option]
  (not= option (->None)))

(defn is-none
  "is_none(option: Option(a)) -> Bool

   Checks whether the `Option` is a `None` value.

   ## Examples

   ```gleam
   assert !option.is_none(Some(1))
   ```

   ```gleam
   assert option.is_none(None)
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any)] :boolean]
   :gleam/src "stdlib-src/src/gleam/option.gleam:92"}
  [option]
  (= option (->None)))

(defn to-result
  "to_result(option: Option(a), e: b) -> Result(a, b)

   Converts an `Option` type to a `Result` type.

   ## Examples

   ```gleam
   assert option.to_result(Some(1), \"some_error\") == Ok(1)
   ```

   ```gleam
   assert option.to_result(None, \"some_error\") == Error(\"some_error\")
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any) :any]
                      (p/result-of :any :any)]
   :gleam/src "stdlib-src/src/gleam/option.gleam:108"}
  [option e]
  (if (instance? Some option)
    (let [a (:value option)]
      (p/->Ok a))
    (p/->Error e)))

(defn from-result
  "from_result(result: Result(a, b)) -> Option(a)

   Converts a `Result` type to an `Option` type.

   ## Examples

   ```gleam
   assert option.from_result(Ok(1)) == Some(1)
   ```

   ```gleam
   assert option.from_result(Error(\"some_error\")) == None
   ```"
  {:malli/schema [:=> [:cat (p/result-of :any :any)] (Option-schema :any)]
   :gleam/src "stdlib-src/src/gleam/option.gleam:127"}
  [result]
  (if (instance? Ok result)
    (let [a (:value result)]
      (->Some a))
    (->None)))

(defn unwrap
  "unwrap(option: Option(a), or default: a) -> a

   Extracts the value from an `Option`, returning a default value if there is none.

   ## Examples

   ```gleam
   assert option.unwrap(Some(1), 0) == 1
   ```

   ```gleam
   assert option.unwrap(None, 0) == 0
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any) :any] :any]
   :gleam/src "stdlib-src/src/gleam/option.gleam:146"}
  [option default]
  (if (instance? Some option)
    (let [x (:value option)]
      x)
    default))

(defn lazy-unwrap
  "lazy_unwrap(option: Option(a), or default: fn() -> a) -> a

   Extracts the value from an `Option`, evaluating the default function if the option is `None`.

   ## Examples

   ```gleam
   assert option.lazy_unwrap(Some(1), fn() { 0 }) == 1
   ```

   ```gleam
   assert option.lazy_unwrap(None, fn() { 0 }) == 0
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any) [:=> [:cat] :any]] :any]
   :gleam/src "stdlib-src/src/gleam/option.gleam:165"}
  [option default]
  (if (instance? Some option)
    (let [x (:value option)]
      x)
    (default)))

(defn map
  "map(over option: Option(a), with fun: fn(a) -> b) -> Option(b)

   Updates a value held within the `Some` of an `Option` by calling a given function
   on it.

   If the `Option` is a `None` rather than `Some`, the function is not called and the
   `Option` stays the same.

   ## Examples

   ```gleam
   assert option.map(over: Some(1), with: fn(x) { x + 1 }) == Some(2)
   ```

   ```gleam
   assert option.map(over: None, with: fn(x) { x + 1 }) == None
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any) [:=> [:cat :any] :any]]
                      (Option-schema :any)]
   :gleam/src "stdlib-src/src/gleam/option.gleam:188"}
  [option fun]
  (if (instance? Some option)
    (let [x (:value option)]
      (->Some (fun x)))
    (->None)))

(defn flatten
  "flatten(option: Option(Option(a))) -> Option(a)

   Merges a nested `Option` into a single layer.

   ## Examples

   ```gleam
   assert option.flatten(Some(Some(1))) == Some(1)
   ```

   ```gleam
   assert option.flatten(Some(None)) == None
   ```

   ```gleam
   assert option.flatten(None) == None
   ```"
  {:malli/schema [:=> [:cat (Option-schema (Option-schema :any))]
                      (Option-schema :any)]
   :gleam/src "stdlib-src/src/gleam/option.gleam:211"}
  [option]
  (if (instance? Some option)
    (let [x (:value option)]
      x)
    (->None)))

(defn then
  "then(option: Option(a), apply fun: fn(a) -> Option(b)) -> Option(b)

   Updates a value held within the `Some` of an `Option` by calling a given function
   on it, where the given function also returns an `Option`. The two options are
   then merged together into one `Option`.

   If the `Option` is a `None` rather than `Some` the function is not called and the
   option stays the same.

   This function is the equivalent of calling `map` followed by `flatten`, and
   it is useful for chaining together multiple functions that return `Option`.

   ## Examples

   ```gleam
   assert option.then(Some(1), fn(x) { Some(x + 1) }) == Some(2)
   ```

   ```gleam
   assert option.then(Some(1), fn(x) { Some(#(\"a\", x)) }) == Some(#(\"a\", 1))
   ```

   ```gleam
   assert option.then(Some(1), fn(_) { None }) == None
   ```

   ```gleam
   assert option.then(None, fn(x) { Some(x + 1) }) == None
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any) [:=> [:cat :any] (Option-schema :any)]]
                      (Option-schema :any)]
   :gleam/src "stdlib-src/src/gleam/option.gleam:246"}
  [option fun]
  (if (instance? Some option)
    (let [x (:value option)]
      (fun x))
    (->None)))

(defn or
  "or(first: Option(a), second: Option(a)) -> Option(a)

   Returns the first value if it is `Some`, otherwise returns the second value.

   ## Examples

   ```gleam
   assert option.or(Some(1), Some(2)) == Some(1)
   ```

   ```gleam
   assert option.or(Some(1), None) == Some(1)
   ```

   ```gleam
   assert option.or(None, Some(2)) == Some(2)
   ```

   ```gleam
   assert option.or(None, None) == None
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any) (Option-schema :any)]
                      (Option-schema :any)]
   :gleam/src "stdlib-src/src/gleam/option.gleam:273"}
  [first' second]
  (if (instance? Some first') first' second))

(defn lazy-or
  "lazy_or(first: Option(a), second: fn() -> Option(a)) -> Option(a)

   Returns the first value if it is `Some`, otherwise evaluates the given function for a fallback value.

   ## Examples

   ```gleam
   assert option.lazy_or(Some(1), fn() { Some(2) }) == Some(1)
   ```

   ```gleam
   assert option.lazy_or(Some(1), fn() { None }) == Some(1)
   ```

   ```gleam
   assert option.lazy_or(None, fn() { Some(2) }) == Some(2)
   ```

   ```gleam
   assert option.lazy_or(None, fn() { None }) == None
   ```"
  {:malli/schema [:=> [:cat (Option-schema :any) [:=> [:cat] (Option-schema :any)]]
                      (Option-schema :any)]
   :gleam/src "stdlib-src/src/gleam/option.gleam:300"}
  [first' second]
  (if (instance? Some first') first' (second)))

(defn- values-loop
  "values_loop(list: List(Option(a)), acc: List(a)) -> List(a)"
  {:gleam/src "stdlib-src/src/gleam/option.gleam:320"}
  [list' acc]
  (cond
    (empty? list')
    (reverse acc)

    (and (seq list') (instance? None (first list')))
    (let [rest' (rest list')]
      (recur rest' acc))

    (and (seq list') (instance? Some (first list')))
    (let [first' (:value (first list')) rest' (rest list')]
      (recur rest' (list* first' acc)))))

(defn values
  "values(options: List(Option(a))) -> List(a)

   Given a list of `Option`s,
   returns only the values inside `Some`.

   ## Examples

   ```gleam
   assert option.values([Some(1), None, Some(3)]) == [1, 3]
   ```"
  {:malli/schema [:=> [:cat [:sequential (Option-schema :any)]]
                      [:sequential :any]]
   :gleam/src "stdlib-src/src/gleam/option.gleam:316"}
  [options]
  (values-loop options (list)))
