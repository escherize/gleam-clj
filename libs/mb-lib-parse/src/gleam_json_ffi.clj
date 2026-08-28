(ns gleam-json-ffi
  "Clojure implementations of gleam_json's native externals. Semantics follow
  the BEAM reference (gleam_json_ffi.erl over OTP's json module): Json values
  are pre-encoded string fragments, decode is strict JSON producing dynamic
  values (maps with string keys, lists, longs/doubles, booleans, nil), and
  decode errors carry the offending byte as 0xNN uppercase hex."
  (:require [clojure.string :as cstr]
            [gleam.prelude :as p]))

;; gleam.json's error constructors; resolved at call time because gleam.json
;; requires this ns.
(def ^:private ->unexpected-end (delay (requiring-resolve 'gleam.json/->UnexpectedEndOfInput)))
(def ^:private ->unexpected-byte (delay (requiring-resolve 'gleam.json/->UnexpectedByte)))
(def ^:private ->unexpected-sequence (delay (requiring-resolve 'gleam.json/->UnexpectedSequence)))

;; ---------- encoding: Json = an already-encoded String ----------

(defn json-to-string [^String j] j)

(defn json-to-iodata
  "StringTree is a vector of strings in this runtime."
  [^String j]
  [j])

(defn do-null [] "null")

(defn do-bool [b] (if b "true" "false"))

(defn do-int [n] (str n))

(defn do-float
  "Lowercase exponent marker to match Erlang's shortest-round-trip printing."
  [x]
  (let [s (str x)]
    (if (.contains ^String s "E") (.replace ^String s "E" "e") s)))

(defn do-string
  "JSON string escaping: the two mandatory escapes, the short control forms,
  \\u00NN for other control characters, UTF-8 passthrough for everything else."
  [^String s]
  (let [sb (StringBuilder. (+ 2 (.length s)))]
    (.append sb \")
    (dotimes [i (.length s)]
      (let [c (.charAt s i)]
        (case c
          \" (.append sb "\\\"")
          \\ (.append sb "\\\\")
          \newline (.append sb "\\n")
          \return (.append sb "\\r")
          \tab (.append sb "\\t")
          \backspace (.append sb "\\b")
          \formfeed (.append sb "\\f")
          (if (< (int c) 0x20)
            (.append sb (format "\\u%04x" (int c)))
            (.append sb c)))))
    (.append sb \")
    (str sb)))

(defn do-preprocessed-array [elems]
  (str "[" (cstr/join "," elems) "]"))

(defn do-object
  "Entries are #(String, Json) tuples: vectors of [key encoded-value]."
  [entries]
  (str "{"
       (cstr/join "," (map (fn [[k v]] (str (do-string k) ":" v)) entries))
       "}"))

;; ---------- decoding: strict JSON -> dynamic values ----------

(defn- fail-byte [^String s ^long i]
  (throw (ex-info "json" {:err (if (>= i (count s))
                                 (@->unexpected-end)
                                 (@->unexpected-byte
                                  (let [b (first (.getBytes (subs s i (inc i)) "UTF-8"))]
                                    (format "0x%X" (bit-and 255 (long b))))))})))

(declare parse-value)

(defn- skip-ws ^long [^String s ^long i]
  (loop [i i]
    (if (and (< i (.length s))
             (case (.charAt s (int i)) (\space \tab \newline \return) true false))
      (recur (inc i))
      i)))

(defn- parse-string [^String s ^long i]
  ;; i points at the opening quote
  (let [sb (StringBuilder.)]
    (loop [i (inc i)]
      (when (>= i (.length s)) (fail-byte s (.length s)))
      (let [c (.charAt s (int i))]
        (cond
          (= c \") [(str sb) (inc i)]
          (= c \\)
          (do
            (when (>= (inc i) (.length s)) (fail-byte s (.length s)))
            (let [e (.charAt s (int (inc i)))]
              (case e
                \" (do (.append sb \") (recur (+ i 2)))
                \\ (do (.append sb \\) (recur (+ i 2)))
                \/ (do (.append sb \/) (recur (+ i 2)))
                \b (do (.append sb \backspace) (recur (+ i 2)))
                \f (do (.append sb \formfeed) (recur (+ i 2)))
                \n (do (.append sb \newline) (recur (+ i 2)))
                \r (do (.append sb \return) (recur (+ i 2)))
                \t (do (.append sb \tab) (recur (+ i 2)))
                \u (let [end (+ i 6)]
                     (when (> end (.length s)) (fail-byte s (.length s)))
                     (let [hex (subs s (+ i 2) end)
                           cp (try (Integer/parseInt hex 16)
                                   (catch Exception _
                                     (throw (ex-info "json"
                                                     {:err (@->unexpected-sequence (str "\\u" hex))}))))]
                       (.append sb (char cp))
                       (recur end)))
                (fail-byte s (inc i)))))
          (< (int c) 0x20) (fail-byte s i)
          :else (do (.append sb c) (recur (inc i))))))))

(defn- parse-number [^String s ^long i]
  (let [n (.length s)
        digits (fn ^long [^long j]
                 (loop [j j]
                   (if (and (< j n) (Character/isDigit (.charAt s (int j)))) (recur (inc j)) j)))
        j (if (and (< i n) (= (.charAt s (int i)) \-)) (inc i) i)
        _ (when (or (>= j n) (not (Character/isDigit (.charAt s (int j))))) (fail-byte s j))
        j (if (= (.charAt s (int j)) \0)
            (let [j' (inc j)]
              ;; leading zeros are invalid JSON
              (when (and (< j' n) (Character/isDigit (.charAt s (int j')))) (fail-byte s j'))
              j')
            (digits j))
        [j frac?] (if (and (< j n) (= (.charAt s (int j)) \.))
                    (let [j' (inc j)]
                      (when (or (>= j' n) (not (Character/isDigit (.charAt s (int j'))))) (fail-byte s j'))
                      [(digits j') true])
                    [j false])
        [j exp?] (if (and (< j n) (case (.charAt s (int j)) (\e \E) true false))
                   (let [j' (inc j)
                         j' (if (and (< j' n) (case (.charAt s (int j')) (\+ \-) true false)) (inc j') j')]
                     (when (or (>= j' n) (not (Character/isDigit (.charAt s (int j'))))) (fail-byte s j'))
                     [(digits j') true])
                   [j false])
        text (subs s i j)]
    [(if (or frac? exp?)
       (Double/parseDouble text)
       (or (parse-long text) (bigint text)))
     j]))

(defn- parse-lit [^String s ^long i ^String lit value]
  ;; A truncated literal at end of input is unexpected-end, not a byte error.
  (loop [k 0]
    (cond
      (= k (.length lit)) [value (+ i k)]
      (>= (+ i k) (.length s)) (fail-byte s (.length s))
      (= (.charAt lit (int k)) (.charAt s (int (+ i k)))) (recur (inc k))
      :else (fail-byte s (+ i k)))))

(defn- parse-value [^String s ^long i]
  (let [i (skip-ws s i)]
    (when (>= i (.length s)) (fail-byte s i))
    (let [c (.charAt s (int i))]
      (cond
        (= c \") (parse-string s i)
        (= c \{)
        (let [i (skip-ws s (inc i))]
          (if (and (< i (.length s)) (= (.charAt s (int i)) \}))
            [{} (inc i)]
            (loop [i i, acc {}]
              (let [i (skip-ws s i)
                    _ (when (or (>= i (.length s)) (not= (.charAt s (int i)) \")) (fail-byte s i))
                    [k i] (parse-string s i)
                    i (skip-ws s i)
                    _ (when (or (>= i (.length s)) (not= (.charAt s (int i)) \:)) (fail-byte s i))
                    [v i] (parse-value s (inc i))
                    i (skip-ws s i)
                    acc (assoc acc k v)]
                (cond
                  (and (< i (.length s)) (= (.charAt s (int i)) \,)) (recur (inc i) acc)
                  (and (< i (.length s)) (= (.charAt s (int i)) \})) [acc (inc i)]
                  :else (fail-byte s i))))))
        (= c \[)
        (let [i' (skip-ws s (inc i))]
          (if (and (< i' (.length s)) (= (.charAt s (int i')) \]))
            [(list) (inc i')]
            (loop [i (inc i), acc []]
              (let [[v i] (parse-value s i)
                    i (skip-ws s i)]
                (cond
                  (and (< i (.length s)) (= (.charAt s (int i)) \,)) (recur (inc i) (conj acc v))
                  (and (< i (.length s)) (= (.charAt s (int i)) \])) [(apply list (conj acc v)) (inc i)]
                  :else (fail-byte s i))))))
        (= c \t) (parse-lit s i "true" true)
        (= c \f) (parse-lit s i "false" false)
        (= c \n) (parse-lit s i "null" nil)
        :else (parse-number s i)))))

(defn decode-string
  "decode_string(String) -> Result(Dynamic, DecodeError)"
  [^String s]
  (try
    (let [[v i] (parse-value s 0)
          i (skip-ws s i)]
      (when (< i (.length s)) (fail-byte s i))
      (p/->Ok v))
    (catch clojure.lang.ExceptionInfo e
      (if-let [err (:err (ex-data e))]
        (p/->Error err)
        (throw e)))))
