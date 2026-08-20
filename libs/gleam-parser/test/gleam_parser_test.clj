(ns gleam-parser-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [gleam-parser]))

(deftest parses-a-module
  (let [m (gleam-parser/parse "
pub type Shape { Circle(Float) Point }
pub fn area(s: Shape) -> Float { 0.0 }
const tau = 6.28
")]
    (is (some? m))
    (is (= [{:kind :function :name "area" :publicity "Public" :parameters 1}
            {:kind :custom-type :name "Shape" :publicity "Public"
             :variants ["Circle" "Point"]}
            {:kind :constant :name "tau" :publicity "Private"}]
           (gleam-parser/defs m)))))

(deftest invalid-source
  (is (nil? (gleam-parser/parse "pub fn oops( {")))
  (is (thrown? clojure.lang.ExceptionInfo (gleam-parser/parse! "pub fn oops( {"))))

(deftest parses-glance-itself
  ;; The compiled parser parsing its own source is the round-trip proof.
  (let [src (slurp "project/build/packages/glance/src/glance.gleam")
        m (gleam-parser/parse src)]
    (is (< 70 (count (:functions m))))
    (is (< 30 (count (:custom-types m))))))

(defn -main [& _]
  (let [{:keys [fail error]} (run-tests 'gleam-parser-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
