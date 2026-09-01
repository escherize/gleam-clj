(ns money
  (:require
   [gleam.int :as int]
   [gleam.io :as io]
   [gleam.prelude :as p]))

;; type Money
(defprotocol IMoney)
(defrecord Money [cents] IMoney)
(defn Money? "True if `v` is a Money value." [v] (instance? Money v))
(defn Money-schema
  "Malli schema for Money(currency)."
  [_currency]
  [:and [:fn Money?] [:map [:cents :int]]])

;; type Usd
(defprotocol IUsd)
(defn Usd? "True if `v` is any Usd value." [v] (instance? money.IUsd v))
(defn Usd-schema
  "Malli schema for Usd."
  []
  [:fn Usd?])

;; type Eur
(defprotocol IEur)
(defn Eur? "True if `v` is any Eur value." [v] (instance? money.IEur v))
(defn Eur-schema
  "Malli schema for Eur."
  []
  [:fn Eur?])

(defn usd
  "usd(cents: Int) -> Money(Usd)"
  {:malli/schema [:=> [:cat :int] (Money-schema (Usd-schema))]
   :gleam/src "money.gleam:14"}
  [cents]
  (->Money cents))

(defn eur
  "eur(cents: Int) -> Money(Eur)"
  {:malli/schema [:=> [:cat :int] (Money-schema (Eur-schema))]
   :gleam/src "money.gleam:18"}
  [cents]
  (->Money cents))

(defn add
  "add(a: Money(a), b: Money(a)) -> Money(a)

   Same-currency arithmetic only: `add(usd(1), eur(1))`
   refuses to compile."
  {:malli/schema [:=> [:cat (Money-schema :any) (Money-schema :any)]
                      (Money-schema :any)]
   :gleam/src "money.gleam:24"}
  [a b]
  (->Money (+' (:cents a) (:cents b))))

(defn main
  "main() -> Nil"
  {:malli/schema [:=> [:cat] :nil] :gleam/src "money.gleam:28"}
  []
  (p/let-assert (->Money 300) (add (usd 100) (usd 200)))
  (p/let-assert (->Money 250) (add (eur 50) (eur 200)))
  (io/println (str "balance: " (int/to-string 300))))

(defn -main [& _]
  (main))
