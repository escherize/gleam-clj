(ns gleam.result
  "Result represents the result of something that may succeed or not.
   `Ok` means it was successful, `Error` means it was not successful."
  (:refer-clojure :exclude [flatten map or partition replace])
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn is-ok
  "is_ok(result: Result(a, b)) -> Bool

   Checks whether the result is an `Ok` value.

   ## Examples

   ```gleam
   assert result.is_ok(Ok(1))
   ```

   ```gleam
   assert !result.is_ok(Error(Nil))
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]]] :boolean]
   :gleam/src "stdlib-src/src/gleam/result.gleam:18"}
  [result]
  (if (instance? gleam.prelude.Error result) false true))

(defn is-error
  "is_error(result: Result(a, b)) -> Bool

   Checks whether the result is an `Error` value.

   ## Examples

   ```gleam
   assert !result.is_error(Ok(1))
   ```

   ```gleam
   assert result.is_error(Error(Nil))
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]]] :boolean]
   :gleam/src "stdlib-src/src/gleam/result.gleam:37"}
  [result]
  (if (instance? Ok result) false true))

(defn map
  "map(over result: Result(a, b), with fun: fn(a) -> c) -> Result(c, b)

   Updates a value held within the `Ok` of a result by calling a given function
   on it.

   If the result is an `Error` rather than `Ok` the function is not called and the
   result stays the same.

   ## Examples

   ```gleam
   assert result.map(over: Ok(1), with: fn(x) { x + 1 }) == Ok(2)
   ```

   ```gleam
   assert result.map(over: Error(1), with: fn(x) { x + 1 }) == Error(1)
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] [:=> [:cat :any] :any]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:60"}
  [result fun]
  (if (instance? Ok result)
    (let [x (:value result)]
      (p/->Ok (fun x)))
    (let [e (:value result)]
      (p/->Error e))))

(defn map-error
  "map_error(over result: Result(a, b), with fun: fn(b) -> c) -> Result(a, c)

   Updates a value held within the `Error` of a result by calling a given function
   on it.

   If the result is `Ok` rather than `Error` the function is not called and the
   result stays the same.

   ## Examples

   ```gleam
   assert result.map_error(over: Error(1), with: fn(x) { x + 1 }) == Error(2)
   ```

   ```gleam
   assert result.map_error(over: Ok(1), with: fn(x) { x + 1 }) == Ok(1)
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] [:=> [:cat :any] :any]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:83"}
  [result fun]
  (if (instance? Ok result)
    (let [x (:value result)]
      (p/->Ok x))
    (let [error (:value result)]
      (p/->Error (fun error)))))

(defn flatten
  "flatten(result: Result(Result(a, b), b)) -> Result(a, b)

   Merges a nested `Result` into a single layer.

   ## Examples

   ```gleam
   assert result.flatten(Ok(Ok(1))) == Ok(1)
   ```

   ```gleam
   assert result.flatten(Ok(Error(\"\"))) == Error(\"\")
   ```

   ```gleam
   assert result.flatten(Error(Nil)) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:109"}
  [result]
  (if (instance? Ok result)
    (let [x (:value result)]
      x)
    (let [error (:value result)]
      (p/->Error error))))

(defn try*
  "try(result: Result(a, b), apply fun: fn(a) -> Result(c, b)) -> Result(c, b)

   \"Updates\" an `Ok` result by passing its value to a function that yields a result,
   and returning the yielded result. (This may \"replace\" the `Ok` with an `Error`.)

   If the input is an `Error` rather than an `Ok`, the function is not called and
   the original `Error` is returned.

   This function is the equivalent of calling `map` followed by `flatten`, and
   it is useful for chaining together multiple functions that may fail.

   ## Examples

   ```gleam
   assert result.try(Ok(1), fn(x) { Ok(x + 1) }) == Ok(2)
   ```

   ```gleam
   assert result.try(Ok(1), fn(x) { Ok(#(\"a\", x)) }) == Ok(#(\"a\", 1))
   ```

   ```gleam
   assert result.try(Ok(1), fn(_) { Error(\"Oh no\") }) == Error(\"Oh no\")
   ```

   ```gleam
   assert result.try(Error(Nil), fn(x) { Ok(x + 1) }) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] [:=> [:cat :any] [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:143"}
  [result fun]
  (if (instance? Ok result)
    (let [x (:value result)]
      (fun x))
    (let [e (:value result)]
      (p/->Error e))))

(defn unwrap
  "unwrap(result: Result(a, b), or default: a) -> a

   Extracts the `Ok` value from a result, returning a default value if the result
   is an `Error`.

   ## Examples

   ```gleam
   assert result.unwrap(Ok(1), 0) == 1
   ```

   ```gleam
   assert result.unwrap(Error(\"\"), 0) == 0
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] :any] :any]
   :gleam/src "stdlib-src/src/gleam/result.gleam:166"}
  [result default]
  (if (instance? Ok result)
    (let [v (:value result)]
      v)
    default))

(defn lazy-unwrap
  "lazy_unwrap(result: Result(a, b), or default: fn() -> a) -> a

   Extracts the `Ok` value from a result, evaluating the default function if the result
   is an `Error`.

   ## Examples

   ```gleam
   assert result.lazy_unwrap(Ok(1), fn() { 0 }) == 1
   ```

   ```gleam
   assert result.lazy_unwrap(Error(\"\"), fn() { 0 }) == 0
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] [:=> [:cat] :any]]
                      :any]
   :gleam/src "stdlib-src/src/gleam/result.gleam:186"}
  [result default]
  (if (instance? Ok result)
    (let [v (:value result)]
      v)
    (default)))

(defn unwrap-error
  "unwrap_error(result: Result(a, b), or default: b) -> b

   Extracts the `Error` value from a result, returning a default value if the result
   is an `Ok`.

   ## Examples

   ```gleam
   assert result.unwrap_error(Error(1), 0) == 1
   ```

   ```gleam
   assert result.unwrap_error(Ok(\"\"), 0) == 0
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] :any] :any]
   :gleam/src "stdlib-src/src/gleam/result.gleam:206"}
  [result default]
  (if (instance? Ok result)
    default
    (let [e (:value result)]
      e)))

(defn or
  "or(first: Result(a, b), second: Result(a, b)) -> Result(a, b)

   Returns the first value if it is `Ok`, otherwise returns the second value.

   ## Examples

   ```gleam
   assert result.or(Ok(1), Ok(2)) == Ok(1)
   ```

   ```gleam
   assert result.or(Ok(1), Error(\"Error 2\")) == Ok(1)
   ```

   ```gleam
   assert result.or(Error(\"Error 1\"), Ok(2)) == Ok(2)
   ```

   ```gleam
   assert result.or(Error(\"Error 1\"), Error(\"Error 2\")) == Error(\"Error 2\")
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] [:or [:fn p/Ok?] [:fn p/Error?]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:233"}
  [first' second]
  (if (instance? Ok first') first' second))

(defn lazy-or
  "lazy_or(first: Result(a, b), second: fn() -> Result(a, b)) -> Result(a, b)

   Returns the first value if it is `Ok`, otherwise evaluates the given function for a fallback value.

   If you need access to the initial error value, use `result.try_recover`.

   ## Examples

   ```gleam
   assert result.lazy_or(Ok(1), fn() { Ok(2) }) == Ok(1)
   ```

   ```gleam
   assert result.lazy_or(Ok(1), fn() { Error(\"Error 2\") }) == Ok(1)
   ```

   ```gleam
   assert result.lazy_or(Error(\"Error 1\"), fn() { Ok(2) }) == Ok(2)
   ```

   ```gleam
   assert result.lazy_or(Error(\"Error 1\"), fn() { Error(\"Error 2\") })
   == Error(\"Error 2\")
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] [:=> [:cat] [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:263"}
  [first' second]
  (if (instance? Ok first') first' (second)))

(defn all
  "all(results: List(Result(a, b))) -> Result(List(a), b)

   Combines a list of results into a single result.
   If all elements in the list are `Ok` then returns an `Ok` holding the list of values.
   If any element is `Error` then returns the first error.

   ## Examples

   ```gleam
   assert result.all([Ok(1), Ok(2)]) == Ok([1, 2])
   ```

   ```gleam
   assert result.all([Ok(1), Error(\"e\")]) == Error(\"e\")
   ```"
  {:malli/schema [:=> [:cat [:sequential [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:287"}
  [results]
  (list/try-map results (fn [result] result)))

(defn- partition-loop
  "partition_loop(results: List(Result(a, b)), oks: List(a), errors: List(b)) -> #(List(a), List(b))"
  {:gleam/src "stdlib-src/src/gleam/result.gleam:307"}
  [results oks errors]
  (cond
    (empty? results)
    [oks errors]

    (and (seq results) (instance? Ok (first results)))
    (let [a (:value (first results)) rest' (rest results)]
      (recur rest' (list* a oks) errors))

    (and (seq results) (instance? gleam.prelude.Error (first results)))
    (let [e (:value (first results)) rest' (rest results)]
      (recur rest' oks (list* e errors)))))

(defn partition
  "partition(results: List(Result(a, b))) -> #(List(a), List(b))

   Given a list of results, returns a pair where the first element is a list
   of all the values inside `Ok` and the second element is a list with all the
   values inside `Error`. The values in both lists appear in reverse order with
   respect to their position in the original list of results.

   ## Examples

   ```gleam
   assert result.partition([Ok(1), Error(\"a\"), Error(\"b\"), Ok(2)])
   == #([2, 1], [\"b\", \"a\"])
   ```"
  {:malli/schema [:=> [:cat [:sequential [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:tuple [:sequential :any] [:sequential :any]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:303"}
  [results]
  (partition-loop results (list) (list)))

(defn replace
  "replace(result: Result(a, b), value: c) -> Result(c, b)

   Replace the value within a result

   ## Examples

   ```gleam
   assert result.replace(Ok(1), Nil) == Ok(Nil)
   ```

   ```gleam
   assert result.replace(Error(1), Nil) == Error(1)
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] :any]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:327"}
  [result value]
  (if (instance? Ok result)
    (p/->Ok value)
    (let [error (:value result)]
      (p/->Error error))))

(defn replace-error
  "replace_error(result: Result(a, b), error: c) -> Result(a, c)

   Replace the error within a result

   ## Examples

   ```gleam
   assert result.replace_error(Error(1), Nil) == Error(Nil)
   ```

   ```gleam
   assert result.replace_error(Ok(1), Nil) == Ok(1)
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] :any]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:346"}
  [result error]
  (if (instance? Ok result)
    (let [x (:value result)]
      (p/->Ok x))
    (p/->Error error)))

(defn values
  "values(results: List(Result(a, b))) -> List(a)

   Given a list of results, returns only the values inside `Ok`.

   ## Examples

   ```gleam
   assert result.values([Ok(1), Error(\"a\"), Ok(3)]) == [1, 3]
   ```"
  {:malli/schema [:=> [:cat [:sequential [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:sequential :any]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:361"}
  [results]
  (list/filter-map results (fn [result] result)))

(defn try-recover
  "try_recover(result: Result(a, b), with fun: fn(b) -> Result(a, c)) -> Result(a, c)

   Updates a value held within the `Error` of a result by calling a given function
   on it, where the given function also returns a result. The two results are
   then merged together into one result.

   If the result is an `Ok` rather than `Error` the function is not called and the
   result stays the same.

   This function is useful for chaining together computations that may fail
   and trying to recover from possible errors.

   If you do not need access to the initial error value, use `result.lazy_or`.

   ## Examples

   ```gleam
   assert Ok(1)
   |> result.try_recover(with: fn(_) { Error(\"failed to recover\") })
   == Ok(1)
   ```

   ```gleam
   assert Error(1)
   |> result.try_recover(with: fn(error) { Ok(error + 1) })
   == Ok(2)
   ```

   ```gleam
   assert Error(1)
   |> result.try_recover(with: fn(error) { Error(\"failed to recover\") })
   == Error(\"failed to recover\")
   ```"
  {:malli/schema [:=> [:cat [:or [:fn p/Ok?] [:fn p/Error?]] [:=> [:cat :any] [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]
   :gleam/src "stdlib-src/src/gleam/result.gleam:397"}
  [result fun]
  (if (instance? Ok result)
    (let [value (:value result)]
      (p/->Ok value))
    (let [error (:value result)]
      (fun error))))
