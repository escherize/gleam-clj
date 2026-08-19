(ns even-or-odd
  (:require
   [gleam.bool :as bool]
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (-> 1 int/is-odd bool/to-string io/print-line)
  (-> 2 int/is-even bool/to-string io/print-line))

(defn -main [& _]
  (main))
