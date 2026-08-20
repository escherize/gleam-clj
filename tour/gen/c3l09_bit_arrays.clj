(ns c3l09-bit-arrays
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (p/echo (p/bit-array (p/ba-int 3 8)) "c3l09_bit_arrays.gleam:3")
  (p/echo (= (p/bit-array (p/ba-int 3 8)) (p/bit-array (p/ba-int 3 8))) "c3l09_bit_arrays.gleam:4")
  (p/echo (p/bit-array (p/ba-int 6147 16)) "c3l09_bit_arrays.gleam:7")
  (p/echo (p/bit-array (p/ba-utf8 "Hello, Joe!")) "c3l09_bit_arrays.gleam:10")
  (let [first' (p/bit-array (p/ba-int 4 8))
        second (p/bit-array (p/ba-int 2 8))]
    (p/echo (p/bit-array first' second) "c3l09_bit_arrays.gleam:15")))

(defn -main [& _]
  (main))
