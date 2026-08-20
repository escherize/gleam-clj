(ns conditional-structures
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn conditional-example [x]
  (cond
    (< x 0) (let [x x]
              (io/print-line (str (-> x int/to-string) " is negative")))
    (= x 0) (let [x x]
              (io/print-line (str (-> x int/to-string) " is zero")))
    :else (io/print-line (str (-> x int/to-string) " is positive"))))

(defn main []
  (conditional-example -10)
  (conditional-example 0)
  (conditional-example 10))

(defn -main [& _]
  (main))
