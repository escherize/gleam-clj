(ns c3l07-nil
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [x nil]
    (p/echo x "c3l07_nil.gleam:5")
    (let [result (io/println "Hello!")]
      (p/echo (= result nil) "c3l07_nil.gleam:10"))))

(defn -main [& _]
  (main))
