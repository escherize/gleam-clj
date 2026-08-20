(ns c4l01-list-module
  (:require
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [ints (list 0 1 2 3 4 5)]
    (io/println "=== map ===")
    (p/echo (list/map ints (fn [x] (*' x 2))) "c4l01_list_module.gleam:8")
    (io/println "=== filter ===")
    (p/echo (list/filter ints (fn [x] (= (rem x 2) 0))) "c4l01_list_module.gleam:11")
    (io/println "=== fold ===")
    (p/echo (list/fold ints 0 (fn [count' e] (+' count' e))) "c4l01_list_module.gleam:14")
    (io/println "=== find ===")
    (let [_ (p/echo (list/find ints (fn [x] (> x 3))) "c4l01_list_module.gleam:17")]
      (p/echo (list/find ints (fn [x] (> x 13))) "c4l01_list_module.gleam:18"))))

(defn -main [& _]
  (main))
