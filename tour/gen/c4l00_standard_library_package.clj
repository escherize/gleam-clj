(ns c4l00-standard-library-package
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/print-line "Hello, Joe!")
  (io/print-line "Hello, Mike!"))

(defn -main [& _]
  (main))
