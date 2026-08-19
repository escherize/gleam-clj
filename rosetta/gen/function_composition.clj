(ns function-composition
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare compose main)

(defn compose [f g]
  (fn [x] (f (g x))))

(defn main []
  (let [times-2-plus-1 (compose (fn [x] (+' x 1)) (fn [x] (*' x 2)))]
    (p/echo (times-2-plus-1 7) "function_composition.gleam:7")))

(defn -main [& _]
  (main))
