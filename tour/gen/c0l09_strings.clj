(ns c0l09-strings
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line "👩‍💻 こんにちは Gleam 🏳️‍🌈")
  (io/print-line "multi\n    line\n    string")
  (io/print-line "😀")
  (io/print-line "\"X\" marks the spot")
  (io/print-line (str "One " "Two"))
  (io/print-line (string/reversed "1 2 3 4 5"))
  (io/print-line (string/append "abc" "def")))

(defn -main [& _]
  (main))
