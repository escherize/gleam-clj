(ns mcnuggets-problem
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.set :as set])
  (:import (gleam.prelude Ok)))

(defn non-mcnuggets [limit]
  (let [candidates (-> (int/range limit 0 (list) list/prepend) set/from-list)]
    (p/with-use [[c x] (int/range 0 (quot limit 6) candidates)
                 [c y] (int/range 0 (quot limit 9) c)
                 [c z] (int/range 0 (quot limit 5) c)]
      (set/delete c (+' (+' (*' x 6) (*' y 9)) (*' z 20))))))

(defn main []
  (let [subject (-> (non-mcnuggets 100) set/to-list (list/max' int/compare))]
    (if (instance? Ok subject)
      (let [n (:value subject)]
        (-> (int/to-string n) io/println))
      (io/println-error "No candidates. Try setting a higher limit."))))

(defn -main [& _]
  (main))
