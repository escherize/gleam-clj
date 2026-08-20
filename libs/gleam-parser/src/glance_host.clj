(ns glance-host
  (:require
   [glance :as glance]
   [gleam.prelude :as p]))

(defn parse
  {:malli/schema [:=> [:cat :string] [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [src]
  (glance/module src))
