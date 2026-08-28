(ns gleam.function
  (:refer-clojure :exclude [identity]))

(defn identity
  "identity(x: a) -> a

   Takes a single argument and always returns its input value."
  {:malli/schema [:=> [:cat :any] :any]
   :gleam/src "stdlib-src/src/gleam/function.gleam:3"}
  [x]
  x)
