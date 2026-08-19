(ns digital-root
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare sum-digits sum-digits-base sum-digits-helper persistance-root persistance-root-helper task main)

(defn sum-digits [n]
  (sum-digits-base n 10))

(defn sum-digits-base [n base]
  (sum-digits-helper n base 0))

(defn- sum-digits-helper [n base acc]
  (cond
    (= n 0) (let [acc acc]
              acc)
    (< n base) (let [n n acc acc]
                 (+' acc n))
    :else (let [n n base base acc acc]
            (recur (quot n base) base (+' acc (rem n base))))))

(defn persistance-root [x]
  (persistance-root-helper (sum-digits x) 1))

(defn- persistance-root-helper [x n]
  (if (< x 10)
    (let [x x n n]
      [n x])
    (let [x x n n]
      (recur (sum-digits x) (+' n 1)))))

(defn task []
  (let [ns (list 627615 39390 588225 393900588225)
        res (list/map-over ns (fn [x] [x (persistance-root x)]))]
    (list/each res
               (fn [pair]
                 (let [[x [y z]] pair]
                   (io/print-line (str (str (str (str (-> x int/to-string) " has additive persitence ") (-> y int/to-string)) " and digital root of ") (-> z int/to-string))))))))

(defn main []
  (task))

(defn -main [& _]
  (main))
