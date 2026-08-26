(ns gleam.dict
  (:refer-clojure :exclude [drop filter get keys merge take])
  (:require
   [gleam-ffi]
   [gleam.option :as option]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Dict
(defprotocol IDict)
(defn Dict? "True if `v` is any Dict value." [v] (instance? gleam.dict.IDict v))

;; type TransientDict
(defprotocol ITransientDict)
(defn TransientDict? "True if `v` is any TransientDict value." [v] (instance? gleam.dict.ITransientDict v))

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:30"} to-transient gleam-ffi/dict-to-transient)

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:36"} from-transient gleam-ffi/dict-from-transient)

(def ^{:malli/schema [:=> [:cat [:map-of :any :any]] :int] :gleam/src "stdlib-src/src/gleam/dict.gleam:53"} size gleam-ffi/dict-size)

(defn is-empty
  "is_empty(dict: Dict(a, b)) -> Bool

   Determines whether or not the dict is empty.

   ## Examples

   ```gleam
   assert dict.new() |> dict.is_empty
   ```

   ```gleam
   assert !{ dict.new() |> dict.insert(\"b\", 1) |> dict.is_empty }
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any]] :boolean]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:67"}
  [dict]
  (= (size dict) 0))

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:493"} do-fold gleam-ffi/dict-fold)

(defn fold
  "fold(over dict: Dict(a, b), from initial: c, with fun: fn(c, a, b) -> c) -> c

   Combines all entries into a single value by calling a given function on each
   one.

   Dicts are not ordered so the values are not returned in any specific order. Do
   not write code that relies on the order entries are used by this function
   as it may change in later versions of Gleam or Erlang.

   ## Examples

   ```gleam
   let dict = dict.from_list([#(\"a\", 1), #(\"b\", 3), #(\"c\", 9)])
   assert dict.fold(dict, 0, fn(accumulator, key, value) { accumulator + value })
   == 13
   ```

   ```gleam
   import gleam/string

   let dict = dict.from_list([#(\"a\", 1), #(\"b\", 3), #(\"c\", 9)])
   assert dict.fold(dict, \"\", fn(accumulator, key, value) {
   string.append(accumulator, key)
   })
   == \"abc\"
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] :any [:=> [:cat :any :any :any] :any]]
                      :any]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:483"}
  [dict initial fun]
  (let [fun (fn [key value acc] (fun acc key value))]
    (do-fold fun initial dict)))

(defn to-list
  "to_list(dict: Dict(a, b)) -> List(#(a, b))

   Converts the dict to a list of 2-element tuples `#(key, value)`, one for
   each key-value pair in the dict.

   The tuples in the list have no specific order.

   ## Examples

   Calling `to_list` on an empty `dict` returns an empty list.

   ```gleam
   assert dict.new() |> dict.to_list == []
   ```

   The ordering of elements in the resulting list is an implementation detail
   that should not be relied upon.

   ```gleam
   assert dict.new()
   |> dict.insert(\"b\", 1)
   |> dict.insert(\"a\", 0)
   |> dict.insert(\"c\", 2)
   |> dict.to_list
   == [#(\"a\", 0), #(\"b\", 1), #(\"c\", 2)]
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any]]
                      [:sequential [:tuple :any :any]]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:97"}
  [dict]
  (fold dict (list) (fn [acc key value] (list* [key value] acc))))

(def ^{:malli/schema [:=> [:cat] [:map-of :any :any]] :gleam/src "stdlib-src/src/gleam/dict.gleam:146"} new* gleam-ffi/dict-new)

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:197"} transient-insert gleam-ffi/transient-insert)

(defn- from-list-loop
  "from_list_loop(transient: TransientDict(a, b), list: List(#(a, b))) -> Dict(a, b)"
  {:gleam/src "stdlib-src/src/gleam/dict.gleam:111"}
  [transient list']
  (if (empty? list')
    (from-transient transient)
    (let [key (nth (first list') 0) value (nth (first list') 1) rest' (rest list')]
      (recur (transient-insert key value transient) rest'))))

(defn from-list
  "from_list(list: List(#(a, b))) -> Dict(a, b)

   Converts a list of 2-element tuples `#(key, value)` to a dict.

   If two tuples have the same key the last one in the list will be the one
   that is present in the dict."
  {:malli/schema [:=> [:cat [:sequential [:tuple :any :any]]]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:107"}
  [list']
  (from-list-loop (to-transient (new*)) list'))

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:140"} do-has-key gleam-ffi/dict-has-key)

(defn has-key
  "has_key(dict: Dict(a, b), key: a) -> Bool

   Determines whether or not a value is present in the dict for a given key.

   ## Examples

   ```gleam
   assert dict.new() |> dict.insert(\"a\", 0) |> dict.has_key(\"a\")
   ```

   ```gleam
   assert !{ dict.new() |> dict.insert(\"a\", 0) |> dict.has_key(\"b\") }
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] :any] :boolean]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:135"}
  [dict key]
  (do-has-key key dict))

(def ^{:malli/schema [:=> [:cat [:map-of :any :any] :any] [:or [:fn p/Ok?] [:fn p/Error?]]] :gleam/src "stdlib-src/src/gleam/dict.gleam:165"} get gleam-ffi/dict-get)

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:193"} do-insert gleam-ffi/dict-insert)

(defn insert
  "insert(into dict: Dict(a, b), for key: a, insert value: b) -> Dict(a, b)

   Inserts a value into the dict with the given key.

   If the dict already has a value for the given key then the value is
   replaced with the new value.

   ## Examples

   ```gleam
   assert dict.new() |> dict.insert(\"a\", 0) == dict.from_list([#(\"a\", 0)])
   ```

   ```gleam
   assert dict.new() |> dict.insert(\"a\", 0) |> dict.insert(\"a\", 5)
   == dict.from_list([#(\"a\", 5)])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] :any :any]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:184"}
  [dict key value]
  (do-insert key value dict))

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:220"} do-map-values gleam-ffi/dict-map-values)

(defn map-values
  "map_values(in dict: Dict(a, b), with fun: fn(a, b) -> c) -> Dict(a, c)

   Updates all values in a given dict by calling a given function on each key
   and value.

   ## Examples

   ```gleam
   assert dict.from_list([#(3, 3), #(2, 4)])
   |> dict.map_values(fn(key, value) { key * value })
   == dict.from_list([#(3, 9), #(2, 8)])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] [:=> [:cat :any :any] :any]]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:215"}
  [dict fun]
  (do-map-values fun dict))

(defn keys
  "keys(dict: Dict(a, b)) -> List(a)

   Gets a list of all keys in a given dict.

   Dicts are not ordered so the keys are not returned in any specific order. Do
   not write code that relies on the order keys are returned by this function
   as it may change in later versions of Gleam or Erlang.

   ## Examples

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)]) |> dict.keys == [\"a\", \"b\"]
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any]] [:sequential :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:235"}
  [dict]
  (fold dict (list) (fn [acc key _] (list* key acc))))

(defn values
  "values(dict: Dict(a, b)) -> List(b)

   Gets a list of all values in a given dict.

   Dicts are not ordered so the values are not returned in any specific order. Do
   not write code that relies on the order values are returned by this function
   as it may change in later versions of Gleam or Erlang.

   ## Examples

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)]) |> dict.values == [0, 1]
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any]] [:sequential :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:252"}
  [dict]
  (fold dict (list) (fn [acc _ value] (list* value acc))))

(defn- do-filter
  "do_filter(f: fn(a, b) -> Bool, dict: Dict(a, b)) -> Dict(a, b)"
  {:gleam/src "stdlib-src/src/gleam/dict.gleam:281"}
  [f dict]
  (let [_pipe (to-transient (new*))
        _pipe (fold dict
              _pipe
              (fn [transient key value]
                (let [subject (f key value)]
                  (if subject
                    (transient-insert key value transient)
                    transient))))]
    (from-transient _pipe)))

(defn filter
  "filter(in dict: Dict(a, b), keeping predicate: fn(a, b) -> Bool) -> Dict(a, b)

   Creates a new dict from a given dict, minus any entries that a given function
   returns `False` for.

   ## Examples

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   |> dict.filter(fn(key, value) { value != 0 })
   == dict.from_list([#(\"b\", 1)])
   ```

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   |> dict.filter(fn(key, value) { True })
   == dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] [:=> [:cat :any :any] :boolean]]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:273"}
  [dict predicate]
  (do-filter predicate dict))

(defn- do-take-loop
  "do_take_loop(dict: Dict(a, b), desired_keys: List(a), acc: TransientDict(a, b)) -> Dict(a, b)"
  {:gleam/src "stdlib-src/src/gleam/dict.gleam:321"}
  [dict desired-keys acc]
  (if (empty? desired-keys)
    (from-transient acc)
    (let [key (first desired-keys) rest' (rest desired-keys) subject (get dict key)]
      (if (instance? Ok subject)
        (let [value (:value subject)]
          (recur dict rest' (transient-insert key value acc)))
        (recur dict rest' acc)))))

(defn- do-take
  "do_take(desired_keys: List(a), dict: Dict(a, b)) -> Dict(a, b)"
  {:gleam/src "stdlib-src/src/gleam/dict.gleam:317"}
  [desired-keys dict]
  (do-take-loop dict desired-keys (to-transient (new*))))

(defn take
  "take(from dict: Dict(a, b), keeping desired_keys: List(a)) -> Dict(a, b)

   Creates a new dict from a given dict, only including any entries for which the
   keys are in a given list.

   ## Examples

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   |> dict.take([\"b\"])
   == dict.from_list([#(\"b\", 1)])
   ```

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   |> dict.take([\"a\", \"b\", \"c\"])
   == dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] [:sequential :any]]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:309"}
  [dict desired-keys]
  (do-take desired-keys dict))

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:565"} transient-update-with gleam-ffi/transient-update-with)

(defn- do-combine
  "do_combine(combine: fn(a, b, b) -> b, left: Dict(a, b), right: Dict(a, b)) -> Dict(a, b)"
  {:gleam/src "stdlib-src/src/gleam/dict.gleam:545"}
  [combine left right]
  (let [[big small combine] (let [subject (>= (size left) (size right))]
                              (if subject
                                [left right combine]
                                [right left (fn [k l r] (combine k r l))])) _pipe (to-transient big) _pipe (fold small _pipe (fn [transient key value] (let [update (fn [existing] (combine key existing value))] (transient-update-with key update value transient))))]
    (from-transient _pipe)))

(defn combine
  "combine(dict: Dict(a, b), other: Dict(a, b), with fun: fn(b, b) -> b) -> Dict(a, b)

   Creates a new dict from a pair of given dicts by combining their entries.

   If there are entries with the same keys in both dicts the given function is
   used to determine the new value to use in the resulting dict.

   ## Examples

   ```gleam
   let a = dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   let b = dict.from_list([#(\"a\", 2), #(\"c\", 3)])
   assert dict.combine(a, b, fn(one, other) { one + other })
   == dict.from_list([#(\"a\", 2), #(\"b\", 1), #(\"c\", 3)])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] [:map-of :any :any] [:=> [:cat :any :any] :any]]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:536"}
  [dict other fun]
  (do-combine (fn [_ l r] (fun l r)) dict other))

(defn merge
  "merge(into dict: Dict(a, b), from new_entries: Dict(a, b)) -> Dict(a, b)

   Creates a new dict from a pair of given dicts by combining their entries.

   If there are entries with the same keys in both dicts the entry from the
   second dict takes precedence.

   ## Examples

   ```gleam
   let a = dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   let b = dict.from_list([#(\"b\", 2), #(\"c\", 3)])
   assert dict.merge(a, b) == dict.from_list([#(\"a\", 0), #(\"b\", 2), #(\"c\", 3)])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] [:map-of :any :any]]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:350"}
  [dict new-entries]
  (combine dict new-entries (fn [_ new-entry] new-entry)))

(def ^{:gleam/src "stdlib-src/src/gleam/dict.gleam:378"} transient-delete gleam-ffi/transient-delete)

(defn delete
  "delete(from dict: Dict(a, b), delete key: a) -> Dict(a, b)

   Creates a new dict from a given dict with all the same entries except for the
   one with a given key, if it exists.

   ## Examples

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)]) |> dict.delete(\"a\")
   == dict.from_list([#(\"b\", 1)])
   ```

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)]) |> dict.delete(\"c\")
   == dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] :any] [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:372"}
  [dict key]
  (-> (to-transient dict)
      ((fn [_capture] (transient-delete key _capture)))
      from-transient))

(defn- drop-loop
  "drop_loop(transient: TransientDict(a, b), disallowed_keys: List(a)) -> Dict(a, b)"
  {:gleam/src "stdlib-src/src/gleam/dict.gleam:412"}
  [transient disallowed-keys]
  (if (empty? disallowed-keys)
    (from-transient transient)
    (let [key (first disallowed-keys) rest' (rest disallowed-keys)]
      (recur (transient-delete key transient) rest'))))

(defn- do-drop
  "do_drop(disallowed_keys: List(a), dict: Dict(a, b)) -> Dict(a, b)"
  {:gleam/src "stdlib-src/src/gleam/dict.gleam:408"}
  [disallowed-keys dict]
  (drop-loop (to-transient dict) disallowed-keys))

(defn drop
  "drop(from dict: Dict(a, b), drop disallowed_keys: List(a)) -> Dict(a, b)

   Creates a new dict from a given dict with all the same entries except any with
   keys found in a given list.

   ## Examples

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)]) |> dict.drop([\"a\"])
   == dict.from_list([#(\"b\", 1)])
   ```

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)]) |> dict.drop([\"c\"])
   == dict.from_list([#(\"a\", 0), #(\"b\", 1)])
   ```

   ```gleam
   assert dict.from_list([#(\"a\", 0), #(\"b\", 1)]) |> dict.drop([\"a\", \"b\", \"c\"])
   == dict.from_list([])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] [:sequential :any]]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:400"}
  [dict disallowed-keys]
  (do-drop disallowed-keys dict))

(defn upsert
  "upsert(in dict: Dict(a, b), update key: a, with fun: fn(Option(b)) -> b) -> Dict(a, b)

   Creates a new dict with one entry inserted or updated using a given function.

   If there was not an entry in the dict for the given key then the function
   gets `None` as its argument, otherwise it gets `Some(value)`.

   ## Examples

   ```gleam
   let dict = dict.from_list([#(\"a\", 0)])
   let increment = fn(x) {
   case x {
   Some(i) -> i + 1
   None -> 0
   }
   }

   assert dict.upsert(dict, \"a\", increment) == dict.from_list([#(\"a\", 1)])
   ```

   ```gleam
   assert dict.upsert(dict, \"b\", increment)
   == dict.from_list([#(\"a\", 0), #(\"b\", 0)])
   ```"
  {:malli/schema [:=> [:cat [:map-of :any :any] :any [:=> [:cat [:fn option/Option?]] :any]]
                      [:map-of :any :any]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:446"}
  [dict key fun]
  (let [subject (get dict key)]
    (if (instance? Ok subject)
      (let [value (:value subject)]
        (insert dict key (fun (option/->Some value))))
      (insert dict key (fun (option/->None))))))

(defn each
  "each(dict: Dict(a, b), fun: fn(a, b) -> c) -> Nil

   Calls a function for each key and value in a dict, discarding the return
   value.

   Useful for producing a side effect for every item of a dict.

   ```gleam
   import gleam/io

   let dict =
   dict.from_list([#(\"a\", \"apple\"), #(\"b\", \"banana\"), #(\"c\", \"cherry\")])

   assert dict.each(dict, fn(k, v) { io.println(k <> \" => \" <> v) }) == Nil
   // a => apple
   // b => banana
   // c => cherry
   ```

   The order of elements in the iteration is an implementation detail that
   should not be relied upon."
  {:malli/schema [:=> [:cat [:map-of :any :any] [:=> [:cat :any :any] :any]]
                      :nil]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:515"}
  [dict fun]
  (fold dict
        nil
        (fn [nil_ k v]
          (fun k v)
          nil_)))

(defn- group-loop
  "group_loop(transient: TransientDict(a, List(b)), to_key: fn(b) -> a, list: List(b)) -> Dict(a, List(b))"
  {:gleam/src "stdlib-src/src/gleam/dict.gleam:577"}
  [transient to-key list']
  (if (empty? list')
    (from-transient transient)
    (let [value (first list') rest' (rest list') key (to-key value) update (fn [existing] (list* value existing))]
      (-> transient
          ((fn [_capture]
            (transient-update-with key update (list value) _capture)))
          (group-loop to-key rest')))))

(defn group
  "group(key: fn(a) -> b, list: List(a)) -> Dict(b, List(a))"
  {:malli/schema [:=> [:cat [:=> [:cat :any] :any] [:sequential :any]]
                      [:map-of :any [:sequential :any]]]
   :gleam/src "stdlib-src/src/gleam/dict.gleam:573"}
  [key list']
  (group-loop (to-transient (new*)) key list'))
