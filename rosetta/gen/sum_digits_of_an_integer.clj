(ns sum-digits-of-an-integer
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- sum-digits-helper [n base acc]
  (cond
    (= n 0) (let [acc acc]
              acc)
    (< n base) (let [n n acc acc]
                 (+' acc n))
    :else (let [n n base base acc acc]
            (recur (quot n base) base (+' acc (rem n base))))))

(defn sum-digits-base [n base]
  (sum-digits-helper n base 0))

(defn sum-digits [n]
  (sum-digits-base n 10))

(defn main []
  (p/echo (sum-digits 1) "sum_digits_of_an_integer.gleam:18")
  (p/echo (sum-digits 1234) "sum_digits_of_an_integer.gleam:19")
  (p/echo (sum-digits-base 0xfe 16) "sum_digits_of_an_integer.gleam:20")
  (p/echo (sum-digits-base 0xf0e 16) "sum_digits_of_an_integer.gleam:21"))

(defn -main [& _]
  (main))
