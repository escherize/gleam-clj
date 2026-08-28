(ns gleam-filepath-ffi
  "Clojure implementation of filepath's one native external.")

(defn is-windows []
  (.contains (.toLowerCase (System/getProperty "os.name")) "windows"))
