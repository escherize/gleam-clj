(ns symmetric-difference
  (:require
   [gleam.prelude :as p]
   [gleam.set :as set])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [seta (set/from-list (list "John" "Bob" "Mary" "Serena"))
        setb (set/from-list (list "Jim" "Mary" "John" "Bob"))
        _ (set/union seta setb)
        _ (set/intersection seta setb)
        symmetric-difference (set/symmetric-difference seta setb)]
    (-> (set/to-list symmetric-difference) p/echo)
    nil))

(defn -main [& _]
  (main))
