(ns gleam.prelude
  "Gleam prelude: types every compiled module depends on.
  Gleam's Nil maps to Clojure nil.")

(defrecord Ok [value])

;; shadow auto-imported java.lang.Error; codegen must emit this ns-unmap
;; for any variant whose name collides with a java.lang class
(ns-unmap *ns* 'Error)
(defrecord Error [value])

(defn ok? [r] (instance? Ok r))

(defn echo
  "Gleam's `echo`: print the value to stderr and return it.
  2-arity gets a location/message prefix from the emitter."
  ([v] (binding [*out* *err*] (prn v)) v)
  ([v prefix] (binding [*out* *err*] (println prefix (pr-str v))) v))
