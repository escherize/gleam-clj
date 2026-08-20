(ns c0l03-unqualified-imports
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/println "This is qualified")
  (io/println "This is unqualified"))

(defn -main [& _]
  (main))
