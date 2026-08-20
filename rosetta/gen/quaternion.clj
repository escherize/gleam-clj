(ns quaternion
  (:require
   [gleam.float :as float]
   [gleam.io :as io]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Quaternion
(defrecord Quaternion [f0 f1 f2 f3])

(defn to-string [q]
  (let [{a :f0 b :f1 c :f2 d :f3} q]
    (str (str (str (str (str (str (str (str "(" (float/to-string a)) ", ") (float/to-string b)) ", ") (float/to-string c)) ", ") (float/to-string d)) ")")))

(defn multiply [q1 q2]
  (let [{a :f0 b :f1 c :f2 d :f3} q1
        {w :f0 x :f1 y :f2 z :f3} q2]
    (->Quaternion (- (- (- (* a w) (* b x)) (* c y)) (* d z))
                  (- (+ (+ (* a x) (* w b)) (* c z)) (* d y))
                  (+ (+ (- (* a y) (* b z)) (* c w)) (* d x))
                  (+ (- (+ (* a z) (* b y)) (* c x)) (* d w)))))

(defn multiply-scalar [q real]
  (let [{a :f0 b :f1 c :f2 d :f3} q]
    (->Quaternion (* a real) (* b real) (* c real) (* d real))))

(defn add [q1 q2]
  (let [{a :f0 b :f1 c :f2 d :f3} q1
        {w :f0 x :f1 y :f2 z :f3} q2]
    (->Quaternion (+ a w) (+ b x) (+ c y) (+ d z))))

(defn add-scalar [q real]
  (let [{a :f0 b :f1 c :f2 d :f3} q]
    (->Quaternion (+ real a) b c d)))

(defn conjugate [q]
  (let [{a :f0 b :f1 c :f2 d :f3} q]
    (->Quaternion a (* -1.0 b) (* -1.0 c) (* -1.0 d))))

(defn negate [q]
  (let [{a :f0 b :f1 c :f2 d :f3} q]
    (->Quaternion (* -1.0 a) (* -1.0 b) (* -1.0 c) (* -1.0 d))))

(defn norm [q]
  (let [{a :f0 b :f1 c :f2 d :f3} q]
    (let [v (float/square-root (+ (+ (+ (* a a) (* b b)) (* c c)) (* d d)))]
      (when-not (instance? Ok v)
        (throw (ex-info "let assert failed" {:value v})))
      (let [result (:value v)]
        result))))

(defn main []
  (let [q (->Quaternion 1.0 2.0 3.0 4.0)
        q1 (->Quaternion 2.0 3.0 4.0 5.0)
        q2 (->Quaternion 3.0 4.0 5.0 6.0)
        r 7.0]
    (io/println (str "q = " (to-string q)))
    (io/println (str "q1 = " (to-string q1)))
    (io/println (str "q2 = " (to-string q2)))
    (io/println (str (str "r = " (float/to-string r)) "\n"))
    (io/println (str "norm(q) = " (float/to-string (norm q))))
    (io/println (str "negate(q) = " (-> (negate q) (to-string))))
    (io/println (str "conjugate(q) = " (-> (conjugate q) (to-string))))
    (io/println (str "add_scalar(q, r) = " (-> (add-scalar q r) (to-string))))
    (io/println (str "add(q1, q2) = " (-> (add q1 q2) (to-string))))
    (io/println (str "multiply_scalar(q, r) = " (-> (multiply-scalar q r) (to-string))))
    (io/println (str "multiply(q1, q2) = " (-> (multiply q1 q2) (to-string))))
    (io/println (str "multiply(q2, q1) = " (-> (multiply q2 q1) (to-string))))))

(defn -main [& _]
  (main))
