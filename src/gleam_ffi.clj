(ns gleam-ffi
  "The native core under the compiled gleam_stdlib: Clojure implementations
  of every body-less @external. Wired up via stdlib-src/clojure-externals.txt.

  Representations: Dict = persistent map, TransientDict = transient map,
  BitArray = vector of byte ints, StringTree = nested vector of strings,
  UtfCodepoint = int, Dynamic = any value."
  (:refer-clojure :exclude [get])
  (:require [clojure.string :as cstr]
            [gleam.prelude])
  (:import (gleam.prelude Ok)
           (java.text BreakIterator)
           (java.util Base64)))

(defn- ok [v] (gleam.prelude/->Ok v))
(defn- err [] (gleam.prelude/->Error nil))

;; ---------- io ----------

(defn print-stdout [s] (print s) (flush))
(defn print-error [s] (binding [*out* *err*] (print s) (flush)))
(defn println-stdout [s] (println s))
(defn println-error [s] (binding [*out* *err*] (println s)))

;; ---------- dict ----------

(defn dict-new [] {})
(defn dict-size [d] (count d))
(defn dict-has-key [k d] (contains? d k))
(defn dict-get [d k] (if-let [e (find d k)] (ok (val e)) (err)))
(defn dict-insert [k v d] (assoc d k v))
(defn dict-map-values [f d] (reduce-kv (fn [m k v] (assoc m k (f k v))) {} d))

(defn- entries-sorted
  "BEAM iterates small maps in key order; sort when comparable for parity."
  [d]
  (try (sort-by key d)
       (catch ClassCastException _ (seq d))))

(defn dict-fold [fun initial d]
  (reduce (fn [acc [k v]] (fun k v acc)) initial (entries-sorted d)))

(defn dict-to-transient [d] (transient d))
(defn dict-from-transient [t] (persistent! t))
(defn transient-insert [k v t] (assoc! t k v))
(defn transient-delete [k t] (dissoc! t k))
(defn transient-update-with [k fun init t]
  (let [sentinel (Object.)
        cur (clojure.core/get t k sentinel)]
    (if (identical? cur sentinel)
      (assoc! t k init)
      (assoc! t k (fun cur)))))

;; ---------- dynamic ----------

(defn classify [v]
  (cond
    (nil? v) "Nil"
    (or (true? v) (false? v)) "Bool"
    (string? v) "String"
    (integer? v) "Int"
    (float? v) "Float"
    (map? v) "Dict"
    (vector? v) "Tuple of some elements"
    (sequential? v) "List"
    (fn? v) "Function"
    :else (str (.getSimpleName (class v)))))

(defn identity1 [v] v)

;; ---------- int ----------

(defn int-parse [s]
  (if (re-matches #"[+-]?\d+" s)
    (ok (Long/parseLong s))
    (err)))

(defn int-base-parse [s base]
  (try (ok (Long/parseLong s (int base)))
       (catch Exception _ (err))))

(defn int-to-string [n] (str n))

(defn int-to-base-string [n base]
  (cstr/upper-case (Long/toString (long n) (int base))))

(defn int-to-float [n] (double n))

(defn bitwise-and [a b] (bit-and a b))
(defn bitwise-not [a] (bit-not a))
(defn bitwise-or [a b] (bit-or a b))
(defn bitwise-xor [a b] (bit-xor a b))
(defn shift-left [a b] (bit-shift-left a b))
(defn shift-right [a b] (bit-shift-right a b))

;; ---------- float ----------

(defn float-parse [s]
  (if (re-matches #"[+-]?\d+\.\d+(e[+-]?\d+)?" s)
    (ok (Double/parseDouble s))
    (err)))

(defn float-to-string [f]
  (let [s (str (double f))]
    ;; Erlang never prints scientific notation for these shortest-repr floats
    ;; the same way Java does; keep Java's repr — corpora will police it.
    s))

(defn f-ceiling [x] (Math/ceil x))
(defn f-floor [x] (Math/floor x))
(defn f-round [x] (Math/round (double x)))
(defn f-truncate [x] (long x))
(defn f-to-float [n] (double n))
(defn f-power [a b] (Math/pow a b))
(defn f-random [] (rand))
(defn f-log [x] (Math/log x))
(defn f-exp [x] (Math/exp x))

;; ---------- string ----------

(defn- graphemes [^String s]
  (let [bi (doto (BreakIterator/getCharacterInstance) (.setText s))]
    (loop [start (.first bi) end (.next bi) acc []]
      (if (= end BreakIterator/DONE)
        acc
        (recur end (.next bi) (conj acc (subs s start end)))))))

(defn str-length [^String s] (count (graphemes s)))
(defn str-lowercase [^String s] (.toLowerCase s))
(defn str-uppercase [^String s] (.toUpperCase s))
(defn str-less-than [a b] (neg? (compare a b)))

(defn grapheme-slice [s index length]
  (let [gs (graphemes s)]
    (apply str (take length (drop index gs)))))

(defn unsafe-byte-slice [^String s index length]
  (String. (.getBytes s "UTF-8") (int index) (int length) "UTF-8"))

(defn crop [^String s ^String sub]
  (let [i (.indexOf s sub)]
    (if (neg? i) s (subs s i))))

(defn str-contains [^String h ^String n] (.contains h n))
(defn starts-with [^String s ^String p] (.startsWith s p))
(defn ends-with [^String s ^String suf] (.endsWith s suf))

(defn erl-split
  "Split at the FIRST occurrence of the pattern, like 2-arg string:split —
  [before after], or [s] when absent. (split_once pattern-matches on
  exactly two elements, so splitting every occurrence here is a bug.)"
  [^String s ^String pat]
  (let [i (if (empty? pat) -1 (.indexOf s pat))]
    (if (neg? i)
      (list s)
      (list (subs s 0 i) (subs s (+ i (count pat)))))))

(defn erl-trim [^String s direction]
  (case (.getSimpleName (class direction))
    "Leading" (cstr/triml s)
    "Trailing" (cstr/trimr s)))

(defn pop-grapheme [^String s]
  (let [gs (graphemes s)]
    (if (empty? gs)
      (err)
      (ok [(first gs) (subs s (count (first gs)))]))))

(defn string-to-codepoints [^String s]
  (apply list (iterator-seq (.iterator (.codePoints s)))))

(defn from-utf-codepoints [cps]
  (let [sb (StringBuilder.)]
    (doseq [cp cps] (.appendCodePoint sb (int cp)))
    (str sb)))

(defn str-join
  "gleam/string.join override: the Gleam body folds with <>, which BEAM's
  binary-append optimization makes linear but JVM string copying makes
  quadratic (9.5s for 250k parts). clojure.string/join is a StringBuilder."
  [strings ^String separator]
  (cstr/join separator strings))

(defn byte-size [^String s] (alength (.getBytes s "UTF-8")))

(defn remove-prefix [^String s ^String p]
  (if (.startsWith s p) (subs s (count p)) s))

(defn remove-suffix [^String s ^String suf]
  (if (and (.endsWith s suf) (pos? (count suf)))
    (subs s 0 (- (count s) (count suf)))
    s))

;; ---------- inspect (returns a StringTree) ----------

(defn- inspect-str [v]
  (cond
    (nil? v) "Nil"
    (true? v) "True"
    (false? v) "False"
    (string? v) (pr-str v)
    (number? v) (str v)
    (record? v) (str (.getSimpleName (class v))
                     (when (seq v)
                       (str "(" (cstr/join ", " (map inspect-str (vals v))) ")")))
    (map? v) (str "dict.from_list(["
                  (cstr/join ", " (map (fn [[k val]]
                                         (str "#(" (inspect-str k) ", "
                                              (inspect-str val) ")"))
                                       (entries-sorted v)))
                  "])")
    (vector? v) (str "#(" (cstr/join ", " (map inspect-str v)) ")")
    (sequential? v) (str "[" (cstr/join ", " (map inspect-str v)) "]")
    (fn? v) "//fn"
    :else (pr-str v)))

(defn do-inspect [term] [(inspect-str term)])

;; ---------- string_tree (nested vectors of strings) ----------

(defn- st-str ^String [tree] (apply str (flatten tree)))

(defn st-append-tree [tree suffix] [tree suffix])
(defn st-from-strings [strings] (vec strings))
(defn st-concat [trees] (vec trees))
(defn st-from-string [s] [s])
(defn st-to-string [tree] (st-str tree))
(defn st-byte-size [tree] (byte-size (st-str tree)))
(defn st-lowercase [tree] [(cstr/lower-case (st-str tree))])
(defn st-uppercase [tree] [(cstr/upper-case (st-str tree))])
(defn st-to-graphemes [s] (apply list (graphemes s)))
(defn- split-all [^String s ^String pat]
  (if (empty? pat)
    (list s)
    (loop [s s acc []]
      (let [i (.indexOf s pat)]
        (if (neg? i)
          (apply list (conj acc s))
          (recur (subs s (+ i (count pat))) (conj acc (subs s 0 i))))))))

(defn st-split [tree ^String pat _direction]
  (apply list (map vector (split-all (st-str tree) pat))))
(defn st-replace [tree ^String pat ^String sub]
  [(.replace (st-str tree) pat sub)])

;; ---------- bit_array (vector of byte ints) ----------

(defn ba-from-string [^String s]
  (mapv #(bit-and 255 %) (.getBytes s "UTF-8")))

(defn ba-bit-size [b] (* 8 (count b)))
(defn ba-byte-size [b] (count b))

(defn ba-slice [b position length]
  (let [n (count b)]
    (cond
      (and (>= length 0) (<= 0 position) (<= (+ position length) n))
      (ok (subvec b position (+ position length)))
      (and (neg? length) (<= 0 (+ position length)) (<= position n))
      (ok (subvec b (+ position length) position))
      :else (err))))

(defn ba-unsafe-to-string [b]
  (String. (byte-array (map unchecked-byte b)) "UTF-8"))

(defn ba-concat [arrays] (vec (apply concat arrays)))

(defn ba-base64-encode [b padding]
  (let [enc (if padding (Base64/getEncoder) (.withoutPadding (Base64/getEncoder)))]
    (.encodeToString enc (byte-array (map unchecked-byte b)))))

(defn ba-decode64 [^String s]
  (try (ok (mapv #(bit-and 255 %) (.decode (Base64/getDecoder) s)))
       (catch IllegalArgumentException _ (err))))

(defn ba-base16-encode [b]
  (cstr/upper-case (apply str (map #(format "%02x" %) b))))

(defn ba-base16-decode [^String s]
  (if (and (even? (count s)) (re-matches #"[0-9a-fA-F]*" s))
    (ok (mapv #(Integer/parseInt (subs s % (+ % 2)) 16)
              (range 0 (count s) 2)))
    (err)))

(defn ba-to-int-and-size [b]
  [(reduce (fn [acc byte] (+' (*' acc 256) byte)) 0 b) (* 8 (count b))])

;; ---------- uri ----------

(defn pop-codeunit [^String s]
  (if (.isEmpty s)
    [0 ""]
    [(int (.charAt s 0)) (subs s 1)]))

(defn codeunit-slice [^String s from length]
  (let [n (count s)
        start (min (max 0 from) n)
        end (min (max start (+ start length)) n)]
    (subs s start end)))

(defn percent-encode [^String s]
  (apply str
         (mapcat (fn [b]
                   (let [c (char (bit-and 255 b))]
                     (if (or (Character/isLetterOrDigit c)
                             (#{\- \. \_ \~} c))
                       [(str c)]
                       [(format "%%%02X" (bit-and 255 b))])))
                 (.getBytes s "UTF-8"))))

(defn percent-decode [^String s]
  (try
    (let [out (java.io.ByteArrayOutputStream.)]
      (loop [i 0]
        (if (>= i (count s))
          (ok (String. (.toByteArray out) "UTF-8"))
          (let [c (.charAt s i)]
            (if (= c \%)
              (do (.write out (Integer/parseInt (subs s (inc i) (+ i 3)) 16))
                  (recur (+ i 3)))
              (do (.write out (int c))
                  (recur (inc i))))))))
    (catch Exception _ (err))))

(defn parse-query [^String q]
  (try
    (ok (apply list
               (for [pair (cstr/split q #"&") :when (not (empty? pair))]
                 (let [[k v] (cstr/split pair #"=" 2)
                       dec #(let [r (percent-decode (cstr/replace % "+" " "))]
                              (if (instance? Ok r)
                                (:value r)
                                (throw (ex-info "bad" {}))))]
                   [(dec (or k "")) (dec (or v ""))]))))
    (catch Exception _ (err))))

;; ---------- dynamic/decode ----------

(defn- some-of [v] ((requiring-resolve 'gleam.option/->Some) v))
(defn- none [] ((requiring-resolve 'gleam.option/->None)))

(defn bare-index [data key]
  (cond
    (map? data) (if-let [e (find data key)]
                  (ok (some-of (val e)))
                  (ok (none)))
    (and (sequential? data) (integer? key))
    (if (< -1 key (count data))
      (ok (some-of (nth (vec data) key)))
      (ok (none)))
    :else (gleam.prelude/->Error (classify data))))

;; On failure these return the type's ZERO as the Error payload (matching
;; gleam_stdlib.erl): decoder combinators like `map` transform the
;; placeholder, so a raw-value payload would reach the wrong-typed fn.
(defn dynamic-string [v] (if (string? v) (ok v) (gleam.prelude/->Error "")))
(defn dynamic-int [v] (if (integer? v) (ok v) (gleam.prelude/->Error 0)))
(defn dynamic-float [v] (if (float? v) (ok v) (gleam.prelude/->Error 0.0)))
(defn dynamic-bit-array [v]
  (if (and (vector? v) (every? integer? v)) (ok v) (gleam.prelude/->Error [])))
(defn decode-dict [v] (if (map? v) (ok v) (err)))
(defn is-null [v] (nil? v))
(defn decode-list
  "decode_list(data, item, push_path, index, acc) -> #(List(t), List(DecodeError)).
  Mirrors gleam_stdlib.erl: first failing element wins, its errors pushed
  under the element's index; non-list input is a List type error."
  [data item push-path index _acc]
  (if-not (sequential? data)
    [(list) (list ((requiring-resolve 'gleam.dynamic.decode/->DecodeError)
                   "List" (classify data) (list)))]
    (loop [xs (seq data), i (long index), out []]
      (if xs
        (let [[o errs] (item (first xs))]
          (if (empty? errs)
            (recur (next xs) (inc i) (conj out o))
            (push-path [(list) errs] (str i))))
        [(apply list out) (list)]))))

;; ---------- bit_array fns overridden whole (sub-byte pattern matching) ----------

(defn ba-is-utf8 [b]
  (try
    (.decode (doto (.newDecoder (java.nio.charset.Charset/forName "UTF-8"))
               (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
               (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
             (java.nio.ByteBuffer/wrap (byte-array (map unchecked-byte b))))
    true
    (catch java.nio.charset.CharacterCodingException _ false)))

(defn ba-inspect
  ([b] (str "<<" (cstr/join ", " (map str b)) ">>"))
  ([b _acc] (ba-inspect b)))

(defn ba-compare [a b]
  (let [c (compare (vec a) (vec b))
        order-of (fn [n] ((requiring-resolve
                           (cond (neg? n) 'gleam.order/->Lt
                                 (zero? n) 'gleam.order/->Eq
                                 :else 'gleam.order/->Gt))))]
    (order-of c)))

(defn to-utf-codepoints
  ([s] (string-to-codepoints s))
  ([s _acc] (string-to-codepoints s)))

(defn ba-starts-with [b prefix]
  (let [b (vec b) p (vec prefix)]
    (and (<= (count p) (count b))
         (= (subvec b 0 (count p)) p))))
