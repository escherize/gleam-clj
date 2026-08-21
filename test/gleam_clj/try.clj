(ns gleam-clj.try
  "One-shot demo: compile a Gleam snippet and call it, no REPL required.

    clojure -M:try
    clojure -M:try 'pub fn add(a: Int, b: Int) -> Int { a + b }' add 2 40

  With no args, runs a built-in demo. With args: <gleam-source> <fn> <args...>
  where the fn is called with each remaining arg read as EDN."
  (:require [clojure.edn :as edn]
            [gleam-clj.load :as gl]))

(def ^:private demo
  "pub fn min_coins(coins: List(Int), amount: Int) -> Result(Int, Nil) {
  case amount {
    0 -> Ok(0)
    a if a < 0 -> Error(Nil)
    _ ->
      coins
      |> list.filter_map(fn(c) {
        case min_coins(coins, amount - c) {
          Ok(n) -> Ok(n + 1)
          Error(Nil) -> Error(Nil)
        }
      })
      |> list.reduce(int.min)
  }
}

import gleam/int
import gleam/list")

(defn -main [& args]
  (if (empty? args)
    (let [ns-sym (gl/eval-gleam demo :module "demo")
          f (resolve (symbol (str ns-sym) "min-coins"))]
      (println "compiled this Gleam:\n")
      (println demo)
      (println "\nnow calling it from Clojure:\n")
      (doseq [[coins amt] [[[1 5 10] 13] [[1 3 4] 6] [[5 10] 3]]]
        (println (format "  (min-coins %s %d) => %s"
                         (pr-str coins) amt
                         (pr-str (f (apply list coins) amt))))))
    (let [[source fn-name & call-args] args
          ns-sym (gl/eval-gleam source)
          f (resolve (symbol (str ns-sym) fn-name))]
      (when-not f
        (println "no such public fn:" fn-name) (System/exit 1))
      (println (pr-str (apply f (map edn/read-string call-args)))))))
