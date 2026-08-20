(ns glexer-ffi
  "Clojure implementations of glexer's native externals (UTF-8 byte ops).")

(defn slice-bytes
  "Byte-indexed UTF-8 slice. Like glexer, trusts callers to cut on
  codepoint boundaries."
  [^String s from sized]
  (String. (.getBytes s "UTF-8") (int from) (int sized) "UTF-8"))

(defn drop-byte [^String s]
  (let [b (.getBytes s "UTF-8")]
    (String. b 1 (dec (alength b)) "UTF-8")))
