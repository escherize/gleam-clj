(ns minimum-multiple-of-m-where-digital-sum-equals-m
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p]
   [gleam.string :as string])
  (:import (gleam.prelude Ok)))

(defn- digit-sum-loop [n sum]
  (if (= n 0) sum (recur (quot n 10) (+' sum (rem n 10)))))

(defn- digit-sum [n]
  (digit-sum-loop n 0))

(defn- task-loop [m n]
  (let [s1 (= (digit-sum (*' m n)) n)]
    (cond
      (= n 71) nil
      (not s1) (recur (+' m 1) n)
      s1 (do (io/write (-> (int/to-string m) (string/pad-start 9 " ")))
             (let [subject (= (rem n 10) 0)]
               (if subject (io/print-line "") nil))
             (recur 1 (+' n 1))))))

(defn main []
  (task-loop 1 1))

(defn -main [& _]
  (main))
