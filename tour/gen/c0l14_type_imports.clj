(ns c0l14-type-imports
  (:require
   [gleam.bytes-tree :as bytes_tree]
   [gleam.prelude :as p]
   [gleam.string-tree :as string_tree])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [_ (bytes_tree/new)
        _ (string_tree/new)]
    nil))

(defn -main [& _]
  (main))
