(ns gleam-parser
  "Parse Gleam source code from Clojure.

  The parser is glance — Gleam's own parser library, written in Gleam —
  compiled to Clojure by gleam-clj. Results are Clojure records with
  keyword access; `defs` flattens the common case."
  (:require [glance]
            [gleam.prelude :as p])
  (:import (gleam.prelude Ok)))

(defn parse
  "Parse a string of Gleam source. Returns the glance Module record
  (keys: :functions :custom-types :constants :imports :type-aliases),
  or nil when the source does not parse."
  [src]
  (let [r (glance/module src)]
    (when (instance? Ok r)
      (:value r))))

(defn parse!
  "Like parse, but throws ex-info (with the glance error record as
  :error) on invalid source."
  [src]
  (let [r (glance/module src)]
    (if (instance? Ok r)
      (:value r)
      (throw (ex-info "gleam parse error" {:error (:value r)})))))

(defn- definition [d]
  ;; glance wraps each item in a Definition record: {:definition x :attributes [...]}
  (:definition d))

(defn defs
  "Flat summary of a parsed module: seq of {:kind :name :publicity} maps
  for functions, custom types, constants, and type aliases."
  [module]
  (concat
   (for [f (map definition (:functions module))]
     {:kind :function
      :name (:name f)
      :publicity (-> f :publicity class .getSimpleName)
      :parameters (count (:parameters f))})
   (for [t (map definition (:custom-types module))]
     {:kind :custom-type
      :name (:name t)
      :publicity (-> t :publicity class .getSimpleName)
      :variants (mapv :name (:variants t))})
   (for [c (map definition (:constants module))]
     {:kind :constant
      :name (:name c)
      :publicity (-> c :publicity class .getSimpleName)})
   (for [a (map definition (:type-aliases module))]
     {:kind :type-alias
      :name (:name a)
      :publicity (-> a :publicity class .getSimpleName)})))
