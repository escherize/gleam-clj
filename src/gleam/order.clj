(ns gleam.order
  "Shims for gleam/order: the Order type (Lt/Eq/Gt).")

(defrecord Lt [])
(defrecord Eq [])
(defrecord Gt [])

(defn to-int [o]
  (cond (instance? Lt o) -1
        (instance? Eq o) 0
        :else 1))

(defn from-int [n]
  (cond (neg? n) (->Lt)
        (zero? n) (->Eq)
        :else (->Gt)))
