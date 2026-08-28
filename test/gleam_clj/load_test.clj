(ns gleam-clj.load-test
  "Smoke test for the REPL loader. Runnable with:
    clojure -Sdeps '{:paths [\"src\" \"stdlib-clj\" \"test\"]}' -M -m gleam-clj.load-test"
  (:require [clojure.test :refer [deftest is run-tests]]
            [gleam-clj.load :as gl]))

(deftest eval-and-call
  (let [ns-sym (gl/eval-gleam "pub fn triple(x: Int) -> Int { x * 3 }")
        triple (resolve (symbol (str ns-sym) "triple"))]
    (is (= 63 (triple 21)))))

(deftest require-and-cache
  (let [a (gl/require-gleam "gleam-src/jellyfish.gleam")
        b (gl/require-gleam "gleam-src/jellyfish.gleam")]
    (is (= a b))
    (is (= 'jellyfish a))))

(deftest require-chases-imports
  (let [ns-sym (gl/require-gleam "gleam-src/quads.gleam")]
    (is (= 'quads ns-sym))
    (is (= 12 ((resolve (symbol (str ns-sym) "quad")) 3)))
    (is (some? (resolve 'doubles/double)))))

(defn -main [& _]
  (let [{:keys [fail error]} (run-tests 'gleam-clj.load-test)]
    (when (pos? (+ fail error)) (System/exit 1))))
