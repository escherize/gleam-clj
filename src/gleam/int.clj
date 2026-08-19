(ns gleam.int
  "Shims for gleam/int. Gleam names that collide with clojure.core are
  renamed here (codegen rename table): range -> fold-range, compare -> cmp.
  int.min / int.max need no shim: codegen emits clojure.core/min & max."
  (:require [gleam.order :as order]
            [gleam.prelude :as p]))

(defn to-string [n] (str n))
(defn to-float [n] (double n))
(defn add [a b] (+ a b))
(defn absolute-value [n] (abs n))
(defn is-odd [n] (odd? n))
(defn is-even [n] (even? n))
(defn sum [lst] (reduce + 0 lst))
(defn product [lst] (reduce * 1 lst))
(defn bitwise-and [a b] (bit-and a b))
(defn bitwise-exclusive-or [a b] (bit-xor a b))
(defn bitwise-or [a b] (bit-or a b))
(defn bitwise-shift-left [n b] (bit-shift-left n b))
(defn bitwise-shift-right [n b] (bit-shift-right n b))
(defn random [n] (rand-int n))

(defn divide
  "Truncated division; Error(Nil) on zero divisor."
  [a b]
  (if (zero? b) (p/->Error nil) (p/->Ok (quot a b))))

(defn parse [s]
  (try (p/->Ok (Long/parseLong s))
       (catch NumberFormatException _ (p/->Error nil))))

(defn base-parse [s base]
  (try (p/->Ok (Long/parseLong s base))
       (catch NumberFormatException _ (p/->Error nil))))

(defn cmp
  "Shim for gleam/int.compare (rename: compare collides with clojure.core)."
  [a b]
  (order/from-int (compare a b)))

(defn fold-range
  "Shim for gleam/int.range: fold over ints from `start` (inclusive) to
  `stop` (exclusive). Counts down when start > stop, matching gleam_stdlib 1.0."
  [start stop acc reducer]
  (let [step (if (< start stop) 1 -1)]
    (reduce reducer acc (range start stop step))))

(defn subtract [a b] (-' a b))
