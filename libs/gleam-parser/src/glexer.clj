(ns glexer
  (:refer-clojure :exclude [comment next])
  (:require
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.option :as option]
   [gleam.prelude :as p]
   [gleam.result :as result]
   [gleam.string :as string]
   [glexer-ffi]
   [glexer.token :as token]
   [splitter :as splitter])
  (:import (gleam.prelude Ok)))

;; type Lexer
(defrecord Lexer [original-source source byte-offset preserve-whitespace preserve-comments mode newlines])
(defn Lexer? "True if `v` is a Lexer value." [v] (instance? Lexer v))

;; type LexerMode
(defrecord Normal [])
(defn Normal? "True if `v` is a Normal value." [v] (instance? Normal v))
(defrecord CheckForMinus [])
(defn CheckForMinus? "True if `v` is a CheckForMinus value." [v] (instance? CheckForMinus v))
(defrecord HasNestedDot [])
(defn HasNestedDot? "True if `v` is a HasNestedDot value." [v] (instance? HasNestedDot v))

;; type Position
(defrecord Position [byte-offset])
(defn Position? "True if `v` is a Position value." [v] (instance? Position v))

;; type CommentKind
(defrecord RegularComment [])
(defn RegularComment? "True if `v` is a RegularComment value." [v] (instance? RegularComment v))
(defrecord DocComment [])
(defn DocComment? "True if `v` is a DocComment value." [v] (instance? DocComment v))
(defrecord ModuleComment [])
(defn ModuleComment? "True if `v` is a ModuleComment value." [v] (instance? ModuleComment v))

;; type LexNumberMode
(defrecord LexInt [])
(defn LexInt? "True if `v` is a LexInt value." [v] (instance? LexInt v))
(defrecord LexFloat [])
(defn LexFloat? "True if `v` is a LexFloat value." [v] (instance? LexFloat v))
(defrecord LexFloatExponent [])
(defn LexFloatExponent? "True if `v` is a LexFloatExponent value." [v] (instance? LexFloatExponent v))

(defn new*
  {:malli/schema [:=> [:cat :string] [:fn Lexer?]]}
  [source]
  (->Lexer source
           source
           0
           true
           true
           (->Normal)
           (splitter/new* (list "\r\n" "\n"))))

(defn discard-whitespace
  {:malli/schema [:=> [:cat [:fn Lexer?]] [:fn Lexer?]]}
  [lexer]
  (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) false (:preserve-comments lexer) (:mode lexer) (:newlines lexer)))

(defn discard-comments
  {:malli/schema [:=> [:cat [:fn Lexer?]] [:fn Lexer?]]}
  [lexer]
  (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) false (:mode lexer) (:newlines lexer)))

(defn- length [string]
  (string/byte-size string))

(defn- some-token [result]
  (let [[lexer token] result]
    [lexer (option/->Some token)]))

(defn- advance [lexer source offset]
  (->Lexer (:original-source lexer) source (+' (:byte-offset lexer) offset) (:preserve-whitespace lexer) (:preserve-comments lexer) (:mode lexer) (:newlines lexer)))

(defn- advanced [token lexer source offset]
  [(advance lexer source offset) token])

(defn- token [lexer token source offset]
  (-> [token (->Position (:byte-offset lexer))]
      (advanced lexer source offset)
      some-token))

(def slice-bytes glexer-ffi/slice-bytes)

(defn- lex-uppercase-name [lexer start slice-size]
  (let [subject (:source lexer)]
    (cond
      (.startsWith ^String subject "a")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "b")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "c")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "d")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "e")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "f")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "g")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "h")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "i")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "j")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "k")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "l")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "m")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "n")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "o")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "p")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "q")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "r")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "s")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "t")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "u")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "v")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "w")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "x")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "y")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "z")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "A")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "B")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "C")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "D")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "E")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "F")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "G")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "H")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "I")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "J")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "K")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "L")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "M")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "N")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "O")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "P")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "Q")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "R")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "S")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "T")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "U")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "V")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "W")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "X")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "Y")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "Z")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "0")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "1")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "2")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "3")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "4")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "5")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "6")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "7")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "8")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "9")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-uppercase-name start (+' slice-size 1))))

      :else
      (let [name (slice-bytes (:original-source lexer) start slice-size)]
        [lexer name]))))

(defn- lex-lowercase-name [lexer start slice-size]
  (let [subject (:source lexer)]
    (cond
      (.startsWith ^String subject "a")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "b")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "c")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "d")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "e")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "f")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "g")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "h")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "i")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "j")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "k")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "l")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "m")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "n")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "o")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "p")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "q")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "r")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "s")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "t")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "u")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "v")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "w")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "x")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "y")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "z")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "0")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "1")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "2")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "3")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "4")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "5")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "6")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "7")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "8")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "9")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      (.startsWith ^String subject "_")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-lowercase-name start (+' slice-size 1))))

      :else
      (let [name (slice-bytes (:original-source lexer) start slice-size)]
        [lexer name]))))

(def drop-byte glexer-ffi/drop-byte)

(defn- lex-string [lexer start slice-size]
  (let [subject (:source lexer)]
    (cond
      (.startsWith ^String subject "\"")
      (let [source (subs subject 1) content (slice-bytes (:original-source lexer) (+' start 1) slice-size)]
        (-> [(token/->String content) (->Position start)]
            (advanced lexer source 1)
            some-token))

      (.startsWith ^String subject "\\")
      (let [source (subs subject 1) subject (string/pop-grapheme source)]
        (if (instance? gleam.prelude.Error subject)
          (-> (advance lexer source 1) (lex-string start (+' slice-size 1)))
          (let [grapheme (nth (:value subject) 0) source (nth (:value subject) 1) offset (+' 1 (length grapheme))]
            (-> (advance lexer source offset)
                (lex-string start (+' slice-size offset))))))

      (= subject "")
      (let [content (slice-bytes (:original-source lexer)
                                 (+' start 1)
                                 slice-size)]
        [lexer (option/->Some [(token/->UnterminatedString content) (->Position start)])])

      :else
      (-> (advance lexer (drop-byte (:source lexer)) 1)
          (lex-string start (+' slice-size 1))))))

(defn- lex-number [lexer mode start slice-size]
  (let [s0 (:source lexer)]
    (cond
      (.startsWith ^String s0 "_")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "0")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "1")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "2")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "3")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "4")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "5")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "6")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "7")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "8")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (.startsWith ^String s0 "9")
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number mode start (+' slice-size 1))))

      (and (.startsWith ^String s0 ".") (instance? LexInt mode))
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number (->LexFloat) start (+' slice-size 1))))

      (and (.startsWith ^String s0 "e-") (instance? LexFloat mode))
      (let [source (subs s0 2)]
        (-> (advance lexer source 2)
            (lex-number (->LexFloatExponent) start (+' slice-size 2))))

      (and (.startsWith ^String s0 "e") (instance? LexFloat mode))
      (let [source (subs s0 1)]
        (-> (advance lexer source 1)
            (lex-number (->LexFloatExponent) start (+' slice-size 1))))

      (instance? LexInt mode)
      (let [lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))
            content (slice-bytes (:original-source lexer) start slice-size)]
        [lexer (option/->Some [(token/->Int content) (->Position start)])])

      (or (instance? LexFloat mode) (instance? LexFloatExponent mode))
      (let [lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))
            content (slice-bytes (:original-source lexer) start slice-size)]
        [lexer (option/->Some [(token/->Float content) (->Position start)])]))))

(defn- lex-hexadecimal [lexer start slice-size]
  (let [subject (:source lexer)]
    (cond
      (.startsWith ^String subject "_")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "0")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "1")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "2")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "3")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "4")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "5")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "6")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "7")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "8")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "9")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "a")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "A")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "b")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "B")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "c")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "C")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "d")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "D")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "e")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "E")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "f")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      (.startsWith ^String subject "F")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1)
            (lex-hexadecimal start (+' slice-size 1))))

      :else
      (let [content (slice-bytes (:original-source lexer) start slice-size)]
        [lexer (option/->Some [(token/->Int content) (->Position start)])]))))

(defn- lex-octal [lexer start slice-size]
  (let [subject (:source lexer)]
    (cond
      (.startsWith ^String subject "_")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      (.startsWith ^String subject "0")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      (.startsWith ^String subject "1")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      (.startsWith ^String subject "2")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      (.startsWith ^String subject "3")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      (.startsWith ^String subject "4")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      (.startsWith ^String subject "5")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      (.startsWith ^String subject "6")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      (.startsWith ^String subject "7")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-octal start (+' slice-size 1))))

      :else
      (let [content (slice-bytes (:original-source lexer) start slice-size)]
        [lexer (option/->Some [(token/->Int content) (->Position start)])]))))

(defn- lex-binary [lexer start slice-size]
  (let [subject (:source lexer)]
    (cond
      (.startsWith ^String subject "_")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-binary start (+' slice-size 1))))

      (.startsWith ^String subject "0")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-binary start (+' slice-size 1))))

      (.startsWith ^String subject "1")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-binary start (+' slice-size 1))))

      :else
      (let [content (slice-bytes (:original-source lexer) start slice-size)]
        [lexer (option/->Some [(token/->Int content) (->Position start)])]))))

(defn- comment [lexer kind start]
  (let [[prefix suffix] (splitter/split-before (:newlines lexer)
                                               (:source lexer))
        eaten (length prefix)
        lexer (advance lexer suffix eaten)
        token (cond
                (instance? ModuleComment kind) (token/->CommentModule prefix)
                (instance? DocComment kind) (token/->CommentDoc prefix)
                (instance? RegularComment kind) (token/->CommentNormal prefix))]
    [lexer (option/->Some [token (->Position start)])]))

(defn- skip-comment
  "Ignores the rest of the line until it finds a newline, and signals the
  caller to continue lexing."
  [lexer]
  (let [[prefix suffix] (splitter/split-before (:newlines lexer)
                                               (:source lexer))
        eaten (length prefix)
        lexer (advance lexer suffix eaten)]
    [lexer (option/->None)]))

(defn- whitespace [lexer start slice-size]
  (let [subject (:source lexer)]
    (cond
      (.startsWith ^String subject " ")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (whitespace start (+' slice-size 1))))

      (.startsWith ^String subject "\t")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (whitespace start (+' slice-size 1))))

      (.startsWith ^String subject "\n")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (whitespace start (+' slice-size 1))))

      (.startsWith ^String subject "\r")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (whitespace start (+' slice-size 1))))

      :else
      (let [subject (:preserve-whitespace lexer)]
        (if (not subject)
          [lexer (option/->None)]
          (let [content (slice-bytes (:original-source lexer)
                                     start
                                     slice-size)]
            [lexer (option/->Some [(token/->Space content) (->Position start)])]))))))

(defn- lex-digits [lexer start slice-size]
  (let [subject (:source lexer)]
    (cond
      (.startsWith ^String subject "_")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "0")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "1")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "2")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "3")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "4")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "5")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "6")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "7")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "8")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      (.startsWith ^String subject "9")
      (let [source (subs subject 1)]
        (-> (advance lexer source 1) (lex-digits start (+' slice-size 1))))

      :else
      (let [digits (slice-bytes (:original-source lexer) start slice-size)]
        [lexer digits]))))

(defn- check-for-minus [lexer]
  (let [subject (:source lexer)]
    (if (.startsWith ^String subject "-")
      (let [source (subs subject 1) [lexer token] (token lexer (token/->Minus) source 1)]
        (p/->Ok [(->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer)) token]))
      (p/->Error nil))))

(defn- next [lexer]
  (let [subject (:mode lexer)]
    (cond
      (instance? CheckForMinus subject)
      (let [subject (check-for-minus lexer)]
        (if (instance? Ok subject)
          (let [result (:value subject)]
            result)
          [(->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer)) (option/->None)]))

      (instance? HasNestedDot subject)
      (let [subject (:source lexer)]
        (cond
          (.startsWith ^String subject "0")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "1")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "2")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "3")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "4")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "5")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "6")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "7")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "8")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          (.startsWith ^String subject "9")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer int] (-> (advance lexer source 1) (lex-digits byte-offset 1)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer))]
            [lexer (option/->Some [(token/->Int int) (->Position byte-offset)])])

          :else
          [(->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->Normal) (:newlines lexer)) (option/->None)]))

      (instance? Normal subject)
      (let [subject (:source lexer)]
        (cond
          (.startsWith ^String subject " ")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1) (whitespace (:byte-offset lexer) 1)))

          (.startsWith ^String subject "\n")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1) (whitespace (:byte-offset lexer) 1)))

          (.startsWith ^String subject "\r")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1) (whitespace (:byte-offset lexer) 1)))

          (.startsWith ^String subject "\t")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1) (whitespace (:byte-offset lexer) 1)))

          (.startsWith ^String subject "////")
          (let [source (subs subject 4) subject (:preserve-comments lexer)]
            (if (not subject)
              (skip-comment lexer)
              (-> (advance lexer source 4)
                  (comment (->ModuleComment) (:byte-offset lexer)))))

          (.startsWith ^String subject "///")
          (let [source (subs subject 3) subject (:preserve-comments lexer)]
            (if (not subject)
              (skip-comment lexer)
              (-> (advance lexer source 3)
                  (comment (->DocComment) (:byte-offset lexer)))))

          (.startsWith ^String subject "//")
          (let [source (subs subject 2) subject (:preserve-comments lexer)]
            (if (not subject)
              (skip-comment lexer)
              (-> (advance lexer source 2)
                  (comment (->RegularComment) (:byte-offset lexer)))))

          (.startsWith ^String subject "(")
          (let [source (subs subject 1)]
            (token lexer (token/->LeftParen) source 1))

          (.startsWith ^String subject ")")
          (let [source (subs subject 1)]
            (token lexer (token/->RightParen) source 1))

          (.startsWith ^String subject "{")
          (let [source (subs subject 1)]
            (token lexer (token/->LeftBrace) source 1))

          (.startsWith ^String subject "}")
          (let [source (subs subject 1)]
            (token lexer (token/->RightBrace) source 1))

          (.startsWith ^String subject "[")
          (let [source (subs subject 1)]
            (token lexer (token/->LeftSquare) source 1))

          (.startsWith ^String subject "]")
          (let [source (subs subject 1)]
            (token lexer (token/->RightSquare) source 1))

          (.startsWith ^String subject "@")
          (let [source (subs subject 1)]
            (token lexer (token/->At) source 1))

          (.startsWith ^String subject ":")
          (let [source (subs subject 1)]
            (token lexer (token/->Colon) source 1))

          (.startsWith ^String subject ",")
          (let [source (subs subject 1)]
            (token lexer (token/->Comma) source 1))

          (.startsWith ^String subject "..")
          (let [source (subs subject 2)]
            (token lexer (token/->DotDot) source 2))

          (.startsWith ^String subject ".")
          (let [source (subs subject 1) [lexer token] (token lexer (token/->Dot) source 1)]
            [(->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->HasNestedDot) (:newlines lexer)) token])

          (.startsWith ^String subject "#")
          (let [source (subs subject 1)]
            (token lexer (token/->Hash) source 1))

          (.startsWith ^String subject "!=")
          (let [source (subs subject 2)]
            (token lexer (token/->NotEqual) source 2))

          (.startsWith ^String subject "!")
          (let [source (subs subject 1)]
            (token lexer (token/->Bang) source 1))

          (.startsWith ^String subject "==")
          (let [source (subs subject 2)]
            (token lexer (token/->EqualEqual) source 2))

          (.startsWith ^String subject "=")
          (let [source (subs subject 1)]
            (token lexer (token/->Equal) source 1))

          (.startsWith ^String subject "|>")
          (let [source (subs subject 2)]
            (token lexer (token/->Pipe) source 2))

          (.startsWith ^String subject "||")
          (let [source (subs subject 2)]
            (token lexer (token/->VBarVBar) source 2))

          (.startsWith ^String subject "|")
          (let [source (subs subject 1)]
            (token lexer (token/->VBar) source 1))

          (.startsWith ^String subject "&&")
          (let [source (subs subject 2)]
            (token lexer (token/->AmperAmper) source 2))

          (.startsWith ^String subject "<<")
          (let [source (subs subject 2)]
            (token lexer (token/->LessLess) source 2))

          (.startsWith ^String subject ">>")
          (let [source (subs subject 2)]
            (token lexer (token/->GreaterGreater) source 2))

          (.startsWith ^String subject "<-")
          (let [source (subs subject 2)]
            (token lexer (token/->LeftArrow) source 2))

          (.startsWith ^String subject "->")
          (let [source (subs subject 2)]
            (token lexer (token/->RightArrow) source 2))

          (.startsWith ^String subject "<>")
          (let [source (subs subject 2)]
            (token lexer (token/->LessGreater) source 2))

          (.startsWith ^String subject "+.")
          (let [source (subs subject 2)]
            (token lexer (token/->PlusDot) source 2))

          (.startsWith ^String subject "-.")
          (let [source (subs subject 2)]
            (token lexer (token/->MinusDot) source 2))

          (.startsWith ^String subject "*.")
          (let [source (subs subject 2)]
            (token lexer (token/->StarDot) source 2))

          (.startsWith ^String subject "/.")
          (let [source (subs subject 2)]
            (token lexer (token/->SlashDot) source 2))

          (.startsWith ^String subject "<=.")
          (let [source (subs subject 3)]
            (token lexer (token/->LessEqualDot) source 3))

          (.startsWith ^String subject "<.")
          (let [source (subs subject 2)]
            (token lexer (token/->LessDot) source 2))

          (.startsWith ^String subject ">=.")
          (let [source (subs subject 3)]
            (token lexer (token/->GreaterEqualDot) source 3))

          (.startsWith ^String subject ">.")
          (let [source (subs subject 2)]
            (token lexer (token/->GreaterDot) source 2))

          (.startsWith ^String subject "0b")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2) (lex-binary (:byte-offset lexer) 2)))

          (.startsWith ^String subject "0o")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2) (lex-octal (:byte-offset lexer) 2)))

          (.startsWith ^String subject "0x")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-hexadecimal (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-0b")
          (let [source (subs subject 3)]
            (-> (advance lexer source 3) (lex-binary (:byte-offset lexer) 3)))

          (.startsWith ^String subject "-0o")
          (let [source (subs subject 3)]
            (-> (advance lexer source 3) (lex-octal (:byte-offset lexer) 3)))

          (.startsWith ^String subject "-0x")
          (let [source (subs subject 3)]
            (-> (advance lexer source 3)
                (lex-hexadecimal (:byte-offset lexer) 3)))

          (.startsWith ^String subject "0")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "1")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "2")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "3")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "4")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "5")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "6")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "7")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "8")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "9")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1)
                (lex-number (->LexInt) (:byte-offset lexer) 1)))

          (.startsWith ^String subject "-0")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-1")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-2")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-3")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-4")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-5")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-6")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-7")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-8")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "-9")
          (let [source (subs subject 2)]
            (-> (advance lexer source 2)
                (lex-number (->LexInt) (:byte-offset lexer) 2)))

          (.startsWith ^String subject "+")
          (let [source (subs subject 1)]
            (token lexer (token/->Plus) source 1))

          (.startsWith ^String subject "-")
          (let [source (subs subject 1)]
            (token lexer (token/->Minus) source 1))

          (.startsWith ^String subject "*")
          (let [source (subs subject 1)]
            (token lexer (token/->Star) source 1))

          (.startsWith ^String subject "/")
          (let [source (subs subject 1)]
            (token lexer (token/->Slash) source 1))

          (.startsWith ^String subject "<=")
          (let [source (subs subject 2)]
            (token lexer (token/->LessEqual) source 2))

          (.startsWith ^String subject "<")
          (let [source (subs subject 1)]
            (token lexer (token/->Less) source 1))

          (.startsWith ^String subject ">=")
          (let [source (subs subject 2)]
            (token lexer (token/->GreaterEqual) source 2))

          (.startsWith ^String subject ">")
          (let [source (subs subject 1)]
            (token lexer (token/->Greater) source 1))

          (.startsWith ^String subject "%")
          (let [source (subs subject 1)]
            (token lexer (token/->Percent) source 1))

          (.startsWith ^String subject "\"")
          (let [source (subs subject 1)]
            (-> (advance lexer source 1) (lex-string (:byte-offset lexer) 0)))

          (.startsWith ^String subject "_")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name (+' byte-offset 1) 0))]
            [lexer (option/->Some [(token/->DiscardName name) (->Position byte-offset)])])

          (.startsWith ^String subject "a")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "b")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "c")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "d")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "e")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "f")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "g")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "h")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "i")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "j")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "k")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "l")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "m")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "n")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "o")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "p")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "q")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "r")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "s")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "t")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "u")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "v")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "w")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "x")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "y")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "z")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-lowercase-name byte-offset 1)) token (cond (= name "as") (token/->As) (= name "assert") (token/->Assert) (= name "auto") (token/->Auto) (= name "case") (token/->Case) (= name "const") (token/->Const) (= name "delegate") (token/->Delegate) (= name "derive") (token/->Derive) (= name "echo") (token/->Echo) (= name "else") (token/->Else) (= name "fn") (token/->Fn) (= name "if") (token/->If) (= name "implement") (token/->Implement) (= name "import") (token/->Import) (= name "let") (token/->Let) (= name "macro") (token/->Macro) (= name "opaque") (token/->Opaque) (= name "panic") (token/->Panic) (= name "pub") (token/->Pub) (= name "test") (token/->Test) (= name "todo") (token/->Todo) (= name "type") (token/->Type) (= name "use") (token/->Use) :else (token/->Name name)) lexer (->Lexer (:original-source lexer) (:source lexer) (:byte-offset lexer) (:preserve-whitespace lexer) (:preserve-comments lexer) (->CheckForMinus) (:newlines lexer))]
            [lexer (option/->Some [token (->Position byte-offset)])])

          (.startsWith ^String subject "A")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "B")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "C")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "D")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "E")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "F")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "G")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "H")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "I")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "J")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "K")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "L")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "M")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "N")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "O")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "P")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "Q")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "R")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "S")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "T")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "U")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "V")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "W")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "X")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "Y")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          (.startsWith ^String subject "Z")
          (let [source (subs subject 1) byte-offset (:byte-offset lexer) [lexer name] (-> (advance lexer source 1) (lex-uppercase-name byte-offset 1))]
            [lexer (option/->Some [(token/->UpperName name) (->Position byte-offset)])])

          :else
          (let [subject (string/pop-grapheme (:source lexer))]
            (if (instance? gleam.prelude.Error subject)
              [lexer (option/->Some [(token/->EndOfFile) (->Position (:byte-offset lexer))])]
              (let [grapheme (nth (:value subject) 0) source (nth (:value subject) 1)]
                (token lexer
                       (token/->UnexpectedGrapheme grapheme)
                       source
                       (length grapheme))))))))))

(defn- do-lex [lexer tokens]
  (let [subject (next lexer)]
    (cond
      (instance? gleam.option.None (nth subject 1))
      (let [lexer (nth subject 0)]
        (recur lexer tokens))

      (and (instance? gleam.option.Some (nth subject 1)) (instance? glexer.token.EndOfFile (nth (:value (nth subject 1)) 0)))
      tokens

      (instance? gleam.option.Some (nth subject 1))
      (let [lexer (nth subject 0) token (:value (nth subject 1))]
        (recur lexer (list* token tokens))))))

(defn lex
  {:malli/schema [:=> [:cat [:fn Lexer?]]
                      [:sequential [:tuple [:or [:fn token/Name?] [:fn token/UpperName?] [:fn token/DiscardName?] [:fn token/Int?] [:fn token/Float?] [:fn token/String?] [:fn token/CommentDoc?] [:fn token/CommentNormal?] [:fn token/CommentModule?] [:fn token/As?] [:fn token/Assert?] [:fn token/Auto?] [:fn token/Case?] [:fn token/Const?] [:fn token/Delegate?] [:fn token/Derive?] [:fn token/Echo?] [:fn token/Else?] [:fn token/Fn?] [:fn token/If?] [:fn token/Implement?] [:fn token/Import?] [:fn token/Let?] [:fn token/Macro?] [:fn token/Opaque?] [:fn token/Panic?] [:fn token/Pub?] [:fn token/Test?] [:fn token/Todo?] [:fn token/Type?] [:fn token/Use?] [:fn token/LeftParen?] [:fn token/RightParen?] [:fn token/LeftBrace?] [:fn token/RightBrace?] [:fn token/LeftSquare?] [:fn token/RightSquare?] [:fn token/Plus?] [:fn token/Minus?] [:fn token/Star?] [:fn token/Slash?] [:fn token/Less?] [:fn token/Greater?] [:fn token/LessEqual?] [:fn token/GreaterEqual?] [:fn token/Percent?] [:fn token/PlusDot?] [:fn token/MinusDot?] [:fn token/StarDot?] [:fn token/SlashDot?] [:fn token/LessDot?] [:fn token/GreaterDot?] [:fn token/LessEqualDot?] [:fn token/GreaterEqualDot?] [:fn token/LessGreater?] [:fn token/At?] [:fn token/Colon?] [:fn token/Comma?] [:fn token/Hash?] [:fn token/Bang?] [:fn token/Equal?] [:fn token/EqualEqual?] [:fn token/NotEqual?] [:fn token/VBar?] [:fn token/VBarVBar?] [:fn token/AmperAmper?] [:fn token/LessLess?] [:fn token/GreaterGreater?] [:fn token/Pipe?] [:fn token/Dot?] [:fn token/DotDot?] [:fn token/LeftArrow?] [:fn token/RightArrow?] [:fn token/EndOfFile?] [:fn token/Space?] [:fn token/UnterminatedString?] [:fn token/UnexpectedGrapheme?]] [:fn Position?]]]]}
  [lexer]
  (-> (do-lex lexer (list)) list/reverse))

(declare unescape-codepoint unescape-loop)

(defn- unescape-codepoint [escaped unescaped codepoint]
  (let [subject (string/pop-grapheme escaped)]
    (cond
      (and (instance? Ok subject) (= (nth (:value subject) 0) "}"))
      (let [escaped (nth (:value subject) 1)]
        (p/with-use [[codepoint] (result/try* (int/base-parse codepoint 16))
                     [codepoint] (result/try* (string/utf-codepoint codepoint))]
          (let [codepoint (string/from-utf-codepoints (list codepoint))]
            (unescape-loop escaped (str unescaped codepoint)))))

      (instance? Ok subject)
      (let [c (nth (:value subject) 0) escaped (nth (:value subject) 1)]
        (recur escaped unescaped (str codepoint c)))

      (and (instance? gleam.prelude.Error subject) (nil? (:value subject)))
      (p/->Error nil))))

(defn- unescape-loop [escaped unescaped]
  (cond
    (.startsWith ^String escaped "\\\"")
    (let [escaped (subs escaped 2)]
      (recur escaped (str unescaped "\"")))

    (.startsWith ^String escaped "\\\\")
    (let [escaped (subs escaped 2)]
      (recur escaped (str unescaped "\\")))

    (.startsWith ^String escaped "\\f")
    (let [escaped (subs escaped 2)]
      (recur escaped (str unescaped "\f")))

    (.startsWith ^String escaped "\\n")
    (let [escaped (subs escaped 2)]
      (recur escaped (str unescaped "\n")))

    (.startsWith ^String escaped "\\r")
    (let [escaped (subs escaped 2)]
      (recur escaped (str unescaped "\r")))

    (.startsWith ^String escaped "\\t")
    (let [escaped (subs escaped 2)]
      (recur escaped (str unescaped "\t")))

    (.startsWith ^String escaped "\\u{")
    (let [escaped (subs escaped 3)]
      (unescape-codepoint escaped unescaped ""))

    (.startsWith ^String escaped "\\")
    (p/->Error nil)

    :else
    (let [subject (string/pop-grapheme escaped)]
      (if (instance? gleam.prelude.Error subject)
        (p/->Ok unescaped)
        (let [grapheme (nth (:value subject) 0) escaped (nth (:value subject) 1)]
          (recur escaped (str unescaped grapheme)))))))

(defn unescape-string
  "Convert the value of a string token to the string it represents.
  
  This function can fail if the original string contains invalid escape sequences.
  
  ```gleam
  unescape_string(\"\\\\\\\"X\\\\\\\" marks the spot\")
  // --> Ok(\"\\\"X\\\" marks the spot\")
  
  unescape_string(\"\\\\u{1F600}\")
  // --> Ok(\"😀\")
  
  unescape_string(\"\\\\x\")
  // --> Error(Nil)
  ```"
  {:malli/schema [:=> [:cat :string] [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [string]
  (unescape-loop string ""))

(defn to-source
  "Turn a sequence of tokens back to their Gleam source code representation."
  {:malli/schema [:=> [:cat [:sequential [:tuple [:or [:fn token/Name?] [:fn token/UpperName?] [:fn token/DiscardName?] [:fn token/Int?] [:fn token/Float?] [:fn token/String?] [:fn token/CommentDoc?] [:fn token/CommentNormal?] [:fn token/CommentModule?] [:fn token/As?] [:fn token/Assert?] [:fn token/Auto?] [:fn token/Case?] [:fn token/Const?] [:fn token/Delegate?] [:fn token/Derive?] [:fn token/Echo?] [:fn token/Else?] [:fn token/Fn?] [:fn token/If?] [:fn token/Implement?] [:fn token/Import?] [:fn token/Let?] [:fn token/Macro?] [:fn token/Opaque?] [:fn token/Panic?] [:fn token/Pub?] [:fn token/Test?] [:fn token/Todo?] [:fn token/Type?] [:fn token/Use?] [:fn token/LeftParen?] [:fn token/RightParen?] [:fn token/LeftBrace?] [:fn token/RightBrace?] [:fn token/LeftSquare?] [:fn token/RightSquare?] [:fn token/Plus?] [:fn token/Minus?] [:fn token/Star?] [:fn token/Slash?] [:fn token/Less?] [:fn token/Greater?] [:fn token/LessEqual?] [:fn token/GreaterEqual?] [:fn token/Percent?] [:fn token/PlusDot?] [:fn token/MinusDot?] [:fn token/StarDot?] [:fn token/SlashDot?] [:fn token/LessDot?] [:fn token/GreaterDot?] [:fn token/LessEqualDot?] [:fn token/GreaterEqualDot?] [:fn token/LessGreater?] [:fn token/At?] [:fn token/Colon?] [:fn token/Comma?] [:fn token/Hash?] [:fn token/Bang?] [:fn token/Equal?] [:fn token/EqualEqual?] [:fn token/NotEqual?] [:fn token/VBar?] [:fn token/VBarVBar?] [:fn token/AmperAmper?] [:fn token/LessLess?] [:fn token/GreaterGreater?] [:fn token/Pipe?] [:fn token/Dot?] [:fn token/DotDot?] [:fn token/LeftArrow?] [:fn token/RightArrow?] [:fn token/EndOfFile?] [:fn token/Space?] [:fn token/UnterminatedString?] [:fn token/UnexpectedGrapheme?]] [:fn Position?]]]]
                      :string]}
  [tokens]
  (p/with-use [[source _use1] (list/fold tokens "")]
    (let [[tok _] _use1]
      (str source (token/to-source tok)))))
