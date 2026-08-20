(ns gleam.io
  "Shims for gleam/io. Gleam names that collide with clojure.core are renamed
  (codegen rename table): println -> print-line, print -> write,
  println_error -> print-line-error, print_error -> write-error.")

(defn print-line [s]
  (println s))

(defn write [s]
  (print s)
  (flush))

(defn debug
  "Print a value to stderr, return it."
  [v]
  (binding [*out* *err*] (prn v))
  v)

(defn print-line-error [s]
  (binding [*out* *err*] (println s)))

(defn write-error [s]
  (binding [*out* *err*] (print s) (flush)))
