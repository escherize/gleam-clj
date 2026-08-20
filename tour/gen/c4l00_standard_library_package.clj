(ns c4l00-standard-library-package
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/println "Hello, Joe!")
  (io/println "Hello, Mike!"))

(defn -main [& _]
  (main))
