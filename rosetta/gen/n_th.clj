(ns n-th
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn nth' [n]
  (str (int/to-string n) (let [s0 (rem n 100)
        s1 (rem n 10)]
    (cond
      (or (= s0 11) (= s0 12) (= s0 13)) "th"
      (= s1 1) "st"
      (= s1 2) "nd"
      (= s1 3) "rd"
      :else "th"))))

(defn- show-nths [x y]
  (int/range x y nil (fn [_ x] (io/print (str (nth' x) " "))))
  (io/println ""))

(defn main []
  (show-nths 0 25)
  (show-nths 250 265)
  (show-nths 1000 1025))

(defn -main [& _]
  (main))
