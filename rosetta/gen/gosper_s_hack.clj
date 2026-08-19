(ns gosper-s-hack
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.result :as result])
  (:import (gleam.prelude Ok)))

(declare main gospers-hack)

(defn main []
  (p/with-use [[x] (list/each (list 1 3 7 15))]
    (io/write (str (int/to-string x) ": "))
    (int/fold-range 1
                    11
                    x
                    (fn [acc _]
                      (let [v (gospers-hack acc)]
                        (when-not (instance? Ok v)
                          (throw (ex-info "let assert failed" {:value v})))
                        (let [y (:value v)]
                          (io/write (str (int/to-string y) " "))
                          y))))
    (io/print-line "")))

(defn gospers-hack [n]
  (let [c (int/bitwise-and n (- n))
        r (+' n c)]
    (-> (int/bitwise-exclusive-or r n)
        (int/bitwise-shift-right 2)
        (int/divide c)
        (result/map-ok (fn [-capture] (int/bitwise-or -capture r))))))

(defn -main [& _]
  (main))
