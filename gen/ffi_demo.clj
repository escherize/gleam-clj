(ns ffi-demo
  (:require
   [clojure.string]
   [gleam.io :as io]))

(def shout clojure.string/upper-case)

(defn main
  {:malli/schema [:=> [:cat] :nil]}
  []
  (io/println (shout "hello from clojure interop")))

(defn -main [& _]
  (main))
