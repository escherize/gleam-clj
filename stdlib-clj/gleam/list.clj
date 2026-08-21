(ns gleam.list
  "Lists are an ordered sequence of elements and are one of the most common
  data types in Gleam.
  
  New elements can be added and removed from the front of a list in
  constant time, while adding and removing from the end requires traversing
  and copying the whole list, so keep this in mind when designing your
  programs.
  
  There is a dedicated syntax for prefixing to a list:
  
  ```gleam
  let new_list = [1, 2, ..existing_list]
  ```
  
  And a matching syntax for getting the first elements of a list:
  
  ```gleam
  case list {
  [first_element, ..rest] -> first_element
  _ -> \"this pattern matches when the list is empty\"
  }
  ```"
  (:refer-clojure :exclude [chunk drop drop-while filter find flatten interleave last map partition reduce repeat reverse shuffle sort take take-while])
  (:require
   [gleam.dict :as dict]
   [gleam.float :as float]
   [gleam.int :as int]
   [gleam.order :as order]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type ContinueOrStop
(defrecord Continue [value])
(defn Continue? "True if `v` is a Continue value." [v] (instance? Continue v))
(defrecord Stop [value])
(defn Stop? "True if `v` is a Stop value." [v] (instance? Stop v))

;; type Sorting
(defrecord Ascending [])
(defn Ascending? "True if `v` is a Ascending value." [v] (instance? Ascending v))
(defrecord Descending [])
(defn Descending? "True if `v` is a Descending value." [v] (instance? Descending v))

(defn- length-loop [list' count']
  (if (seq list')
    (let [list' (rest list')]
      (recur list' (+' count' 1)))
    count'))

(defn length
  "Counts the number of elements in a given list.
  
  This function has to traverse the list to determine the number of elements,
  so it runs in linear time.
  
  This function is natively implemented by the virtual machine and is highly
  optimised.
  
  ## Examples
  
  ```gleam
  assert list.length([]) == 0
  ```
  
  ```gleam
  assert list.length([1]) == 1
  ```
  
  ```gleam
  assert list.length([1, 2]) == 2
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]] :int]}
  [list']
  (length-loop list' 0))

(defn- count-loop [list' predicate acc]
  (if (empty? list')
    acc
    (let [first' (first list') rest' (rest list') subject (predicate first')]
      (if subject
        (recur rest' predicate (+' acc 1))
        (recur rest' predicate acc)))))

(defn count'
  "Counts the number of elements in a given list satisfying a given predicate.
  
  This function has to traverse the list to determine the number of elements,
  so it runs in linear time.
  
  ## Examples
  
  ```gleam
  assert list.count([], fn(a) { a > 0 }) == 0
  ```
  
  ```gleam
  assert list.count([1], fn(a) { a > 0 }) == 1
  ```
  
  ```gleam
  assert list.count([1, 2, 3], int.is_odd) == 2
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      :int]}
  [list' predicate]
  (count-loop list' predicate 0))

(defn- reverse-and-prepend
  "Reverses a list and prepends it to another list.
  This function runs in linear time, proportional to the length of the list
  to prepend."
  [prefix suffix]
  (if (empty? prefix)
    suffix
    (let [first' (first prefix) rest' (rest prefix)]
      (recur rest' (list* first' suffix)))))

(defn reverse
  "Creates a new list from a given list containing the same elements but in the
  opposite order.
  
  This function has to traverse the list to create the new reversed list, so
  it runs in linear time.
  
  This function is natively implemented by the virtual machine and is highly
  optimised.
  
  ## Examples
  
  ```gleam
  assert list.reverse([]) == []
  ```
  
  ```gleam
  assert list.reverse([1]) == [1]
  ```
  
  ```gleam
  assert list.reverse([1, 2]) == [2, 1]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]] [:sequential :any]]}
  [list']
  (reverse-and-prepend list' (list)))

(defn is-empty
  "Determines whether or not the list is empty.
  
  This function runs in constant time.
  
  ## Examples
  
  ```gleam
  assert list.is_empty([])
  ```
  
  ```gleam
  assert !list.is_empty([1])
  ```
  
  ```gleam
  assert !list.is_empty([1, 1])
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]] :boolean]}
  [list']
  (= list' (list)))

(defn contains
  "Determines whether or not a given element exists within a given list.
  
  This function traverses the list to find the element, so it runs in linear
  time.
  
  ## Examples
  
  ```gleam
  assert !list.contains([], any: 0)
  ```
  
  ```gleam
  assert [0] |> list.contains(any: 0)
  ```
  
  ```gleam
  assert !list.contains([1], any: 0)
  ```
  
  ```gleam
  assert !list.contains([1, 1], any: 0)
  ```
  
  ```gleam
  assert [1, 0] |> list.contains(any: 0)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :any] :boolean]}
  [list' elem]
  (cond
    (empty? list')
    false

    (and (seq list') (= (first list') elem))
    true

    (seq list')
    (let [rest' (rest list')]
      (recur rest' elem))))

(defn first'
  "Gets the first element from the start of the list, if there is one.
  
  ## Examples
  
  ```gleam
  assert list.first([]) == Error(Nil)
  ```
  
  ```gleam
  assert list.first([0]) == Ok(0)
  ```
  
  ```gleam
  assert list.first([1, 2]) == Ok(1)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list']
  (if (empty? list')
    (p/->Error nil)
    (let [first' (first list')]
      (p/->Ok first'))))

(defn rest'
  "Returns the list minus the first element. If the list is empty, `Error(Nil)` is
  returned.
  
  This function runs in constant time and does not make a copy of the list.
  
  ## Examples
  
  ```gleam
  assert list.rest([]) == Error(Nil)
  ```
  
  ```gleam
  assert list.rest([0]) == Ok([])
  ```
  
  ```gleam
  assert list.rest([1, 2]) == Ok([2])
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list']
  (if (empty? list')
    (p/->Error nil)
    (let [rest' (rest list')]
      (p/->Ok rest'))))

(defn group
  "Groups the elements from the given list by the given key function.
  
  Does not preserve the initial value order.
  
  ## Examples
  
  ```gleam
  import gleam/dict
  
  assert [Ok(3), Error(\"Wrong\"), Ok(200), Ok(73)]
  |> list.group(by: fn(i) {
  case i {
  Ok(_) -> \"Successful\"
  Error(_) -> \"Failed\"
  }
  })
  |> dict.to_list
  == [
  #(\"Failed\", [Error(\"Wrong\")]),
  #(\"Successful\", [Ok(73), Ok(200), Ok(3)]),
  ]
  ```
  
  ```gleam
  import gleam/dict
  
  assert list.group([1, 2, 3, 4, 5], by: fn(i) { i - i / 3 * 3 })
  |> dict.to_list
  == [#(0, [3]), #(1, [4, 1]), #(2, [5, 2])]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :any]]
                      [:map-of :any [:sequential :any]]]}
  [list' key]
  (dict/group key list'))

(defn- filter-loop [list' fun acc]
  (if (empty? list')
    (reverse acc)
    (let [first' (first list') rest' (rest list') new-acc (let [subject (fun first')] (if subject (list* first' acc) acc))]
      (recur rest' fun new-acc))))

(defn filter
  "Returns a new list containing only the elements from the first list for
  which the given functions returns `True`.
  
  ## Examples
  
  ```gleam
  assert list.filter([2, 4, 6, 1], fn(x) { x > 2 }) == [4, 6]
  ```
  
  ```gleam
  assert list.filter([2, 4, 6, 1], fn(x) { x > 6 }) == []
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      [:sequential :any]]}
  [list' predicate]
  (filter-loop list' predicate (list)))

(defn- filter-map-loop [list' fun acc]
  (if (empty? list')
    (reverse acc)
    (let [first' (first list') rest' (rest list') new-acc (let [subject (fun first')] (if (instance? Ok subject) (let [first' (:value subject)] (list* first' acc)) acc))]
      (recur rest' fun new-acc))))

(defn filter-map
  "Returns a new list containing only the elements from the first list for
  which the given functions returns `Ok(_)`.
  
  ## Examples
  
  ```gleam
  assert list.filter_map([2, 4, 6, 1], Error) == []
  ```
  
  ```gleam
  assert list.filter_map([2, 4, 6, 1], fn(x) { Ok(x + 1) }) == [3, 5, 7, 2]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:sequential :any]]}
  [list' fun]
  (filter-map-loop list' fun (list)))

(defn- map-loop [list' fun acc]
  (if (empty? list')
    (reverse acc)
    (let [first' (first list') rest' (rest list')]
      (recur rest' fun (list* (fun first') acc)))))

(defn map
  "Returns a new list containing the results of applying the supplied function to each element.
  
  ## Examples
  
  ```gleam
  assert list.map([2, 4, 6], fn(x) { x * 2 }) == [4, 8, 12]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :any]]
                      [:sequential :any]]}
  [list' fun]
  (map-loop list' fun (list)))

(defn- map2-loop [list1 list2 fun acc]
  (if (or (empty? list1) (empty? list2))
    (reverse acc)
    (let [a (first list1) as- (rest list1) b (first list2) bs (rest list2)]
      (recur as- bs fun (list* (fun a b) acc)))))

(defn map2
  "Combines two lists into a single list using the given function.
  
  If a list is longer than the other, the extra elements are dropped.
  
  ## Examples
  
  ```gleam
  assert list.map2([1, 2, 3], [4, 5, 6], fn(x, y) { x + y }) == [5, 7, 9]
  ```
  
  ```gleam
  assert list.map2([1, 2], [\"a\", \"b\", \"c\"], fn(i, x) { #(i, x) })
  == [#(1, \"a\"), #(2, \"b\")]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:sequential :any] [:=> [:cat :any :any] :any]]
                      [:sequential :any]]}
  [list1 list2 fun]
  (map2-loop list1 list2 fun (list)))

(defn- map-fold-loop [list' fun acc list-acc]
  (if (empty? list')
    [acc (reverse list-acc)]
    (let [first' (first list') rest' (rest list') [acc first'] (fun acc first')]
      (recur rest' fun acc (list* first' list-acc)))))

(defn map-fold
  "Similar to `map` but also lets you pass around an accumulated value.
  
  ## Examples
  
  ```gleam
  assert list.map_fold(over: [1, 2, 3], from: 100, with: fn(memo, i) {
  #(memo + i, i * 2)
  })
  == #(106, [2, 4, 6])
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :any [:=> [:cat :any :any] [:tuple :any :any]]]
                      [:tuple :any [:sequential :any]]]}
  [list' initial fun]
  (map-fold-loop list' fun initial (list)))

(defn- index-map-loop [list' fun index acc]
  (if (empty? list')
    (reverse acc)
    (let [first' (first list') rest' (rest list') acc (list* (fun first' index) acc)]
      (recur rest' fun (+' index 1) acc))))

(defn index-map
  "Similar to `map`, but the supplied function will also be passed the index
  of the element being mapped as an additional argument.
  
  The index starts at 0, so the first element is 0, the second is 1, and so
  on.
  
  ## Examples
  
  ```gleam
  assert list.index_map([\"a\", \"b\"], fn(x, i) { #(i, x) })
  == [#(0, \"a\"), #(1, \"b\")]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any :int] :any]]
                      [:sequential :any]]}
  [list' fun]
  (index-map-loop list' fun 0 (list)))

(defn- try-map-loop [list' fun acc]
  (if (empty? list')
    (p/->Ok (reverse acc))
    (let [first' (first list') rest' (rest list') subject (fun first')]
      (if (instance? Ok subject)
        (let [first' (:value subject)]
          (recur rest' fun (list* first' acc)))
        (let [error (:value subject)]
          (p/->Error error))))))

(defn try-map
  "Takes a function that returns a `Result` and applies it to each element in a
  given list in turn.
  
  If the function returns `Ok(new_value)` for all elements in the list then a
  list of the new values is returned.
  
  If the function returns `Error(reason)` for any of the elements then it is
  returned immediately. None of the elements in the list are processed after
  one returns an `Error`.
  
  ## Examples
  
  ```gleam
  assert list.try_map([1, 2, 3], fn(x) { Ok(x + 2) }) == Ok([3, 4, 5])
  ```
  
  ```gleam
  assert list.try_map([1, 2, 3], fn(_) { Error(0) }) == Error(0)
  ```
  
  ```gleam
  assert list.try_map([[1], [2, 3]], list.first) == Ok([1, 2])
  ```
  
  ```gleam
  assert list.try_map([[1], [], [2]], list.first) == Error(Nil)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' fun]
  (try-map-loop list' fun (list)))

(defn drop
  "Returns a list that is the given list with up to the given number of
  elements removed from the front of the list.
  
  If the list has less than the number of elements an empty list is
  returned.
  
  This function runs in linear time but does not copy the list.
  
  ## Examples
  
  ```gleam
  assert list.drop([1, 2, 3, 4], 2) == [3, 4]
  ```
  
  ```gleam
  assert list.drop([1, 2, 3, 4], 9) == []
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :int] [:sequential :any]]}
  [list' n]
  (let [subject (<= n 0)]
    (if subject
      list'
      (if (empty? list')
        (list)
        (let [rest' (rest list')]
          (recur rest' (-' n 1)))))))

(defn- take-loop [list' n acc]
  (let [subject (<= n 0)]
    (if subject
      (reverse acc)
      (if (empty? list')
        (reverse acc)
        (let [first' (first list') rest' (rest list')]
          (recur rest' (-' n 1) (list* first' acc)))))))

(defn take
  "Returns a list containing the first given number of elements from the given
  list.
  
  If the list has less than the number of elements then the full list is
  returned.
  
  This function runs in linear time.
  
  ## Examples
  
  ```gleam
  assert list.take([1, 2, 3, 4], 2) == [1, 2]
  ```
  
  ```gleam
  assert list.take([1, 2, 3, 4], 9) == [1, 2, 3, 4]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :int] [:sequential :any]]}
  [list' n]
  (take-loop list' n (list)))

(defn new*
  "Returns a new empty list.
  
  ## Examples
  
  ```gleam
  assert list.new() == []
  ```"
  {:malli/schema [:=> [:cat] [:sequential :any]]}
  []
  (list))

(defn wrap
  "Returns the given item wrapped in a list.
  
  ## Examples
  
  ```gleam
  assert list.wrap(1) == [1]
  ```
  
  ```gleam
  assert list.wrap([\"a\", \"b\", \"c\"]) == [[\"a\", \"b\", \"c\"]]
  ```
  
  ```gleam
  assert list.wrap([[]]) == [[[]]]
  ```"
  {:malli/schema [:=> [:cat :any] [:sequential :any]]}
  [item]
  (list item))

(defn- append-loop [first' second]
  (if (empty? first')
    second
    (let [rest' (rest first') first' (first first')]
      (recur rest' (list* first' second)))))

(defn append
  "Joins one list onto the end of another.
  
  This function runs in linear time, and it traverses and copies the first
  list.
  
  ## Examples
  
  ```gleam
  assert list.append([1, 2], [3]) == [1, 2, 3]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:sequential :any]]
                      [:sequential :any]]}
  [first' second]
  (append-loop (reverse first') second))

(defn prepend
  "Prefixes an item to a list. This can also be done using the dedicated
  syntax instead.
  
  ```gleam
  let existing_list = [2, 3, 4]
  assert [1, ..existing_list] == [1, 2, 3, 4]
  ```
  
  ```gleam
  let existing_list = [2, 3, 4]
  assert list.prepend(to: existing_list, this: 1) == [1, 2, 3, 4]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :any] [:sequential :any]]}
  [list' item]
  (list* item list'))

(defn- flatten-loop [lists acc]
  (if (empty? lists)
    (reverse acc)
    (let [list' (first lists) further-lists (rest lists)]
      (recur further-lists (reverse-and-prepend list' acc)))))

(defn flatten
  "Joins a list of lists into a single list.
  
  This function traverses all elements twice on the JavaScript target.
  This function traverses all elements once on the Erlang target.
  
  ## Examples
  
  ```gleam
  assert list.flatten([[1], [2, 3], []]) == [1, 2, 3]
  ```"
  {:malli/schema [:=> [:cat [:sequential [:sequential :any]]]
                      [:sequential :any]]}
  [lists]
  (flatten-loop lists (list)))

(defn flat-map
  "Maps the list with the given function into a list of lists, and then flattens it.
  
  ## Examples
  
  ```gleam
  assert list.flat_map([2, 4, 6], fn(x) { [x, x + 1] }) == [2, 3, 4, 5, 6, 7]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] [:sequential :any]]]
                      [:sequential :any]]}
  [list' fun]
  (flatten (map list' fun)))

(defn fold
  "Reduces a list of elements into a single value by calling a given function
  on each element, going from left to right.
  
  `fold([1, 2, 3], 0, add)` is the equivalent of
  `add(add(add(0, 1), 2), 3)`.
  
  This function runs in linear time."
  {:malli/schema [:=> [:cat [:sequential :any] :any [:=> [:cat :any :any] :any]]
                      :any]}
  [list' initial fun]
  (if (empty? list')
    initial
    (let [first' (first list') rest' (rest list')]
      (recur rest' (fun initial first') fun))))

(defn fold-right
  "Reduces a list of elements into a single value by calling a given function
  on each element, going from right to left.
  
  `fold_right([1, 2, 3], 0, add)` is the equivalent of
  `add(add(add(0, 3), 2), 1)`.
  
  This function runs in linear time.
  
  Unlike `fold` this function is not tail recursive. Where possible use
  `fold` instead as it will use less memory."
  {:malli/schema [:=> [:cat [:sequential :any] :any [:=> [:cat :any :any] :any]]
                      :any]}
  [list' initial fun]
  (if (empty? list')
    initial
    (let [first' (first list') rest' (rest list')]
      (fun (fold-right rest' initial fun) first'))))

(defn- index-fold-loop [over acc with index]
  (if (empty? over)
    acc
    (let [first' (first over) rest' (rest over)]
      (recur rest' (with acc first' index) with (+' index 1)))))

(defn index-fold
  "Like `fold` but the folding function also receives the index of the current element.
  
  ## Examples
  
  ```gleam
  assert [\"a\", \"b\", \"c\"]
  |> list.index_fold(\"\", fn(acc, item, index) {
  acc <> int.to_string(index) <> \":\" <> item <> \" \"
  })
  == \"0:a 1:b 2:c\"
  ```
  
  ```gleam
  assert [10, 20, 30]
  |> list.index_fold(0, fn(acc, item, index) { acc + item * index })
  == 80
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :any [:=> [:cat :any :any :int] :any]]
                      :any]}
  [list' initial fun]
  (index-fold-loop list' initial fun 0))

(defn try-fold
  "A variant of fold that might fail.
  
  The folding function should return `Result(accumulator, error)`.
  If the returned value is `Ok(accumulator)` try_fold will try the next value in the list.
  If the returned value is `Error(error)` try_fold will stop and return that error.
  
  ## Examples
  
  ```gleam
  assert [1, 2, 3, 4]
  |> list.try_fold(0, fn(acc, i) {
  case i < 3 {
  True -> Ok(acc + i)
  False -> Error(Nil)
  }
  })
  == Error(Nil)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :any [:=> [:cat :any :any] [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' initial fun]
  (if (empty? list')
    (p/->Ok initial)
    (let [first' (first list') rest' (rest list') subject (fun initial first')]
      (if (instance? Ok subject)
        (let [result (:value subject)]
          (recur rest' result fun))
        (let [error subject]
          error)))))

(defn fold-until
  "A variant of fold that allows to stop folding earlier.
  
  The folding function should return `ContinueOrStop(accumulator)`.
  If the returned value is `Continue(accumulator)` fold_until will try the next value in the list.
  If the returned value is `Stop(accumulator)` fold_until will stop and return that accumulator.
  
  ## Examples
  
  ```gleam
  assert [1, 2, 3, 4]
  |> list.fold_until(0, fn(acc, i) {
  case i < 3 {
  True -> Continue(acc + i)
  False -> Stop(acc)
  }
  })
  == 3
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :any [:=> [:cat :any :any] [:or [:fn Continue?] [:fn Stop?]]]]
                      :any]}
  [list' initial fun]
  (if (empty? list')
    initial
    (let [first' (first list') rest' (rest list') subject (fun initial first')]
      (if (instance? Continue subject)
        (let [next-accumulator (:value subject)]
          (recur rest' next-accumulator fun))
        (let [b (:value subject)]
          b)))))

(defn find
  "Finds the first element in a given list for which the given function returns
  `True`.
  
  Returns `Error(Nil)` if no such element is found.
  
  ## Examples
  
  ```gleam
  assert list.find([1, 2, 3], fn(x) { x > 2 }) == Ok(3)
  ```
  
  ```gleam
  assert list.find([1, 2, 3], fn(x) { x > 4 }) == Error(Nil)
  ```
  
  ```gleam
  assert list.find([], fn(_) { True }) == Error(Nil)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' is-desired]
  (if (empty? list')
    (p/->Error nil)
    (let [first' (first list') rest' (rest list') subject (is-desired first')]
      (if subject (p/->Ok first') (recur rest' is-desired)))))

(defn find-map
  "Finds the first element in a given list for which the given function returns
  `Ok(new_value)`, then returns the wrapped `new_value`.
  
  Returns `Error(Nil)` if no such element is found.
  
  ## Examples
  
  ```gleam
  assert list.find_map([[], [2], [3]], list.first) == Ok(2)
  ```
  
  ```gleam
  assert list.find_map([[], []], list.first) == Error(Nil)
  ```
  
  ```gleam
  assert list.find_map([], list.first) == Error(Nil)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' fun]
  (if (empty? list')
    (p/->Error nil)
    (let [first' (first list') rest' (rest list') subject (fun first')]
      (if (instance? Ok subject)
        (let [first' (:value subject)]
          (p/->Ok first'))
        (recur rest' fun)))))

(defn all
  "Returns `True` if the given function returns `True` for all the elements in
  the given list. If the function returns `False` for any of the elements it
  immediately returns `False` without checking the rest of the list.
  
  ## Examples
  
  ```gleam
  assert list.all([], fn(x) { x > 3 })
  ```
  
  ```gleam
  assert list.all([4, 5], fn(x) { x > 3 })
  ```
  
  ```gleam
  assert !list.all([4, 3], fn(x) { x > 3 })
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      :boolean]}
  [list' predicate]
  (if (empty? list')
    true
    (let [first' (first list') rest' (rest list') subject (predicate first')]
      (if subject (recur rest' predicate) false))))

(defn any
  "Returns `True` if the given function returns `True` for any the elements in
  the given list. If the function returns `True` for any of the elements it
  immediately returns `True` without checking the rest of the list.
  
  ## Examples
  
  ```gleam
  assert !list.any([], fn(x) { x > 3 })
  ```
  
  ```gleam
  assert list.any([4, 5], fn(x) { x > 3 })
  ```
  
  ```gleam
  assert list.any([4, 3], fn(x) { x > 4 })
  ```
  
  ```gleam
  assert list.any([3, 4], fn(x) { x > 3 })
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      :boolean]}
  [list' predicate]
  (if (empty? list')
    false
    (let [first' (first list') rest' (rest list') subject (predicate first')]
      (if subject true (recur rest' predicate)))))

(defn- zip-loop [one other acc]
  (if (and (seq one) (seq other))
    (let [first-one (first one) rest-one (rest one) first-other (first other) rest-other (rest other)]
      (recur rest-one rest-other (list* [first-one first-other] acc)))
    (reverse acc)))

(defn zip
  "Takes two lists and returns a single list of 2-element tuples.
  
  If one of the lists is longer than the other, the remaining elements from
  the longer list are not used.
  
  ## Examples
  
  ```gleam
  assert list.zip([], []) == []
  ```
  
  ```gleam
  assert list.zip([1, 2], [3]) == [#(1, 3)]
  ```
  
  ```gleam
  assert list.zip([1], [3, 4]) == [#(1, 3)]
  ```
  
  ```gleam
  assert list.zip([1, 2], [3, 4]) == [#(1, 3), #(2, 4)]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:sequential :any]]
                      [:sequential [:tuple :any :any]]]}
  [list' other]
  (zip-loop list' other (list)))

(defn- strict-zip-loop [one other acc]
  (cond
    (and (empty? one) (empty? other))
    (p/->Ok (reverse acc))

    (or (empty? one) (empty? other))
    (p/->Error nil)

    (and (seq one) (seq other))
    (let [first-one (first one) rest-one (rest one) first-other (first other) rest-other (rest other)]
      (recur rest-one rest-other (list* [first-one first-other] acc)))))

(defn strict-zip
  "Takes two lists and returns a single list of 2-element tuples.
  
  If one of the lists is longer than the other, an `Error` is returned.
  
  ## Examples
  
  ```gleam
  assert list.strict_zip([], []) == Ok([])
  ```
  
  ```gleam
  assert list.strict_zip([1, 2], [3]) == Error(Nil)
  ```
  
  ```gleam
  assert list.strict_zip([1], [3, 4]) == Error(Nil)
  ```
  
  ```gleam
  assert list.strict_zip([1, 2], [3, 4]) == Ok([#(1, 3), #(2, 4)])
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:sequential :any]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' other]
  (strict-zip-loop list' other (list)))

(defn- unzip-loop [input one other]
  (if (empty? input)
    [(reverse one) (reverse other)]
    (let [first-one (nth (first input) 0) first-other (nth (first input) 1) rest' (rest input)]
      (recur rest' (list* first-one one) (list* first-other other)))))

(defn unzip
  "Takes a single list of 2-element tuples and returns two lists.
  
  ## Examples
  
  ```gleam
  assert list.unzip([#(1, 2), #(3, 4)]) == #([1, 3], [2, 4])
  ```
  
  ```gleam
  assert list.unzip([]) == #([], [])
  ```"
  {:malli/schema [:=> [:cat [:sequential [:tuple :any :any]]]
                      [:tuple [:sequential :any] [:sequential :any]]]}
  [input]
  (unzip-loop input (list) (list)))

(defn- intersperse-loop [list' separator acc]
  (if (empty? list')
    (reverse acc)
    (let [first' (first list') rest' (rest list')]
      (recur rest' separator (list* first' separator acc)))))

(defn intersperse
  "Inserts a given value between each existing element in a given list.
  
  This function runs in linear time and copies the list.
  
  ## Examples
  
  ```gleam
  assert list.intersperse([1, 1, 1], 2) == [1, 2, 1, 2, 1]
  ```
  
  ```gleam
  assert list.intersperse([], 2) == []
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :any] [:sequential :any]]}
  [list' elem]
  (if (or (empty? list') (= (count list') 1))
    list'
    (let [first' (first list') rest' (rest list')]
      (intersperse-loop rest' elem (list first')))))

(defn- unique-loop [list' seen acc]
  (if (empty? list')
    (reverse acc)
    (let [first' (first list') rest' (rest list') subject (dict/has-key seen first')]
      (if subject
        (recur rest' seen acc)
        (recur rest' (dict/insert seen first' nil) (list* first' acc))))))

(defn unique
  "Removes any duplicate elements from a given list.
  
  This function returns in loglinear time.
  
  ## Examples
  
  ```gleam
  assert list.unique([1, 1, 1, 4, 7, 3, 3, 4]) == [1, 4, 7, 3]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]] [:sequential :any]]}
  [list']
  (unique-loop list' (dict/new*) (list)))

(defn- merge-descendings
  "This is exactly the same as merge_ascendings but mirrored: it merges two
  lists sorted in descending order into a single list sorted in ascending
  order according to the given comparator function.
  
  This reversing of the sort order is not avoidable if we want to implement
  merge as a tail recursive function. We could reverse the accumulator before
  returning it but that would end up being less efficient; so the merging
  algorithm has to play around this."
  [list1 list2 compare acc]
  (cond
    (empty? list1)
    (let [list' list2]
      (reverse-and-prepend list' acc))

    (empty? list2)
    (let [list' list1]
      (reverse-and-prepend list' acc))

    (and (seq list1) (seq list2))
    (let [first1 (first list1) rest1 (rest list1) first2 (first list2) rest2 (rest list2) subject (compare first1 first2)]
      (if (instance? gleam.order.Lt subject)
        (recur list1 rest2 compare (list* first2 acc))
        (recur rest1 list2 compare (list* first1 acc))))))

(defn- merge-descending-pairs
  "This is the same as merge_ascending_pairs but flipped for descending lists."
  [sequences compare acc]
  (cond
    (empty? sequences)
    (reverse acc)

    (= (count sequences) 1)
    (let [sequence (first sequences)]
      (reverse (list* (reverse sequence) acc)))

    (<= 2 (count sequences))
    (let [descending1 (first sequences) descending2 (nth sequences 1) rest' (nthrest sequences 2) ascending (merge-descendings descending1 descending2 compare (list))]
      (recur rest' compare (list* ascending acc)))))

(defn- merge-ascendings
  "Merges two lists sorted in ascending order into a single list sorted in
  descending order according to the given comparator function.
  
  This reversing of the sort order is not avoidable if we want to implement
  merge as a tail recursive function. We could reverse the accumulator before
  returning it but that would end up being less efficient; so the merging
  algorithm has to play around this."
  [list1 list2 compare acc]
  (cond
    (empty? list1)
    (let [list' list2]
      (reverse-and-prepend list' acc))

    (empty? list2)
    (let [list' list1]
      (reverse-and-prepend list' acc))

    (and (seq list1) (seq list2))
    (let [first1 (first list1) rest1 (rest list1) first2 (first list2) rest2 (rest list2) subject (compare first1 first2)]
      (if (instance? gleam.order.Lt subject)
        (recur rest1 list2 compare (list* first1 acc))
        (recur list1 rest2 compare (list* first2 acc))))))

(defn- merge-ascending-pairs
  "Given a list of ascending lists, it merges adjacent pairs into a single
  descending list, halving their number.
  It returns a list of the remaining descending lists."
  [sequences compare acc]
  (cond
    (empty? sequences)
    (reverse acc)

    (= (count sequences) 1)
    (let [sequence (first sequences)]
      (reverse (list* (reverse sequence) acc)))

    (<= 2 (count sequences))
    (let [ascending1 (first sequences) ascending2 (nth sequences 1) rest' (nthrest sequences 2) descending (merge-ascendings ascending1 ascending2 compare (list))]
      (recur rest' compare (list* descending acc)))))

(defn- merge-all
  "Given some some sorted sequences (assumed to be sorted in `direction`) it
  merges them all together until we're left with just a list sorted in
  ascending order."
  [sequences direction compare]
  (cond
    (empty? sequences)
    (list)

    (and (= (count sequences) 1) (instance? Ascending direction))
    (let [sequence (first sequences)]
      sequence)

    (and (= (count sequences) 1) (instance? Descending direction))
    (let [sequence (first sequences)]
      (reverse sequence))

    (instance? Ascending direction)
    (let [sequences (merge-ascending-pairs sequences compare (list))]
      (recur sequences (->Descending) compare))

    (instance? Descending direction)
    (let [sequences (merge-descending-pairs sequences compare (list))]
      (recur sequences (->Ascending) compare))))

(defn- sequences
  "Given a list it returns slices of it that are locally sorted in ascending
  order.
  
  Imagine you have this list:
  
  ```
  [1, 2, 3, 2, 1, 0]
  ^^^^^^^  ^^^^^^^ This is a slice in descending order
  |
  | This is a slice that is sorted in ascending order
  ```
  
  So the produced result will contain these two slices, each one sorted in
  ascending order: `[[1, 2, 3], [0, 1, 2]]`.
  
  - `growing` is an accumulator with the current slice being grown
  - `direction` is the growing direction of the slice being grown, it could
  either be ascending or strictly descending
  - `prev` is the previous element that needs to be added to the growing slice
  it is carried around to check whether we have to keep growing the current
  slice or not
  - `acc` is the accumulator containing the slices sorted in ascending order"
  [list' compare growing direction prev acc]
  (let [growing (list* prev growing)]
    (if (empty? list')
      (if (instance? Ascending direction)
        (list* (reverse growing) acc)
        (list* growing acc))
      (let [new (first list') rest' (rest list') s0 (compare prev new)]
        (if (or (and (instance? gleam.order.Gt s0) (instance? Descending direction)) (and (instance? gleam.order.Lt s0) (instance? Ascending direction)) (and (instance? gleam.order.Eq s0) (instance? Ascending direction)))
          (recur rest' compare growing direction new acc)
          (let [acc (if (instance? Ascending direction)
                      (list* (reverse growing) acc)
                      (list* growing acc))]
            (if (empty? rest')
              (list* (list new) acc)
              (let [next (first rest') rest' (rest rest') direction (let [subject (compare new next)] (if (or (instance? gleam.order.Lt subject) (instance? gleam.order.Eq subject)) (->Ascending) (->Descending)))]
                (recur rest' compare (list new) direction next acc)))))))))

(defn sort
  "Sorts from smallest to largest based upon the ordering specified by a given
  function.
  
  ## Examples
  
  ```gleam
  import gleam/int
  
  assert list.sort([4, 3, 6, 5, 4, 1, 2], by: int.compare)
  == [1, 2, 3, 4, 4, 5, 6]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any :any] [:or [:fn order/Lt?] [:fn order/Eq?] [:fn order/Gt?]]]]
                      [:sequential :any]]}
  [list' compare]
  (cond
    (empty? list')
    (list)

    (= (count list') 1)
    (let [x (first list')]
      (list x))

    (<= 2 (count list'))
    (let [x (first list') y (nth list' 1) rest' (nthrest list' 2) direction (let [subject (compare x y)] (if (or (instance? gleam.order.Lt subject) (instance? gleam.order.Eq subject)) (->Ascending) (->Descending))) sequences (sequences rest' compare (list x) direction y (list))]
      (merge-all sequences (->Ascending) compare))))

(defn- repeat-loop [item times acc]
  (let [subject (<= times 0)]
    (if subject acc (recur item (-' times 1) (list* item acc)))))

(defn repeat
  "Builds a list of a given value a given number of times.
  
  ## Examples
  
  ```gleam
  assert list.repeat(\"a\", times: 0) == []
  ```
  
  ```gleam
  assert list.repeat(\"a\", times: 5) == [\"a\", \"a\", \"a\", \"a\", \"a\"]
  ```"
  {:malli/schema [:=> [:cat :any :int] [:sequential :any]]}
  [a times]
  (repeat-loop a times (list)))

(defn- split-loop [list' n taken]
  (let [subject (<= n 0)]
    (if subject
      [(reverse taken) list']
      (if (empty? list')
        [(reverse taken) (list)]
        (let [first' (first list') rest' (rest list')]
          (recur rest' (-' n 1) (list* first' taken)))))))

(defn split
  "Splits a list in two before the given index.
  
  If the list is not long enough to have the given index the before list will
  be the input list, and the after list will be empty.
  
  ## Examples
  
  ```gleam
  assert list.split([6, 7, 8, 9], 0) == #([], [6, 7, 8, 9])
  ```
  
  ```gleam
  assert list.split([6, 7, 8, 9], 2) == #([6, 7], [8, 9])
  ```
  
  ```gleam
  assert list.split([6, 7, 8, 9], 4) == #([6, 7, 8, 9], [])
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :int]
                      [:tuple [:sequential :any] [:sequential :any]]]}
  [list' index]
  (split-loop list' index (list)))

(defn- split-while-loop [list' f acc]
  (if (empty? list')
    [(reverse acc) (list)]
    (let [first' (first list') rest' (rest list') subject (f first')]
      (if subject (recur rest' f (list* first' acc)) [(reverse acc) list']))))

(defn split-while
  "Splits a list in two before the first element that a given function returns
  `False` for.
  
  If the function returns `True` for all elements the first list will be the
  input list, and the second list will be empty.
  
  ## Examples
  
  ```gleam
  assert list.split_while([1, 2, 3, 4, 5], fn(x) { x <= 3 })
  == #([1, 2, 3], [4, 5])
  ```
  
  ```gleam
  assert list.split_while([1, 2, 3, 4, 5], fn(x) { x <= 5 })
  == #([1, 2, 3, 4, 5], [])
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      [:tuple [:sequential :any] [:sequential :any]]]}
  [list' predicate]
  (split-while-loop list' predicate (list)))

(defn key-find
  "Given a list of 2-element tuples, finds the first tuple that has a given
  key as the first element and returns the second element.
  
  If no tuple is found with the given key then `Error(Nil)` is returned.
  
  This function may be useful for interacting with Erlang code where lists of
  tuples are common.
  
  ## Examples
  
  ```gleam
  assert list.key_find([#(\"a\", 0), #(\"b\", 1)], \"a\") == Ok(0)
  ```
  
  ```gleam
  assert list.key_find([#(\"a\", 0), #(\"b\", 1)], \"b\") == Ok(1)
  ```
  
  ```gleam
  assert list.key_find([#(\"a\", 0), #(\"b\", 1)], \"c\") == Error(Nil)
  ```"
  {:malli/schema [:=> [:cat [:sequential [:tuple :any :any]] :any]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [keyword-list desired-key]
  (find-map keyword-list
            (fn [keyword]
              (let [[key value] keyword subject (= key desired-key)]
                (if subject (p/->Ok value) (p/->Error nil))))))

(defn key-filter
  "Given a list of 2-element tuples, finds all tuples that have a given
  key as the first element and returns the second element.
  
  This function may be useful for interacting with Erlang code where lists of
  tuples are common.
  
  ## Examples
  
  ```gleam
  assert list.key_filter([#(\"a\", 0), #(\"b\", 1), #(\"a\", 2)], \"a\") == [0, 2]
  ```
  
  ```gleam
  assert list.key_filter([#(\"a\", 0), #(\"b\", 1)], \"c\") == []
  ```"
  {:malli/schema [:=> [:cat [:sequential [:tuple :any :any]] :any]
                      [:sequential :any]]}
  [keyword-list desired-key]
  (filter-map keyword-list
              (fn [keyword]
                (let [[key value] keyword subject (= key desired-key)]
                  (if subject (p/->Ok value) (p/->Error nil))))))

(defn- key-pop-loop [list' key checked]
  (cond
    (empty? list')
    (p/->Error nil)

    (and (seq list') (= (nth (first list') 0) key))
    (let [v (nth (first list') 1) rest' (rest list')]
      (p/->Ok [v (reverse-and-prepend checked rest')]))

    (seq list')
    (let [first' (first list') rest' (rest list')]
      (recur rest' key (list* first' checked)))))

(defn key-pop
  "Given a list of 2-element tuples, finds the first tuple that has a given
  key as the first element. This function will return the second element
  of the found tuple and list with tuple removed.
  
  If no tuple is found with the given key then `Error(Nil)` is returned.
  
  ## Examples
  
  ```gleam
  assert list.key_pop([#(\"a\", 0), #(\"b\", 1)], \"a\") == Ok(#(0, [#(\"b\", 1)]))
  ```
  
  ```gleam
  assert list.key_pop([#(\"a\", 0), #(\"b\", 1)], \"b\") == Ok(#(1, [#(\"a\", 0)]))
  ```
  
  ```gleam
  assert list.key_pop([#(\"a\", 0), #(\"b\", 1)], \"c\") == Error(Nil)
  ```"
  {:malli/schema [:=> [:cat [:sequential [:tuple :any :any]] :any]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' key]
  (key-pop-loop list' key (list)))

(defn- key-set-loop [list' key value inspected]
  (cond
    (and (seq list') (= (nth (first list') 0) key))
    (let [k (nth (first list') 0) rest' (rest list')]
      (reverse-and-prepend inspected (list* [k value] rest')))

    (seq list')
    (let [first' (first list') rest' (rest list')]
      (recur rest' key value (list* first' inspected)))

    (empty? list')
    (reverse (list* [key value] inspected))))

(defn key-set
  "Given a list of 2-element tuples, inserts a key and value into the list.
  
  If there was already a tuple with the key then it is replaced, otherwise it
  is added to the end of the list.
  
  ## Examples
  
  ```gleam
  assert list.key_set([#(5, 0), #(4, 1)], 4, 100) == [#(5, 0), #(4, 100)]
  ```
  
  ```gleam
  assert list.key_set([#(5, 0), #(4, 1)], 1, 100)
  == [#(5, 0), #(4, 1), #(1, 100)]
  ```"
  {:malli/schema [:=> [:cat [:sequential [:tuple :any :any]] :any :any]
                      [:sequential [:tuple :any :any]]]}
  [list' key value]
  (key-set-loop list' key value (list)))

(defn each
  "Calls a function for each element in a list, discarding the return value.
  
  Useful for calling a side effect for every item of a list.
  
  ```gleam
  import gleam/io
  
  assert list.each([\"1\", \"2\", \"3\"], io.println) == Nil
  // 1
  // 2
  // 3
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :any]] :nil]}
  [list' f]
  (if (empty? list')
    nil
    (let [first' (first list') rest' (rest list')]
      (f first')
      (recur rest' f))))

(defn try-each
  "Calls a `Result` returning function for each element in a list, discarding
  the return value. If the function returns `Error` then the iteration is
  stopped and the error is returned.
  
  Useful for calling a side effect for every item of a list.
  
  ## Examples
  
  ```gleam
  assert list.try_each(over: [1, 2, 3], with: function_that_might_fail)
  == Ok(Nil)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] [:or [:fn p/Ok?] [:fn p/Error?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' fun]
  (if (empty? list')
    (p/->Ok nil)
    (let [first' (first list') rest' (rest list') subject (fun first')]
      (if (instance? Ok subject)
        (recur rest' fun)
        (let [e (:value subject)]
          (p/->Error e))))))

(defn- partition-loop [list' categorise trues falses]
  (if (empty? list')
    [(reverse trues) (reverse falses)]
    (let [first' (first list') rest' (rest list') subject (categorise first')]
      (if subject
        (recur rest' categorise (list* first' trues) falses)
        (recur rest' categorise trues (list* first' falses))))))

(defn partition
  "Partitions a list into a tuple/pair of lists
  by a given categorisation function.
  
  ## Examples
  
  ```gleam
  import gleam/int
  
  assert [1, 2, 3, 4, 5] |> list.partition(int.is_odd) == #([1, 3, 5], [2, 4])
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      [:tuple [:sequential :any] [:sequential :any]]]}
  [list' categorise]
  (partition-loop list' categorise (list) (list)))

(declare permutation-prepend permutation-zip permutations)

(defn- permutation-prepend [el permutations list-1 list-2 acc]
  (if (empty? permutations)
    (permutation-zip list-1 list-2 acc)
    (let [head (first permutations) tail (rest permutations)]
      (recur el tail list-1 list-2 (list* (list* el head) acc)))))

(defn- permutation-zip [list' rest' acc]
  (if (empty? list')
    (reverse acc)
    (let [head (first list') tail (rest list')]
      (permutation-prepend head
                           (permutations (reverse-and-prepend rest' tail))
                           tail
                           (list* head rest')
                           acc))))

(defn permutations
  "Returns all the permutations of a list.
  
  ## Examples
  
  ```gleam
  assert list.permutations([1, 2]) == [[1, 2], [2, 1]]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]]
                      [:sequential [:sequential :any]]]}
  [list']
  (if (empty? list')
    (list (list))
    (let [l list']
      (permutation-zip l (list) (list)))))

(defn- window-loop [acc list' n]
  (let [window (take list' n) subject (= (length window) n)]
    (if subject (recur (list* window acc) (drop list' 1) n) (reverse acc))))

(defn window
  "Returns a list of sliding windows.
  
  ## Examples
  
  ```gleam
  assert list.window([1, 2, 3, 4, 5], 3) == [[1, 2, 3], [2, 3, 4], [3, 4, 5]]
  ```
  
  ```gleam
  assert list.window([1, 2], 4) == []
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :int]
                      [:sequential [:sequential :any]]]}
  [list' n]
  (let [subject (<= n 0)]
    (if subject (list) (window-loop (list) list' n))))

(defn window-by-2
  "Returns a list of tuples containing two contiguous elements.
  
  ## Examples
  
  ```gleam
  assert list.window_by_2([1, 2, 3, 4]) == [#(1, 2), #(2, 3), #(3, 4)]
  ```
  
  ```gleam
  assert list.window_by_2([1]) == []
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]]
                      [:sequential [:tuple :any :any]]]}
  [list']
  (zip list' (drop list' 1)))

(defn drop-while
  "Drops the first elements in a given list for which the predicate function returns `True`.
  
  ## Examples
  
  ```gleam
  assert list.drop_while([1, 2, 3, 4], fn(x) { x < 3 }) == [3, 4]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      [:sequential :any]]}
  [list' predicate]
  (if (empty? list')
    (list)
    (let [first' (first list') rest' (rest list') subject (predicate first')]
      (if subject (recur rest' predicate) (list* first' rest')))))

(defn- take-while-loop [list' predicate acc]
  (if (empty? list')
    (reverse acc)
    (let [first' (first list') rest' (rest list') subject (predicate first')]
      (if subject (recur rest' predicate (list* first' acc)) (reverse acc)))))

(defn take-while
  "Takes the first elements in a given list for which the predicate function returns `True`.
  
  ## Examples
  
  ```gleam
  assert list.take_while([1, 2, 3, 2, 4], fn(x) { x < 3 }) == [1, 2]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :boolean]]
                      [:sequential :any]]}
  [list' predicate]
  (take-while-loop list' predicate (list)))

(defn- chunk-loop [list' f previous-key current-chunk acc]
  (if (seq list')
    (let [first' (first list') rest' (rest list') key (f first') subject (= key previous-key)]
      (if subject
        (recur rest' f key (list* first' current-chunk) acc)
        (let [new-acc (list* (reverse current-chunk) acc)]
          (recur rest' f key (list first') new-acc))))
    (reverse (list* (reverse current-chunk) acc))))

(defn chunk
  "Returns a list of chunks in which
  the return value of calling `f` on each element is the same.
  
  ## Examples
  
  ```gleam
  assert [1, 2, 2, 3, 4, 4, 6, 7, 7] |> list.chunk(by: fn(n) { n % 2 })
  == [[1], [2, 2], [3], [4, 4, 6], [7, 7]]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any] :any]]
                      [:sequential [:sequential :any]]]}
  [list' f]
  (if (empty? list')
    (list)
    (let [first' (first list') rest' (rest list')]
      (chunk-loop rest' f (f first') (list first') (list)))))

(defn- sized-chunk-loop [list' count' left current-chunk acc]
  (if (empty? list')
    (if (empty? current-chunk)
      (reverse acc)
      (let [remaining current-chunk]
        (reverse (list* (reverse remaining) acc))))
    (let [first' (first list') rest' (rest list') chunk (list* first' current-chunk) subject (> left 1)]
      (if subject
        (recur rest' count' (-' left 1) chunk acc)
        (recur rest' count' count' (list) (list* (reverse chunk) acc))))))

(defn sized-chunk
  "Returns a list of chunks containing `count` elements each.
  
  If the last chunk does not have `count` elements, it is instead
  a partial chunk, with less than `count` elements.
  
  For any `count` less than 1 this function behaves as if it was set to 1.
  
  ## Examples
  
  ```gleam
  assert [1, 2, 3, 4, 5, 6] |> list.sized_chunk(into: 2)
  == [[1, 2], [3, 4], [5, 6]]
  ```
  
  ```gleam
  assert [1, 2, 3, 4, 5, 6, 7, 8] |> list.sized_chunk(into: 3)
  == [[1, 2, 3], [4, 5, 6], [7, 8]]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :int]
                      [:sequential [:sequential :any]]]}
  [list' count']
  (sized-chunk-loop list' count' count' (list) (list)))

(defn reduce
  "This function acts similar to fold, but does not take an initial state.
  Instead, it starts from the first element in the list
  and combines it with each subsequent element in turn using the given
  function. The function is called as `fun(accumulator, current_element)`.
  
  Returns `Ok` to indicate a successful run, and `Error` if called on an
  empty list.
  
  ## Examples
  
  ```gleam
  assert [] |> list.reduce(fn(acc, x) { acc + x }) == Error(Nil)
  ```
  
  ```gleam
  assert [1, 2, 3, 4, 5] |> list.reduce(fn(acc, x) { acc + x }) == Ok(15)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any :any] :any]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' fun]
  (if (empty? list')
    (p/->Error nil)
    (let [first' (first list') rest' (rest list')]
      (p/->Ok (fold rest' first' fun)))))

(defn- scan-loop [list' accumulator accumulated fun]
  (if (empty? list')
    (reverse accumulated)
    (let [first' (first list') rest' (rest list') next (fun accumulator first')]
      (recur rest' next (list* next accumulated) fun))))

(defn scan
  "Similar to `fold`, but yields the state of the accumulator at each stage.
  
  ## Examples
  
  ```gleam
  assert list.scan(over: [1, 2, 3], from: 100, with: fn(acc, i) { acc + i })
  == [101, 103, 106]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :any [:=> [:cat :any :any] :any]]
                      [:sequential :any]]}
  [list' initial fun]
  (scan-loop list' initial (list) fun))

(defn last
  "Returns the last element in the given list.
  
  Returns `Error(Nil)` if the list is empty.
  
  This function runs in linear time.
  
  ## Examples
  
  ```gleam
  assert list.last([]) == Error(Nil)
  ```
  
  ```gleam
  assert list.last([1, 2, 3, 4, 5]) == Ok(5)
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list']
  (cond
    (empty? list')
    (p/->Error nil)

    (= (count list') 1)
    (let [last (first list')]
      (p/->Ok last))

    (seq list')
    (let [rest' (rest list')]
      (recur rest'))))

(defn combinations
  "Return unique combinations of elements in the list.
  
  ## Examples
  
  ```gleam
  assert list.combinations([1, 2, 3], 2) == [[1, 2], [1, 3], [2, 3]]
  ```
  
  ```gleam
  assert list.combinations([1, 2, 3, 4], 3)
  == [[1, 2, 3], [1, 2, 4], [1, 3, 4], [2, 3, 4]]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :int]
                      [:sequential [:sequential :any]]]}
  [items n]
  (cond
    (= n 0)
    (list (list))

    (empty? items)
    (list)

    (seq items)
    (let [first' (first items) rest' (rest items)]
      (-> rest'
          (combinations (-' n 1))
          (map (fn [combination] (list* first' combination)))
          reverse
          (fold (combinations rest' n) (fn [acc c] (list* c acc)))))))

(defn- combination-pairs-loop [items acc]
  (if (empty? items)
    (reverse acc)
    (let [first' (first items) rest' (rest items) first-combinations (map rest' (fn [other] [first' other])) acc (reverse-and-prepend first-combinations acc)]
      (recur rest' acc))))

(defn combination-pairs
  "Return unique pair combinations of elements in the list.
  
  ## Examples
  
  ```gleam
  assert list.combination_pairs([1, 2, 3]) == [#(1, 2), #(1, 3), #(2, 3)]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]]
                      [:sequential [:tuple :any :any]]]}
  [items]
  (combination-pairs-loop items (list)))

(defn- take-firsts [rows column remaining-rows]
  (cond
    (empty? rows)
    [(reverse column) (reverse remaining-rows)]

    (and (seq rows) (empty? (first rows)))
    (let [rest' (rest rows)]
      (recur rest' column remaining-rows))

    (and (seq rows) (seq (first rows)))
    (let [first' (first (first rows)) remaining-row (rest (first rows)) rest-rows (rest rows) remaining-rows (list* remaining-row remaining-rows)]
      (recur rest-rows (list* first' column) remaining-rows))))

(defn- transpose-loop [rows columns]
  (if (empty? rows)
    (reverse columns)
    (let [[column rest'] (take-firsts rows (list) (list))]
      (if (seq column)
        (recur rest' (list* column columns))
        (recur rest' columns)))))

(defn transpose
  "Transpose rows and columns of the list of lists.
  
  Notice: This function is not tail recursive,
  and thus may exceed stack size if called,
  with large lists (on the JavaScript target).
  
  ## Examples
  
  ```gleam
  assert list.transpose([[1, 2, 3], [101, 102, 103]])
  == [[1, 101], [2, 102], [3, 103]]
  ```"
  {:malli/schema [:=> [:cat [:sequential [:sequential :any]]]
                      [:sequential [:sequential :any]]]}
  [list-of-lists]
  (transpose-loop list-of-lists (list)))

(defn interleave
  "Make a list alternating the elements from the given lists
  
  ## Examples
  
  ```gleam
  assert list.interleave([[1, 2], [101, 102], [201, 202]])
  == [1, 101, 201, 2, 102, 202]
  ```"
  {:malli/schema [:=> [:cat [:sequential [:sequential :any]]]
                      [:sequential :any]]}
  [list']
  (-> list' transpose flatten))

(defn- shuffle-pair-unwrap-loop [list' acc]
  (if (empty? list')
    acc
    (let [elem-pair (first list') enumerable (rest list')]
      (recur enumerable (list* (nth elem-pair 1) acc)))))

(defn- do-shuffle-by-pair-indexes [list-of-pairs]
  (sort list-of-pairs
        (fn [a-pair b-pair] (float/compare (nth a-pair 0) (nth b-pair 0)))))

(defn shuffle
  "Takes a list, randomly sorts all items and returns the shuffled list.
  
  This function uses `float.random` to decide the order of the elements.
  
  ## Example
  
  ```gleam
  [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] |> list.shuffle
  // -> [1, 6, 9, 10, 3, 8, 4, 2, 7, 5]
  ```"
  {:malli/schema [:=> [:cat [:sequential :any]] [:sequential :any]]}
  [list']
  (-> list'
      (fold (list) (fn [acc a] (list* [(float/random) a] acc)))
      do-shuffle-by-pair-indexes
      (shuffle-pair-unwrap-loop (list))))

(defn- max-loop [list' compare max']
  (if (empty? list')
    max'
    (let [first' (first list') rest' (rest list') subject (compare first' max')]
      (if (instance? gleam.order.Gt subject)
        (recur rest' compare first')
        (recur rest' compare max')))))

(defn max'
  "Takes a list and a comparator, and returns the maximum element in the list
  
  ## Examples
  
  ```gleam
  assert [1, 2, 3, 4, 5] |> list.max(int.compare) == Ok(5)
  ```
  
  ```gleam
  assert [\"a\", \"c\", \"b\"] |> list.max(string.compare) == Ok(\"c\")
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] [:=> [:cat :any :any] [:or [:fn order/Lt?] [:fn order/Eq?] [:fn order/Gt?]]]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [list' compare]
  (if (empty? list')
    (p/->Error nil)
    (let [first' (first list') rest' (rest list')]
      (p/->Ok (max-loop rest' compare first')))))

(def ^:private min-positive 2.2250738585072014e-308)

(defn- log-random []
  (let [v (float/logarithm (+ (float/random) min-positive))]
    (when-not (instance? Ok v)
      (throw (ex-info "let assert failed" {:value v})))
    (let [random (:value v)]
      random)))

(defn- sample-loop [list' reservoir n w]
  (let [skip (let [v (float/logarithm (- 1.0 w))]
               (when-not (instance? Ok v)
                 (throw (ex-info "let assert failed" {:value v})))
               (let [log (:value v)]
                 (float/round (float/floor (/ (log-random) log))))) subject (drop list' skip)]
    (if (empty? subject)
      reservoir
      (let [first' (first subject) rest' (rest subject) reservoir (dict/insert reservoir (int/random n) first') w (* w (float/exponential (/ (log-random) (int/to-float n))))]
        (recur rest' reservoir n w)))))

(defn- build-reservoir-loop [list' size reservoir]
  (let [reservoir-size (dict/size reservoir) subject (>= reservoir-size size)]
    (if subject
      [reservoir list']
      (if (empty? list')
        [reservoir (list)]
        (let [first' (first list') rest' (rest list') reservoir (dict/insert reservoir reservoir-size first')]
          (recur rest' size reservoir))))))

(defn- build-reservoir
  "Builds the initial reservoir used by Algorithm L.
  This is a dictionary with keys ranging from `0` up to `n - 1` where each
  value is the corresponding element at that position in `list`.
  
  This also returns the remaining elements of `list` that didn't end up in
  the reservoir."
  [list' n]
  (build-reservoir-loop list' n (dict/new*)))

(defn sample
  "Returns a random sample of up to n elements from a list using reservoir
  sampling via [Algorithm L](https://en.wikipedia.org/wiki/Reservoir_sampling#Optimal:_Algorithm_L).
  Returns an empty list if the sample size is less than or equal to 0.
  
  Order is not random, only selection is.
  
  ## Examples
  
  ```gleam
  list.sample([1, 2, 3, 4, 5], 3)
  // -> [2, 4, 5]  // A random sample of 3 items
  ```"
  {:malli/schema [:=> [:cat [:sequential :any] :int] [:sequential :any]]}
  [list' n]
  (let [[reservoir rest'] (build-reservoir list' n) subject (dict/is-empty reservoir)]
    (if subject
      (list)
      (let [w (float/exponential (/ (log-random) (int/to-float n)))]
        (dict/values (sample-loop rest' reservoir n w))))))
