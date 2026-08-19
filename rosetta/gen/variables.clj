(ns variables
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [a 50
        a 10
        b (+' a 42)]
    nil))

(defn -main [& _]
  (main))
