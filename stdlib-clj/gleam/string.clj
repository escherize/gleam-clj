(ns gleam.string
  (:refer-clojure :exclude [compare concat last repeat replace reverse])
  (:require
   [gleam-ffi]
   [gleam.list :as list]
   [gleam.option :as option]
   [gleam.order :as order]
   [gleam.prelude :as p]
   [gleam.string-tree :as string_tree])
  (:import (gleam.prelude Ok)))

;; type Direction
(defrecord Leading [])
(defrecord Trailing [])

(defn is-empty
  "Determines if a `String` is empty.
  
  ## Examples
  
  ```gleam
  assert string.is_empty(\"\")
  ```
  
  ```gleam
  assert !string.is_empty(\"the world\")
  ```"
  [str']
  (= str' ""))

(def length gleam-ffi/str-length)

(defn reverse
  "Reverses a `String`.
  
  This function has to iterate across the whole `String` so it runs in linear
  time. Avoid using this in a loop.
  
  ## Examples
  
  ```gleam
  assert string.reverse(\"stressed\") == \"desserts\"
  ```"
  [string]
  (-> string
      string_tree/from-string
      string_tree/reverse
      string_tree/to-string))

(defn replace
  "Creates a new `String` by replacing all occurrences of a given substring.
  
  ## Examples
  
  ```gleam
  assert string.replace(\"www.example.com\", each: \".\", with: \"-\")
  == \"www-example-com\"
  ```
  
  ```gleam
  assert string.replace(\"a,b,c,d,e\", each: \",\", with: \"/\") == \"a/b/c/d/e\"
  ```"
  [string pattern substitute]
  (-> string
      string_tree/from-string
      (string_tree/replace pattern substitute)
      string_tree/to-string))

(def lowercase gleam-ffi/str-lowercase)

(def uppercase gleam-ffi/str-uppercase)

(def less-than gleam-ffi/str-less-than)

(defn compare
  "Compares two `String`s to see which is \"larger\" by comparing their graphemes.
  
  This does not compare the size or length of the given `String`s.
  
  ## Examples
  
  ```gleam
  import gleam/order
  
  assert string.compare(\"Anthony\", \"Anthony\") == order.Eq
  ```
  
  ```gleam
  import gleam/order
  
  assert string.compare(\"A\", \"B\") == order.Lt
  ```"
  [a b]
  (let [subject (= a b)]
    (if subject
      (order/->Eq)
      (let [subject (less-than a b)]
        (if subject (order/->Lt) (order/->Gt))))))

(def grapheme-slice gleam-ffi/grapheme-slice)

(defn slice
  "Takes a substring given a start grapheme index and a length. Negative indexes
  are taken starting from the *end* of the string.
  
  This function runs in linear time with the size of the index and the
  length. Negative indexes are linear with the size of the input string in
  addition to the other costs.
  
  ## Examples
  
  ```gleam
  assert string.slice(from: \"gleam\", at_index: 1, length: 2) == \"le\"
  ```
  
  ```gleam
  assert string.slice(from: \"gleam\", at_index: 1, length: 10) == \"leam\"
  ```
  
  ```gleam
  assert string.slice(from: \"gleam\", at_index: 10, length: 3) == \"\"
  ```
  
  ```gleam
  assert string.slice(from: \"gleam\", at_index: -2, length: 2) == \"am\"
  ```
  
  ```gleam
  assert string.slice(from: \"gleam\", at_index: -12, length: 2) == \"\"
  ```"
  [string idx len]
  (let [subject (<= len 0)]
    (if subject
      ""
      (let [subject (< idx 0)]
        (if subject
          (let [translated-idx (+' (length string) idx)]
            (let [subject (< translated-idx 0)]
              (if subject "" (grapheme-slice string translated-idx len))))
          (grapheme-slice string idx len))))))

(def unsafe-byte-slice gleam-ffi/unsafe-byte-slice)

(def crop gleam-ffi/crop)

(def byte-size gleam-ffi/byte-size)

(defn drop-start
  "Drops *n* graphemes from the start of a `String`.
  
  This function runs in linear time with the number of graphemes to drop.
  
  ## Examples
  
  ```gleam
  assert string.drop_start(from: \"The Lone Gunmen\", up_to: 2) == \"e Lone Gunmen\"
  ```"
  [string num-graphemes]
  (let [subject (<= num-graphemes 0)]
    (if subject
      string
      (let [prefix (grapheme-slice string 0 num-graphemes)
            prefix-size (byte-size prefix)]
        (unsafe-byte-slice string
                           prefix-size
                           (-' (byte-size string) prefix-size))))))

(defn drop-end
  "Drops *n* graphemes from the end of a `String`.
  
  This function traverses the full string, so it runs in linear time with the
  size of the string. Avoid using this in a loop.
  
  ## Examples
  
  ```gleam
  assert string.drop_end(from: \"Cigarette Smoking Man\", up_to: 2)
  == \"Cigarette Smoking M\"
  ```"
  [string num-graphemes]
  (let [subject (<= num-graphemes 0)]
    (if subject string (slice string 0 (-' (length string) num-graphemes)))))

(def contains gleam-ffi/str-contains)

(def starts-with gleam-ffi/starts-with)

(def ends-with gleam-ffi/ends-with)

(def pop-grapheme gleam-ffi/pop-grapheme)

(defn- to-graphemes-loop [string acc]
  (let [subject (pop-grapheme string)]
    (if (instance? Ok subject)
      (let [grapheme (nth (:value subject) 0) rest' (nth (:value subject) 1)]
        (recur rest' (list* grapheme acc)))
      acc)))

(defn to-graphemes
  "Converts a `String` to a list of
  [graphemes](https://en.wikipedia.org/wiki/Grapheme).
  
  ```gleam
  assert string.to_graphemes(\"abc\") == [\"a\", \"b\", \"c\"]
  ```"
  [string]
  (-> string (to-graphemes-loop (list)) list/reverse))

(defn split
  "Creates a list of `String`s by splitting a given string on a given substring.
  
  ## Examples
  
  ```gleam
  assert string.split(\"home/gleam/desktop/\", on: \"/\")
  == [\"home\", \"gleam\", \"desktop\", \"\"]
  ```"
  [x substring]
  (if (= substring "")
    (to-graphemes x)
    (-> x
        string_tree/from-string
        (string_tree/split substring)
        (list/map string_tree/to-string))))

(def erl-split gleam-ffi/erl-split)

(defn split-once
  "Splits a `String` a single time on the given substring.
  
  Returns an `Error` if substring not present.
  
  ## Examples
  
  ```gleam
  assert string.split_once(\"home/gleam/desktop/\", on: \"/\")
  == Ok(#(\"home\", \"gleam/desktop/\"))
  ```
  
  ```gleam
  assert string.split_once(\"home/gleam/desktop/\", on: \"?\") == Error(Nil)
  ```"
  [string substring]
  (let [subject (erl-split string substring)]
    (if (= (count subject) 2)
      (let [first' (first subject) rest' (nth subject 1)]
        (p/->Ok [first' rest']))
      (p/->Error nil))))

(defn append
  "Creates a new `String` by joining two `String`s together.
  
  This function typically copies both `String`s and runs in linear time, but
  the exact behaviour will depend on how the runtime you are using optimises
  your code. Benchmark and profile your code if you need to understand its
  performance better.
  
  If you are joining together large string and want to avoid copying any data
  you may want to investigate using the [`string_tree`](../gleam/string_tree.html)
  module.
  
  ## Examples
  
  ```gleam
  assert string.append(to: \"butter\", suffix: \"fly\") == \"butterfly\"
  ```"
  [first' second]
  (str first' second))

(defn- concat-loop [strings accumulator]
  (if (seq strings)
    (let [string (first strings) strings (rest strings)]
      (recur strings (str accumulator string)))
    accumulator))

(defn concat
  "Creates a new `String` by joining many `String`s together.
  
  This function copies all the `String`s and runs in linear time.
  
  ## Examples
  
  ```gleam
  assert string.concat([\"never\", \"the\", \"less\"]) == \"nevertheless\"
  ```"
  [strings]
  (concat-loop strings ""))

(defn- repeat-loop [times doubling-acc acc]
  (let [acc (let [subject (rem times 2)]
              (if (= subject 0) acc (str acc doubling-acc)))
        times (quot times 2)]
    (let [subject (<= times 0)]
      (if subject acc (recur times (str doubling-acc doubling-acc) acc)))))

(defn repeat
  "Creates a new `String` by repeating a `String` a given number of times.
  
  This function runs in loglinear time.
  
  ## Examples
  
  ```gleam
  assert string.repeat(\"ha\", times: 3) == \"hahaha\"
  ```"
  [string times]
  (let [subject (<= times 0)]
    (if subject "" (repeat-loop times string ""))))

(defn- join-loop [strings separator accumulator]
  (if (empty? strings)
    accumulator
    (let [string (first strings) strings (rest strings)]
      (recur strings separator (str (str accumulator separator) string)))))

(defn join
  "Joins many `String`s together with a given separator.
  
  This function runs in linear time.
  
  ## Examples
  
  ```gleam
  assert string.join([\"home\", \"evan\", \"Desktop\"], with: \"/\")
  == \"home/evan/Desktop\"
  ```"
  [strings separator]
  (if (empty? strings)
    ""
    (let [first' (first strings) rest' (rest strings)]
      (join-loop rest' separator first'))))

(defn- padding [size pad-string]
  (let [pad-string-length (length pad-string)
        num-pads (quot size pad-string-length)
        extra (rem size pad-string-length)]
    (str (repeat pad-string num-pads) (slice pad-string 0 extra))))

(defn pad-start
  "Pads the start of a `String` until it has a given length.
  
  ## Examples
  
  ```gleam
  assert string.pad_start(\"121\", to: 5, with: \".\") == \"..121\"
  ```
  
  ```gleam
  assert string.pad_start(\"121\", to: 3, with: \".\") == \"121\"
  ```
  
  ```gleam
  assert string.pad_start(\"121\", to: 2, with: \".\") == \"121\"
  ```"
  [string desired-length pad-string]
  (let [current-length (length string)
        to-pad-length (-' desired-length current-length)]
    (let [subject (<= to-pad-length 0)]
      (if subject string (str (padding to-pad-length pad-string) string)))))

(defn pad-end
  "Pads the end of a `String` until it has a given length.
  
  ## Examples
  
  ```gleam
  assert string.pad_end(\"123\", to: 5, with: \".\") == \"123..\"
  ```
  
  ```gleam
  assert string.pad_end(\"123\", to: 3, with: \".\") == \"123\"
  ```
  
  ```gleam
  assert string.pad_end(\"123\", to: 2, with: \".\") == \"123\"
  ```"
  [string desired-length pad-string]
  (let [current-length (length string)
        to-pad-length (-' desired-length current-length)]
    (let [subject (<= to-pad-length 0)]
      (if subject string (str string (padding to-pad-length pad-string))))))

(def erl-trim gleam-ffi/erl-trim)

(defn trim-end
  "Removes whitespace at the end of a `String`.
  
  ## Examples
  
  ```gleam
  assert string.trim_end(\"  hats  \\n\") == \"  hats\"
  ```"
  [string]
  (erl-trim string (->Trailing)))

(defn trim-start
  "Removes whitespace at the start of a `String`.
  
  ## Examples
  
  ```gleam
  assert string.trim_start(\"  hats  \\n\") == \"hats  \\n\"
  ```"
  [string]
  (erl-trim string (->Leading)))

(defn trim
  "Removes whitespace on both sides of a `String`.
  
  Whitespace in this function is the set of nonbreakable whitespace
  codepoints, defined as Pattern_White_Space in [Unicode Standard Annex #31][1].
  
  [1]: https://unicode.org/reports/tr31/
  
  ## Examples
  
  ```gleam
  assert string.trim(\"  hats  \\n\") == \"hats\"
  ```"
  [string]
  (-> string trim-start trim-end))

(def unsafe-int-to-utf-codepoint gleam-ffi/identity1)

(def to-utf-codepoints-loop gleam-ffi/to-utf-codepoints)

(defn- do-to-utf-codepoints [string]
  (to-utf-codepoints-loop (p/bit-array (p/ba-utf8 string)) (list)))

(def to-utf-codepoints gleam-ffi/to-utf-codepoints)

(def from-utf-codepoints gleam-ffi/from-utf-codepoints)

(defn utf-codepoint
  "Converts an integer to a `UtfCodepoint`.
  
  Returns an `Error` if the integer does not represent a valid UTF codepoint."
  [value]
  (cond
    (> value 1114111) (p/->Error nil)
    (and (>= value 55296) (<= value 57343)) (p/->Error nil)
    (< value 0) (p/->Error nil)
    :else (let [i value]
            (p/->Ok (unsafe-int-to-utf-codepoint i)))))

(def utf-codepoint-to-int gleam-ffi/identity1)

(defn to-option
  "Converts a `String` into `Option(String)` where an empty `String` becomes
  `None`.
  
  ## Examples
  
  ```gleam
  assert string.to_option(\"\") == None
  ```
  
  ```gleam
  assert string.to_option(\"hats\") == Some(\"hats\")
  ```"
  [string]
  (if (= string "") (option/->None) (option/->Some string)))

(defn first'
  "Returns the first grapheme cluster in a given `String` and wraps it in a
  `Result(String, Nil)`. If the `String` is empty, it returns `Error(Nil)`.
  Otherwise, it returns `Ok(String)`.
  
  ## Examples
  
  ```gleam
  assert string.first(\"\") == Error(Nil)
  ```
  
  ```gleam
  assert string.first(\"icecream\") == Ok(\"i\")
  ```"
  [string]
  (let [subject (pop-grapheme string)]
    (if (instance? Ok subject)
      (let [first' (nth (:value subject) 0)]
        (p/->Ok first'))
      (let [e (:value subject)]
        (p/->Error e)))))

(defn last
  "Returns the last grapheme cluster in a given `String` and wraps it in a
  `Result(String, Nil)`. If the `String` is empty, it returns `Error(Nil)`.
  Otherwise, it returns `Ok(String)`.
  
  This function traverses the full string, so it runs in linear time with the
  length of the string. Avoid using this in a loop.
  
  ## Examples
  
  ```gleam
  assert string.last(\"\") == Error(Nil)
  ```
  
  ```gleam
  assert string.last(\"icecream\") == Ok(\"m\")
  ```"
  [string]
  (let [subject (pop-grapheme string)]
    (cond
      (and (instance? Ok subject) (= (nth (:value subject) 1) "")) (let [first' (nth (:value subject) 0)]
                                                                     (p/->Ok first'))
      (instance? Ok subject) (let [rest' (nth (:value subject) 1)]
                               (p/->Ok (slice rest' -1 1)))
      (instance? gleam.prelude.Error subject) (let [e (:value subject)]
                                                (p/->Error e)))))

(defn capitalise
  "Creates a new `String` with the first grapheme in the input `String`
  converted to uppercase and the remaining graphemes to lowercase.
  
  ## Examples
  
  ```gleam
  assert string.capitalise(\"mamouna\") == \"Mamouna\"
  ```"
  [string]
  (let [subject (pop-grapheme string)]
    (if (instance? Ok subject)
      (let [first' (nth (:value subject) 0) rest' (nth (:value subject) 1)]
        (append (uppercase first') (lowercase rest')))
      "")))

(def do-inspect gleam-ffi/do-inspect)

(defn inspect
  "Returns a `String` representation of a term in Gleam syntax.
  
  This may be occasionally useful for quick-and-dirty printing of values in
  scripts. For error reporting and other uses prefer constructing strings by
  pattern matching on the values.
  
  ## Limitations
  
  The output format of this function is not stable and could change at any
  time. The output is not suitable for parsing.
  
  This function works using runtime reflection, so the output may not be
  perfectly accurate for data structures where the runtime structure doesn't
  hold enough information to determine the original syntax. For example,
  tuples with an Erlang atom in the first position will be mistaken for Gleam
  records.
  
  ## Security and safety
  
  There is no limit to how large the strings that this function can produce.
  Be careful not to call this function with large data structures or you
  could use very large amounts of memory, potentially causing runtime
  problems."
  [term]
  (-> term do-inspect string_tree/to-string))

(def remove-prefix gleam-ffi/remove-prefix)

(def remove-suffix gleam-ffi/remove-suffix)
