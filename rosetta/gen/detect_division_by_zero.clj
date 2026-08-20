(ns detect-division-by-zero
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn safe-div [a b]
  (if (= b 0) (p/->Error nil) (p/->Ok (quot a b))))

(defn main []
  (let [subject (safe-div 10 5)]
    (if (and (instance? gleam.prelude.Error subject) (nil? (:value subject)))
      (io/print-error "Division by zero")
      (let [d (:value subject)]
        (io/println (str "result of division is " (-> d int/to-string))))))
  (let [subject (safe-div 10 0)]
    (if (and (instance? gleam.prelude.Error subject) (nil? (:value subject)))
      (io/print-error "Division by zero")
      (let [d (:value subject)]
        (io/println (str "result of division is " (-> d int/to-string)))))))

(defn -main [& _]
  (main))
