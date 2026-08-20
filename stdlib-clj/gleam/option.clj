(ns gleam.option
  (:refer-clojure :exclude [flatten map or reverse])
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Option
(defrecord Some [value])
(defrecord None [])

(defn- reverse-and-prepend [prefix suffix]
  (if (empty? prefix)
    suffix
    (let [first' (first prefix) rest' (rest prefix)]
      (recur rest' (list* first' suffix)))))

(defn- reverse [list']
  (reverse-and-prepend list' (list)))

(defn- all-loop [list' acc]
  (cond
    (empty? list') (->Some (reverse acc))
    (and (seq list') (instance? None (first list'))) (->None)
    (and (seq list') (instance? Some (first list'))) (let [first' (:value (first list')) rest' (rest list')]
                                                       (recur rest' (list* first' acc)))))

(defn all
  "Combines a list of `Option`s into a single `Option`.
  If all elements in the list are `Some` then returns a `Some` holding the list of values.
  If any element is `None` then returns `None`.
  
  ## Examples
  
  ```gleam
  assert option.all([Some(1), Some(2)]) == Some([1, 2])
  ```
  
  ```gleam
  assert option.all([Some(1), None]) == None
  ```"
  {:malli/schema [:=> [:cat [:sequential [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]}
  [list']
  (all-loop list' (list)))

(defn is-some
  "Checks whether the `Option` is a `Some` value.
  
  ## Examples
  
  ```gleam
  assert option.is_some(Some(1))
  ```
  
  ```gleam
  assert !option.is_some(None)
  ```"
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]] :boolean]}
  [option]
  (not= option (->None)))

(defn is-none
  "Checks whether the `Option` is a `None` value.
  
  ## Examples
  
  ```gleam
  assert !option.is_none(Some(1))
  ```
  
  ```gleam
  assert option.is_none(None)
  ```"
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]] :boolean]}
  [option]
  (= option (->None)))

(defn to-result
  "Converts an `Option` type to a `Result` type.
  
  ## Examples
  
  ```gleam
  assert option.to_result(Some(1), \"some_error\") == Ok(1)
  ```
  
  ```gleam
  assert option.to_result(None, \"some_error\") == Error(\"some_error\")
  ```"
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]] :any] [:or [:fn (partial instance? gleam.prelude.Ok)] [:fn (partial instance? gleam.prelude.Error)]]]}
  [option e]
  (if (instance? Some option)
    (let [a (:value option)]
      (p/->Ok a))
    (p/->Error e)))

(defn from-result
  "Converts a `Result` type to an `Option` type.
  
  ## Examples
  
  ```gleam
  assert option.from_result(Ok(1)) == Some(1)
  ```
  
  ```gleam
  assert option.from_result(Error(\"some_error\")) == None
  ```"
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.prelude.Ok)] [:fn (partial instance? gleam.prelude.Error)]]] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]}
  [result]
  (if (instance? Ok result)
    (let [a (:value result)]
      (->Some a))
    (->None)))

(defn unwrap
  "Extracts the value from an `Option`, returning a default value if there is none.
  
  ## Examples
  
  ```gleam
  assert option.unwrap(Some(1), 0) == 1
  ```
  
  ```gleam
  assert option.unwrap(None, 0) == 0
  ```"
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]] :any] :any]}
  [option default]
  (if (instance? Some option)
    (let [x (:value option)]
      x)
    default))

(defn lazy-unwrap
  "Extracts the value from an `Option`, evaluating the default function if the option is `None`.
  
  ## Examples
  
  ```gleam
  assert option.lazy_unwrap(Some(1), fn() { 0 }) == 1
  ```
  
  ```gleam
  assert option.lazy_unwrap(None, fn() { 0 }) == 0
  ```"
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]] [:=> [:cat] :any]] :any]}
  [option default]
  (if (instance? Some option)
    (let [x (:value option)]
      x)
    (default)))

(defn map
  "Updates a value held within the `Some` of an `Option` by calling a given function
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
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]] [:=> [:cat :any] :any]] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]}
  [option fun]
  (if (instance? Some option)
    (let [x (:value option)]
      (->Some (fun x)))
    (->None)))

(defn flatten
  "Merges a nested `Option` into a single layer.
  
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
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]}
  [option]
  (if (instance? Some option)
    (let [x (:value option)]
      x)
    (->None)))

(defn then
  "Updates a value held within the `Some` of an `Option` by calling a given function
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
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]] [:=> [:cat :any] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]}
  [option fun]
  (if (instance? Some option)
    (let [x (:value option)]
      (fun x))
    (->None)))

(defn or
  "Returns the first value if it is `Some`, otherwise returns the second value.
  
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
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]}
  [first' second]
  (if (instance? Some first') first' second))

(defn lazy-or
  "Returns the first value if it is `Some`, otherwise evaluates the given function for a fallback value.
  
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
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]] [:=> [:cat] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]] [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]}
  [first' second]
  (if (instance? Some first') first' (second)))

(defn- values-loop [list' acc]
  (cond
    (empty? list') (reverse acc)
    (and (seq list') (instance? None (first list'))) (let [rest' (rest list')]
                                                       (recur rest' acc))
    (and (seq list') (instance? Some (first list'))) (let [first' (:value (first list')) rest' (rest list')]
                                                       (recur rest' (list* first' acc)))))

(defn values
  "Given a list of `Option`s,
  returns only the values inside `Some`.
  
  ## Examples
  
  ```gleam
  assert option.values([Some(1), None, Some(3)]) == [1, 3]
  ```"
  {:malli/schema [:=> [:cat [:sequential [:or [:fn (partial instance? gleam.option.Some)] [:fn (partial instance? gleam.option.None)]]]] [:sequential :any]]}
  [options]
  (values-loop options (list)))
