(ns set
  (:require
   [gleam.prelude :as p]
   [gleam.set :as set])
  (:import (gleam.prelude Ok)))

(defn main []
  (let [s (set/new*)
        sa (set/insert s "a")
        sab (set/from-list (list "a" "b"))]
    (p/echo (set/contains sa "a") "set.gleam:7")
    (let [union (set/union sa sab)
          union-list (set/to-list union)]
      (p/echo union-list "set.gleam:10")
      (let [intersection (set/intersection sa sab)
            intersection-list (set/to-list intersection)]
        (p/echo intersection-list "set.gleam:13")
        (let [subtract (set/difference sab sa)
              subtract-list (set/to-list subtract)]
          (p/echo subtract-list "set.gleam:16")
          (let [is-subset (set/is-subset sa sab)]
            (p/echo is-subset "set.gleam:18")
            (p/echo (= sa sab) "set.gleam:19")))))))

(defn -main [& _]
  (main))
