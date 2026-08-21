(ns gleam.bytes-tree
  "`BytesTree` is a type used for efficiently building binary content to be
   written to a file or a socket. Internally it is represented as a tree so to
   append or prepend to a bytes tree is a constant time operation that
   allocates a new node in the tree without copying any of the content. When
   writing to an output stream the tree is traversed and the content is sent
   directly rather than copying it into a single buffer beforehand.
   
   If we append one bit array to another the bit arrays must be copied to a
   new location in memory so that they can sit together. This behaviour
   enables efficient reading of the data but copying can be expensive,
   especially if we want to join many bit arrays together.
   
   BytesTree is different in that it can be joined together in constant
   time using minimal memory, and then can be efficiently converted to a
   bit array using the `to_bit_array` function.
   
   Byte trees are always byte aligned, so that a number of bits that is not
   divisible by 8 will be padded with 0s.
   
   On Erlang this type is compatible with Erlang's iolists."
  (:refer-clojure :exclude [concat])
  (:require
   [gleam.bit-array :as bit_array]
   [gleam.list :as list]
   [gleam.string-tree :as string_tree]))

;; type BytesTree
(defprotocol IBytesTree)
(defrecord Bytes [value] IBytesTree)
(defn Bytes? "True if `v` is a Bytes value." [v] (instance? Bytes v))
(defrecord Text [value] IBytesTree)
(defn Text? "True if `v` is a Text value." [v] (instance? Text v))
(defrecord Many [value] IBytesTree)
(defn Many? "True if `v` is a Many value." [v] (instance? Many v))
(defn BytesTree? "True if `v` is any BytesTree value." [v] (instance? gleam.bytes_tree.IBytesTree v))

(defn concat
  "Joins a list of bytes trees into a single one.
   
   Runs in constant time."
  {:malli/schema [:=> [:cat [:sequential [:fn BytesTree?]]] [:fn BytesTree?]]}
  [trees]
  (->Many trees))

(defn new*
  "Create an empty `BytesTree`. Useful as the start of a pipe chaining many
   trees together."
  {:malli/schema [:=> [:cat] [:fn BytesTree?]]}
  []
  (concat (list)))

(defn- wrap-list [bits]
  (->Bytes bits))

(defn from-bit-array
  "Creates a new bytes tree from a bit array.
   
   Runs in constant time."
  {:malli/schema [:=> [:cat [:vector :int]] [:fn BytesTree?]]}
  [bits]
  (-> bits bit_array/pad-to-bytes wrap-list))

(defn append-tree
  "Appends a bytes tree onto the end of another.
   
   Runs in constant time."
  {:malli/schema [:=> [:cat [:fn BytesTree?] [:fn BytesTree?]]
                      [:fn BytesTree?]]}
  [first' second]
  (if (instance? Many second)
    (let [trees (:value second)]
      (->Many (list* first' trees)))
    (->Many (list first' second))))

(defn prepend
  "Prepends a bit array to the start of a bytes tree.
   
   Runs in constant time."
  {:malli/schema [:=> [:cat [:fn BytesTree?] [:vector :int]]
                      [:fn BytesTree?]]}
  [second first']
  (append-tree (from-bit-array first') second))

(defn append
  "Appends a bit array to the end of a bytes tree.
   
   Runs in constant time."
  {:malli/schema [:=> [:cat [:fn BytesTree?] [:vector :int]]
                      [:fn BytesTree?]]}
  [first' second]
  (append-tree first' (from-bit-array second)))

(defn prepend-tree
  "Prepends a bytes tree onto the start of another.
   
   Runs in constant time."
  {:malli/schema [:=> [:cat [:fn BytesTree?] [:fn BytesTree?]]
                      [:fn BytesTree?]]}
  [second first']
  (append-tree first' second))

(defn from-string
  "Creates a new bytes tree from a string.
   
   Runs in constant time when running on Erlang.
   Runs in linear time otherwise."
  {:malli/schema [:=> [:cat :string] [:fn BytesTree?]]}
  [string]
  (->Text (string_tree/from-string string)))

(defn prepend-string
  "Prepends a string onto the start of a bytes tree.
   
   Runs in constant time when running on Erlang.
   Runs in linear time with the length of the string otherwise."
  {:malli/schema [:=> [:cat [:fn BytesTree?] :string] [:fn BytesTree?]]}
  [second first']
  (append-tree (from-string first') second))

(defn append-string
  "Appends a string onto the end of a bytes tree.
   
   Runs in constant time when running on Erlang.
   Runs in linear time with the length of the string otherwise."
  {:malli/schema [:=> [:cat [:fn BytesTree?] :string] [:fn BytesTree?]]}
  [first' second]
  (append-tree first' (from-string second)))

(defn concat-bit-arrays
  "Joins a list of bit arrays into a single bytes tree.
   
   Runs in constant time."
  {:malli/schema [:=> [:cat [:sequential [:vector :int]]] [:fn BytesTree?]]}
  [bits]
  (-> bits (list/map from-bit-array) concat))

(defn from-string-tree
  "Creates a new bytes tree from a string tree.
   
   Runs in constant time when running on Erlang.
   Runs in linear time otherwise."
  {:malli/schema [:=> [:cat [:or ]] [:fn BytesTree?]]}
  [tree]
  (->Text tree))

(defn- to-list [stack acc]
  (cond
    (empty? stack)
    acc

    (and (seq stack) (empty? (first stack)))
    (let [remaining-stack (rest stack)]
      (recur remaining-stack acc))

    (and (seq stack) (seq (first stack)) (instance? Bytes (first (first stack))))
    (let [bits (:value (first (first stack))) rest' (rest (first stack)) remaining-stack (rest stack)]
      (recur (list* rest' remaining-stack) (list* bits acc)))

    (and (seq stack) (seq (first stack)) (instance? Text (first (first stack))))
    (let [tree (:value (first (first stack))) rest' (rest (first stack)) remaining-stack (rest stack) bits (bit_array/from-string (string_tree/to-string tree))]
      (recur (list* rest' remaining-stack) (list* bits acc)))

    (and (seq stack) (seq (first stack)) (instance? Many (first (first stack))))
    (let [trees (:value (first (first stack))) rest' (rest (first stack)) remaining-stack (rest stack)]
      (recur (list* trees rest' remaining-stack) acc))))

(defn to-bit-array
  "Turns a bytes tree into a bit array.
   
   Runs in linear time.
   
   When running on Erlang this function is implemented natively by the
   virtual machine and is highly optimised."
  {:malli/schema [:=> [:cat [:fn BytesTree?]] [:vector :int]]}
  [tree]
  (-> (list (list tree)) (to-list (list)) list/reverse bit_array/concat))

(defn byte-size
  "Returns the size of the bytes tree's content in bytes.
   
   Runs in linear time."
  {:malli/schema [:=> [:cat [:fn BytesTree?]] :int]}
  [tree]
  (-> (list (list tree))
      (to-list (list))
      (list/fold 0 (fn [acc bits] (+' (bit_array/byte-size bits) acc)))))
