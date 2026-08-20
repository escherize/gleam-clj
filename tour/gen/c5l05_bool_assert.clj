(ns c5l05-bool-assert
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- add [a b]
  (+' a b))

(defn main []
  (when-not (= (add 1 2) 3)
    (throw (ex-info "assert failed" {:gleam/assert true})))
  (when-not (< (add 1 2) (add 1 3))
    (throw (ex-info "assert failed" {:gleam/assert true})))
  (when-not (= (add 6 2) (add 2 6))
    (throw (ex-info "Addition should be commutative" {:gleam/assert true})))
  (when-not (= (add 2 2) 5)
    (throw (ex-info "assert failed" {:gleam/assert true}))))

(defn -main [& _]
  (main))
