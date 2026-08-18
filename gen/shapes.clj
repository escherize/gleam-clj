(ns shapes
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Shape
(defrecord Circle [value])
(defrecord Rect [width height])
(defrecord Point [])

(declare area sum total-area main)

(defn area
  "Area with pi = 3.0, engineering approximation."
  [shape]
  (cond
    (instance? Circle shape) (let [r (:value shape)]
                               (* (* 3.0 r) r))
    (instance? Rect shape) (let [w (:width shape) h (:height shape)]
                             (* w h))
    (instance? Point shape) 0.0))

(defn- sum
  [xs]
  (if (empty? xs)
    0.0
    (let [x (first xs) rest (rest xs)]
      (+ x (sum rest)))))

(defn total-area
  [shapes]
  (-> shapes (list/map-over area) sum))

(defn main
  []
  (let [v (area (->Circle 2.0))]
    (when-not (= v 12.0)
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (area (->Rect 2.0 3.0))]
    (when-not (= v 6.0)
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (area (->Point))]
    (when-not (= v 0.0)
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (total-area (list (->Circle 2.0) (->Rect 2.0 3.0) (->Point)))]
    (when-not (= v 18.0)
      (throw (ex-info "let assert failed" {:value v}))))
  (let [v (total-area (list))]
    (when-not (= v 0.0)
      (throw (ex-info "let assert failed" {:value v})))))

(defn -main [& _]
  (main))
