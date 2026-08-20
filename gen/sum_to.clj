(ns sum-to
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- sum-to
  "Sum 1..n with a tail-recursive accumulator."
  [n acc]
  (if (= n 0) acc (recur (-' n 1) (+' acc n))))

(defn main []
  (p/let-assert 55 (sum-to 10 0))
  (p/let-assert 500000500000 (sum-to 1000000 0)))

(defn -main [& _]
  (main))

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'main [:=> [:cat] :int]})
