(ns associative-array-creation
  (:require
   [gleam.dict :as dict]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [_ (dict/new*)
        stuff (dict/from-list (list ["key1" 1] ["key2" 2]))
        _ (dict/get stuff "key1")
        _ (dict/insert stuff "key3" 3)
        _ (dict/has-key stuff "key1")]
    nil))

(defn -main [& _]
  (main))
