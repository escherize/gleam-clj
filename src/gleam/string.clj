(ns gleam.string
  "Shims for gleam/string. Renames for clojure.core collisions:
  repeat -> repeat-str. Graphemes are approximated by chars (v0)."
  (:require [clojure.string :as str]
            [gleam.prelude :as p]))

(defn length [s]
  (count s))

(defn byte-size [^String s]
  (alength (.getBytes s "UTF-8")))

(defn pop-grapheme
  "First grapheme (approximated by code point) and the rest, as a Result."
  [^String s]
  (if (.isEmpty s)
    (p/->Error nil)
    (let [n (Character/charCount (.codePointAt s 0))]
      (p/->Ok [(subs s 0 n) (subs s n)]))))

(defn utf-codepoint
  "Validate an int as a codepoint (no surrogates), as a Result."
  [v]
  (if (and (<= 0 v 0x10FFFF) (not (<= 0xD800 v 0xDFFF)))
    (p/->Ok v)
    (p/->Error nil)))

(defn from-utf-codepoints [cps]
  (let [sb (StringBuilder.)]
    (doseq [cp cps] (.appendCodePoint sb (int cp)))
    (str sb)))

(defn append [a b]
  (str a b))

(defn join [lst sep]
  (str/join sep lst))

(defn concat-all
  "Shim for gleam/string.concat (rename: concat collides with clojure.core)."
  [lst]
  (apply str lst))

(defn repeat-str [s times]
  (apply str (repeat times s)))

(defn pad-start [s to-length pad]
  (let [need (- to-length (count s))]
    (if (pos? need)
      (str (apply str (take need (cycle pad))) s)
      s)))

(defn to-graphemes [s]
  (mapv str s))

(defn reversed
  "Shim for gleam/string.reverse."
  [s]
  (str/reverse s))

(defn drop-start [s n]
  (subs s (min n (count s))))

(defn drop-end [s n]
  (subs s 0 (max 0 (- (count s) n))))

(defn inspect
  "Gleam's string.inspect: render a value in Gleam literal syntax."
  [v]
  (cond
    (nil? v) "Nil"
    (true? v) "True"
    (false? v) "False"
    (string? v) (pr-str v)
    (number? v) (str v)
    (record? v) (str (.getSimpleName (class v))
                     (when (seq v)
                       (str "(" (str/join ", " (map inspect (vals v))) ")")))
    (map? v) (str "dict.from_list(["
                  (str/join ", " (map (fn [[k val]]
                                        (str "#(" (inspect k) ", " (inspect val) ")"))
                                      v))
                  "])")
    (set? v) (str "set.from_list([" (str/join ", " (map inspect v)) "])")
    (vector? v) (str "#(" (str/join ", " (map inspect v)) ")")
    (sequential? v) (str "[" (str/join ", " (map inspect v)) "]")
    (fn? v) "//fn"
    :else (pr-str v)))
