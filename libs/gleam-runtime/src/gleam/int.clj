(ns gleam.int
  "Functions for working with integers.
   
   ## Division by zero
   
   In Erlang division by zero results in a crash, however Gleam does not have
   partial functions and operators in core so instead division by zero returns
   zero, a behaviour taken from Pony, Coq, and Lean.
   
   This may seem unexpected at first, but it is no less mathematically valid
   than crashing or returning a special value. Division by zero is undefined
   in mathematics."
  (:refer-clojure :exclude [compare range])
  (:require
   [gleam-ffi]
   [gleam.float :as float]
   [gleam.order :as order]
   [gleam.prelude :as p]))

(defn absolute-value
  "absolute_value(x: Int) -> Int

   Returns the absolute value of the input.

   ## Examples

   ```gleam
   assert int.absolute_value(-12) == 12
   ```

   ```gleam
   assert int.absolute_value(10) == 10
   ```"
  {:malli/schema [:=> [:cat :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:28"}
  [x]
  (let [subject (>= x 0)]
    (if subject x (*' x -1))))

(def ^{:malli/schema [:=> [:cat :int] :double] :gleam/src "stdlib-src/src/gleam/int.gleam:254"} to-float gleam-ffi/int-to-float)

(defn power
  "power(base: Int, of exponent: Float) -> Result(Float, Nil)

   Returns the result of the base being raised to the power of the
   exponent, as a `Float`.

   ## Examples

   ```gleam
   assert int.power(2, -1.0) == Ok(0.5)
   ```

   ```gleam
   assert int.power(2, 2.0) == Ok(4.0)
   ```

   ```gleam
   assert int.power(8, 1.5) == Ok(22.627416997969522)
   ```

   ```gleam
   assert 4 |> int.power(of: 2.0) == Ok(16.0)
   ```

   ```gleam
   assert int.power(-1, 0.5) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :int :double] (p/result-of :double :nil)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:60"}
  [base ^double exponent]
  (-> base to-float (float/power exponent)))

(defn square-root
  "square_root(x: Int) -> Result(Float, Nil)

   Returns the square root of the input as a `Float`.

   ## Examples

   ```gleam
   assert int.square_root(4) == Ok(2.0)
   ```

   ```gleam
   assert int.square_root(-16) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :int] (p/result-of :double :nil)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:78"}
  [x]
  (-> x to-float float/square-root))

(def ^{:malli/schema [:=> [:cat :string] (p/result-of :int :nil)] :gleam/src "stdlib-src/src/gleam/int.gleam:98"} parse gleam-ffi/int-parse)

(def ^{:gleam/src "stdlib-src/src/gleam/int.gleam:137"} do-base-parse gleam-ffi/int-base-parse)

(defn base-parse
  "base_parse(string: String, base: Int) -> Result(Int, Nil)

   Parses a given string as an int in a given base, returning an error if the
   input was not a valid number for the given base.

   Supports only bases 2 to 36, for values outside of which this function
   returns an `Error(Nil)`.

   ## Examples

   ```gleam
   assert int.base_parse(\"10\", 2) == Ok(2)
   ```

   ```gleam
   assert int.base_parse(\"30\", 16) == Ok(48)
   ```

   ```gleam
   assert int.base_parse(\"1C\", 36) == Ok(48)
   ```

   ```gleam
   assert int.base_parse(\"48\", 1) == Error(Nil)
   ```

   ```gleam
   assert int.base_parse(\"48\", 37) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :string :int] (p/result-of :int :nil)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:128"}
  [^java.lang.String string base]
  (let [subject (and (>= base 2) (<= base 36))]
    (if subject (do-base-parse string base) (p/->Error nil))))

(def ^{:malli/schema [:=> [:cat :int] :string] :gleam/src "stdlib-src/src/gleam/int.gleam:149"} to-string gleam-ffi/int-to-string)

(def ^{:gleam/src "stdlib-src/src/gleam/int.gleam:186"} do-to-base-string gleam-ffi/int-to-base-string)

(defn to-base-string
  "to_base_string(x: Int, base: Int) -> Result(String, Nil)

   Prints a given int to a string using the base number provided.
   Supports only bases 2 to 36, for values outside of which this function returns an `Error(Nil)`.
   For common bases (2, 8, 16, 36), use the `to_baseN` functions.

   ## Examples

   ```gleam
   assert int.to_base_string(2, 2) == Ok(\"10\")
   ```

   ```gleam
   assert int.to_base_string(48, 16) == Ok(\"30\")
   ```

   ```gleam
   assert int.to_base_string(48, 36) == Ok(\"1C\")
   ```

   ```gleam
   assert int.to_base_string(48, 1) == Error(Nil)
   ```

   ```gleam
   assert int.to_base_string(48, 37) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :int :int] (p/result-of :string :nil)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:177"}
  [x base]
  (let [subject (and (>= base 2) (<= base 36))]
    (if subject (p/->Ok (do-to-base-string x base)) (p/->Error nil))))

(defn to-base2
  "to_base2(x: Int) -> String

   Prints a given int to a string using base-2.

   ## Examples

   ```gleam
   assert int.to_base2(2) == \"10\"
   ```"
  {:malli/schema [:=> [:cat :int] :string]
   :gleam/src "stdlib-src/src/gleam/int.gleam:196"}
  ^java.lang.String [x]
  (do-to-base-string x 2))

(defn to-base8
  "to_base8(x: Int) -> String

   Prints a given int to a string using base-8.

   ## Examples

   ```gleam
   assert int.to_base8(15) == \"17\"
   ```"
  {:malli/schema [:=> [:cat :int] :string]
   :gleam/src "stdlib-src/src/gleam/int.gleam:208"}
  ^java.lang.String [x]
  (do-to-base-string x 8))

(defn to-base16
  "to_base16(x: Int) -> String

   Prints a given int to a string using base-16.

   ## Examples

   ```gleam
   assert int.to_base16(48) == \"30\"
   ```"
  {:malli/schema [:=> [:cat :int] :string]
   :gleam/src "stdlib-src/src/gleam/int.gleam:220"}
  ^java.lang.String [x]
  (do-to-base-string x 16))

(defn to-base36
  "to_base36(x: Int) -> String

   Prints a given int to a string using base-36.

   ## Examples

   ```gleam
   assert int.to_base36(48) == \"1C\"
   ```"
  {:malli/schema [:=> [:cat :int] :string]
   :gleam/src "stdlib-src/src/gleam/int.gleam:232"}
  ^java.lang.String [x]
  (do-to-base-string x 36))

(defn max'
  "max(a: Int, b: Int) -> Int

   Compares two ints, returning the larger of the two.

   ## Examples

   ```gleam
   assert int.max(2, 3) == 3
   ```"
  {:malli/schema [:=> [:cat :int :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:329"}
  [a b]
  (let [subject (> a b)]
    (if subject a b)))

(defn min'
  "min(a: Int, b: Int) -> Int

   Compares two ints, returning the smaller of the two.

   ## Examples

   ```gleam
   assert int.min(2, 3) == 2
   ```"
  {:malli/schema [:=> [:cat :int :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:314"}
  [a b]
  (let [subject (< a b)]
    (if subject a b)))

(defn clamp
  "clamp(x: Int, min min_bound: Int, max max_bound: Int) -> Int

   Restricts an int between two bounds.

   Note: If the `min` argument is larger than the `max` argument then they
   will be swapped, so the minimum bound is always lower than the maximum
   bound.

   ## Examples

   ```gleam
   assert int.clamp(40, min: 50, max: 60) == 50
   ```

   ```gleam
   assert int.clamp(40, min: 50, max: 30) == 40
   ```"
  {:malli/schema [:=> [:cat :int :int :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:272"}
  [x min-bound max-bound]
  (let [subject (>= min-bound max-bound)]
    (if subject
      (-> x (min' min-bound) (max' max-bound))
      (-> x (min' max-bound) (max' min-bound)))))

(defn compare
  "compare(a: Int, with b: Int) -> Order

   Compares two ints, returning an order.

   ## Examples

   ```gleam
   assert int.compare(2, 3) == Lt
   ```

   ```gleam
   assert int.compare(4, 3) == Gt
   ```

   ```gleam
   assert int.compare(3, 3) == Eq
   ```"
  {:malli/schema [:=> [:cat :int :int] (order/Order-schema)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:295"}
  [a b]
  (let [subject (= a b)]
    (if subject
      (order/->Eq)
      (let [subject (< a b)]
        (if subject (order/->Lt) (order/->Gt))))))

(defn is-even
  "is_even(x: Int) -> Bool

   Returns whether the value provided is even.

   ## Examples

   ```gleam
   assert int.is_even(2)
   ```

   ```gleam
   assert !int.is_even(3)
   ```"
  {:malli/schema [:=> [:cat :int] :boolean]
   :gleam/src "stdlib-src/src/gleam/int.gleam:348"}
  [x]
  (= (rem x 2) 0))

(defn is-odd
  "is_odd(x: Int) -> Bool

   Returns whether the value provided is odd.

   ## Examples

   ```gleam
   assert int.is_odd(3)
   ```

   ```gleam
   assert !int.is_odd(2)
   ```"
  {:malli/schema [:=> [:cat :int] :boolean]
   :gleam/src "stdlib-src/src/gleam/int.gleam:364"}
  [x]
  (not= (rem x 2) 0))

(defn negate
  "negate(x: Int) -> Int

   Returns the negative of the value provided.

   ## Examples

   ```gleam
   assert int.negate(1) == -1
   ```"
  {:malli/schema [:=> [:cat :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:376"}
  [x]
  (*' -1 x))

(defn- sum-loop
  "sum_loop(numbers: List(Int), initial: Int) -> Int"
  {:gleam/src "stdlib-src/src/gleam/int.gleam:392"}
  [numbers initial]
  (if (seq numbers)
    (let [first' (first numbers) rest' (rest numbers)]
      (recur rest' (+' first' initial)))
    initial))

(defn sum
  "sum(numbers: List(Int)) -> Int

   Sums a list of ints.

   ## Example

   ```gleam
   assert int.sum([1, 2, 3]) == 6
   ```"
  {:malli/schema [:=> [:cat [:sequential :int]] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:388"}
  [numbers]
  (sum-loop numbers 0))

(defn- product-loop
  "product_loop(numbers: List(Int), initial: Int) -> Int"
  {:gleam/src "stdlib-src/src/gleam/int.gleam:411"}
  [numbers initial]
  (if (seq numbers)
    (let [first' (first numbers) rest' (rest numbers)]
      (recur rest' (*' first' initial)))
    initial))

(defn product
  "product(numbers: List(Int)) -> Int

   Multiplies a list of ints and returns the product.

   ## Example

   ```gleam
   assert int.product([2, 3, 4]) == 24
   ```"
  {:malli/schema [:=> [:cat [:sequential :int]] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:407"}
  [numbers]
  (product-loop numbers 1))

(defn random
  "random(max: Int) -> Int

   Generates a random int between zero and the given maximum.

   The lower number is inclusive, the upper number is exclusive.

   ## Examples

   ```gleam
   int.random(10)
   // -> 4
   ```

   ```gleam
   int.random(1)
   // -> 0
   ```

   ```gleam
   int.random(-1)
   // -> -1
   ```"
  {:malli/schema [:=> [:cat :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:439"}
  [max']
  (-> (* (float/random) (to-float max')) float/floor float/round))

(defn divide
  "divide(dividend: Int, by divisor: Int) -> Result(Int, Nil)

   Performs a truncated integer division.

   Returns division of the inputs as a `Result`: If the given divisor equals
   `0`, this function returns an `Error`.

   ## Examples

   ```gleam
   assert int.divide(0, 1) == Ok(0)
   ```

   ```gleam
   assert int.divide(1, 0) == Error(Nil)
   ```

   ```gleam
   assert int.divide(5, 2) == Ok(2)
   ```

   ```gleam
   assert int.divide(-99, 2) == Ok(-49)
   ```"
  {:malli/schema [:=> [:cat :int :int] (p/result-of :int :nil)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:468"}
  [dividend divisor]
  (if (= divisor 0)
    (p/->Error nil)
    (let [divisor divisor]
      (p/->Ok (quot dividend divisor)))))

(defn remainder
  "remainder(dividend: Int, by divisor: Int) -> Result(Int, Nil)

   Computes the remainder of an integer division of inputs as a `Result`.

   Returns division of the inputs as a `Result`: If the given divisor equals
   `0`, this function returns an `Error`.

   Most of the time you will want to use the `%` operator instead of this
   function.

   ## Examples

   ```gleam
   assert int.remainder(3, 2) == Ok(1)
   ```

   ```gleam
   assert int.remainder(1, 0) == Error(Nil)
   ```

   ```gleam
   assert int.remainder(10, -1) == Ok(0)
   ```

   ```gleam
   assert int.remainder(13, by: 3) == Ok(1)
   ```

   ```gleam
   assert int.remainder(-13, by: 3) == Ok(-1)
   ```

   ```gleam
   assert int.remainder(13, by: -3) == Ok(1)
   ```

   ```gleam
   assert int.remainder(-13, by: -3) == Ok(-1)
   ```"
  {:malli/schema [:=> [:cat :int :int] (p/result-of :int :nil)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:513"}
  [dividend divisor]
  (if (= divisor 0)
    (p/->Error nil)
    (let [divisor divisor]
      (p/->Ok (rem dividend divisor)))))

(defn modulo
  "modulo(dividend: Int, by divisor: Int) -> Result(Int, Nil)

   Computes the modulo of an integer division of inputs as a `Result`.

   Returns division of the inputs as a `Result`: If the given divisor equals
   `0`, this function returns an `Error`.

   Note that this is different from `int.remainder` and `%` in that the
   computed value will always have the same sign as the `divisor`.

   ## Examples

   ```gleam
   assert int.modulo(3, 2) == Ok(1)
   ```

   ```gleam
   assert int.modulo(1, 0) == Error(Nil)
   ```

   ```gleam
   assert int.modulo(10, -1) == Ok(0)
   ```

   ```gleam
   assert int.modulo(13, by: 3) == Ok(1)
   ```

   ```gleam
   assert int.modulo(-13, by: 3) == Ok(2)
   ```

   ```gleam
   assert int.modulo(13, by: -3) == Ok(-2)
   ```"
  {:malli/schema [:=> [:cat :int :int] (p/result-of :int :nil)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:554"}
  [dividend divisor]
  (if (= divisor 0)
    (p/->Error nil)
    (let [remainder (rem dividend divisor) subject (< (*' remainder divisor) 0)]
      (if subject (p/->Ok (+' remainder divisor)) (p/->Ok remainder)))))

(defn floor-divide
  "floor_divide(dividend: Int, by divisor: Int) -> Result(Int, Nil)

   Performs a *floored* integer division, which means that the result will
   always be rounded towards negative infinity.

   If you want to perform truncated integer division (rounding towards zero),
   use `int.divide()` or the `/` operator instead.

   Returns division of the inputs as a `Result`: If the given divisor equals
   `0`, this function returns an `Error`.

   ## Examples

   ```gleam
   assert int.floor_divide(1, 0) == Error(Nil)
   ```

   ```gleam
   assert int.floor_divide(5, 2) == Ok(2)
   ```

   ```gleam
   assert int.floor_divide(6, -4) == Ok(-2)
   ```

   ```gleam
   assert int.floor_divide(-99, 2) == Ok(-50)
   ```"
  {:malli/schema [:=> [:cat :int :int] (p/result-of :int :nil)]
   :gleam/src "stdlib-src/src/gleam/int.gleam:594"}
  [dividend divisor]
  (if (= divisor 0)
    (p/->Error nil)
    (let [divisor divisor subject (and (< (*' dividend divisor) 0) (not= (rem dividend divisor) 0))]
      (if subject
        (p/->Ok (-' (quot dividend divisor) 1))
        (p/->Ok (quot dividend divisor))))))

(defn add
  "add(a: Int, b: Int) -> Int

   Adds two integers together.

   It's the function equivalent of the `+` operator.
   This function is useful in higher order functions or pipes.

   ## Examples

   ```gleam
   assert int.add(1, 2) == 3
   ```

   ```gleam
   import gleam/list

   assert list.fold([1, 2, 3], 0, int.add) == 6
   ```

   ```gleam
   assert 3 |> int.add(2) == 5
   ```"
  {:malli/schema [:=> [:cat :int :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:626"}
  [a b]
  (+' a b))

(defn multiply
  "multiply(a: Int, b: Int) -> Int

   Multiplies two integers together.

   It's the function equivalent of the `*` operator.
   This function is useful in higher order functions or pipes.

   ## Examples

   ```gleam
   assert int.multiply(2, 4) == 8
   ```

   ```gleam
   import gleam/list

   assert list.fold([2, 3, 4], 1, int.multiply) == 24
   ```

   ```gleam
   assert 3 |> int.multiply(2) == 6
   ```"
  {:malli/schema [:=> [:cat :int :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:651"}
  [a b]
  (*' a b))

(defn subtract
  "subtract(a: Int, b: Int) -> Int

   Subtracts one int from another.

   It's the function equivalent of the `-` operator.
   This function is useful in higher order functions or pipes.

   ## Examples

   ```gleam
   assert int.subtract(3, 1) == 2
   ```

   ```gleam
   import gleam/list

   assert list.fold([1, 2, 3], 10, int.subtract) == 4
   ```

   ```gleam
   assert 3 |> int.subtract(2) == 1
   ```

   ```gleam
   assert 3 |> int.subtract(2, _) == -1
   ```"
  {:malli/schema [:=> [:cat :int :int] :int]
   :gleam/src "stdlib-src/src/gleam/int.gleam:680"}
  [a b]
  (-' a b))

(def ^{:malli/schema [:=> [:cat :int :int] :int] :gleam/src "stdlib-src/src/gleam/int.gleam:699"} bitwise-and gleam-ffi/bitwise-and)

(def ^{:malli/schema [:=> [:cat :int] :int] :gleam/src "stdlib-src/src/gleam/int.gleam:716"} bitwise-not gleam-ffi/bitwise-not)

(def ^{:malli/schema [:=> [:cat :int :int] :int] :gleam/src "stdlib-src/src/gleam/int.gleam:733"} bitwise-or gleam-ffi/bitwise-or)

(def ^{:malli/schema [:=> [:cat :int :int] :int] :gleam/src "stdlib-src/src/gleam/int.gleam:750"} bitwise-exclusive-or gleam-ffi/bitwise-xor)

(def ^{:malli/schema [:=> [:cat :int :int] :int] :gleam/src "stdlib-src/src/gleam/int.gleam:767"} bitwise-shift-left gleam-ffi/shift-left)

(def ^{:malli/schema [:=> [:cat :int :int] :int] :gleam/src "stdlib-src/src/gleam/int.gleam:784"} bitwise-shift-right gleam-ffi/shift-right)

(defn- range-loop
  "range_loop(current: Int, stop: Int, increment: Int, acc: a, reducer: fn(a, Int) -> a) -> a"
  {:gleam/src "stdlib-src/src/gleam/int.gleam:816"}
  [current stop increment acc reducer]
  (let [subject (= current stop)]
    (if subject
      acc
      (let [acc (reducer acc current)
            current (+' current increment)]
        (recur current stop increment acc reducer)))))

(defn range
  "range(from start: Int, to stop: Int, with acc: a, run reducer: fn(a, Int) -> a) -> a

   Run a function for each int between ints `from` and `to`.

   `from` is inclusive, and `to` is exclusive.

   ## Examples

   ```gleam
   assert int.range(from: 0, to: 3, with: \"\", run: fn(acc, i) {
   acc <> int.to_string(i)
   })
   == \"012\"
   ```

   ```gleam
   assert int.range(from: 1, to: -2, with: [], run: list.prepend) == [-1, 0, 1]
   ```"
  {:malli/schema [:=> [:cat :int :int :any [:=> [:cat :any :int] :any]] :any]
   :gleam/src "stdlib-src/src/gleam/int.gleam:803"}
  [start stop acc reducer]
  (let [increment (let [subject (< start stop)]
                    (if subject 1 -1))]
    (range-loop start stop increment acc reducer)))
