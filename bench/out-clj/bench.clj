(ns bench
  "Cross-VM benchmarks. Timed in-process on both VMs (startup excluded);
   each workload runs once as warmup, then three timed rounds."
  (:refer-clojure :exclude [time])
  (:require
   [bench-ffi]
   [gleam.dict :as dict]
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.list :as list]
   [gleam.string :as string]
   [parse-bench :as parse_bench])
  (:import (gleam.prelude Ok)))

(def ^{:gleam/src "bench/src/bench.gleam:12"} now-ms bench-ffi/now-ms)

(defn- time
  "time(name: String, work: fn() -> Int) -> Nil"
  {:gleam/src "bench/src/bench.gleam:14"}
  [^java.lang.String name work]
  (let [_ (work)
        rounds (list/map (list 1 2 3)
                         (fn [_]
                           (let [start (now-ms)
                                 _ (work)]
                             (-' (now-ms) start))))]
    (io/println (str name ": " (string/join (list/map rounds int/to-string) " ") " ms"))))

(defn- step
  "step(coins: List(Int), table: Dict(Int, Int), a: Int) -> Dict(Int, Int)"
  {:gleam/src "bench/src/bench.gleam:42"}
  [coins table a]
  (let [best (-> coins
                 (list/filter-map (fn [c] (dict/get table (-' a c))))
                 (list/reduce int/min'))]
    (if (instance? Ok best)
      (let [b (:value best)]
        (dict/insert table a (+' b 1)))
      table)))

(defn- min-coins
  "min_coins(coins: List(Int), amount: Int) -> Int"
  {:gleam/src "bench/src/bench.gleam:31"}
  [coins amount]
  (let [table (int/range 1
                         (+' amount 1)
                         (dict/from-list (list [0 0]))
                         (fn [table a] (step coins table a))) subject (dict/get table amount)]
    (if (instance? Ok subject)
      (let [n (:value subject)]
        n)
      -1)))

(defn- coin-work
  "coin_work() -> Int"
  {:gleam/src "bench/src/bench.gleam:53"}
  []
  (min-coins (list 1 3 4 5 17 29) 500000))

(defn- build-range
  "build_range(n: Int, acc: List(Int)) -> List(Int)"
  {:gleam/src "bench/src/bench.gleam:58"}
  [n acc]
  (if (= n 0) acc (recur (-' n 1) (list* n acc))))

(defn- pipeline-work
  "pipeline_work() -> Int"
  {:gleam/src "bench/src/bench.gleam:65"}
  []
  (-> (build-range 2000000 (list))
      (list/map (fn [n] (*' n 3)))
      (list/filter (fn [n] (= (rem n 2) 0)))
      (list/map int/to-string)
      (string/join ",")
      string/length))

(defn main
  "main() -> Nil"
  {:malli/schema [:=> [:cat] :nil] :gleam/src "bench/src/bench.gleam:74"}
  []
  (time "coin_change dp (amount 500k)" coin-work)
  (time "list+string pipeline (2M)" pipeline-work)
  (time "glance parse (5000x)" (fn [] (parse_bench/parse-50))))

(defn -main [& _]
  (main))
