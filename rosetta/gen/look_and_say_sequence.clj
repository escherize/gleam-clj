(ns look-and-say-sequence
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.result :as result])
  (:import (gleam.prelude Ok)))

(defn next-looknsay [list']
  (-> (list/chunk list' (fn [x] x))
      (list/flat-map (fn [x]
                       (list (list/length x) (-> (list/head x) (result/unwrap 0)))))))

(defn main []
  (int/fold-range 1
                  11
                  (list 1)
                  (fn [acc _]
                    (list/each acc (fn [x] (io/write (int/to-string x))))
                    (io/print-line "")
                    (next-looknsay acc))))

(defn -main [& _]
  (main))
