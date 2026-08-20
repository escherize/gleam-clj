(ns parse-gleam-test
  "Parity suite: every case from metabase's lib/parameters/parse_test.cljc,
  adapted to metabase.lib.parse's raw output shapes, run against the
  Gleam-compiled parser."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing run-tests]]
            [metabase.lib.parse-gleam :as parse]
            [mb-lib-parse :as impl])
  (:import (mb_lib_parse Str)))

(def ^:private opts {:parse-error-type :invalid-query})

(defn- param [k] {:type :metabase.lib.parse/param, :name k})
(defn- optional [& args] {:type :metabase.lib.parse/optional, :contents (vec args)})

(defn- kw [piece]
  (if (instance? Str piece)
    (:value piece)
    (-> piece :f0 class .getSimpleName
        (str/replace #"([a-z])([A-Z])" "$1-$2")
        str/lower-case
        keyword)))

(defn- tokenize [s sql?] (mapv kw (impl/tokenize s sql?)))

(deftest tokenize-test
  (doseq [[query expected]
          {"{{num_toucans}}"
           [:param-begin "num_toucans" :param-end]

           "[[AND num_toucans > {{num_toucans}}]]"
           [:optional-begin "AND num_toucans > " :param-begin "num_toucans" :param-end :optional-end]

           "}}{{]][["
           [:param-end :param-begin :optional-end :optional-begin]

           "SELECT * FROM toucanneries WHERE TRUE [[AND num_toucans > {{num_toucans}}]]"
           ["SELECT * FROM toucanneries WHERE TRUE " :optional-begin "AND num_toucans > "
            :param-begin "num_toucans" :param-end :optional-end]

           "SELECT * FROM -- {{foo}}\n"
           ["SELECT * FROM " :line-comment-begin " " :param-begin "foo" :param-end :newline]

           "/*SELECT {{foo}}*/"
           [:block-comment-begin "SELECT " :param-begin "foo" :param-end :block-comment-end]}]
    (is (= expected (tokenize query true)) (pr-str query))))

(deftest tokenize-no-sql-comments-test
  (doseq [[query expected]
          {"-- {{num_toucans}}"
           ["-- " :param-begin "num_toucans" :param-end]

           "/* [[AND num_toucans > {{num_toucans}} --]] */"
           ["/* " :optional-begin "AND num_toucans > " :param-begin "num_toucans" :param-end
            " --" :optional-end " */"]}]
    (is (= expected (tokenize query false)) (pr-str query))))

(deftest parse-test
  (doseq [[group s->expected]
          {"queries with one param"
           {"select * from foo where bar=1"              ["select * from foo where bar=1"]
            "select * from foo where bar={{baz}}"        ["select * from foo where bar=" (param "baz")]
            "select * from foo [[where bar = {{baz}} ]]" ["select * from foo " (optional "where bar = " (param "baz") " ")]}

           "multiple params"
           {"SELECT * FROM bird_facts WHERE toucans_are_cool = {{toucans_are_cool}} AND bird_type = {{bird_type}}"
            ["SELECT * FROM bird_facts WHERE toucans_are_cool = " (param "toucans_are_cool")
             " AND bird_type = " (param "bird_type")]}

           "Multiple optional clauses"
           {(str "select * from foo where bar1 = {{baz}} "
                 "[[and bar2 = {{baz}}]] "
                 "[[and bar3 = {{baz}}]] "
                 "[[and bar4 = {{baz}}]]")
            ["select * from foo where bar1 = " (param "baz") " "
             (optional "and bar2 = " (param "baz")) " "
             (optional "and bar3 = " (param "baz")) " "
             (optional "and bar4 = " (param "baz"))]

            "SELECT * FROM toucanneries WHERE TRUE [[AND num_toucans > {{num_toucans}}]] [[AND total_birds > {{total_birds}}]]"
            ["SELECT * FROM toucanneries WHERE TRUE "
             (optional "AND num_toucans > " (param "num_toucans"))
             " "
             (optional "AND total_birds > " (param "total_birds"))]

            "select * from foobars [[ where foobars.id in (string_to_array({{foobar_id}}, ',')::integer[]) ]]"
            ["select * from foobars "
             (optional " where foobars.id in (string_to_array(" (param "foobar_id") ", ',')::integer[]) ")]}

           "single square brackets shouldn't get parsed"
           (let [query (str "SELECT [test_data.checkins.venue_id] AS [venue_id], "
                            "       [test_data.checkins.user_id] AS [user_id], "
                            "       [test_data.checkins.id] AS [checkins_id] "
                            "FROM [test_data.checkins] "
                            "LIMIT 2")]
             {query [query]})

           "Valid syntax in PG -- shouldn't get parsed"
           (let [query "SELECT array_dims(1 || '[0:1]={2,3}'::int[])"]
             {query [query]})

           "Queries with newlines (#11526)"
           {"SELECT count(*)\nFROM products\nWHERE category = {{category}}"
            ["SELECT count(*)\nFROM products\nWHERE category = " (param "category")]}

           "Queries with params in SQL comments (#7742)"
           {"SELECT -- {{foo}}" ["SELECT -- {{foo}}"]
            "[[{{this}} -- and]] that" [(optional (param "this") " -- and") " that"]
            "SELECT /* \n --{{foo}} */ {{bar}}" ["SELECT /* \n --{{foo}} */ " (param "bar")]}

           "-- inside {{...}} card ref tags should not trigger comment mode"
           {"SELECT * FROM {{#35885-monthly-revenue--customer---replacement-}}"
            ["SELECT * FROM " (param "#35885-monthly-revenue--customer---replacement-")]}

           "/* */ inside {{...}} card ref tags should not trigger comment mode"
           {"SELECT * FROM {{#1-revenue/*monthly*/}}"
            ["SELECT * FROM " (param "#1-revenue/*monthly*/")]}

           "JSON queries that contain non-param fragments like '}}'"
           {"{x: {y: \"{{param}}\"}}"         ["{x: {y: \"" (param "param") "\"}}"]
            "{$match: {{{date}}, field: 1}}}" ["{$match: {" (param "date") ", field: 1}}}"]}

           "Queries that contain non-param fragments like '}}'"
           {"select ']]' from t [[where x = {{foo}}]]" ["select ']]' from t " (optional "where x = " (param "foo"))]
            "select '}}' from t [[where x = {{foo}}]]" ["select '}}' from t " (optional "where x = " (param "foo"))]}}]
    (testing group
      (doseq [[s expected] s->expected]
        (is (= expected (parse/parse opts s)) (pr-str s)))))
  (testing "invalid/unterminated clauses throw"
    (doseq [invalid ["select * from foo [[where bar = {{baz}} "
                     "select * from foo [[where bar = {{baz]]"
                     "select * from foo {{bar}} {{baz"
                     "select * from foo [[clause 1 {{bar}}]] [[clause 2"
                     "select * from foo where bar = {{baz]]"
                     "select * from foo [[where bar = {{baz}}}}"]]
      (is (thrown? clojure.lang.ExceptionInfo (parse/parse opts invalid))
          (pr-str invalid)))))

(deftest disable-comment-handling-test
  (doseq [[query result] [["{{{foo}}: -- {{bar}}}"
                           ["{" (param "foo") ": -- " (param "bar") "}"]]
                          ["{{{foo}}: \"/* {{bar}} */\"}"
                           ["{" (param "foo") ": \"/* " (param "bar") " */\"}"]]]]
    (is (= result (parse/parse opts query false)) (pr-str query))))

(deftest tokens-in-strings-test
  (testing "skip malformed parameter tokens in strings"
    (is (= ["'{{'"] (parse/parse opts "'{{'")))
    (is (= ["'{{"] (parse/parse opts "'{{")))
    (is (= ["'{{}}'"] (parse/parse opts "'{{}}'")))
    (is (= ["'[['"] (parse/parse opts "'[['")))
    (is (= ["'[[]]'"] (parse/parse opts "'[[]]'")))
    (is (= ["'[[{{]]'"] (parse/parse opts "'[[{{]]'"))))
  (testing "parse well-formed tokens in strings"
    (is (= ["'" (param "x") "'"] (parse/parse opts "'{{x}}'")))
    (is (= ["'" (param "snippet: 'test'") "'"] (parse/parse opts "'{{snippet: 'test'}}'")))
    (is (= ["'{{" (param "x") "'"] (parse/parse opts "'{{{{x}}'")))
    (is (= ["'[[" (param "x") "'"] (parse/parse opts "'[[{{x}}'")))
    (is (= ["'" (optional (param "x")) "'"] (parse/parse opts "'[[{{x}}]]'"))))
  (testing "skip comment start in strings"
    (is (= ["concat('--'," (param "x") ")"] (parse/parse opts "concat('--',{{x}})")))
    (is (= ["concat('/*'," (param "x") ")"] (parse/parse opts "concat('/*',{{x}})")))))

(defn -main [& _]
  (let [{:keys [fail error]} (run-tests 'parse-gleam-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
