(ns c2l11-guards
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- get-first-larger [numbers limit]
  (cond
    (and (seq numbers) (> (first numbers) limit)) (let [first' (first numbers)]
                                                    first')
    (seq numbers) (let [rest' (rest numbers)]
                    (recur rest' limit))
    (empty? numbers) 0))

(defn main []
  (let [numbers (list 1 2 3 4 5)]
    (p/echo (get-first-larger numbers 3) "c2l11_guards.gleam:3")
    (p/echo (get-first-larger numbers 5) "c2l11_guards.gleam:4")))

(defn -main [& _]
  (main))
