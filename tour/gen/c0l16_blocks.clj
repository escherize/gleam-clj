(ns c0l16-blocks
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [fahrenheit (let [degrees 64]
                     degrees)
        celsius (quot (*' (-' fahrenheit 32) 5) 9)]
    (p/echo celsius "c0l16_blocks.gleam:11")))

(defn -main [& _]
  (main))
