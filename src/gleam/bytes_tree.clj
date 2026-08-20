(ns gleam.bytes-tree
  "Minimal shim for gleam/bytes_tree.")

(defrecord BytesTree [])

(defn new [] (->BytesTree))
