(ns fizzbuzz
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn fizz-buzz [i]
  (let [s0 (rem i 3)
        s1 (rem i 5)]
    (cond
      (and (= s0 0) (= s1 0)) "FizzBuzz"
      (= s0 0) "Fizz"
      (= s1 0) "Buzz"
      :else (int/to-string i))))

(defn main []
  (-> (int/fold-range 100 0 (list) list/prepend)
      (list/map-over fizz-buzz)
      (list/each io/print-line)))

(defn -main [& _]
  (main))
