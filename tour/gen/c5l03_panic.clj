(ns c5l03-panic
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn print-score [score]
  (cond
    (> score 1000) (io/print-line "High score!")
    (> score 0) (io/print-line "Still working on it")
    :else (throw (ex-info "Scores should never be negative!" {:gleam/panic true}))))

(defn main []
  (print-score 10)
  (print-score 100000)
  (print-score -1))

(defn -main [& _]
  (main))
