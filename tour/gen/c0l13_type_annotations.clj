(ns c0l13-type-annotations
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [_ "Gleam"
        _ true
        _ 1]
    nil))

(defn -main [& _]
  (main))
