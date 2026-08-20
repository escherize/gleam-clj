(ns bernoulli-s-triangle
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.prelude :as p]
   [gleam.result :as result])
  (:import (gleam.prelude Ok)))

(defn- next-row [row]
  (let [next (-> row
                 list/window-by-2
                 (list/map (fn [x] (+' (nth x 0) (nth x 1))))
                 (list/prepend 1))]
    (-> next
        (list/append (list (-> (list/last next) (result/unwrap 1) (int/add 1)))))))

(defn- print-list [list']
  (cond
    (empty? list') (io/println "[]")
    (= (count list') 1) (let [x (first list')]
                          (io/println (int/to-string x)))
    (seq list') (let [x (first list') rest' (rest list')]
                  (do (io/print (str (int/to-string x) " "))
                      (recur rest')))))

(defn- bernoulli [row n]
  (if (= n 0)
    nil
    (let [x n]
      (do (print-list row)
          (recur (next-row row) (-' x 1))))))

(defn main []
  (bernoulli (list 1) 15))

(defn -main [& _]
  (main))
