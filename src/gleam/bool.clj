(ns gleam.bool
  "Shims for gleam/bool.")

(defn to-string [b]
  (if b "True" "False"))

(defn negate [b]
  (not b))
