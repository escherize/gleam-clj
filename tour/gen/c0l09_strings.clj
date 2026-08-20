(ns c0l09-strings
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/println "👩‍💻 こんにちは Gleam 🏳️‍🌈")
  (io/println "multi\n    line\n    string")
  (io/println "😀")
  (io/println "\"X\" marks the spot")
  (io/println (str "One " "Two"))
  (io/println (string/reverse "1 2 3 4 5"))
  (io/println (string/append "abc" "def")))

(defn -main [& _]
  (main))
