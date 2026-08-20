(ns ffi-demo
  (:require
   [clojure.string]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(def shout clojure.string/upper-case)

(defn main []
  (io/print-line (shout "hello from clojure interop")))

(defn -main [& _]
  (main))
