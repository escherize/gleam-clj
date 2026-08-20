(ns jewels-and-stones
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.set :as set]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn count-jewels
  "Counts how many letters in stones are in jewels."
  [stones jewels]
  (let [jewels (-> (string/to-graphemes jewels) set/from-list)]
    (-> stones
        string/to-graphemes
        (list/count' (fn [-capture] (set/contains jewels -capture))))))

(defn main []
  (p/echo (count-jewels "aAAbbbb" "aA") "jewels_and_stones.gleam:6"))

(defn -main [& _]
  (main))
