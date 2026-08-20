(ns c2l08-multiple-subjects
  (:require
   [gleam.int :as int]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [x (int/random 2)
        y (int/random 2)]
    (p/echo x "c2l08_multiple_subjects.gleam:6")
    (p/echo y "c2l08_multiple_subjects.gleam:7")
    (let [result (cond
                   (and (= x 0) (= y 0)) "Both are zero"
                   (= x 0) "First is zero"
                   (= y 0) "Second is zero"
                   :else "Neither are zero")]
      (p/echo result "c2l08_multiple_subjects.gleam:15"))))

(defn -main [& _]
  (main))
