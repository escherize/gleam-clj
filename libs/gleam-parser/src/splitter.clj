(ns splitter
  (:require
   [gleam.list :as list]
   [splitter-ffi]))

;; type Splitter
(defprotocol ISplitter)
(defn Splitter? "True if `v` is any Splitter value." [v] (instance? splitter.ISplitter v))
(defn Splitter-schema
  "Malli schema for Splitter."
  []
  [:fn Splitter?])

(def ^{:gleam/src "project/build/packages/splitter/src/splitter.gleam:127"} make splitter-ffi/make)

(defn new*
  "new(substrings: List(String)) -> Splitter

   Create a new splitter for a given list of substrings.

   Substrings are matched for in the order the appear in the list, if one
   substring is the substring of another place it later in the list than the
   superstring.

   Empty strings are discarded, and an empty list will not split off any
   prefix.

   There is a small cost to creating a splitter, so if you are going to split
   a string multiple times, and you want as much performance as possible, then
   it is better to reuse the same splitter than to create a new one each time."
  {:malli/schema [:=> [:cat [:sequential :string]] (Splitter-schema)]
   :gleam/src "project/build/packages/splitter/src/splitter.gleam:18"}
  [substrings]
  (-> substrings (list/filter (fn [x] (not= x ""))) make))

(def ^{:malli/schema [:=> [:cat (Splitter-schema) :string] [:tuple :string :string :string]] :gleam/src "project/build/packages/splitter/src/splitter.gleam:49"} split splitter-ffi/split)

(def ^{:malli/schema [:=> [:cat (Splitter-schema) :string] [:tuple :string :string]] :gleam/src "project/build/packages/splitter/src/splitter.gleam:75"} split-before splitter-ffi/split-before)

(def ^{:malli/schema [:=> [:cat (Splitter-schema) :string] [:tuple :string :string]] :gleam/src "project/build/packages/splitter/src/splitter.gleam:101"} split-after splitter-ffi/split-after)

(def ^{:malli/schema [:=> [:cat (Splitter-schema) :string] :boolean] :gleam/src "project/build/packages/splitter/src/splitter.gleam:123"} would-split splitter-ffi/would-split)
