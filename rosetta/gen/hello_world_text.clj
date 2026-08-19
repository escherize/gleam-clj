(ns hello-world-text
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line "Hello world!"))

(defn -main [& _]
  (main))
