(ns empty-string
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line "")
  (when-not (= (string/length "") 0)
    (throw (ex-info "assert failed" {:gleam/assert true}))))

(defn -main [& _]
  (main))
