(ns c0l03-unqualified-imports
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line "This is qualified")
  (println "This is unqualified"))

(defn -main [& _]
  (main))
