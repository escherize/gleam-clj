(ns c0l02-modules
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as text])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/println "Hello, Mike!")
  (io/println (text/reverse "Hello, Joe!")))

(defn -main [& _]
  (main))
