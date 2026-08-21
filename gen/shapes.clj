(ns shapes
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p]))

;; type Shape
(defprotocol IShape)
(defrecord Circle [value] IShape)
(defn Circle? "True if `v` is a Circle value." [v] (instance? Circle v))
(defrecord Rect [width height] IShape)
(defn Rect? "True if `v` is a Rect value." [v] (instance? Rect v))
(defrecord Point [] IShape)
(defn Point? "True if `v` is a Point value." [v] (instance? Point v))
(defn Shape? "True if `v` is any Shape value." [v] (instance? shapes.IShape v))

(defn area
  "Area with pi = 3.0, engineering approximation."
  {:malli/schema [:=> [:cat [:fn Shape?]] :double]}
  [shape]
  (cond
    (instance? Circle shape)
    (let [r (:value shape)]
      (* (* 3.0 r) r))

    (instance? Rect shape)
    (let [w (:width shape) h (:height shape)]
      (* w h))

    (instance? Point shape)
    0.0))

(defn- sum [xs]
  (if (empty? xs)
    0.0
    (let [x (first xs) rest' (rest xs)]
      (+ x (sum rest')))))

(defn total-area
  {:malli/schema [:=> [:cat [:sequential [:fn Shape?]]] :double]}
  [shapes]
  (-> shapes (list/map area) sum))

(defn main
  {:malli/schema [:=> [:cat] :double]}
  []
  (p/let-assert 12.0 (area (->Circle 2.0)))
  (p/let-assert 6.0 (area (->Rect 2.0 3.0)))
  (p/let-assert 0.0 (area (->Point)))
  (p/let-assert 18.0
                (total-area (list (->Circle 2.0) (->Rect 2.0 3.0) (->Point))))
  (p/let-assert 0.0 (total-area (list))))

(defn -main [& _]
  (main))
