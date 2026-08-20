(ns multiplication-tables
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn- show [n padding]
  (-> n int/to-string (string/pad-start padding " ") io/print))

(defn main []
  (io/print " x |")
  (int/range 1 13 nil (fn [_ x] (show x 4)))
  (io/println (str "\n---+" (string/repeat "-" 48)))
  (p/with-use [[_ x] (int/range 1 13 nil)]
    (show x 2)
    (io/print (str " |" (string/repeat " " (*' (-' x 1) 4))))
    (int/range x 13 nil (fn [_ y] (show (*' x y) 4)))
    (io/println "")))

(defn -main [& _]
  (main))
