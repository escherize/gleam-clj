(ns c4l03-dict-module
  (:require
   [gleam.dict :as dict]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [scores (dict/from-list (list ["Lucy" 13] ["Drew" 15]))]
    (p/echo (dict/to-list scores) "c4l03_dict_module.gleam:5")
    (let [scores (-> scores
                     (dict/insert "Bushra" 16)
                     (dict/insert "Darius" 14)
                     (dict/delete "Drew"))]
      (p/echo (dict/to-list scores) "c4l03_dict_module.gleam:12"))))

(defn -main [& _]
  (main))
