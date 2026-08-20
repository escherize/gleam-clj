(ns gleam.set
  (:refer-clojure :exclude [drop filter map take])
  (:require
   [gleam.dict :as dict]
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.result :as result])
  (:import (gleam.prelude Ok)))

;; type Set
(defrecord Set [dict])

(defn new*
  "Creates a new empty set."
  []
  (->Set (dict/new*)))

(defn size
  "Gets the number of members in a set.
  
  This function runs in constant time.
  
  ## Examples
  
  ```gleam
  assert set.new()
  |> set.insert(1)
  |> set.insert(2)
  |> set.size
  == 2
  ```"
  [set]
  (dict/size (:dict set)))

(defn is-empty
  "Determines whether or not the set is empty.
  
  ## Examples
  
  ```gleam
  assert set.new() |> set.is_empty
  ```
  
  ```gleam
  assert !{ set.new() |> set.insert(1) |> set.is_empty }
  ```"
  [set]
  (= set (new*)))

(def ^:private token (list))

(defn insert
  "Inserts a member into the set.
  
  This function runs in logarithmic time.
  
  ## Examples
  
  ```gleam
  assert set.new()
  |> set.insert(1)
  |> set.insert(2)
  |> set.size
  == 2
  ```"
  [set member]
  (->Set (dict/insert (:dict set) member token)))

(defn contains
  "Checks whether a set contains a given member.
  
  This function runs in logarithmic time.
  
  ## Examples
  
  ```gleam
  assert set.new()
  |> set.insert(2)
  |> set.contains(2)
  ```
  
  ```gleam
  assert !{
  set.new()
  |> set.insert(2)
  |> set.contains(1)
  }
  ```"
  [set member]
  (-> (:dict set) (dict/get member) result/is-ok))

(defn delete
  "Removes a member from a set. If the set does not contain the member then
  the set is returned unchanged.
  
  This function runs in logarithmic time.
  
  ## Examples
  
  ```gleam
  assert !{
  set.new()
  |> set.insert(2)
  |> set.delete(2)
  |> set.contains(2)
  }
  ```"
  [set member]
  (->Set (dict/delete (:dict set) member)))

(defn to-list
  "Converts the set into a list of the contained members.
  
  The list has no specific ordering, any unintentional ordering may change in
  future versions of Gleam or Erlang.
  
  This function runs in linear time.
  
  ## Examples
  
  ```gleam
  assert set.new() |> set.insert(2) |> set.to_list == [2]
  ```"
  [set]
  (dict/keys (:dict set)))

(defn from-list
  "Creates a new set of the members in a given list.
  
  This function runs in loglinear time.
  
  ## Examples
  
  ```gleam
  import gleam/int
  import gleam/list
  
  assert [1, 1, 2, 4, 3, 2]
  |> set.from_list
  |> set.to_list
  |> list.sort(by: int.compare)
  == [1, 2, 3, 4]
  ```"
  [members]
  (let [dict (list/fold members
                        (dict/new*)
                        (fn [m k] (dict/insert m k token)))]
    (->Set dict)))

(defn fold
  "Combines all entries into a single value by calling a given function on each
  one.
  
  Sets are not ordered so the values are not returned in any specific order.
  Do not write code that relies on the order entries are used by this
  function as it may change in later versions of Gleam or Erlang.
  
  ## Examples
  
  ```gleam
  assert set.from_list([1, 3, 9])
  |> set.fold(0, fn(accumulator, member) { accumulator + member })
  == 13
  ```"
  [set initial reducer]
  (dict/fold (:dict set) initial (fn [a k _] (reducer a k))))

(defn filter
  "Creates a new set from an existing set, minus any members that a given
  function returns `False` for.
  
  This function runs in loglinear time.
  
  ## Examples
  
  ```gleam
  import gleam/int
  
  assert set.from_list([1, 4, 6, 3, 675, 44, 67])
  |> set.filter(keeping: int.is_even)
  |> set.to_list
  == [4, 6, 44]
  ```"
  [set predicate]
  (->Set (dict/filter (:dict set) (fn [m _] (predicate m)))))

(defn map
  "Creates a new set from a given set with the result of applying the given
  function to each member.
  
  ## Examples
  
  ```gleam
  assert set.from_list([1, 2, 3, 4])
  |> set.map(with: fn(x) { x * 2 })
  |> set.to_list
  == [2, 4, 6, 8]
  ```"
  [set fun]
  (fold set (new*) (fn [acc member] (insert acc (fun member)))))

(defn drop
  "Creates a new set from a given set with all the same entries except any
  entry found on the given list.
  
  ## Examples
  
  ```gleam
  assert set.from_list([1, 2, 3, 4])
  |> set.drop([1, 3])
  |> set.to_list
  == [2, 4]
  ```"
  [set disallowed]
  (list/fold disallowed set delete))

(defn take
  "Creates a new set from a given set, only including any members which are in
  a given list.
  
  This function runs in loglinear time.
  
  ## Examples
  
  ```gleam
  assert set.from_list([1, 2, 3])
  |> set.take([1, 3, 5])
  |> set.to_list
  == [1, 3]
  ```"
  [set desired]
  (->Set (dict/take (:dict set) desired)))

(defn- order [first' second]
  (let [subject (> (dict/size (:dict first')) (dict/size (:dict second)))]
    (if subject [first' second] [second first'])))

(defn union
  "Creates a new set that contains all members of both given sets.
  
  This function runs in loglinear time.
  
  ## Examples
  
  ```gleam
  assert set.union(set.from_list([1, 2]), set.from_list([2, 3])) |> set.to_list
  == [1, 2, 3]
  ```"
  [first' second]
  (let [[larger smaller] (order first' second)]
    (fold smaller larger insert)))

(defn intersection
  "Creates a new set that contains members that are present in both given sets.
  
  This function runs in loglinear time.
  
  ## Examples
  
  ```gleam
  assert set.intersection(set.from_list([1, 2]), set.from_list([2, 3]))
  |> set.to_list
  == [2]
  ```"
  [first' second]
  (let [[larger smaller] (order first' second)]
    (take larger (to-list smaller))))

(defn difference
  "Creates a new set that contains members that are present in the first set
  but not the second.
  
  ## Examples
  
  ```gleam
  assert set.difference(set.from_list([1, 2]), set.from_list([2, 3, 4]))
  |> set.to_list
  == [1]
  ```"
  [first' second]
  (drop first' (to-list second)))

(defn is-subset
  "Determines if a set is fully contained by another.
  
  ## Examples
  
  ```gleam
  assert set.is_subset(set.from_list([1]), set.from_list([1, 2]))
  ```
  
  ```gleam
  assert !set.is_subset(set.from_list([1, 2, 3]), set.from_list([3, 4, 5]))
  ```"
  [first' second]
  (= (intersection first' second) first'))

(defn is-disjoint
  "Determines if two sets contain no common members
  
  ## Examples
  
  ```gleam
  assert set.is_disjoint(set.from_list([1, 2, 3]), set.from_list([4, 5, 6]))
  ```
  
  ```gleam
  assert !set.is_disjoint(set.from_list([1, 2, 3]), set.from_list([3, 4, 5]))
  ```"
  [first' second]
  (= (intersection first' second) (new*)))

(defn symmetric-difference
  "Creates a new set that contains members that are present in either set, but
  not both.
  
  ## Examples
  
  ```gleam
  assert set.symmetric_difference(
  set.from_list([1, 2, 3]),
  set.from_list([3, 4]),
  )
  |> set.to_list
  == [1, 2, 4]
  ```"
  [first' second]
  (difference (union first' second) (intersection first' second)))

(defn each
  "Calls a function for each member in a set, discarding the return
  value.
  
  Useful for producing a side effect for every item of a set.
  
  The order of elements in the iteration is an implementation detail that
  should not be relied upon.
  
  ## Examples
  
  ```gleam
  let set = set.from_list([\"apple\", \"banana\", \"cherry\"])
  
  assert set.each(set, io.println) == Nil
  // apple
  // banana
  // cherry
  ```"
  [set fun]
  (fold set
        nil
        (fn [nil' member]
          (fun member)
          nil')))

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'contains [:=> [:cat [:fn (partial instance? gleam.set.Set)] :any] :boolean]
   'delete [:=> [:cat [:fn (partial instance? gleam.set.Set)] :any] [:fn (partial instance? gleam.set.Set)]]
   'difference [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:fn (partial instance? gleam.set.Set)]] [:fn (partial instance? gleam.set.Set)]]
   'drop [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:sequential :any]] [:fn (partial instance? gleam.set.Set)]]
   'each [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:=> [:cat :any] :any]] :nil]
   'filter [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:=> [:cat :any] :boolean]] [:fn (partial instance? gleam.set.Set)]]
   'fold [:=> [:cat [:fn (partial instance? gleam.set.Set)] :any [:=> [:cat :any :any] :any]] :any]
   'from-list [:=> [:cat [:sequential :any]] [:fn (partial instance? gleam.set.Set)]]
   'insert [:=> [:cat [:fn (partial instance? gleam.set.Set)] :any] [:fn (partial instance? gleam.set.Set)]]
   'intersection [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:fn (partial instance? gleam.set.Set)]] [:fn (partial instance? gleam.set.Set)]]
   'is-disjoint [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:fn (partial instance? gleam.set.Set)]] :boolean]
   'is-empty [:=> [:cat [:fn (partial instance? gleam.set.Set)]] :boolean]
   'is-subset [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:fn (partial instance? gleam.set.Set)]] :boolean]
   'map [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:=> [:cat :any] :any]] [:fn (partial instance? gleam.set.Set)]]
   'new* [:=> [:cat] [:fn (partial instance? gleam.set.Set)]]
   'size [:=> [:cat [:fn (partial instance? gleam.set.Set)]] :int]
   'symmetric-difference [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:fn (partial instance? gleam.set.Set)]] [:fn (partial instance? gleam.set.Set)]]
   'take [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:sequential :any]] [:fn (partial instance? gleam.set.Set)]]
   'to-list [:=> [:cat [:fn (partial instance? gleam.set.Set)]] [:sequential :any]]
   'union [:=> [:cat [:fn (partial instance? gleam.set.Set)] [:fn (partial instance? gleam.set.Set)]] [:fn (partial instance? gleam.set.Set)]]})
