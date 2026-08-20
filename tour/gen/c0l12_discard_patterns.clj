(ns c0l12-discard-patterns
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [_ 1000]
    nil))

(defn -main [& _]
  (main))
