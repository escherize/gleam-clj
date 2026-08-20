(ns gleam.bool
  (:refer-clojure :exclude [and or])
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn and
  "Returns the and of two bools, but it evaluates both arguments.
  
  It's the function equivalent of the `&&` operator.
  This function is useful in higher order functions or pipes.
  
  ## Examples
  
  ```gleam
  assert bool.and(True, True)
  ```
  
  ```gleam
  assert !bool.and(False, True)
  ```
  
  ```gleam
  assert !bool.and(False, True)
  ```
  
  ```gleam
  assert !bool.and(False, False)
  ```"
  [a b]
  (and a b))

(defn or
  "Returns the or of two bools, but it evaluates both arguments.
  
  It's the function equivalent of the `||` operator.
  This function is useful in higher order functions or pipes.
  
  ## Examples
  
  ```gleam
  assert bool.or(True, True)
  ```
  
  ```gleam
  assert bool.or(False, True)
  ```
  
  ```gleam
  assert bool.or(True, False)
  ```
  
  ```gleam
  assert !bool.or(False, False)
  ```"
  [a b]
  (or a b))

(defn negate
  "Returns the opposite bool value.
  
  This is the same as the `!` or `not` operators in some other languages.
  
  ## Examples
  
  ```gleam
  assert !bool.negate(True)
  ```
  
  ```gleam
  assert bool.negate(False)
  ```"
  [bool]
  (not bool))

(defn nor
  "Returns the nor of two bools.
  
  ## Examples
  
  ```gleam
  assert bool.nor(False, False)
  ```
  
  ```gleam
  assert !bool.nor(False, True)
  ```
  
  ```gleam
  assert !bool.nor(True, False)
  ```
  
  ```gleam
  assert !bool.nor(True, True)
  ```"
  [a b]
  (not (or a b)))

(defn nand
  "Returns the nand of two bools.
  
  ## Examples
  
  ```gleam
  assert bool.nand(False, False)
  ```
  
  ```gleam
  assert bool.nand(False, True)
  ```
  
  ```gleam
  assert bool.nand(True, False)
  ```
  
  ```gleam
  assert !bool.nand(True, True)
  ```"
  [a b]
  (not (and a b)))

(defn exclusive-or
  "Returns the exclusive or of two bools.
  
  ## Examples
  
  ```gleam
  assert !bool.exclusive_or(False, False)
  ```
  
  ```gleam
  assert bool.exclusive_or(False, True)
  ```
  
  ```gleam
  assert bool.exclusive_or(True, False)
  ```
  
  ```gleam
  assert !bool.exclusive_or(True, True)
  ```"
  [a b]
  (not= a b))

(defn exclusive-nor
  "Returns the exclusive nor of two bools.
  
  ## Examples
  
  ```gleam
  assert bool.exclusive_nor(False, False)
  ```
  
  ```gleam
  assert !bool.exclusive_nor(False, True)
  ```
  
  ```gleam
  assert !bool.exclusive_nor(True, False)
  ```
  
  ```gleam
  assert bool.exclusive_nor(True, True)
  ```"
  [a b]
  (= a b))

(defn to-string
  "Returns a string representation of the given bool.
  
  ## Examples
  
  ```gleam
  assert bool.to_string(True) == \"True\"
  ```
  
  ```gleam
  assert bool.to_string(False) == \"False\"
  ```"
  [bool]
  (if (not bool) "False" "True"))

(defn guard
  "Run a callback function if the given bool is `False`, otherwise return a
  default value.
  
  With a `use` expression this function can simulate the early-return pattern
  found in some other programming languages.
  
  In a procedural language:
  
  ```js
  if (predicate) return value;
  // ...
  ```
  
  In Gleam with a `use` expression:
  
  ```gleam
  use <- bool.guard(when: predicate, return: value)
  todo
  // ...
  ```
  
  Like everything in Gleam `use` is an expression, so it short circuits the
  current block, not the entire function. As a result you can assign the value
  to a variable:
  
  ```gleam
  let x = {
  use <- bool.guard(when: predicate, return: value)
  todo
  // ...
  }
  ```
  
  Note that unlike in procedural languages the `return` value is evaluated
  even when the predicate is `False`, so it is advisable not to perform
  expensive computation nor side-effects there.
  
  
  ## Examples
  
  ```gleam
  let name = \"\"
  use <- bool.guard(when: name == \"\", return: \"Welcome!\")
  \"Hello, \" <> name
  // -> \"Welcome!\"
  ```
  
  ```gleam
  let name = \"Kamaka\"
  use <- bool.guard(when: name == \"\", return: \"Welcome!\")
  \"Hello, \" <> name
  // -> \"Hello, Kamaka\"
  ```"
  [requirement consequence alternative]
  (if requirement consequence (alternative)))

(defn lazy-guard
  "Runs a callback function if the given bool is `True`, otherwise runs an
  alternative callback function.
  
  Useful when further computation should be delayed regardless of the given
  bool's value.
  
  See [`guard`](#guard) for more info.
  
  ## Examples
  
  ```gleam
  let name = \"Kamaka\"
  let inquiry = fn() { \"How may we address you?\" }
  use <- bool.lazy_guard(when: name == \"\", return: inquiry)
  \"Hello, \" <> name
  // -> \"Hello, Kamaka\"
  ```
  
  ```gleam
  import gleam/int
  
  let name = \"\"
  let greeting = fn() { \"Hello, \" <> name }
  use <- bool.lazy_guard(when: name == \"\", otherwise: greeting)
  let number = int.random(99)
  let name = \"User \" <> int.to_string(number)
  \"Welcome, \" <> name
  // -> \"Welcome, User 54\"
  ```"
  [requirement consequence alternative]
  (if requirement (consequence) (alternative)))

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'and [:=> [:cat :boolean :boolean] :boolean]
   'exclusive-nor [:=> [:cat :boolean :boolean] :boolean]
   'exclusive-or [:=> [:cat :boolean :boolean] :boolean]
   'guard [:=> [:cat :boolean :any [:=> [:cat] :any]] :any]
   'lazy-guard [:=> [:cat :boolean [:=> [:cat] :any] [:=> [:cat] :any]] :any]
   'nand [:=> [:cat :boolean :boolean] :boolean]
   'negate [:=> [:cat :boolean] :boolean]
   'nor [:=> [:cat :boolean :boolean] :boolean]
   'or [:=> [:cat :boolean :boolean] :boolean]
   'to-string [:=> [:cat :boolean] :string]})
