(ns multiplication-tables
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(declare main show)

(defn main []
  (io/write " x |")
  (int/fold-range 1 13 nil (fn [_ x] (show x 4)))
  (io/print-line (str "\n---+" (string/repeat-str "-" 48)))
  (p/with-use [[_ x] (int/fold-range 1 13 nil)]
    (show x 2)
    (io/write (str " |" (string/repeat-str " " (*' (-' x 1) 4))))
    (int/fold-range x 13 nil (fn [_ y] (show (*' x y) 4)))
    (io/print-line "")))

(defn- show [n padding]
  (-> n int/to-string (string/pad-start padding " ") io/write))

(defn -main [& _]
  (main))
