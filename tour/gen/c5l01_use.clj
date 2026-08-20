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
  (result/attempt (get-username)
                  (fn [username]
                    (result/attempt (get-password)
                                    (fn [password]
                                      (result/map-ok (log-in username
                                                             password)
                                                     (fn [greeting]
                                                       (str (str greeting ", ") username))))))))

(defn with-use []
  (p/with-use [[username] (result/attempt (get-username))
               [password] (result/attempt (get-password))
               [greeting] (result/map-ok (log-in username password))]
    (str (str greeting ", ") username)))

(defn main []
  (let [_ (p/echo (with-use) "c5l01_use.gleam:6")
        _ (p/echo (without-use) "c5l01_use.gleam:7")]
    nil))

(defn -main [& _]
  (main))
