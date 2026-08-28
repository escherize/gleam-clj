(ns gleeunit
  "Shim for gleeunit: run every public fn named *-test in the given test
  namespaces. BEAM gleeunit discovers modules from the test/ directory,
  whatever they are named, and only filters fn names; the 1-arity mirrors
  that when a driver knows the exact namespaces. The zero-arity falls back
  to every loaded *-test namespace, for compiled Gleam callers."
  (:require [clojure.string :as str]))

(defn main
  ([]
   (main (filter #(str/ends-with? (str (ns-name %)) "-test") (all-ns))))
  ([nses]
   (let [tests (sort-by str
                        (for [ns nses
                              [sym v] (ns-publics (the-ns ns))
                              :when (str/ends-with? (name sym) "-test")]
                          v))
         failures (vec (keep (fn [v]
                               (try (v) (print ".") nil
                                    (catch Throwable t
                                      (print "F")
                                      [v t])))
                             tests))]
     (println)
     (println (count tests) "tests," (count failures) "failures")
     (doseq [[v t] failures]
       (println " " (str v) "-" (ex-message t)))
     (when (seq failures)
       (System/exit 1)))))
