(ns mcnuggets-problem
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.set :as set])
  (:import (gleam.prelude Ok)))

(defn non-mcnuggets [limit]
  (let [candidates (-> (int/fold-range limit 0 (list) list/prepend)
                       set/from-list)]
    (p/with-use [[c x] (int/fold-range 0 (quot limit 6) candidates)
                 [c y] (int/fold-range 0 (quot limit 9) c)
                 [c z] (int/fold-range 0 (quot limit 5) c)]
      (set/delete c (+' (+' (*' x 6) (*' y 9)) (*' z 20))))))

(defn main []
  (let [subject (-> (non-mcnuggets 100) set/to-list (list/largest int/cmp))]
    (if (instance? Ok subject)
      (let [n (:value subject)]
        (-> (int/to-string n) io/print-line))
      (io/print-line-error "No candidates. Try setting a higher limit."))))

(defn -main [& _]
  (main))
