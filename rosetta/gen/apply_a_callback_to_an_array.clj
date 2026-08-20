(ns apply-a-callback-to-an-array
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> (list 1 14 99 23) (list/map (fn [x] (*' x 2))) (p/echo)))

(defn -main [& _]
  (main))
