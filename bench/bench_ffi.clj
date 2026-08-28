(ns bench-ffi
  "JVM side of the benchmark clock.")

(defn now-ms [] (System/currentTimeMillis))
