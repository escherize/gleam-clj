(ns solve-equations-with-substitution-method
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(declare solve main)

(defn solve
  "Solves a system of two linear equations."
  [eq1 eq2]
  (let [[a1 b1 c1] eq1
        [a2 b2 c2] eq2
        x (quot (-' (*' b2 c1) (*' b1 c2)) (-' (*' b2 a1) (*' b1 a2)))
        y (quot (-' (*' a1 x) c1) (- b1))]
    [x y]))

(defn main []
  (p/echo (solve [3 1 -1] [2 -3 -19]) "solve_equations_with_substitution_method.gleam:12"))

(defn -main [& _]
  (main))
