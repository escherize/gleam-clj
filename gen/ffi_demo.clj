(ns ffi-demo
  (:require
   [clojure.string]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(def shout clojure.string/upper-case)

(defn main []
  (io/println (shout "hello from clojure interop")))

(defn -main [& _]
  (main))

(def malli-schemas
  "Malli schemas for this module's public fns, derived from Gleam's types."
  {'main [:=> [:cat] :nil]})
