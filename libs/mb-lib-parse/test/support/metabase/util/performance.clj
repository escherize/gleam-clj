(ns metabase.util.performance
  "Test stub: the real ns provides drop-in faster variants of these."
  (:refer-clojure :exclude [some empty? for]))

(def some clojure.core/some)
(def empty? clojure.core/empty?)
(defmacro for [& body] `(clojure.core/for ~@body))
