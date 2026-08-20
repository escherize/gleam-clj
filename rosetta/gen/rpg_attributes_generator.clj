(ns rpg-attributes-generator
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- attribute-loop [i min' total]
  (let [s1 (+' (int/random 6) 1)]
    (cond
      (= i 0) (-' total min')
      (< s1 min') (let [x s1]
                    (recur (-' i 1) x (+' total x)))
      :else (let [x s1]
              (recur (-' i 1) min' (+' total x))))))

(defn- attribute []
  (attribute-loop 4 6 0))

(defn- attributes-loop [attrs iter good-attrs total]
  (let [attr (attribute)]
    (let [s1 (and (>= good-attrs 2) (>= total 75))
          s2 (>= attr 15)]
      (cond
        (and (= iter 0) (not s1)) (recur (list) 6 0 0)
        (and (= iter 0) s1) [attrs total]
        s2 (recur (list* attr attrs) (-' iter 1) (+' good-attrs 1) (+' total attr))
        (not s2) (recur (list* attr attrs) (-' iter 1) good-attrs (+' total attr))))))

(defn attributes []
  (attributes-loop (list) 6 0 0))

(defn main []
  (let [[attrs total] (attributes)]
    (p/echo attrs "rpg_attributes_generator.gleam:6")
    (io/println (str "Total: " (int/to-string total)))))

(defn -main [& _]
  (main))
