(ns catamorphism
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [_ (-> (list 1 4 8 3) (list/reduce1 (fn [x y] (+' x y))))
        _ (-> (list 1 4 8 3) (list/fold 100 (fn [x y] (+' x y))))]
    nil))

(defn -main [& _]
  (main))
