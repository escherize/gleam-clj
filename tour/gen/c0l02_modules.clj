(ns c0l02-modules
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as text])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line "Hello, Mike!")
  (io/print-line (text/reversed "Hello, Joe!")))

(defn -main [& _]
  (main))
