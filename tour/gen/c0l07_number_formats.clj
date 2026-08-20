(ns c0l07-number-formats
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (p/echo 1000000 "c0l07_number_formats.gleam:3")
  (p/echo 10000.01 "c0l07_number_formats.gleam:4")
  (p/echo 2r00001111 "c0l07_number_formats.gleam:7")
  (p/echo 8r17 "c0l07_number_formats.gleam:8")
  (p/echo 0xF "c0l07_number_formats.gleam:9")
  (p/echo 7.0e7 "c0l07_number_formats.gleam:12")
  (p/echo 3.0e-4 "c0l07_number_formats.gleam:13"))

(defn -main [& _]
  (main))
