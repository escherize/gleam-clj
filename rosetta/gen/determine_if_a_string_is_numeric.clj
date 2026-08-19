(ns determine-if-a-string-is-numeric
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [subject (int/base-parse "1234" 10)]
    (if (instance? Ok subject)
      (io/print-line "String is numeric")
      (io/print-line "String isn't numeric"))))

(defn -main [& _]
  (main))
