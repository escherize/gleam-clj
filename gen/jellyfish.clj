(ns jellyfish
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Fish
(defrecord Starfish [name favourite-color])
(defrecord Jellyfish [name jiggly])

(defn- describe [fish]
  (cond
    (instance? Starfish fish) (let [name (:name fish) color (:favourite-color fish)]
                                (str (str name " likes the color ") color))
    (and (instance? Jellyfish fish) (:jiggly fish)) (let [name (:name fish)]
                                                      (str name " is jiggly!"))
    (and (instance? Jellyfish fish) (not (:jiggly fish))) (let [name (:name fish)]
                                                            (str name " is not jiggly"))))

(defn main []
  (io/println (describe (->Starfish "Sandy" "pink")))
  (io/println (describe (->Jellyfish "Jelly" true)))
  (io/println (describe (->Jellyfish "Bob" false))))

(defn -main [& _]
  (main))

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'main [:=> [:cat] :nil]})
