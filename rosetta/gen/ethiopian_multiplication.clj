(ns ethiopian-multiplication
  (:require
   [gleam.int :as int]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- double [x]
  (int/bitwise-shift-left x 1))

(defn- halve [x]
  (int/bitwise-shift-right x 1))

(defn- is-even [x]
  (= (int/bitwise-and x 1) 0))

(defn- ethiopian-multiply-loop [x y product]
  (let [s2 (is-even x)]
    (cond
      (= x 0) product
      s2 (let [x x y y]
           (recur (halve x) (double y) product))
      (not s2) (let [x x y y]
                 (recur (halve x) (double y) (+' product y))))))

(defn ethiopian-multiply [x y]
  (ethiopian-multiply-loop x y 0))

(defn main []
  (let [_ (p/echo (ethiopian-multiply 17 34) "ethiopian_multiplication.gleam:4")]
    nil))

(defn -main [& _]
  (main))
