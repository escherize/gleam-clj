(ns c5l02-todo
  (:require
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn main []
  (throw (ex-info "I haven't written this code yet!" {:gleam/todo true})))

(defn todo-without-reason []
  (throw (ex-info "todo" {:gleam/todo true})))

(defn -main [& _]
  (main))
