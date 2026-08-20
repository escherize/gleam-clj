(ns tree-traversal
  (:require
   [gleam.list :as list]
   [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

;; type Tree
(defrecord Empty [])
(defrecord Node [f0 f1 f2])

(defn preorder [t]
  (if (instance? Empty t)
    (list)
    (let [v (:f0 t) l (:f1 t) r (:f2 t)]
      (list* v (list/append (preorder l) (preorder r))))))

(defn inorder [t]
  (if (instance? Empty t)
    (list)
    (let [v (:f0 t) l (:f1 t) r (:f2 t)]
      (list/append (list/append (inorder l) (list v)) (inorder r)))))

(defn postorder [t]
  (if (instance? Empty t)
    (list)
    (let [v (:f0 t) l (:f1 t) r (:f2 t)]
      (list/append (list/append (postorder l) (postorder r)) (list v)))))

(defn- levelorder-helper [trees]
  (cond
    (empty? trees) (list)
    (and (seq trees) (instance? Empty (first trees))) (let [rest' (rest trees)]
                                                        (recur rest'))
    (and (seq trees) (instance? Node (first trees))) (let [v (:f0 (first trees)) l (:f1 (first trees)) r (:f2 (first trees)) rest' (rest trees)]
                                                       (list* v (levelorder-helper (list/append rest'
                                                                                       (list l r)))))))

(defn levelorder [t]
  (levelorder-helper (list t)))

(defn main []
  (let [example (->Node 1
                        (->Node 2
                                (->Node 4
                                        (->Node 7 (->Empty) (->Empty))
                                        (->Empty))
                                (->Node 5 (->Empty) (->Empty)))
                        (->Node 3
                                (->Node 6
                                        (->Node 8 (->Empty) (->Empty))
                                        (->Node 9 (->Empty) (->Empty)))
                                (->Empty)))]
    (p/echo (preorder example) "tree_traversal.gleam:51")
    (p/echo (inorder example) "tree_traversal.gleam:52")
    (p/echo (postorder example) "tree_traversal.gleam:53")
    (p/echo (levelorder example) "tree_traversal.gleam:54")
    nil))

(defn -main [& _]
  (main))
