(ns gleam.float
  "Shims for gleam/float. Rename: compare -> cmp."
  (:require [gleam.order :as order]
            [gleam.prelude :as p]))

(defn to-string [f]
  (str f))

(defn to-precision [x precision]
  (let [m (Math/pow 10.0 precision)]
    (/ (Math/round (* x m)) m)))

(defn square-root [x]
  (if (neg? x)
    (p/->Error nil)
    (p/->Ok (Math/sqrt x))))

(defn floor [x] (Math/floor x))
(defn ceiling [x] (Math/ceil x))
(defn round [x] (Math/round x))

(defn cmp [a b]
  (order/from-int (compare a b)))

(defn random []
  (rand))
