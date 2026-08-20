(ns gleam.float
  (:refer-clojure :exclude [compare])
  (:require
   [gleam-ffi]
   [gleam.order :as order]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(def parse gleam-ffi/float-parse)

(def to-string gleam-ffi/float-to-string)

(defn max'
  "Compares two `Float`s, returning the larger of the two.
  
  ## Examples
  
  ```gleam
  assert float.max(2.0, 2.3) == 2.3
  ```"
  [a b]
  (let [subject (> a b)]
    (if subject a b)))

(defn min'
  "Compares two `Float`s, returning the smaller of the two.
  
  ## Examples
  
  ```gleam
  assert float.min(2.0, 2.3) == 2.0
  ```"
  [a b]
  (let [subject (< a b)]
    (if subject a b)))

(defn clamp
  "Restricts a float between two bounds.
  
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
  [x min-bound max-bound]
  (let [subject (>= min-bound max-bound)]
    (if subject
      (-> x (min' min-bound) (max' max-bound))
      (-> x (min' max-bound) (max' min-bound)))))

(defn compare
  "Compares two `Float`s, returning an `Order`:
  `Lt` for lower than, `Eq` for equals, or `Gt` for greater than.
  
  ## Examples
  
  ```gleam
  assert float.compare(2.0, 2.3) == Lt
  ```
  
  To handle
  [Floating Point Imprecision](https://en.wikipedia.org/wiki/Floating-point_arithmetic#Accuracy_problems)
  you may use [`loosely_compare`](#loosely_compare) instead."
  [a b]
  (let [subject (= a b)]
    (if subject
      (order/->Eq)
      (let [subject (< a b)]
        (if subject (order/->Lt) (order/->Gt))))))

(defn absolute-value
  "Returns the absolute value of the input as a `Float`.
  
  ## Examples
  
  ```gleam
  assert float.absolute_value(-12.5) == 12.5
  ```
  
  ```gleam
  assert float.absolute_value(10.2) == 10.2
  ```"
  [x]
  (let [subject (>= x 0.0)]
    (if subject x (- 0.0 x))))

(defn loosely-compare
  "Compares two `Float`s within a tolerance, returning an `Order`:
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
  [a b tolerance]
  (let [difference (absolute-value (- a b))]
    (let [subject (<= difference tolerance)]
      (if subject (order/->Eq) (compare a b)))))

(defn loosely-equals
  "Checks for equality of two `Float`s within a tolerance,
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
  [a b tolerance]
  (let [difference (absolute-value (- a b))]
    (<= difference tolerance)))

(def ceiling gleam-ffi/f-ceiling)

(def floor gleam-ffi/f-floor)

(defn negate
  "Returns the negative of the value provided.
  
  ## Examples
  
  ```gleam
  assert float.negate(1.0) == -1.0
  ```"
  [x]
  (* -1.0 x))

(def js-round gleam-ffi/f-round)

(defn round
  "Rounds the value to the nearest whole number as an `Int`.
  
  ## Examples
  
  ```gleam
  assert float.round(2.3) == 2
  ```
  
  ```gleam
  assert float.round(2.5) == 3
  ```"
  [x]
  (let [subject (>= x 0.0)]
    (if subject (js-round x) (-' 0 (js-round (negate x))))))

(def truncate gleam-ffi/f-truncate)

(def do-to-float gleam-ffi/f-to-float)

(def do-power gleam-ffi/f-power)

(defn to-precision
  "Converts the value to a given precision as a `Float`.
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
  [x precision]
  (let [subject (<= precision 0)]
    (if subject
      (let [factor (do-power 10.0 (do-to-float (- precision)))]
        (* (do-to-float (round (/ x factor))) factor))
      (let [factor (do-power 10.0 (do-to-float precision))]
        (/ (do-to-float (round (* x factor))) factor)))))

(defn power
  "Returns the result of the base being raised to the power of the
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
  [base exponent]
  (let [fractional (> (- (ceiling exponent) exponent) 0.0)]
    (let [subject (or (and (< base 0.0) fractional) (and (= base 0.0) (< exponent 0.0)))]
      (if subject (p/->Error nil) (p/->Ok (do-power base exponent))))))

(defn square-root
  "Returns the square root of the input as a `Float`.
  
  ## Examples
  
  ```gleam
  assert float.square_root(4.0) == Ok(2.0)
  ```
  
  ```gleam
  assert float.square_root(-16.0) == Error(Nil)
  ```"
  [x]
  (power x 0.5))

(defn- sum-loop [numbers initial]
  (if (seq numbers)
    (let [first' (first numbers) rest' (rest numbers)]
      (recur rest' (+ first' initial)))
    initial))

(defn sum
  "Sums a list of `Float`s.
  
  ## Example
  
  ```gleam
  assert float.sum([1.0, 2.2, 3.3]) == 6.5
  ```"
  [numbers]
  (sum-loop numbers 0.0))

(defn- product-loop [numbers initial]
  (if (seq numbers)
    (let [first' (first numbers) rest' (rest numbers)]
      (recur rest' (* first' initial)))
    initial))

(defn product
  "Multiplies a list of `Float`s and returns the product.
  
  ## Example
  
  ```gleam
  assert float.product([2.5, 3.2, 4.2]) == 33.6
  ```"
  [numbers]
  (product-loop numbers 1.0))

(def random gleam-ffi/f-random)

(defn modulo
  "Computes the modulo of a float division of inputs as a `Result`.
  
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
  [dividend divisor]
  (if (= divisor 0.0)
    (p/->Error nil)
    (p/->Ok (- dividend (* (floor (/ dividend divisor)) divisor)))))

(defn divide
  "Returns division of the inputs as a `Result`.
  
  ## Examples
  
  ```gleam
  assert float.divide(0.0, 1.0) == Ok(0.0)
  ```
  
  ```gleam
  assert float.divide(1.0, 0.0) == Error(Nil)
  ```"
  [a b]
  (if (= b 0.0)
    (p/->Error nil)
    (let [b b]
      (p/->Ok (/ a b)))))

(defn add
  "Adds two floats together.
  
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
  [a b]
  (+ a b))

(defn multiply
  "Multiplies two floats together.
  
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
  [a b]
  (* a b))

(defn subtract
  "Subtracts one float from another.
  
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
  [a b]
  (- a b))

(def do-log gleam-ffi/f-log)

(defn logarithm
  "Returns the natural logarithm (base e) of the given `Float` as a `Result`. If the
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
  [x]
  (let [subject (<= x 0.0)]
    (if subject (p/->Error nil) (p/->Ok (do-log x)))))

(def exponential gleam-ffi/f-exp)

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'absolute-value [:=> [:cat :double] :double]
   'add [:=> [:cat :double :double] :double]
   'ceiling [:=> [:cat :double] :double]
   'clamp [:=> [:cat :double :double :double] :double]
   'compare [:=> [:cat :double :double] [:or [:fn (partial instance? gleam.order.Lt)] [:fn (partial instance? gleam.order.Eq)] [:fn (partial instance? gleam.order.Gt)]]]
   'divide [:=> [:cat :double :double] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'exponential [:=> [:cat :double] :double]
   'floor [:=> [:cat :double] :double]
   'logarithm [:=> [:cat :double] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'loosely-compare [:=> [:cat :double :double :double] [:or [:fn (partial instance? gleam.order.Lt)] [:fn (partial instance? gleam.order.Eq)] [:fn (partial instance? gleam.order.Gt)]]]
   'loosely-equals [:=> [:cat :double :double :double] :boolean]
   'max' [:=> [:cat :double :double] :double]
   'min' [:=> [:cat :double :double] :double]
   'modulo [:=> [:cat :double :double] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'multiply [:=> [:cat :double :double] :double]
   'negate [:=> [:cat :double] :double]
   'parse [:=> [:cat :string] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'power [:=> [:cat :double :double] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'product [:=> [:cat [:sequential :double]] :double]
   'random [:=> [:cat] :double]
   'round [:=> [:cat :double] :int]
   'square-root [:=> [:cat :double] [:or [:fn (partial instance? gleam.prelude.Ok)]                      [:fn (partial instance? gleam.prelude.Error)]]]
   'subtract [:=> [:cat :double :double] :double]
   'sum [:=> [:cat [:sequential :double]] :double]
   'to-precision [:=> [:cat :double :int] :double]
   'to-string [:=> [:cat :double] :string]
   'truncate [:=> [:cat :double] :int]})
