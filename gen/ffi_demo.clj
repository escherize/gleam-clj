(ns ffi-demo
  (:require
   [clojure.string]
   [gleam.io :as io]))

(def ^{:gleam/src "ffi_demo.gleam:4"} shout clojure.string/upper-case)

(defn main
  "main() -> Nil"
  {:malli/schema [:=> [:cat] :nil] :gleam/src "ffi_demo.gleam:6"}
  []
  (io/println (shout "hello from clojure interop")))

(defn -main [& _]
  (main))
