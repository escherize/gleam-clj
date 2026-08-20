(ns c2l03-string-patterns
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- get-name [x]
  (if (.startsWith ^String x "Hello, ")
    (let [name (subs x 7)]
      name)
    "Unknown"))

(defn main []
  (p/echo (get-name "Hello, Joe") "c2l03_string_patterns.gleam:2")
  (p/echo (get-name "Hello, Mike") "c2l03_string_patterns.gleam:3")
  (p/echo (get-name "System still working?") "c2l03_string_patterns.gleam:4"))

(defn -main [& _]
  (main))
