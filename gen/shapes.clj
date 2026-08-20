(ns shapes
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Shape
(defrecord Circle [value])
(defrecord Rect [width height])
(defrecord Point [])

(defn area
  "Area with pi = 3.0, engineering approximation."
  [shape]
  (cond
    (instance? Circle shape) (let [r (:value shape)]
                               (* (* 3.0 r) r))
    (instance? Rect shape) (let [w (:width shape) h (:height shape)]
                             (* w h))
    (instance? Point shape) 0.0))

(defn- sum [xs]
  (if (empty? xs)
    0.0
    (let [x (first xs) rest' (rest xs)]
      (+ x (sum rest')))))

(defn total-area [shapes]
  (-> shapes (list/map-over area) sum))

(defn main []
  (p/let-assert 12.0 (area (->Circle 2.0)))
  (p/let-assert 6.0 (area (->Rect 2.0 3.0)))
  (p/let-assert 0.0 (area (->Point)))
  (p/let-assert 18.0
                (total-area (list (->Circle 2.0) (->Rect 2.0 3.0) (->Point))))
  (p/let-assert 0.0 (total-area (list))))

(defn -main [& _]
  (main))
