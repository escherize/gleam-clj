(ns jellyfish
  (:require
   [gleam.io :as io]))

;; type Fish
(defrecord Starfish [name favourite-color])
(defn Starfish? "True if `v` is a Starfish value." [v] (instance? Starfish v))
(defrecord Jellyfish [name jiggly])
(defn Jellyfish? "True if `v` is a Jellyfish value." [v] (instance? Jellyfish v))

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
