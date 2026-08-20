(ns c4l02-result-module
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.result :as result])
  (:import (gleam.prelude Ok)))

(defn main []
  (io/println "=== map ===")
  (let [_ (p/echo (result/map (p/->Ok 1) (fn [x] (*' x 2))) "c4l02_result_module.gleam:7")
        _ (p/echo (result/map (p/->Error 1) (fn [x] (*' x 2))) "c4l02_result_module.gleam:8")]
    (io/println "=== try ===")
    (let [_ (p/echo (result/try* (p/->Ok "1") int/parse) "c4l02_result_module.gleam:11")
          _ (p/echo (result/try* (p/->Ok "no") int/parse) "c4l02_result_module.gleam:12")
          _ (p/echo (result/try* (p/->Error nil) int/parse) "c4l02_result_module.gleam:13")]
      (io/println "=== unwrap ===")
      (p/echo (result/unwrap (p/->Ok "1234") "default") "c4l02_result_module.gleam:16")
      (p/echo (result/unwrap (p/->Error nil) "default") "c4l02_result_module.gleam:17")
      (io/println "=== pipeline ===")
      (-> (int/parse "-1234")
          (result/map int/absolute-value)
          (result/try* (fn [-capture] (int/remainder -capture 42)))
          (p/echo)))))

(defn -main [& _]
  (main))
