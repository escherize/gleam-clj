(ns regressions
  "Wild-code sweep regressions: shapes that once crashed the emitter."
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn print-all
  "print_all(items: List(Int)) -> Nil

   `use Nil, ...` — zero-arity constructor as a use pattern (from iv)."
  {:malli/schema [:=> [:cat [:sequential :int]] :nil]
   :gleam/src "regressions.gleam:8"}
  [items]
  (p/with-use [[_use0 item] (list/fold items nil)]
    (let [_ _use0]
      (io/println (int/to-string item)))))

(defn describe
  "describe(b: BitArray) -> String

   Empty bit-array pattern (from gleam_http)."
  {:malli/schema [:=> [:cat [:vector :int]] :string]
   :gleam/src "regressions.gleam:14"}
  ^java.lang.String [b]
  (if (= [] b) "empty" "bytes"))

(defn bracket-label
  "bracket_label(n: Int) -> String

   let-assert destructure whose collapsed let binds bracket-heavy strings
   (from gap): the binding-vector scanner must ignore [ ] inside strings."
  {:malli/schema [:=> [:cat :int] :string] :gleam/src "regressions.gleam:23"}
  ^java.lang.String [n]
  (let [v (list/first' (list n))]
    (when-not (instance? Ok v)
      (throw (ex-info "let assert failed" {:value v})))
    (let [head (:value v) open "[" close "]"]
      (str open (int/to-string head) close))))

(defn main
  "main() -> Nil"
  {:malli/schema [:=> [:cat] :nil] :gleam/src "regressions.gleam:30"}
  []
  (print-all (list 1 2))
  (io/println (describe (p/bit-array )))
  (io/println (describe (p/bit-array (p/ba-int 7 8))))
  (io/println (bracket-label 42)))

(defn -main [& _]
  (main))
