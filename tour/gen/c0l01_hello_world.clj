(ns c0l01-hello-world
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line "Hello, Joe!"))

(defn -main [& _]
  (main))
