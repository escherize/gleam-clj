(ns temperature-conversion
  (:require
   [gleam.float :as float]
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(declare main convert show-temp)

(defn main []
  (convert 21.0))

(defn convert [kelvin]
  (let [rankine (/ (* kelvin 9.0) 5.0)]
    (show-temp kelvin "K")
    (show-temp (- kelvin 273.15) "C")
    (show-temp (- rankine 459.67) "F")
    (show-temp rankine "R")))

(defn- show-temp [temperature unit]
  (-> temperature
      (float/to-precision 2)
      float/to-string
      (string/append (str " °" unit))
      io/print-line))

(defn -main [& _]
  (main))
