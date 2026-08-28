(ns gleam-regexp-ffi
  "Clojure implementations of gleam_regexp's native externals, over
  java.util.regex. Semantics follow the BEAM reference (gleam_regexp_ffi.erl):
  unicode character classes on, empty submatches become None, trailing
  non-participating groups are trimmed from scan results, and replacement
  strings understand & and \\N. Compile-error MESSAGES differ from PCRE's;
  the Ok/Error outcome and match behavior are what parity covers."
  (:refer-clojure :exclude [compile replace])
  (:require [gleam.option :as option]
            [gleam.prelude :as p])
  (:import (java.util.regex Matcher Pattern PatternSyntaxException)))

;; gleam.regexp's records are compiled Gleam whose ns requires this one;
;; resolve its constructors at call time to avoid the require cycle.
(def ^:private ->match (delay (requiring-resolve 'gleam.regexp/->Match)))
(def ^:private ->compile-error (delay (requiring-resolve 'gleam.regexp/->CompileError)))

(defn compile
  "do_compile(pattern, Options) -> Result(Regexp, CompileError)"
  [^String pattern options]
  (let [flags (cond-> (bit-or Pattern/UNICODE_CASE Pattern/UNICODE_CHARACTER_CLASS)
                (:case-insensitive options) (bit-or Pattern/CASE_INSENSITIVE)
                (:multi-line options) (bit-or Pattern/MULTILINE))]
    (try
      (p/->Ok (Pattern/compile pattern flags))
      (catch PatternSyntaxException e
        (p/->Error (@->compile-error (.getDescription e) (max 0 (.getIndex e))))))))

(defn check [^Pattern regexp ^String s]
  (.find (.matcher regexp s)))

(defn split
  "Keeps trailing empty parts, like Erlang's re:split."
  [^Pattern regexp ^String s]
  (apply list (.split regexp s -1)))

(defn- sub-option
  "A submatch that did not participate or matched the empty string is None."
  [^String g]
  (if (or (nil? g) (= g "")) (option/->None) (option/->Some g)))

(defn- submatches
  "Group submatches up to the last participating group (Erlang's re trims
  trailing non-participating ones); interior unset or empty groups are None."
  [^Matcher m]
  (let [last-set (long (loop [i (.groupCount m)]
                         (cond
                           (zero? i) 0
                           (some? (.group m (int i))) i
                           :else (recur (dec i)))))]
    (apply list (mapv #(sub-option (.group m (int %))) (range 1 (inc last-set))))))

(defn scan
  "do_scan(regexp, string) -> List(Match)"
  [^Pattern regexp ^String s]
  (let [m (.matcher regexp s)]
    (loop [out []]
      (if (.find m)
        (recur (conj out (@->match (.group m) (submatches m))))
        (apply list out)))))

(defn- expand-replacement
  "Erlang re:replace replacement syntax: & is the whole match, \\N and
  \\g{N} are groups, backslash escapes the next character."
  [^Matcher m ^String rep]
  (let [sb (StringBuilder.)
        n (.length rep)
        group-str (fn [^long i]
                    (or (when (<= i (.groupCount m)) (.group m (int i))) ""))]
    (loop [i 0]
      (if (>= i n)
        (str sb)
        (let [c (.charAt rep i)]
          (cond
            (= c \&)
            (do (.append sb (.group m)) (recur (inc i)))

            (and (= c \\) (< (inc i) n))
            (let [d (.charAt rep (inc i))]
              (cond
                (Character/isDigit d)
                (let [j (long (loop [j (inc i)]
                                (if (and (< j n) (Character/isDigit (.charAt rep j)))
                                  (recur (inc j))
                                  j)))]
                  (.append sb ^String (group-str (parse-long (subs rep (inc i) j))))
                  (recur j))

                (= d \g)
                (if (and (< (+ i 2) n) (= (.charAt rep (+ i 2)) \{))
                  (let [close (.indexOf rep "}" (+ i 3))]
                    (if (neg? close)
                      (do (.append sb c) (recur (inc i)))
                      (do (.append sb ^String (group-str (parse-long (subs rep (+ i 3) close))))
                          (recur (inc close)))))
                  (do (.append sb c) (recur (inc i))))

                :else (do (.append sb d) (recur (+ i 2)))))

            :else (do (.append sb c) (recur (inc i)))))))))

(defn- replace-with
  [^Pattern regexp ^String s per-match]
  (let [m (.matcher regexp s)
        sb (StringBuilder.)]
    (loop [last 0]
      (if (.find m)
        (do (.append sb s last (.start m))
            (.append sb ^String (per-match m))
            (recur (long (.end m))))
        (do (.append sb s last (.length s))
            (str sb))))))

(defn replace [^Pattern regexp ^String s ^String replacement]
  (replace-with regexp s (fn [^Matcher m] (expand-replacement m replacement))))

(defn match-map
  [^Pattern regexp ^String s substitute]
  (replace-with
   regexp s
   (fn [^Matcher m] (substitute (@->match (.group m) (submatches m))))))
