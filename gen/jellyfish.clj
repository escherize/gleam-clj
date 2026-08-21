(ns jellyfish
  (:require
   [gleam.io :as io]))

;; type Fish
(defprotocol IFish)
(defrecord Starfish [name favourite-color] IFish)
(defn Starfish? "True if `v` is a Starfish value." [v] (instance? Starfish v))
(defrecord Jellyfish [name jiggly] IFish)
(defn Jellyfish? "True if `v` is a Jellyfish value." [v] (instance? Jellyfish v))
(defn Fish? "True if `v` is any Fish value." [v] (instance? jellyfish.IFish v))

(defn- describe [fish]
  (cond
    (instance? Starfish fish)
    (let [name (:name fish) color (:favourite-color fish)]
      (str (str name " likes the color ") color))

    (and (instance? Jellyfish fish) (:jiggly fish))
    (let [name (:name fish)]
      (str name " is jiggly!"))

    (and (instance? Jellyfish fish) (not (:jiggly fish)))
    (let [name (:name fish)]
      (str name " is not jiggly"))))

(defn main
  {:malli/schema [:=> [:cat] :nil]}
  []
  (io/println (describe (->Starfish "Sandy" "pink")))
  (io/println (describe (->Jellyfish "Jelly" true)))
  (io/println (describe (->Jellyfish "Bob" false))))

(defn -main [& _]
  (main))
