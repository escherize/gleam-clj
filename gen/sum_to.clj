(ns sum-to
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare sum-to main)

(defn- sum-to
  "Sum 1..n with a tail-recursive accumulator."
  [n acc]
  (if (= n 0) acc (recur (- n 1) (+ acc n))))

(defn main
  []
  (let [v (sum-to 10 0)]
    (when-not (= v 55)
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (sum-to 1000000 0)]
    (when-not (= v 500000500000)
      (throw (ex-info "let assert failed" {:value v})))))

(defn -main [& _]
  (main))
