(ns c5l04-let-assert
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn unsafely-get-first-element [items]
  (let [v items]
    (when-not (seq v)
      (throw (ex-info "List should not be empty" {:value v})))
    (let [first' (first v)]
      first')))

(defn main []
  (let [a (unsafely-get-first-element (list 123))]
    (p/echo a "c5l04_let_assert.gleam:3")
    (let [b (unsafely-get-first-element (list))]
      (p/echo b "c5l04_let_assert.gleam:6"))))

(defn -main [& _]
  (main))
