(ns greatest-common-divisor
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- gcd-loop [a b]
  (if (= b 0) a (recur b (rem a b))))

(defn gcd [a b]
  (gcd-loop (int/absolute-value a) (int/absolute-value b)))

(defn main []
  (-> (gcd 40902 24140) int/to-string io/println))

(defn -main [& _]
  (main))
