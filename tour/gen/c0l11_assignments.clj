(ns c0l11-assignments
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [x "Original"]
    (io/print-line x)
    (let [y x]
      (io/print-line y)
      (let [x "New"]
        (io/print-line x)
        (io/print-line y)))))

(defn -main [& _]
  (main))
