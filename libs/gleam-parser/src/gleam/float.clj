(ns gleam.float
  "Functions for working with floats.
   
   ## Float representation
   
   Floats are represented as 64 bit floating point numbers on both the Erlang
   and JavaScript runtimes. The floating point behaviour is native to their
   respective runtimes, so their exact behaviour will be slightly different on
   the two runtimes.
   
   ### Infinity and NaN
   
   Under the JavaScript runtime, exceeding the maximum (or minimum)
   representable value for a floating point value will result in Infinity (or
   -Infinity). Should you try to divide two infinities you will get NaN as a
   result.
   
   When running on BEAM, exceeding the maximum (or minimum) representable
   value for a floating point value will raise an error.
   
   ## Division by zero
   
   Gleam runs on the Erlang virtual machine, which does not follow the IEEE
   754 standard for floating point arithmetic and does not have an `Infinity`
   value.  In Erlang division by zero results in a crash, however Gleam does
   not have partial functions and operators in core so instead division by zero
   returns zero, a behaviour taken from Pony, Coq, and Lean.
   
   This may seem unexpected at first, but it is no less mathematically valid
   than crashing or returning a special value. Division by zero is undefined
   in mathematics."
  (:refer-clojure :exclude [compare])
  (:require
   [gleam-ffi]
   [gleam.order :as order]
   [gleam.prelude :as p]))

(def ^{:malli/schema [:=> [:cat :string] (p/result-of :double :nil)] :gleam/src "stdlib-src/src/gleam/float.gleam:49"} parse gleam-ffi/float-parse)

(def ^{:malli/schema [:=> [:cat :double] :string] :gleam/src "stdlib-src/src/gleam/float.gleam:61"} to-string gleam-ffi/float-to-string)

(defn max'
  "max(a: Float, b: Float) -> Float

   Compares two `Float`s, returning the larger of the two.

   ## Examples

   ```gleam
   assert float.max(2.0, 2.3) == 2.3
   ```"
  {:malli/schema [:=> [:cat :double :double] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:192"}
  ^double [^double a ^double b]
  (let [subject (> a b)]
    (if subject a b)))

(defn min'
  "min(a: Float, b: Float) -> Float

   Compares two `Float`s, returning the smaller of the two.

   ## Examples

   ```gleam
   assert float.min(2.0, 2.3) == 2.0
   ```"
  {:malli/schema [:=> [:cat :double :double] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:177"}
  ^double [^double a ^double b]
  (let [subject (< a b)]
    (if subject a b)))

(defn clamp
  "clamp(x: Float, min min_bound: Float, max max_bound: Float) -> Float

   Restricts a float between two bounds.

   Note: If the `min` argument is larger than the `max` argument then they
   will be swapped, so the minimum bound is always lower than the maximum
   bound.


   ## Examples

   ```gleam
   assert float.clamp(1.2, min: 1.4, max: 1.6) == 1.4
   ```

   ```gleam
   assert float.clamp(1.2, min: 1.4, max: 0.6) == 1.2
   ```"
  {:malli/schema [:=> [:cat :double :double :double] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:80"}
  ^double [^double x ^double min-bound ^double max-bound]
  (let [subject (>= min-bound max-bound)]
    (if subject
      (-> x (min' min-bound) (max' max-bound))
      (-> x (min' max-bound) (max' min-bound)))))

(defn compare
  "compare(a: Float, with b: Float) -> Order

   Compares two `Float`s, returning an `Order`:
   `Lt` for lower than, `Eq` for equals, or `Gt` for greater than.

   ## Examples

   ```gleam
   assert float.compare(2.0, 2.3) == Lt
   ```

   To handle
   [Floating Point Imprecision](https://en.wikipedia.org/wiki/Floating-point_arithmetic#Accuracy_problems)
   you may use [`loosely_compare`](#loosely_compare) instead."
  {:malli/schema [:=> [:cat :double :double] (order/Order-schema)]
   :gleam/src "stdlib-src/src/gleam/float.gleam:100"}
  [^double a ^double b]
  (let [subject (= a b)]
    (if subject
      (order/->Eq)
      (let [subject (< a b)]
        (if subject (order/->Lt) (order/->Gt))))))

(defn absolute-value
  "absolute_value(x: Float) -> Float

   Returns the absolute value of the input as a `Float`.

   ## Examples

   ```gleam
   assert float.absolute_value(-12.5) == 12.5
   ```

   ```gleam
   assert float.absolute_value(10.2) == 10.2
   ```"
  {:malli/schema [:=> [:cat :double] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:302"}
  ^double [^double x]
  (let [subject (>= x 0.0)]
    (if subject x (- 0.0 x))))

(defn loosely-compare
  "loosely_compare(a: Float, with b: Float, tolerating tolerance: Float) -> Order

   Compares two `Float`s within a tolerance, returning an `Order`:
   `Lt` for lower than, `Eq` for equals, or `Gt` for greater than.

   This function allows Float comparison while handling
   [Floating Point Imprecision](https://en.wikipedia.org/wiki/Floating-point_arithmetic#Accuracy_problems).

   Notice: For `Float`s the tolerance won't be exact:
   `5.3 - 5.0` is not exactly `0.3`.

   ## Examples

   ```gleam
   assert float.loosely_compare(5.0, with: 5.3, tolerating: 0.5) == Eq
   ```

   If you want to check only for equality you may use
   [`loosely_equals`](#loosely_equals) instead."
  {:malli/schema [:=> [:cat :double :double :double] (order/Order-schema)]
   :gleam/src "stdlib-src/src/gleam/float.gleam:129"}
  [^double a ^double b ^double tolerance]
  (let [difference (absolute-value (- a b)) subject (<= difference tolerance)]
    (if subject (order/->Eq) (compare a b))))

(defn loosely-equals
  "loosely_equals(a: Float, with b: Float, tolerating tolerance: Float) -> Bool

   Checks for equality of two `Float`s within a tolerance,
   returning a `Bool`.

   This function allows Float comparison while handling
   [Floating Point Imprecision](https://en.wikipedia.org/wiki/Floating-point_arithmetic#Accuracy_problems).

   Notice: For `Float`s the tolerance won't be exact:
   `5.3 - 5.0` is not exactly `0.3`.

   ## Examples

   ```gleam
   assert float.loosely_equals(5.0, with: 5.3, tolerating: 0.5)
   ```

   ```gleam
   assert !float.loosely_equals(5.0, with: 5.1, tolerating: 0.1)
   ```"
  {:malli/schema [:=> [:cat :double :double :double] :boolean]
   :gleam/src "stdlib-src/src/gleam/float.gleam:160"}
  [^double a ^double b ^double tolerance]
  (let [difference (absolute-value (- a b))]
    (<= difference tolerance)))

(def ^{:malli/schema [:=> [:cat :double] :double] :gleam/src "stdlib-src/src/gleam/float.gleam:209"} ceiling gleam-ffi/f-ceiling)

(def ^{:malli/schema [:=> [:cat :double] :double] :gleam/src "stdlib-src/src/gleam/float.gleam:221"} floor gleam-ffi/f-floor)

(defn negate
  "negate(x: Float) -> Float

   Returns the negative of the value provided.

   ## Examples

   ```gleam
   assert float.negate(1.0) == -1.0
   ```"
  {:malli/schema [:=> [:cat :double] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:376"}
  ^double [^double x]
  (* -1.0 x))

(def ^{:gleam/src "stdlib-src/src/gleam/float.gleam:244"} js-round gleam-ffi/f-round)

(defn round
  "round(x: Float) -> Int

   Rounds the value to the nearest whole number as an `Int`.

   ## Examples

   ```gleam
   assert float.round(2.3) == 2
   ```

   ```gleam
   assert float.round(2.5) == 3
   ```"
  {:malli/schema [:=> [:cat :double] :int]
   :gleam/src "stdlib-src/src/gleam/float.gleam:236"}
  [^double x]
  (let [subject (>= x 0.0)]
    (if subject (js-round x) (-' 0 (js-round (negate x))))))

(def ^{:malli/schema [:=> [:cat :double] :int] :gleam/src "stdlib-src/src/gleam/float.gleam:256"} truncate gleam-ffi/f-truncate)

(def ^{:gleam/src "stdlib-src/src/gleam/float.gleam:288"} do-to-float gleam-ffi/f-to-float)

(def ^{:gleam/src "stdlib-src/src/gleam/float.gleam:350"} do-power gleam-ffi/f-power)

(defn to-precision
  "to_precision(x: Float, precision: Int) -> Float

   Converts the value to a given precision as a `Float`.
   The precision is the number of allowed decimal places.
   Negative precisions are allowed and force rounding
   to the nearest tenth, hundredth, thousandth etc.

   ## Examples

   ```gleam
   assert float.to_precision(2.43434348473, 2) == 2.43
   ```

   ```gleam
   assert float.to_precision(547_890.453444, -3) == 548_000.0
   ```"
  {:malli/schema [:=> [:cat :double :int] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:273"}
  ^double [^double x precision]
  (let [subject (<= precision 0)]
    (if subject
      (let [factor (do-power 10.0 (do-to-float (- precision)))]
        (* (do-to-float (round (/ x factor))) factor))
      (let [factor (do-power 10.0 (do-to-float precision))]
        (/ (do-to-float (round (* x factor))) factor)))))

(defn power
  "power(base: Float, of exponent: Float) -> Result(Float, Nil)

   Returns the result of the base being raised to the power of the
   exponent, as a `Float`.

   ## Examples

   ```gleam
   assert float.power(2.0, -1.0) == Ok(0.5)
   ```

   ```gleam
   assert float.power(2.0, 2.0) == Ok(4.0)
   ```

   ```gleam
   assert float.power(8.0, 1.5) == Ok(22.627416997969522)
   ```

   ```gleam
   assert 4.0 |> float.power(of: 2.0) == Ok(16.0)
   ```

   ```gleam
   assert float.power(-1.0, 0.5) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :double :double] (p/result-of :double :nil)]
   :gleam/src "stdlib-src/src/gleam/float.gleam:334"}
  [^double base ^double exponent]
  (let [fractional (> (- (ceiling exponent) exponent) 0.0) subject (or (and (< base 0.0) fractional) (and (= base 0.0) (< exponent 0.0)))]
    (if subject (p/->Error nil) (p/->Ok (do-power base exponent)))))

(defn square-root
  "square_root(x: Float) -> Result(Float, Nil)

   Returns the square root of the input as a `Float`.

   ## Examples

   ```gleam
   assert float.square_root(4.0) == Ok(2.0)
   ```

   ```gleam
   assert float.square_root(-16.0) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :double] (p/result-of :double :nil)]
   :gleam/src "stdlib-src/src/gleam/float.gleam:364"}
  [^double x]
  (power x 0.5))

(defn- sum-loop
  "sum_loop(numbers: List(Float), initial: Float) -> Float"
  {:gleam/src "stdlib-src/src/gleam/float.gleam:392"}
  ^double [numbers initial]
  (if (seq numbers)
    (let [first' (first numbers) rest' (rest numbers)]
      (recur rest' (+ first' initial)))
    initial))

(defn sum
  "sum(numbers: List(Float)) -> Float

   Sums a list of `Float`s.

   ## Example

   ```gleam
   assert float.sum([1.0, 2.2, 3.3]) == 6.5
   ```"
  {:malli/schema [:=> [:cat [:sequential :double]] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:388"}
  ^double [numbers]
  (sum-loop numbers 0.0))

(defn- product-loop
  "product_loop(numbers: List(Float), initial: Float) -> Float"
  {:gleam/src "stdlib-src/src/gleam/float.gleam:411"}
  ^double [numbers initial]
  (if (seq numbers)
    (let [first' (first numbers) rest' (rest numbers)]
      (recur rest' (* first' initial)))
    initial))

(defn product
  "product(numbers: List(Float)) -> Float

   Multiplies a list of `Float`s and returns the product.

   ## Example

   ```gleam
   assert float.product([2.5, 3.2, 4.2]) == 33.6
   ```"
  {:malli/schema [:=> [:cat [:sequential :double]] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:407"}
  ^double [numbers]
  (product-loop numbers 1.0))

(def ^{:malli/schema [:=> [:cat] :double] :gleam/src "stdlib-src/src/gleam/float.gleam:433"} random gleam-ffi/f-random)

(defn modulo
  "modulo(dividend: Float, by divisor: Float) -> Result(Float, Nil)

   Computes the modulo of a float division of inputs as a `Result`.

   Returns division of the inputs as a `Result`: If the given divisor equals
   `0`, this function returns an `Error`.

   The computed value will always have the same sign as the `divisor`.

   ## Examples

   ```gleam
   assert float.modulo(13.3, by: 3.3) == Ok(0.1)
   ```

   ```gleam
   assert float.modulo(-13.3, by: 3.3) == Ok(3.2)
   ```

   ```gleam
   assert float.modulo(13.3, by: -3.3) == Ok(-3.2)
   ```

   ```gleam
   assert float.modulo(-13.3, by: -3.3) == Ok(-0.1)
   ```"
  {:malli/schema [:=> [:cat :double :double] (p/result-of :double :nil)]
   :gleam/src "stdlib-src/src/gleam/float.gleam:460"}
  [^double dividend ^double divisor]
  (if (= divisor 0.0)
    (p/->Error nil)
    (p/->Ok (- dividend (* (floor (/ dividend divisor)) divisor)))))

(defn divide
  "divide(a: Float, by b: Float) -> Result(Float, Nil)

   Returns division of the inputs as a `Result`.

   ## Examples

   ```gleam
   assert float.divide(0.0, 1.0) == Ok(0.0)
   ```

   ```gleam
   assert float.divide(1.0, 0.0) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :double :double] (p/result-of :double :nil)]
   :gleam/src "stdlib-src/src/gleam/float.gleam:479"}
  [^double a ^double b]
  (if (= b 0.0)
    (p/->Error nil)
    (let [b b]
      (p/->Ok (/ a b)))))

(defn add
  "add(a: Float, b: Float) -> Float

   Adds two floats together.

   It's the function equivalent of the `+.` operator.
   This function is useful in higher order functions or pipes.

   ## Examples

   ```gleam
   assert float.add(1.0, 2.0) == 3.0
   ```

   ```gleam
   import gleam/list

   assert list.fold([1.0, 2.0, 3.0], 0.0, float.add) == 6.0
   ```

   ```gleam
   assert 3.0 |> float.add(2.0) == 5.0
   ```"
  {:malli/schema [:=> [:cat :double :double] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:507"}
  ^double [^double a ^double b]
  (+ a b))

(defn multiply
  "multiply(a: Float, b: Float) -> Float

   Multiplies two floats together.

   It's the function equivalent of the `*.` operator.
   This function is useful in higher order functions or pipes.

   ## Examples

   ```gleam
   assert float.multiply(2.0, 4.0) == 8.0
   ```

   ```gleam
   import gleam/list

   assert list.fold([2.0, 3.0, 4.0], 1.0, float.multiply) == 24.0
   ```

   ```gleam
   assert 3.0 |> float.multiply(2.0) == 6.0
   ```"
  {:malli/schema [:=> [:cat :double :double] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:532"}
  ^double [^double a ^double b]
  (* a b))

(defn subtract
  "subtract(a: Float, b: Float) -> Float

   Subtracts one float from another.

   It's the function equivalent of the `-.` operator.
   This function is useful in higher order functions or pipes.

   ## Examples

   ```gleam
   assert float.subtract(3.0, 1.0) == 2.0
   ```

   ```gleam
   import gleam/list

   assert list.fold([1.0, 2.0, 3.0], 10.0, float.subtract) == 4.0
   ```

   ```gleam
   assert 3.0 |> float.subtract(2.0) == 1.0
   ```

   ```gleam
   assert 3.0 |> float.subtract(2.0, _) == -1.0
   ```"
  {:malli/schema [:=> [:cat :double :double] :double]
   :gleam/src "stdlib-src/src/gleam/float.gleam:561"}
  ^double [^double a ^double b]
  (- a b))

(def ^{:gleam/src "stdlib-src/src/gleam/float.gleam:600"} do-log gleam-ffi/f-log)

(defn logarithm
  "logarithm(x: Float) -> Result(Float, Nil)

   Returns the natural logarithm (base e) of the given `Float` as a `Result`. If the
   input is less than or equal to 0, returns `Error(Nil)`.

   ## Examples

   ```gleam
   assert float.logarithm(1.0) == Ok(0.0)
   ```

   ```gleam
   assert float.logarithm(2.718281828459045) == Ok(1.0)
   ```

   ```gleam
   assert float.logarithm(0.0) == Error(Nil)
   ```

   ```gleam
   assert float.logarithm(-1.0) == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :double] (p/result-of :double :nil)]
   :gleam/src "stdlib-src/src/gleam/float.gleam:586"}
  [^double x]
  (let [subject (<= x 0.0)]
    (if subject (p/->Error nil) (p/->Ok (do-log x)))))

(def ^{:malli/schema [:=> [:cat :double] :double] :gleam/src "stdlib-src/src/gleam/float.gleam:621"} exponential gleam-ffi/f-exp)
