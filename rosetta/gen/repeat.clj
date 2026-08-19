(ns repeat
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare main repeat)

(defn main []
  (repeat 3 (fn [] (io/print-line "hello"))))

(defn repeat [times func]
  (if (< times 1)
    nil
    (do (func)
        (recur (-' times 1) func))))

(defn -main [& _]
  (main))
