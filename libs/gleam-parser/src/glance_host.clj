(ns glance-host
  (:require
   [glance :as glance]
   [gleam.prelude :as p]))

(defn parse
  "parse(src: String) -> Result(Module, Error)"
  {:malli/schema [:=> [:cat :string]
                      (p/result-of (glance/Module-schema) (glance/Error-schema))]
   :gleam/src "project/src/glance_host.gleam:5"}
  [^java.lang.String src]
  (glance/module src))
