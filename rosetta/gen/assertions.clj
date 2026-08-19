(ns assertions
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [v (list 1 2 3)]
    (when-not (seq v)
      (throw (ex-info "let assert failed" {:value v}))))
  (when-not (= (+' 1 2) 3)
    (throw (ex-info "1 + 2 isn't 3?" {:gleam/assert true}))))

(defn -main [& _]
  (main))
