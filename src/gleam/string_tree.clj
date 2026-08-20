(ns gleam.string-tree
  "Minimal shim for gleam/string_tree.")

(defrecord StringTree [])

(defn new [] (->StringTree))
