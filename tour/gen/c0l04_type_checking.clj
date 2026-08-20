(ns c0l04-type-checking
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line "My lucky number is:"))

(defn -main [& _]
  (main))
