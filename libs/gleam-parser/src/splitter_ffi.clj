(ns splitter-ffi
  "Clojure implementations of splitter's native externals. A Splitter is the
  pattern vector; matching is leftmost-in-string, then first-pattern order —
  same semantics as the package's regex-alternation JS implementation.")

(defn make [patterns]
  (vec (remove empty? patterns)))

(defn- find-match
  "[index matched-pattern] of the leftmost match, or nil."
  [patterns ^String s]
  (when (seq patterns)
    (->> patterns
         (keep (fn [^String p]
                 (let [i (.indexOf s p)]
                   (when (>= i 0) [i p]))))
         (sort-by first)
         first)))

(defn split [patterns s]
  (if-let [[i ^String p] (find-match patterns s)]
    [(subs s 0 i) p (subs s (+ i (count p)))]
    [s "" ""]))

(defn split-before [patterns s]
  (if-let [[i _] (find-match patterns s)]
    [(subs s 0 i) (subs s i)]
    [s ""]))

(defn split-after [patterns s]
  (if-let [[i ^String p] (find-match patterns s)]
    [(subs s 0 (+ i (count p))) (subs s (+ i (count p)))]
    [s ""]))

(defn would-split [patterns s]
  (some? (find-match patterns s)))
