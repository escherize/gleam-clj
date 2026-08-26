(ns sum-to
  (:require
   [gleam.prelude :as p]))

(defn- sum-to
  "sum_to(n: Int, acc: Int) -> Int

   Sum 1..n with a tail-recursive accumulator."
  {:gleam/src "sum_to.gleam:2"}
  [n acc]
  (if (= n 0) acc (recur (-' n 1) (+' acc n))))

(defn main
  "main() -> Int"
  {:malli/schema [:=> [:cat] :int] :gleam/src "sum_to.gleam:9"}
  []
  (p/let-assert 55 (sum-to 10 0))
  (p/let-assert 500000500000 (sum-to 1000000 0)))

(defn -main [& _]
  (main))
