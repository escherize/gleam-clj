(ns c0l11-assignments
  (:require
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [x "Original"]
    (io/println x)
    (let [y x]
      (io/println y)
      (let [x "New"]
        (io/println x)
        (io/println y)))))

(defn -main [& _]
  (main))
