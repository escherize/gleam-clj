(ns gleam.string
  "Shims for gleam/string. Renames for clojure.core collisions:
  repeat -> repeat-str. Graphemes are approximated by chars (v0)."
  (:require [clojure.string :as str]))

(defn length [s]
  (count s))

(defn append [a b]
  (str a b))

(defn join [lst sep]
  (str/join sep lst))

(defn repeat-str [s times]
  (apply str (repeat times s)))

(defn pad-start [s to-length pad]
  (let [need (- to-length (count s))]
    (if (pos? need)
      (str (apply str (take need (cycle pad))) s)
      s)))

(defn to-graphemes [s]
  (mapv str s))
