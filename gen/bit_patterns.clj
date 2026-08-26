(ns bit-patterns
  (:refer-clojure :exclude [take])
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn- show
  "show(r: Result(String, Nil)) -> String"
  {:gleam/src "bit_patterns.gleam:4"}
  ^java.lang.String [r]
  (if (instance? Ok r)
    (let [s (:value r)]
      s)
    "no-match"))

(defn- bit-size
  "bit_size(b: BitArray) -> Int"
  {:gleam/src "bit_patterns.gleam:22"}
  [b]
  (cond
    (= (count b) 0)
    0

    (>= (count b) 1)
    (let [rest' (subvec b 1)]
      (+' 8 (bit-size rest')))

    :else
    0))

(defn- classify
  "classify(b: BitArray) -> Result(String, Nil)"
  {:gleam/src "bit_patterns.gleam:11"}
  [b]
  (cond
    (= (count b) 0)
    (p/->Ok "empty")

    (and (>= (count b) 2) (p/ba-seg= b 0 (p/ba-utf8 "--")))
    (let [rest' (subvec b 2)]
      (p/->Ok (str "dashes+" (int/to-string (quot (bit-size rest') 8)))))

    (and (>= (count b) 2) (= 1 (nth b 0)))
    (let [x (nth b 1) rest' (subvec b 2)]
      (p/->Ok (str "one," (int/to-string x) "," (int/to-string (quot (bit-size rest') 8)))))

    (>= (count b) 2)
    (let [n (p/ba-uint b 0 2)]
      (p/->Ok (str "u16=" (int/to-string n))))

    :else
    (p/->Error nil)))

(defn- take
  "take(b: BitArray, len: Int) -> Result(String, Nil)"
  {:gleam/src "bit_patterns.gleam:30"}
  [b len]
  (if (and (>= (count b) len) (<= 0 len))
    (let [chunk (subvec b 0 len) rest' (subvec b len)]
      (p/->Ok (str (int/to-string (quot (bit-size chunk) 8)) "+" (int/to-string (quot (bit-size rest') 8)))))
    (p/->Error nil)))

(defn main
  "main() -> Nil"
  {:malli/schema [:=> [:cat] :nil] :gleam/src "bit_patterns.gleam:38"}
  []
  (io/println (show (classify (p/bit-array ))))
  (io/println (show (classify (p/bit-array (p/ba-utf8 "--stuff")))))
  (io/println (show (classify (p/bit-array (p/ba-int 1 8) (p/ba-int 42 8) (p/ba-int 9 8) (p/ba-int 9 8) (p/ba-int 9 8)))))
  (io/println (show (classify (p/bit-array (p/ba-int 200 8) (p/ba-int 1 8) (p/ba-int 7 8)))))
  (io/println (show (take (p/bit-array (p/ba-int 1 8) (p/ba-int 2 8) (p/ba-int 3 8) (p/ba-int 4 8) (p/ba-int 5 8))
                          2)))
  (io/println (show (take (p/bit-array (p/ba-int 1 8) (p/ba-int 2 8)) 5)))
  (io/println (show (take (p/bit-array (p/ba-int 1 8) (p/ba-int 2 8)) -1))))

(defn -main [& _]
  (main))
