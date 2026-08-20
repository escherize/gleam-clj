(ns compound-data-type
  (:require
   [gleam.float :as float]
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Point
(defrecord Point [x y])

(defn point-to-string [p to-string]
  (let [{x :x y :y} p]
    (str (str (str (str "Point{x = " (-> x to-string)) ", y = ") (-> y to-string)) "}")))

(defn main []
  (let [p1 (->Point 10 5)
        p2 (->Point 10.0 5.0)]
    (io/println (point-to-string p1 int/to-string))
    (io/println (point-to-string p2 float/to-string))))

(defn -main [& _]
  (main))
