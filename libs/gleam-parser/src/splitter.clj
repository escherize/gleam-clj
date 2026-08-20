(ns splitter
  (:require
   [gleam.list :as list]
   [splitter-ffi]))

;; type Splitter

(def make splitter-ffi/make)

(defn new*
  "Create a new splitter for a given list of substrings.
  
  Substrings are matched for in the order the appear in the list, if one
  substring is the substring of another place it later in the list than the
  superstring.
  
  Empty strings are discarded, and an empty list will not split off any
  prefix.
  
  There is a small cost to creating a splitter, so if you are going to split
  a string multiple times, and you want as much performance as possible, then
  it is better to reuse the same splitter than to create a new one each time."
  {:malli/schema [:=> [:cat [:sequential :string]] [:or ]]}
  [substrings]
  (-> substrings (list/filter (fn [x] (not= x ""))) make))

(def ^{:malli/schema [:=> [:cat [:or ] :string] [:tuple :string :string :string]]} split splitter-ffi/split)

(def ^{:malli/schema [:=> [:cat [:or ] :string] [:tuple :string :string]]} split-before splitter-ffi/split-before)

(def ^{:malli/schema [:=> [:cat [:or ] :string] [:tuple :string :string]]} split-after splitter-ffi/split-after)

(def ^{:malli/schema [:=> [:cat [:or ] :string] :boolean]} would-split splitter-ffi/would-split)
