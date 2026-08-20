(ns differential-test
  "Differential fuzz: the Gleam-compiled parser vs Metabase's original
  metabase.lib.parse, loaded from the local Metabase checkout (skipped when
  absent; MB source is AGPL and never enters this repo)."
  (:require [clojure.test :refer [deftest is run-tests]]
            [metabase.lib.parse-gleam :as ours]))

(def ^:private mb-parse-file
  (str (System/getProperty "user.home") "/dv/mb/metabase/src/metabase/lib/parse.cljc"))

(def ^:private original-parse
  (when (.exists (java.io.File. mb-parse-file))
    (load-file mb-parse-file)
    (resolve 'metabase.lib.parse/parse)))

(def ^:private alphabet
  ["{{" "}}" "[[" "]]" "'" "--" "/*" "*/" "\n" " " "x" "y" "{" "}" "[" "]" "-" "*" "/"])

(defn- gen-input [^java.util.Random rng]
  (apply str (repeatedly (.nextInt rng 25) #(nth alphabet (.nextInt rng (count alphabet))))))

(defn- run-one [f opts s sql?]
  (try [:ok (mapv #(if (string? %) % (into {} %)) (f opts s sql?))]
       (catch clojure.lang.ExceptionInfo e [:threw (:type (ex-data e))])))

(deftest differential-fuzz
  (if-not original-parse
    (println "SKIP: no local Metabase checkout")
    (let [rng (java.util.Random. 42)
          n 5000]
      (doseq [_ (range n)]
        (let [s (gen-input rng)]
          (doseq [opts [{:parse-error-type :invalid-query} {}]
                  sql? [true false]]
            (is (= (run-one original-parse opts s sql?)
                   (run-one ours/parse opts s sql?))
                (pr-str {:input s :opts opts :sql? sql?})))))
      (println "fuzzed" n "inputs x 4 modes against the original"))))

(defn -main [& _]
  (let [{:keys [fail error]} (run-tests 'differential-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
