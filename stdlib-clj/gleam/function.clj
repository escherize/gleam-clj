(ns gleam.function
  (:refer-clojure :exclude [identity]))

(defn identity
  "Takes a single argument and always returns its input value."
  {:malli/schema [:=> [:cat :any] :any]}
  [x]
  x)
