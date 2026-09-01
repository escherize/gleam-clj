(ns gleam.set
  (:refer-clojure :exclude [drop filter map take])
  (:require
   [gleam.dict :as dict]
   [gleam.list :as list]
   [gleam.result :as result]))

;; type Set
(defprotocol ISet)
(defrecord Set [dict] ISet)
(alter-meta! #'->Set assoc :private true)
(alter-meta! #'map->Set assoc :private true)
(defn Set? "True if `v` is a Set value." [v] (instance? Set v))
(defn Set-schema
  "Malli schema for Set(member)."
  [member]
  [:and [:fn Set?] [:map [:dict [:map-of member [:sequential :nil]]]]])

(defn new*
  "new() -> Set(a)

   Creates a new empty set."
  {:malli/schema [:=> [:cat] (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:32"}
  []
  (->Set (dict/new*)))

(defn size
  "size(set: Set(a)) -> Int

   Gets the number of members in a set.

   This function runs in constant time.

   ## Examples

   ```gleam
   assert set.new()
   |> set.insert(1)
   |> set.insert(2)
   |> set.size
   == 2
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any)] :int]
   :gleam/src "stdlib-src/src/gleam/set.gleam:50"}
  [set]
  (dict/size (:dict set)))

(defn is-empty
  "is_empty(set: Set(a)) -> Bool

   Determines whether or not the set is empty.

   ## Examples

   ```gleam
   assert set.new() |> set.is_empty
   ```

   ```gleam
   assert !{ set.new() |> set.insert(1) |> set.is_empty }
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any)] :boolean]
   :gleam/src "stdlib-src/src/gleam/set.gleam:66"}
  [set]
  (= set (new*)))

(def ^:private token (list))

(defn insert
  "insert(into set: Set(a), this member: a) -> Set(a)

   Inserts a member into the set.

   This function runs in logarithmic time.

   ## Examples

   ```gleam
   assert set.new()
   |> set.insert(1)
   |> set.insert(2)
   |> set.size
   == 2
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) :any] (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:84"}
  [set member]
  (->Set (dict/insert (:dict set) member token)))

(defn contains
  "contains(in set: Set(a), this member: a) -> Bool

   Checks whether a set contains a given member.

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
  {:malli/schema [:=> [:cat (Set-schema :any) :any] :boolean]
   :gleam/src "stdlib-src/src/gleam/set.gleam:108"}
  [set member]
  (-> (:dict set) (dict/get member) result/is-ok))

(defn delete
  "delete(from set: Set(a), this member: a) -> Set(a)

   Removes a member from a set. If the set does not contain the member then
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
  {:malli/schema [:=> [:cat (Set-schema :any) :any] (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:130"}
  [set member]
  (->Set (dict/delete (:dict set) member)))

(defn to-list
  "to_list(set: Set(a)) -> List(a)

   Converts the set into a list of the contained members.

   The list has no specific ordering, any unintentional ordering may change in
   future versions of Gleam or Erlang.

   This function runs in linear time.

   ## Examples

   ```gleam
   assert set.new() |> set.insert(2) |> set.to_list == [2]
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any)] [:sequential :any]]
   :gleam/src "stdlib-src/src/gleam/set.gleam:147"}
  [set]
  (dict/keys (:dict set)))

(defn from-list
  "from_list(members: List(a)) -> Set(a)

   Creates a new set of the members in a given list.

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
  {:malli/schema [:=> [:cat [:sequential :any]] (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:168"}
  [members]
  (let [dict (list/fold members
                        (dict/new*)
                        (fn [m k] (dict/insert m k token)))]
    (->Set dict)))

(defn fold
  "fold(over set: Set(a), from initial: b, with reducer: fn(b, a) -> b) -> b

   Combines all entries into a single value by calling a given function on each
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
  {:malli/schema [:=> [:cat (Set-schema :any) :any [:=> [:cat :any :any] :any]]
                      :any]
   :gleam/src "stdlib-src/src/gleam/set.gleam:191"}
  [set initial reducer]
  (dict/fold (:dict set) initial (fn [a k _] (reducer a k))))

(defn filter
  "filter(in set: Set(a), keeping predicate: fn(a) -> Bool) -> Set(a)

   Creates a new set from an existing set, minus any members that a given
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
  {:malli/schema [:=> [:cat (Set-schema :any) [:=> [:cat :any] :boolean]]
                      (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:215"}
  [set predicate]
  (->Set (dict/filter (:dict set) (fn [m _] (predicate m)))))

(defn map
  "map(set: Set(a), with fun: fn(a) -> b) -> Set(b)

   Creates a new set from a given set with the result of applying the given
   function to each member.

   ## Examples

   ```gleam
   assert set.from_list([1, 2, 3, 4])
   |> set.map(with: fn(x) { x * 2 })
   |> set.to_list
   == [2, 4, 6, 8]
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) [:=> [:cat :any] :any]]
                      (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:234"}
  [set fun]
  (fold set (new*) (fn [acc member] (insert acc (fun member)))))

(defn drop
  "drop(from set: Set(a), drop disallowed: List(a)) -> Set(a)

   Creates a new set from a given set with all the same entries except any
   entry found on the given list.

   ## Examples

   ```gleam
   assert set.from_list([1, 2, 3, 4])
   |> set.drop([1, 3])
   |> set.to_list
   == [2, 4]
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) [:sequential :any]]
                      (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:252"}
  [set disallowed]
  (list/fold disallowed set delete))

(defn take
  "take(from set: Set(a), keeping desired: List(a)) -> Set(a)

   Creates a new set from a given set, only including any members which are in
   a given list.

   This function runs in loglinear time.

   ## Examples

   ```gleam
   assert set.from_list([1, 2, 3])
   |> set.take([1, 3, 5])
   |> set.to_list
   == [1, 3]
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) [:sequential :any]]
                      (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:273"}
  [set desired]
  (->Set (dict/take (:dict set) desired)))

(defn- order
  "order(first: Set(a), second: Set(a)) -> #(Set(a), Set(a))"
  {:gleam/src "stdlib-src/src/gleam/set.gleam:296"}
  [first' second]
  (let [subject (> (dict/size (:dict first')) (dict/size (:dict second)))]
    (if subject [first' second] [second first'])))

(defn union
  "union(of first: Set(a), and second: Set(a)) -> Set(a)

   Creates a new set that contains all members of both given sets.

   This function runs in loglinear time.

   ## Examples

   ```gleam
   assert set.union(set.from_list([1, 2]), set.from_list([2, 3])) |> set.to_list
   == [1, 2, 3]
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) (Set-schema :any)]
                      (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:291"}
  [first' second]
  (let [[larger smaller] (order first' second)]
    (fold smaller larger insert)))

(defn intersection
  "intersection(of first: Set(a), and second: Set(a)) -> Set(a)

   Creates a new set that contains members that are present in both given sets.

   This function runs in loglinear time.

   ## Examples

   ```gleam
   assert set.intersection(set.from_list([1, 2]), set.from_list([2, 3]))
   |> set.to_list
   == [2]
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) (Set-schema :any)]
                      (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:318"}
  [first' second]
  (let [[larger smaller] (order first' second)]
    (take larger (to-list smaller))))

(defn difference
  "difference(from first: Set(a), minus second: Set(a)) -> Set(a)

   Creates a new set that contains members that are present in the first set
   but not the second.

   ## Examples

   ```gleam
   assert set.difference(set.from_list([1, 2]), set.from_list([2, 3, 4]))
   |> set.to_list
   == [1]
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) (Set-schema :any)]
                      (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:337"}
  [first' second]
  (drop first' (to-list second)))

(defn is-subset
  "is_subset(first: Set(a), of second: Set(a)) -> Bool

   Determines if a set is fully contained by another.

   ## Examples

   ```gleam
   assert set.is_subset(set.from_list([1]), set.from_list([1, 2]))
   ```

   ```gleam
   assert !set.is_subset(set.from_list([1, 2, 3]), set.from_list([3, 4, 5]))
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) (Set-schema :any)] :boolean]
   :gleam/src "stdlib-src/src/gleam/set.gleam:356"}
  [first' second]
  (= (intersection first' second) first'))

(defn is-disjoint
  "is_disjoint(first: Set(a), from second: Set(a)) -> Bool

   Determines if two sets contain no common members

   ## Examples

   ```gleam
   assert set.is_disjoint(set.from_list([1, 2, 3]), set.from_list([4, 5, 6]))
   ```

   ```gleam
   assert !set.is_disjoint(set.from_list([1, 2, 3]), set.from_list([3, 4, 5]))
   ```"
  {:malli/schema [:=> [:cat (Set-schema :any) (Set-schema :any)] :boolean]
   :gleam/src "stdlib-src/src/gleam/set.gleam:372"}
  [first' second]
  (= (intersection first' second) (new*)))

(defn symmetric-difference
  "symmetric_difference(of first: Set(a), and second: Set(a)) -> Set(a)

   Creates a new set that contains members that are present in either set, but
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
  {:malli/schema [:=> [:cat (Set-schema :any) (Set-schema :any)]
                      (Set-schema :any)]
   :gleam/src "stdlib-src/src/gleam/set.gleam:390"}
  [first' second]
  (difference (union first' second) (intersection first' second)))

(defn each
  "each(set: Set(a), fun: fn(a) -> b) -> Nil

   Calls a function for each member in a set, discarding the return
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
  {:malli/schema [:=> [:cat (Set-schema :any) [:=> [:cat :any] :any]] :nil]
   :gleam/src "stdlib-src/src/gleam/set.gleam:419"}
  [set fun]
  (fold set
        nil
        (fn [nil_ member]
          (fun member)
          nil_)))
