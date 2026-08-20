(ns gleam.bytes-tree
  (:refer-clojure :exclude [concat])
  (:require
   [gleam.bit-array :as bit_array]
   [gleam.list :as list]
   [gleam.string-tree :as string_tree]))

;; type BytesTree
(defrecord Bytes [value])
(defrecord Text [value])
(defrecord Many [value])

(defn concat
  "Joins a list of bytes trees into a single one.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:sequential [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [trees]
  (->Many trees))

(defn new*
  "Create an empty `BytesTree`. Useful as the start of a pipe chaining many
  trees together."
  {:malli/schema [:=> [:cat] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  []
  (concat (list)))

(defn- wrap-list [bits]
  (->Bytes bits))

(defn from-bit-array
  "Creates a new bytes tree from a bit array.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:vector :int]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [bits]
  (-> bits bit_array/pad-to-bytes wrap-list))

(defn append-tree
  "Appends a bytes tree onto the end of another.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [first' second]
  (if (instance? Many second)
    (let [trees (:value second)]
      (->Many (list* first' trees)))
    (->Many (list first' second))))

(defn prepend
  "Prepends a bit array to the start of a bytes tree.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]] [:vector :int]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [second first']
  (append-tree (from-bit-array first') second))

(defn append
  "Appends a bit array to the end of a bytes tree.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]] [:vector :int]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [first' second]
  (append-tree first' (from-bit-array second)))

(defn prepend-tree
  "Prepends a bytes tree onto the start of another.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [second first']
  (append-tree first' second))

(defn from-string
  "Creates a new bytes tree from a string.
  
  Runs in constant time when running on Erlang.
  Runs in linear time otherwise."
  {:malli/schema [:=> [:cat :string] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [string]
  (->Text (string_tree/from-string string)))

(defn prepend-string
  "Prepends a string onto the start of a bytes tree.
  
  Runs in constant time when running on Erlang.
  Runs in linear time with the length of the string otherwise."
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]] :string] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [second first']
  (append-tree (from-string first') second))

(defn append-string
  "Appends a string onto the end of a bytes tree.
  
  Runs in constant time when running on Erlang.
  Runs in linear time with the length of the string otherwise."
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]] :string] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [first' second]
  (append-tree first' (from-string second)))

(defn concat-bit-arrays
  "Joins a list of bit arrays into a single bytes tree.
  
  Runs in constant time."
  {:malli/schema [:=> [:cat [:sequential [:vector :int]]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [bits]
  (-> bits (list/map from-bit-array) concat))

(defn from-string-tree
  "Creates a new bytes tree from a string tree.
  
  Runs in constant time when running on Erlang.
  Runs in linear time otherwise."
  {:malli/schema [:=> [:cat [:or ]] [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]]}
  [tree]
  (->Text tree))

(defn- to-list [stack acc]
  (cond
    (empty? stack) acc
    (and (seq stack) (empty? (first stack))) (let [remaining-stack (rest stack)]
                                               (recur remaining-stack acc))
    (and (seq stack) (seq (first stack)) (instance? Bytes (first (first stack)))) (let [bits (:value (first (first stack))) rest' (rest (first stack)) remaining-stack (rest stack)]
                                                                                    (recur (list* rest' remaining-stack) (list* bits acc)))
    (and (seq stack) (seq (first stack)) (instance? Text (first (first stack)))) (let [tree (:value (first (first stack))) rest' (rest (first stack)) remaining-stack (rest stack) bits (bit_array/from-string (string_tree/to-string tree))]
                                                                                   (recur (list* rest' remaining-stack) (list* bits acc)))
    (and (seq stack) (seq (first stack)) (instance? Many (first (first stack)))) (let [trees (:value (first (first stack))) rest' (rest (first stack)) remaining-stack (rest stack)]
                                                                                   (recur (list* trees rest' remaining-stack) acc))))

(defn to-bit-array
  "Turns a bytes tree into a bit array.
  
  Runs in linear time.
  
  When running on Erlang this function is implemented natively by the
  virtual machine and is highly optimised."
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]] [:vector :int]]}
  [tree]
  (-> (list (list tree)) (to-list (list)) list/reverse bit_array/concat))

(defn byte-size
  "Returns the size of the bytes tree's content in bytes.
  
  Runs in linear time."
  {:malli/schema [:=> [:cat [:or [:fn (partial instance? gleam.bytes_tree.Bytes)] [:fn (partial instance? gleam.bytes_tree.Text)] [:fn (partial instance? gleam.bytes_tree.Many)]]] :int]}
  [tree]
  (-> (list (list tree))
      (to-list (list))
      (list/fold 0 (fn [acc bits] (+' (bit_array/byte-size bits) acc)))))
