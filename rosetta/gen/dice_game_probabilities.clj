(ns dice-game-probabilities
  (:require
   [gleam.int :as int]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare main probability probability-loop roll roll-loop)

(defn main []
  (p/echo (probability 9 4 6 6) "dice_game_probabilities.gleam:4")
  (p/echo (probability 5 10 6 7) "dice_game_probabilities.gleam:5"))

(defn probability
  "Returns the probability that rolling `a` dice with `b` sides will
  have a higher total than rolling `c` dice with `d` sides."
  [a b c d]
  (probability-loop a b c d 10000 0))

(defn- probability-loop [a b c d iter wins]
  (let [s1 (> (roll a b) (roll c d))]
    (cond
      (= iter 0) (/ (int/to-float wins) 10000.0)
      s1 (recur a b c d (-' iter 1) (+' wins 1))
      (not s1) (recur a b c d (-' iter 1) wins))))

(defn- roll
  "Rolls some dice with the given number of sides and returns their total."
  [dice sides]
  (roll-loop dice sides 0))

(defn- roll-loop [dice sides total]
  (if (= dice 0)
    total
    (recur (-' dice 1) sides (+' (+' total (int/random sides)) 1))))

(defn -main [& _]
  (main))
