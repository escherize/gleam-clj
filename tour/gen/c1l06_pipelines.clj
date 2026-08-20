(ns c1l06-pipelines
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line (string/drop-start (string/drop-end "Hello, Joe!" 1) 7))
  (-> "Hello, Mike!" (string/drop-end 1) (string/drop-start 7) io/print-line)
  (-> "1"
      (string/append "2")
      ((fn [-capture] (string/append "3" -capture)))
      io/print-line))

(defn -main [& _]
  (main))
