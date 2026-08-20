(ns c0l05-ints
  (:require
   [gleam.int :as int]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (p/echo (+' 1 1) "c0l05_ints.gleam:5")
  (p/echo (-' 5 1) "c0l05_ints.gleam:6")
  (p/echo (quot 5 2) "c0l05_ints.gleam:7")
  (p/echo (*' 3 3) "c0l05_ints.gleam:8")
  (p/echo (rem 5 2) "c0l05_ints.gleam:9")
  (p/echo (> 3 (+' 1 1)) "c0l05_ints.gleam:12")
  (p/echo (< 2 (-' 1 1)) "c0l05_ints.gleam:13")
  (p/echo (>= 8 (+' 1 3)) "c0l05_ints.gleam:14")
  (p/echo (<= 8 (-' 5 3)) "c0l05_ints.gleam:15")
  (p/echo (= 2 (+' 1 1)) "c0l05_ints.gleam:18")
  (p/echo (= 2 (-' 1 1)) "c0l05_ints.gleam:19")
  (p/echo (max 42 77) "c0l05_ints.gleam:22")
  (p/echo (int/clamp 5 10 20) "c0l05_ints.gleam:23"))

(defn -main [& _]
  (main))
