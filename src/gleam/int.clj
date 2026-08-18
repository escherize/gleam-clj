(ns gleam.int
  "Shims for gleam/int. Gleam names that collide with clojure.core are
  renamed here (codegen rename table): range -> fold-range.
  int.min / int.max need no shim: codegen emits clojure.core/min & max.")

(defn fold-range
  "Shim for gleam/int.range: fold over ints from `start` (inclusive) to
  `stop` (exclusive). Counts down when start > stop, matching gleam_stdlib 1.0."
  [start stop acc reducer]
  (let [step (if (< start stop) 1 -1)]
    (reduce reducer acc (range start stop step))))
