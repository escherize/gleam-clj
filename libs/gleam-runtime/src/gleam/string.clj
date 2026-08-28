(ns gleam.string
  "Strings are Gleam's text type, written in code using double quotes,
   `\"like this\"`.
   
   Two strings can be joined together using the concatenation operator: `<>`.
   
   Strings use the native string type of the compilation target. On Erlang
   they are UTF8 encoded binary strings, and on JavaScript they are UTF16
   encoded strings.
   
   Several escape sequences can be used in strings:
   
   `\\\"` - Double quote
   `\\\\` - Backslash
   `\\f` - Form feed
   `\\n` - Newline
   `\\r` - Carriage return
   `\\t` - Tab
   `\\u{xxxxxx}` - Unicode codepoint, where each `x` is a digit 0-9."
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
(defprotocol IDirection)
(defrecord Leading [] IDirection)
(defn Leading? "True if `v` is a Leading value." [v] (instance? Leading v))
(defrecord Trailing [] IDirection)
(defn Trailing? "True if `v` is a Trailing value." [v] (instance? Trailing v))
(defn Direction? "True if `v` is any Direction value." [v] (instance? gleam.string.IDirection v))
(defn Direction-schema
  "Malli schema for Direction."
  []
  [:or
   [:fn Leading?]
   [:fn Trailing?]])

(defn is-empty
  "is_empty(str: String) -> Bool

   Determines if a `String` is empty.

   ## Examples

   ```gleam
   assert string.is_empty(\"\")
   ```

   ```gleam
   assert !string.is_empty(\"the world\")
   ```"
  {:malli/schema [:=> [:cat :string] :boolean]
   :gleam/src "stdlib-src/src/gleam/string.gleam:37"}
  [^java.lang.String str']
  (= str' ""))

(def ^{:malli/schema [:=> [:cat :string] :int] :gleam/src "stdlib-src/src/gleam/string.gleam:62"} length gleam-ffi/str-length)

(defn reverse
  "reverse(string: String) -> String

   Reverses a `String`.

   This function has to iterate across the whole `String` so it runs in linear
   time. Avoid using this in a loop.

   ## Examples

   ```gleam
   assert string.reverse(\"stressed\") == \"desserts\"
   ```"
  {:malli/schema [:=> [:cat :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:75"}
  ^java.lang.String [^java.lang.String string]
  (-> string
      string_tree/from-string
      string_tree/reverse
      string_tree/to-string))

(defn replace
  "replace(in string: String, each pattern: String, with substitute: String) -> String

   Creates a new `String` by replacing all occurrences of a given substring.

   ## Examples

   ```gleam
   assert string.replace(\"www.example.com\", each: \".\", with: \"-\")
   == \"www-example-com\"
   ```

   ```gleam
   assert string.replace(\"a,b,c,d,e\", each: \",\", with: \"/\") == \"a/b/c/d/e\"
   ```"
  {:malli/schema [:=> [:cat :string :string :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:95"}
  ^java.lang.String [^java.lang.String string ^java.lang.String pattern ^java.lang.String substitute]
  (-> string
      string_tree/from-string
      (string_tree/replace pattern substitute)
      string_tree/to-string))

(def ^{:malli/schema [:=> [:cat :string] :string] :gleam/src "stdlib-src/src/gleam/string.gleam:119"} lowercase gleam-ffi/str-lowercase)

(def ^{:malli/schema [:=> [:cat :string] :string] :gleam/src "stdlib-src/src/gleam/string.gleam:134"} uppercase gleam-ffi/str-uppercase)

(def ^{:gleam/src "stdlib-src/src/gleam/string.gleam:167"} less-than gleam-ffi/str-less-than)

(defn compare
  "compare(a: String, b: String) -> Order

   Compares two `String`s to see which is \"larger\" by comparing their graphemes.

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
  {:malli/schema [:=> [:cat :string :string] (order/Order-schema)]
   :gleam/src "stdlib-src/src/gleam/string.gleam:154"}
  [^java.lang.String a ^java.lang.String b]
  (let [subject (= a b)]
    (if subject
      (order/->Eq)
      (let [subject (less-than a b)]
        (if subject (order/->Lt) (order/->Gt))))))

(def ^{:gleam/src "stdlib-src/src/gleam/string.gleam:221"} grapheme-slice gleam-ffi/grapheme-slice)

(defn slice
  "slice(from string: String, at_index idx: Int, length len: Int) -> String

   Takes a substring given a start grapheme index and a length. Negative indexes
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
  {:malli/schema [:=> [:cat :string :int :int] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:198"}
  ^java.lang.String [^java.lang.String string idx len]
  (let [subject (<= len 0)]
    (if subject
      ""
      (let [subject (< idx 0)]
        (if subject
          (let [translated-idx (+' (length string) idx) subject (< translated-idx 0)]
            (if subject "" (grapheme-slice string translated-idx len)))
          (grapheme-slice string idx len))))))

(def ^{:gleam/src "stdlib-src/src/gleam/string.gleam:225"} unsafe-byte-slice gleam-ffi/unsafe-byte-slice)

(def ^{:malli/schema [:=> [:cat :string :string] :string] :gleam/src "stdlib-src/src/gleam/string.gleam:239"} crop gleam-ffi/crop)

(def ^{:malli/schema [:=> [:cat :string] :int] :gleam/src "stdlib-src/src/gleam/string.gleam:874"} byte-size gleam-ffi/byte-size)

(defn drop-start
  "drop_start(from string: String, up_to num_graphemes: Int) -> String

   Drops *n* graphemes from the start of a `String`.

   This function runs in linear time with the number of graphemes to drop.

   ## Examples

   ```gleam
   assert string.drop_start(from: \"The Lone Gunmen\", up_to: 2) == \"e Lone Gunmen\"
   ```"
  {:malli/schema [:=> [:cat :string :int] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:251"}
  ^java.lang.String [^java.lang.String string num-graphemes]
  (let [subject (<= num-graphemes 0)]
    (if subject
      string
      (let [prefix (grapheme-slice string 0 num-graphemes)
            prefix-size (byte-size prefix)]
        (unsafe-byte-slice string
                           prefix-size
                           (-' (byte-size string) prefix-size))))))

(defn drop-end
  "drop_end(from string: String, up_to num_graphemes: Int) -> String

   Drops *n* graphemes from the end of a `String`.

   This function traverses the full string, so it runs in linear time with the
   size of the string. Avoid using this in a loop.

   ## Examples

   ```gleam
   assert string.drop_end(from: \"Cigarette Smoking Man\", up_to: 2)
   == \"Cigarette Smoking M\"
   ```"
  {:malli/schema [:=> [:cat :string :int] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:274"}
  ^java.lang.String [^java.lang.String string num-graphemes]
  (let [subject (<= num-graphemes 0)]
    (if subject string (slice string 0 (-' (length string) num-graphemes)))))

(def ^{:malli/schema [:=> [:cat :string :string] :boolean] :gleam/src "stdlib-src/src/gleam/string.gleam:299"} contains gleam-ffi/str-contains)

(def ^{:malli/schema [:=> [:cat :string :string] :boolean] :gleam/src "stdlib-src/src/gleam/string.gleam:311"} starts-with gleam-ffi/starts-with)

(def ^{:malli/schema [:=> [:cat :string :string] :boolean] :gleam/src "stdlib-src/src/gleam/string.gleam:323"} ends-with gleam-ffi/ends-with)

(def ^{:malli/schema [:=> [:cat :string] (p/result-of [:tuple :string :string] :nil)] :gleam/src "stdlib-src/src/gleam/string.gleam:616"} pop-grapheme gleam-ffi/pop-grapheme)

(defn- to-graphemes-loop
  "to_graphemes_loop(string: String, acc: List(String)) -> List(String)"
  {:gleam/src "stdlib-src/src/gleam/string.gleam:632"}
  [^java.lang.String string acc]
  (let [subject (pop-grapheme string)]
    (if (instance? Ok subject)
      (let [grapheme (nth (:value subject) 0) rest' (nth (:value subject) 1)]
        (recur rest' (list* grapheme acc)))
      acc)))

(defn to-graphemes
  "to_graphemes(string: String) -> List(String)

   Converts a `String` to a list of
   [graphemes](https://en.wikipedia.org/wiki/Grapheme).

   ```gleam
   assert string.to_graphemes(\"abc\") == [\"a\", \"b\", \"c\"]
   ```"
  {:malli/schema [:=> [:cat :string] [:sequential :string]]
   :gleam/src "stdlib-src/src/gleam/string.gleam:626"}
  [^java.lang.String string]
  (-> string (to-graphemes-loop (list)) list/reverse))

(defn split
  "split(x: String, on substring: String) -> List(String)

   Creates a list of `String`s by splitting a given string on a given substring.

   ## Examples

   ```gleam
   assert string.split(\"home/gleam/desktop/\", on: \"/\")
   == [\"home\", \"gleam\", \"desktop\", \"\"]
   ```"
  {:malli/schema [:=> [:cat :string :string] [:sequential :string]]
   :gleam/src "stdlib-src/src/gleam/string.gleam:334"}
  [^java.lang.String x ^java.lang.String substring]
  (if (= substring "")
    (to-graphemes x)
    (-> x
        string_tree/from-string
        (string_tree/split substring)
        (list/map string_tree/to-string))))

(def ^{:gleam/src "stdlib-src/src/gleam/string.gleam:372"} erl-split gleam-ffi/erl-split)

(defn split-once
  "split_once(string: String, on substring: String) -> Result(#(String, String), Nil)

   Splits a `String` a single time on the given substring.

   Returns an `Error` if substring not present.

   ## Examples

   ```gleam
   assert string.split_once(\"home/gleam/desktop/\", on: \"/\")
   == Ok(#(\"home\", \"gleam/desktop/\"))
   ```

   ```gleam
   assert string.split_once(\"home/gleam/desktop/\", on: \"?\") == Error(Nil)
   ```"
  {:malli/schema [:=> [:cat :string :string]
                      (p/result-of [:tuple :string :string] :nil)]
   :gleam/src "stdlib-src/src/gleam/string.gleam:361"}
  [^java.lang.String string ^java.lang.String substring]
  (let [subject (erl-split string substring)]
    (if (= (count subject) 2)
      (let [first' (first subject) rest' (nth subject 1)]
        (p/->Ok [first' rest']))
      (p/->Error nil))))

(defn append
  "append(to first: String, suffix second: String) -> String

   Creates a new `String` by joining two `String`s together.

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
  {:malli/schema [:=> [:cat :string :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:391"}
  ^java.lang.String [^java.lang.String first' ^java.lang.String second]
  (str first' second))

(defn- concat-loop
  "concat_loop(strings: List(String), accumulator: String) -> String"
  {:gleam/src "stdlib-src/src/gleam/string.gleam:410"}
  ^java.lang.String [strings ^java.lang.String accumulator]
  (if (seq strings)
    (let [string (first strings) strings (rest strings)]
      (recur strings (str accumulator string)))
    accumulator))

(defn concat
  "concat(strings: List(String)) -> String

   Creates a new `String` by joining many `String`s together.

   This function copies all the `String`s and runs in linear time.

   ## Examples

   ```gleam
   assert string.concat([\"never\", \"the\", \"less\"]) == \"nevertheless\"
   ```"
  {:malli/schema [:=> [:cat [:sequential :string]] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:406"}
  ^java.lang.String [strings]
  (concat-loop strings ""))

(defn- repeat-loop
  "repeat_loop(times: Int, doubling_acc: String, acc: String) -> String"
  {:gleam/src "stdlib-src/src/gleam/string.gleam:434"}
  ^java.lang.String [times ^java.lang.String doubling-acc ^java.lang.String acc]
  (let [acc (let [subject (rem times 2)]
              (if (= subject 0) acc (str acc doubling-acc)))
        times (quot times 2) subject (<= times 0)]
    (if subject acc (recur times (str doubling-acc doubling-acc) acc))))

(defn repeat
  "repeat(string: String, times times: Int) -> String

   Creates a new `String` by repeating a `String` a given number of times.

   This function runs in loglinear time.

   ## Examples

   ```gleam
   assert string.repeat(\"ha\", times: 3) == \"hahaha\"
   ```"
  {:malli/schema [:=> [:cat :string :int] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:427"}
  ^java.lang.String [^java.lang.String string times]
  (let [subject (<= times 0)]
    (if subject "" (repeat-loop times string ""))))

(def ^{:malli/schema [:=> [:cat [:sequential :string] :string] :string] :gleam/src "stdlib-src/src/gleam/string.gleam:457"} join gleam-ffi/str-join)

(defn- padding
  "padding(size: Int, pad_string: String) -> String"
  {:gleam/src "stdlib-src/src/gleam/string.gleam:536"}
  ^java.lang.String [size ^java.lang.String pad-string]
  (let [pad-string-length (length pad-string)
        num-pads (quot size pad-string-length)
        extra (rem size pad-string-length)]
    (str (repeat pad-string num-pads) (slice pad-string 0 extra))))

(defn pad-start
  "pad_start(string: String, to desired_length: Int, with pad_string: String) -> String

   Pads the start of a `String` until it has a given length.

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
  {:malli/schema [:=> [:cat :string :int :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:492"}
  ^java.lang.String [^java.lang.String string desired-length ^java.lang.String pad-string]
  (let [current-length (length string)
        to-pad-length (-' desired-length current-length) subject (<= to-pad-length 0)]
    (if subject string (str (padding to-pad-length pad-string) string))))

(defn pad-end
  "pad_end(string: String, to desired_length: Int, with pad_string: String) -> String

   Pads the end of a `String` until it has a given length.

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
  {:malli/schema [:=> [:cat :string :int :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:522"}
  ^java.lang.String [^java.lang.String string desired-length ^java.lang.String pad-string]
  (let [current-length (length string)
        to-pad-length (-' desired-length current-length) subject (<= to-pad-length 0)]
    (if subject string (str string (padding to-pad-length pad-string)))))

(def ^{:gleam/src "stdlib-src/src/gleam/string.gleam:562"} erl-trim gleam-ffi/erl-trim)

(defn trim-end
  "trim_end(string: String) -> String

   Removes whitespace at the end of a `String`.

   ## Examples

   ```gleam
   assert string.trim_end(\"  hats  \\n\") == \"  hats\"
   ```"
  {:malli/schema [:=> [:cat :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:591"}
  ^java.lang.String [^java.lang.String string]
  (erl-trim string (->Trailing)))

(defn trim-start
  "trim_start(string: String) -> String

   Removes whitespace at the start of a `String`.

   ## Examples

   ```gleam
   assert string.trim_start(\"  hats  \\n\") == \"hats  \\n\"
   ```"
  {:malli/schema [:=> [:cat :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:578"}
  ^java.lang.String [^java.lang.String string]
  (erl-trim string (->Leading)))

(defn trim
  "trim(string: String) -> String

   Removes whitespace on both sides of a `String`.

   Whitespace in this function is the set of nonbreakable whitespace
   codepoints, defined as Pattern_White_Space in [Unicode Standard Annex #31][1].

   [1]: https://unicode.org/reports/tr31/

   ## Examples

   ```gleam
   assert string.trim(\"  hats  \\n\") == \"hats\"
   ```"
  {:malli/schema [:=> [:cat :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:557"}
  ^java.lang.String [^java.lang.String string]
  (-> string trim-start trim-end))

(def ^{:gleam/src "stdlib-src/src/gleam/string.gleam:641"} unsafe-int-to-utf-codepoint gleam-ffi/identity1)

(def ^{:malli/schema [:=> [:cat :string] [:sequential :int]] :gleam/src "stdlib-src/src/gleam/string.gleam:668"} to-utf-codepoints gleam-ffi/to-utf-codepoints)

(def ^{:malli/schema [:=> [:cat [:sequential :int]] :string] :gleam/src "stdlib-src/src/gleam/string.gleam:717"} from-utf-codepoints gleam-ffi/from-utf-codepoints)

(defn utf-codepoint
  "utf_codepoint(value: Int) -> Result(UtfCodepoint, Nil)

   Converts an integer to a `UtfCodepoint`.

   Returns an `Error` if the integer does not represent a valid UTF codepoint."
  {:malli/schema [:=> [:cat :int] (p/result-of :int :nil)]
   :gleam/src "stdlib-src/src/gleam/string.gleam:723"}
  [value]
  (cond
    (> value 1114111)
    (p/->Error nil)

    (and (>= value 55296) (<= value 57343))
    (p/->Error nil)

    (< value 0)
    (p/->Error nil)

    :else
    (let [i value]
      (p/->Ok (unsafe-int-to-utf-codepoint i)))))

(def ^{:malli/schema [:=> [:cat :int] :int] :gleam/src "stdlib-src/src/gleam/string.gleam:743"} utf-codepoint-to-int gleam-ffi/identity1)

(defn to-option
  "to_option(string: String) -> Option(String)

   Converts a `String` into `Option(String)` where an empty `String` becomes
   `None`.

   ## Examples

   ```gleam
   assert string.to_option(\"\") == None
   ```

   ```gleam
   assert string.to_option(\"hats\") == Some(\"hats\")
   ```"
  {:malli/schema [:=> [:cat :string] (option/Option-schema :string)]
   :gleam/src "stdlib-src/src/gleam/string.gleam:758"}
  [^java.lang.String string]
  (if (= string "") (option/->None) (option/->Some string)))

(defn first'
  "first(string: String) -> Result(String, Nil)

   Returns the first grapheme cluster in a given `String` and wraps it in a
   `Result(String, Nil)`. If the `String` is empty, it returns `Error(Nil)`.
   Otherwise, it returns `Ok(String)`.

   ## Examples

   ```gleam
   assert string.first(\"\") == Error(Nil)
   ```

   ```gleam
   assert string.first(\"icecream\") == Ok(\"i\")
   ```"
  {:malli/schema [:=> [:cat :string] (p/result-of :string :nil)]
   :gleam/src "stdlib-src/src/gleam/string.gleam:779"}
  [^java.lang.String string]
  (let [subject (pop-grapheme string)]
    (if (instance? Ok subject)
      (let [first' (nth (:value subject) 0)]
        (p/->Ok first'))
      (let [e (:value subject)]
        (p/->Error e)))))

(defn last
  "last(string: String) -> Result(String, Nil)

   Returns the last grapheme cluster in a given `String` and wraps it in a
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
  {:malli/schema [:=> [:cat :string] (p/result-of :string :nil)]
   :gleam/src "stdlib-src/src/gleam/string.gleam:803"}
  [^java.lang.String string]
  (let [subject (pop-grapheme string)]
    (cond
      (and (instance? Ok subject) (= (nth (:value subject) 1) ""))
      (let [first' (nth (:value subject) 0)]
        (p/->Ok first'))

      (instance? Ok subject)
      (let [rest' (nth (:value subject) 1)]
        (p/->Ok (slice rest' -1 1)))

      (instance? gleam.prelude.Error subject)
      (let [e (:value subject)]
        (p/->Error e)))))

(defn capitalise
  "capitalise(string: String) -> String

   Creates a new `String` with the first grapheme in the input `String`
   converted to uppercase and the remaining graphemes to lowercase.

   ## Examples

   ```gleam
   assert string.capitalise(\"mamouna\") == \"Mamouna\"
   ```"
  {:malli/schema [:=> [:cat :string] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:820"}
  ^java.lang.String [^java.lang.String string]
  (let [subject (pop-grapheme string)]
    (if (instance? Ok subject)
      (let [first' (nth (:value subject) 0) rest' (nth (:value subject) 1)]
        (append (uppercase first') (lowercase rest')))
      "")))

(def ^{:gleam/src "stdlib-src/src/gleam/string.gleam:859"} do-inspect gleam-ffi/do-inspect)

(defn inspect
  "inspect(term: a) -> String

   Returns a `String` representation of a term in Gleam syntax.

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
  {:malli/schema [:=> [:cat :any] :string]
   :gleam/src "stdlib-src/src/gleam/string.gleam:851"}
  ^java.lang.String [term]
  (-> term do-inspect string_tree/to-string))

(def ^{:malli/schema [:=> [:cat :string :string] :string] :gleam/src "stdlib-src/src/gleam/string.gleam:893"} remove-prefix gleam-ffi/remove-prefix)

(def ^{:malli/schema [:=> [:cat :string :string] :string] :gleam/src "stdlib-src/src/gleam/string.gleam:912"} remove-suffix gleam-ffi/remove-suffix)
