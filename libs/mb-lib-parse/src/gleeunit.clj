(ns gleeunit
  "Shim for gleeunit: run every public fn named *-test in loaded namespaces.
  The Gleam test module requires itself before calling main, so discovery
  over loaded namespaces finds exactly the compiled test fns."
  (:require [clojure.string :as str]))

(defn main []
  (let [tests (sort-by str
                       (for [ns (all-ns)
                             :when (str/ends-with? (str (ns-name ns)) "-test")
                             [sym v] (ns-publics ns)
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
      (System/exit 1))))
