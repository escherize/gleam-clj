(ns gleam.option
  "Shims for gleam/option: the Option type.")

(defrecord Some [value])
(defrecord None [])

(defn is-some [o] (instance? Some o))
(defn is-none [o] (instance? None o))

(defn unwrap [o default]
  (if (instance? Some o) (:value o) default))
