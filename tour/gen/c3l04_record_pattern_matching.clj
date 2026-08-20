(ns c3l04-record-pattern-matching
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Fish
(defrecord Starfish [name favourite-colour])
(defrecord Jellyfish [name jiggly])

;; type IceCream
(defrecord IceCream [flavour])

(defn- handle-ice-cream [ice-cream]
  (let [{flavour :flavour} ice-cream]
    (io/println flavour)))

(defn- handle-fish [fish]
  (if (instance? Starfish fish)
    (let [favourite-colour (:favourite-colour fish)]
      (io/println favourite-colour))
    (let [name (:name fish)]
      (io/println name))))

(defn main []
  (handle-fish (->Starfish "Lucy" "Pink"))
  (handle-ice-cream (->IceCream "strawberry")))

(defn -main [& _]
  (main))
