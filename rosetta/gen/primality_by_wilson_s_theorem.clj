(ns primality-by-wilson-s-theorem
  (:require
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- factorial-loop [n product]
  (if (= n 0) product (recur (-' n 1) (*' n product))))

(defn- factorial [n]
  (factorial-loop n 1))

(defn is-wprime [n]
  (if (< n 2) false (= (rem (+' (factorial (-' n 1)) 1) n) 0)))

(defn main []
  (-> (int/fold-range 100 1 (list) list/prepend)
      (list/keep-if is-wprime)
      (p/echo)))

(defn -main [& _]
  (main))
