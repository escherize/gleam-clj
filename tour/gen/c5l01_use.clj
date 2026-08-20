(ns c5l01-use
  (:require
   [gleam.prelude :as p]
   [gleam.result :as result])
  (:import (gleam.prelude Ok)))

(defn- log-in [_ _]
  (p/->Ok "Welcome"))

(defn- get-password []
  (p/->Ok "hunter2"))

(defn- get-username []
  (p/->Ok "alice"))

(defn without-use []
  (result/try* (get-username)
               (fn [username]
                 (result/try* (get-password)
                              (fn [password]
                                (result/map (log-in username password)
                                            (fn [greeting]
                                              (str (str greeting ", ") username))))))))

(defn with-use []
  (p/with-use [[username] (result/try* (get-username))
               [password] (result/try* (get-password))
               [greeting] (result/map (log-in username password))]
    (str (str greeting ", ") username)))

(defn main []
  (let [_ (p/echo (with-use) "c5l01_use.gleam:6")
        _ (p/echo (without-use) "c5l01_use.gleam:7")]
    nil))

(defn -main [& _]
  (main))
