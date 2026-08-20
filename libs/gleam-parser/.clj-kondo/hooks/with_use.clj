(ns hooks.with-use
  "Expand gleam.prelude/with-use for analysis the same way the macro does:
  each [params call] pair becomes call with an appended (fn params ...)
  continuation."
  (:require [clj-kondo.hooks-api :as api]))

(defn with-use [{:keys [node]}]
  (let [[_ binding-vec & body] (:children node)
        pairs (partition 2 (:children binding-vec))
        node* (reduce
               (fn [acc [params call]]
                 (let [call-children (if (seq (:children call))
                                       (:children call)
                                       [call])]
                   (api/list-node
                    (concat call-children
                            [(api/list-node
                              [(api/token-node 'fn) params acc])]))))
               (api/list-node (list* (api/token-node 'do) body))
               (reverse pairs))]
    {:node node*}))
