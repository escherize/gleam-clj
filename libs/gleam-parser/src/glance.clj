(ns glance
  (:refer-clojure :exclude [name slurp])
  (:require
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.option :as option]
   [gleam.prelude :as p]
   [gleam.result :as result]
   [gleam.string :as string]
   [glexer :as glexer]
   [glexer.token :as t])
  (:import (gleam.prelude Ok)))

;; type Definition
(defrecord Definition [attributes definition])
(defn Definition? [v] (instance? Definition v))

;; type Attribute
(defrecord Attribute [name arguments])
(defn Attribute? [v] (instance? Attribute v))

;; type Module
(defrecord Module [imports custom-types type-aliases constants functions])
(defn Module? [v] (instance? Module v))

;; type Function
(defrecord Function [location name publicity parameters return body])
(defn Function? [v] (instance? Function v))

;; type Span
(defrecord Span [start end])
(defn Span? [v] (instance? Span v))

;; type Statement
(defrecord Use [location patterns function])
(defn Use? [v] (instance? Use v))
(defrecord Assignment [location kind pattern annotation value])
(defn Assignment? [v] (instance? Assignment v))
(defrecord Assert [location expression message])
(defn Assert? [v] (instance? Assert v))
(defrecord Expression [value])
(defn Expression? [v] (instance? Expression v))

;; type AssignmentKind
(defrecord Let [])
(defn Let? [v] (instance? Let v))
(defrecord LetAssert [message])
(defn LetAssert? [v] (instance? LetAssert v))

;; type UsePattern
(defrecord UsePattern [pattern annotation])
(defn UsePattern? [v] (instance? UsePattern v))

;; type Pattern
(defrecord PatternInt [location value])
(defn PatternInt? [v] (instance? PatternInt v))
(defrecord PatternFloat [location value])
(defn PatternFloat? [v] (instance? PatternFloat v))
(defrecord PatternString [location value])
(defn PatternString? [v] (instance? PatternString v))
(defrecord PatternDiscard [location name])
(defn PatternDiscard? [v] (instance? PatternDiscard v))
(defrecord PatternVariable [location name])
(defn PatternVariable? [v] (instance? PatternVariable v))
(defrecord PatternTuple [location elements])
(defn PatternTuple? [v] (instance? PatternTuple v))
(defrecord PatternList [location elements tail])
(defn PatternList? [v] (instance? PatternList v))
(defrecord PatternAssignment [location pattern name])
(defn PatternAssignment? [v] (instance? PatternAssignment v))
(defrecord PatternConcatenate [location prefix prefix-name rest-name])
(defn PatternConcatenate? [v] (instance? PatternConcatenate v))
(defrecord PatternBitString [location segments])
(defn PatternBitString? [v] (instance? PatternBitString v))
(defrecord PatternVariant [location module constructor arguments with-spread])
(defn PatternVariant? [v] (instance? PatternVariant v))

;; type Expression
(defrecord Int [location value])
(defn Int? [v] (instance? Int v))
(ns-unmap *ns* 'Float)
(defrecord Float [location value])
(defn Float? [v] (instance? Float v))
(ns-unmap *ns* 'String)
(defrecord String [location value])
(defn String? [v] (instance? String v))
(defrecord Variable [location name])
(defn Variable? [v] (instance? Variable v))
(defrecord NegateInt [location value])
(defn NegateInt? [v] (instance? NegateInt v))
(defrecord NegateBool [location value])
(defn NegateBool? [v] (instance? NegateBool v))
(defrecord Block [location statements])
(defn Block? [v] (instance? Block v))
(defrecord Panic [location message])
(defn Panic? [v] (instance? Panic v))
(defrecord Todo [location message])
(defn Todo? [v] (instance? Todo v))
(defrecord Tuple [location elements])
(defn Tuple? [v] (instance? Tuple v))
(defrecord List [location elements rest])
(defn List? [v] (instance? List v))
(defrecord Fn [location arguments return-annotation body])
(defn Fn? [v] (instance? Fn v))
(defrecord RecordUpdate [location module constructor record fields])
(defn RecordUpdate? [v] (instance? RecordUpdate v))
(defrecord FieldAccess [location container label])
(defn FieldAccess? [v] (instance? FieldAccess v))
(defrecord Call [location function arguments])
(defn Call? [v] (instance? Call v))
(defrecord TupleIndex [location tuple index])
(defn TupleIndex? [v] (instance? TupleIndex v))
(defrecord FnCapture [location label function arguments-before arguments-after])
(defn FnCapture? [v] (instance? FnCapture v))
(defrecord BitString [location segments])
(defn BitString? [v] (instance? BitString v))
(defrecord Case [location subjects clauses])
(defn Case? [v] (instance? Case v))
(defrecord BinaryOperator [location name left right])
(defn BinaryOperator? [v] (instance? BinaryOperator v))
(defrecord Echo [location expression message])
(defn Echo? [v] (instance? Echo v))

;; type Clause
(defrecord Clause [patterns guard body])
(defn Clause? [v] (instance? Clause v))

;; type BitStringSegmentOption
(defrecord BytesOption [])
(defn BytesOption? [v] (instance? BytesOption v))
(defrecord IntOption [])
(defn IntOption? [v] (instance? IntOption v))
(defrecord FloatOption [])
(defn FloatOption? [v] (instance? FloatOption v))
(defrecord BitsOption [])
(defn BitsOption? [v] (instance? BitsOption v))
(defrecord Utf8Option [])
(defn Utf8Option? [v] (instance? Utf8Option v))
(defrecord Utf16Option [])
(defn Utf16Option? [v] (instance? Utf16Option v))
(defrecord Utf32Option [])
(defn Utf32Option? [v] (instance? Utf32Option v))
(defrecord Utf8CodepointOption [])
(defn Utf8CodepointOption? [v] (instance? Utf8CodepointOption v))
(defrecord Utf16CodepointOption [])
(defn Utf16CodepointOption? [v] (instance? Utf16CodepointOption v))
(defrecord Utf32CodepointOption [])
(defn Utf32CodepointOption? [v] (instance? Utf32CodepointOption v))
(defrecord SignedOption [])
(defn SignedOption? [v] (instance? SignedOption v))
(defrecord UnsignedOption [])
(defn UnsignedOption? [v] (instance? UnsignedOption v))
(defrecord BigOption [])
(defn BigOption? [v] (instance? BigOption v))
(defrecord LittleOption [])
(defn LittleOption? [v] (instance? LittleOption v))
(defrecord NativeOption [])
(defn NativeOption? [v] (instance? NativeOption v))
(defrecord SizeValueOption [value])
(defn SizeValueOption? [v] (instance? SizeValueOption v))
(defrecord SizeOption [value])
(defn SizeOption? [v] (instance? SizeOption v))
(defrecord UnitOption [value])
(defn UnitOption? [v] (instance? UnitOption v))

;; type BitArraySize
(defrecord BitArraySizeInt [location value])
(defn BitArraySizeInt? [v] (instance? BitArraySizeInt v))
(defrecord BitArraySizeVariable [location name])
(defn BitArraySizeVariable? [v] (instance? BitArraySizeVariable v))
(defrecord BitArraySizeBinaryOperator [location operator left right])
(defn BitArraySizeBinaryOperator? [v] (instance? BitArraySizeBinaryOperator v))
(defrecord BitArraySizeBlock [location inner])
(defn BitArraySizeBlock? [v] (instance? BitArraySizeBlock v))

;; type BitArraySizeOperator
(defrecord BitArraySizeAdd [])
(defn BitArraySizeAdd? [v] (instance? BitArraySizeAdd v))
(defrecord BitArraySizeSubtract [])
(defn BitArraySizeSubtract? [v] (instance? BitArraySizeSubtract v))
(defrecord BitArraySizeMultiply [])
(defn BitArraySizeMultiply? [v] (instance? BitArraySizeMultiply v))
(defrecord BitArraySizeDivide [])
(defn BitArraySizeDivide? [v] (instance? BitArraySizeDivide v))
(defrecord BitArraySizeRemainder [])
(defn BitArraySizeRemainder? [v] (instance? BitArraySizeRemainder v))

;; type BinaryOperator
(defrecord And [])
(defn And? [v] (instance? And v))
(defrecord Or [])
(defn Or? [v] (instance? Or v))
(defrecord Eq [])
(defn Eq? [v] (instance? Eq v))
(defrecord NotEq [])
(defn NotEq? [v] (instance? NotEq v))
(defrecord LtInt [])
(defn LtInt? [v] (instance? LtInt v))
(defrecord LtEqInt [])
(defn LtEqInt? [v] (instance? LtEqInt v))
(defrecord LtFloat [])
(defn LtFloat? [v] (instance? LtFloat v))
(defrecord LtEqFloat [])
(defn LtEqFloat? [v] (instance? LtEqFloat v))
(defrecord GtEqInt [])
(defn GtEqInt? [v] (instance? GtEqInt v))
(defrecord GtInt [])
(defn GtInt? [v] (instance? GtInt v))
(defrecord GtEqFloat [])
(defn GtEqFloat? [v] (instance? GtEqFloat v))
(defrecord GtFloat [])
(defn GtFloat? [v] (instance? GtFloat v))
(defrecord Pipe [])
(defn Pipe? [v] (instance? Pipe v))
(defrecord AddInt [])
(defn AddInt? [v] (instance? AddInt v))
(defrecord AddFloat [])
(defn AddFloat? [v] (instance? AddFloat v))
(defrecord SubInt [])
(defn SubInt? [v] (instance? SubInt v))
(defrecord SubFloat [])
(defn SubFloat? [v] (instance? SubFloat v))
(defrecord MultInt [])
(defn MultInt? [v] (instance? MultInt v))
(defrecord MultFloat [])
(defn MultFloat? [v] (instance? MultFloat v))
(defrecord DivInt [])
(defn DivInt? [v] (instance? DivInt v))
(defrecord DivFloat [])
(defn DivFloat? [v] (instance? DivFloat v))
(defrecord RemainderInt [])
(defn RemainderInt? [v] (instance? RemainderInt v))
(defrecord Concatenate [])
(defn Concatenate? [v] (instance? Concatenate v))

;; type FnParameter
(defrecord FnParameter [name type-])
(defn FnParameter? [v] (instance? FnParameter v))

;; type FunctionParameter
(defrecord FunctionParameter [label name type-])
(defn FunctionParameter? [v] (instance? FunctionParameter v))

;; type AssignmentName
(defrecord Named [value])
(defn Named? [v] (instance? Named v))
(defrecord Discarded [value])
(defn Discarded? [v] (instance? Discarded v))

;; type Import
(defrecord Import [location module alias unqualified-types unqualified-values])
(defn Import? [v] (instance? Import v))

;; type Constant
(defrecord Constant [location name publicity annotation value])
(defn Constant? [v] (instance? Constant v))

;; type UnqualifiedImport
(defrecord UnqualifiedImport [name alias])
(defn UnqualifiedImport? [v] (instance? UnqualifiedImport v))

;; type Publicity
(defrecord Public [])
(defn Public? [v] (instance? Public v))
(defrecord Private [])
(defn Private? [v] (instance? Private v))

;; type TypeAlias
(defrecord TypeAlias [location name publicity parameters aliased])
(defn TypeAlias? [v] (instance? TypeAlias v))

;; type CustomType
(defrecord CustomType [location name publicity opaque- parameters variants])
(defn CustomType? [v] (instance? CustomType v))

;; type Variant
(defrecord Variant [name fields attributes])
(defn Variant? [v] (instance? Variant v))

;; type RecordUpdateField
(defrecord RecordUpdateField [label item])
(defn RecordUpdateField? [v] (instance? RecordUpdateField v))

;; type VariantField
(defrecord LabelledVariantField [item label])
(defn LabelledVariantField? [v] (instance? LabelledVariantField v))
(defrecord UnlabelledVariantField [item])
(defn UnlabelledVariantField? [v] (instance? UnlabelledVariantField v))

;; type Field
(defrecord LabelledField [label label-location item])
(defn LabelledField? [v] (instance? LabelledField v))
(defrecord ShorthandField [label location])
(defn ShorthandField? [v] (instance? ShorthandField v))
(defrecord UnlabelledField [item])
(defn UnlabelledField? [v] (instance? UnlabelledField v))

;; type Type
(defrecord NamedType [location name module parameters])
(defn NamedType? [v] (instance? NamedType v))
(defrecord TupleType [location elements])
(defn TupleType? [v] (instance? TupleType v))
(defrecord FunctionType [location parameters return])
(defn FunctionType? [v] (instance? FunctionType v))
(defrecord VariableType [location name])
(defn VariableType? [v] (instance? VariableType v))
(defrecord HoleType [location name])
(defn HoleType? [v] (instance? HoleType v))

;; type Error
(defrecord UnexpectedEndOfInput [])
(defn UnexpectedEndOfInput? [v] (instance? UnexpectedEndOfInput v))
(defrecord UnexpectedToken [token position])
(defn UnexpectedToken? [v] (instance? UnexpectedToken v))

;; type UnqualifiedImports
(defrecord UnqualifiedImports [types values end remaining-tokens])
(defn UnqualifiedImports? [v] (instance? UnqualifiedImports v))

;; type PatternConstructorArguments
(defrecord PatternConstructorArguments [fields spread end remaining-tokens])
(defn PatternConstructorArguments? [v] (instance? PatternConstructorArguments v))

;; type ParseExpressionUnitContext
(defrecord RegularExpressionUnit [])
(defn RegularExpressionUnit? [v] (instance? RegularExpressionUnit v))
(defrecord ExpressionUnitAfterPipe [])
(defn ExpressionUnitAfterPipe? [v] (instance? ExpressionUnitAfterPipe v))

;; type ParsedList
(defrecord ParsedList [values spread remaining-tokens end])
(defn ParsedList? [v] (instance? ParsedList v))

(defn precedence
  {:malli/schema [:=> [:cat [:or [:fn And?] [:fn Or?] [:fn Eq?] [:fn NotEq?] [:fn LtInt?] [:fn LtEqInt?] [:fn LtFloat?] [:fn LtEqFloat?] [:fn GtEqInt?] [:fn GtInt?] [:fn GtEqFloat?] [:fn GtFloat?] [:fn Pipe?] [:fn AddInt?] [:fn AddFloat?] [:fn SubInt?] [:fn SubFloat?] [:fn MultInt?] [:fn MultFloat?] [:fn DivInt?] [:fn DivFloat?] [:fn RemainderInt?] [:fn Concatenate?]]]
                      :int]}
  [operator]
  (cond
    (instance? Or operator)
    1

    (instance? And operator)
    2

    (or (instance? Eq operator) (instance? NotEq operator))
    3

    (or (instance? LtInt operator) (instance? LtEqInt operator) (instance? LtFloat operator) (instance? LtEqFloat operator) (instance? GtEqInt operator) (instance? GtInt operator) (instance? GtEqFloat operator) (instance? GtFloat operator))
    4

    (instance? Concatenate operator)
    5

    (instance? Pipe operator)
    6

    (or (instance? AddInt operator) (instance? AddFloat operator) (instance? SubInt operator) (instance? SubFloat operator))
    7

    (or (instance? MultInt operator) (instance? MultFloat operator) (instance? DivInt operator) (instance? DivFloat operator) (instance? RemainderInt operator))
    8))

(defn- unexpected-error [tokens]
  (if (seq tokens)
    (let [token (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken token position)))
    (p/->Error (->UnexpectedEndOfInput))))

(defn- expect [expected tokens next]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (= (nth (first tokens) 0) expected))
    (let [position (nth (first tokens) 1) tokens (rest tokens)]
      (next position tokens))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))))

(defn- list' [parser discard acc tokens]
  (cond
    (and (seq tokens) (instance? glexer.token.RightSquare (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok (->ParsedList (list/reverse acc)
                            (option/->None)
                            tokens
                            (+' end 1))))

    (and (<= 2 (count tokens)) (instance? glexer.token.Comma (nth (first tokens) 0)) (instance? glexer.token.RightSquare (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)) (not= acc (list)))
    (let [end (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
      (p/->Ok (->ParsedList (list/reverse acc)
                            (option/->None)
                            tokens
                            (+' end 1))))

    (and (<= 2 (count tokens)) (instance? glexer.token.DotDot (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.RightSquare (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [start (:byte-offset (nth (first tokens) 1)) close (nth tokens 1) end (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
      (if (instance? gleam.option.None discard)
        (unexpected-error (list* close tokens))
        (let [discard (:value discard) value (discard (->Span start (+' start 1))) parsed-list (->ParsedList (list/reverse acc) (option/->Some value) tokens (+' end 1))]
          (p/->Ok parsed-list))))

    (and (seq tokens) (instance? glexer.token.DotDot (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/try* (parser tokens))]
        (let [[rest' tokens] _use0]
          (p/with-use [[_use0 tokens] (expect (t/->RightSquare) tokens)]
            (let [{end :byte-offset} _use0]
              (p/->Ok (->ParsedList (list/reverse acc)
                                    (option/->Some rest')
                                    tokens
                                    (+' end 1))))))))

    :else
    (p/with-use [[_use0] (result/try* (parser tokens))]
      (let [[element tokens] _use0
            acc (list* element acc)]
        (cond
          (and (seq tokens) (instance? glexer.token.RightSquare (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
          (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
            (p/->Ok (->ParsedList (list/reverse acc)
                                  (option/->None)
                                  tokens
                                  (+' end 1))))

          (and (<= 2 (count tokens)) (instance? glexer.token.Comma (nth (first tokens) 0)) (instance? glexer.token.RightSquare (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
          (let [end (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
            (p/->Ok (->ParsedList (list/reverse acc)
                                  (option/->None)
                                  tokens
                                  (+' end 1))))

          (and (<= 3 (count tokens)) (instance? glexer.token.Comma (nth (first tokens) 0)) (instance? glexer.token.DotDot (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)) (instance? glexer.token.RightSquare (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
          (let [start (:byte-offset (nth (nth tokens 1) 1)) close (nth tokens 2) end (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3)]
            (if (instance? gleam.option.None discard)
              (unexpected-error (list* close tokens))
              (let [discard (:value discard) value (discard (->Span start (+' start 1))) parsed-list (->ParsedList (list/reverse acc) (option/->Some value) tokens (+' end 1))]
                (p/->Ok parsed-list))))

          (and (<= 2 (count tokens)) (instance? glexer.token.Comma (nth (first tokens) 0)) (instance? glexer.token.DotDot (nth (nth tokens 1) 0)))
          (let [tokens (nthrest tokens 2)]
            (p/with-use [[_use0] (result/try* (parser tokens))]
              (let [[rest' tokens] _use0]
                (p/with-use [[_use0 tokens] (expect (t/->RightSquare) tokens)]
                  (let [{end :byte-offset} _use0]
                    (p/->Ok (->ParsedList (list/reverse acc)
                                          (option/->Some rest')
                                          tokens
                                          (+' end 1))))))))

          (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0)))
          (let [tokens (rest tokens)]
            (list' parser discard acc tokens))

          (seq tokens)
          (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
            (p/->Error (->UnexpectedToken other position)))

          (empty? tokens)
          (p/->Error (->UnexpectedEndOfInput)))))))

(defn- push-function [module attributes function]
  (->Module (:imports module) (:custom-types module) (:type-aliases module) (:constants module) (list* (->Definition (list/reverse attributes) function) (:functions module))))

(defn- handle-operator
  "Simple-Precedence-Parser, handle seeing an operator or end"
  [next operators values]
  (cond
    (and (instance? gleam.option.Some next) (empty? operators))
    (let [operator (:value next)]
      [(option/->None) (list operator) values])

    (and (instance? gleam.option.Some next) (seq operators) (<= 2 (count values)))
    (let [next (:value next) previous (first operators) a (first values) b (nth values 1) rest-values (nthrest values 2) operators (rest operators) subject (>= (precedence previous) (precedence next))]
      (if subject
        (let [span (->Span (:start (:location b)) (:end (:location a)))
              expression (->BinaryOperator span previous b a)
              values (list* expression rest-values)]
          (recur (option/->Some next) operators values))
        [(option/->None) (list* next previous operators) values]))

    (and (instance? gleam.option.None next) (seq operators) (<= 2 (count values)))
    (let [operator (first operators) a (first values) b (nth values 1) operators (rest operators) values (nthrest values 2) values (list* (->BinaryOperator (->Span (:start (:location b)) (:end (:location a))) operator b a) values)]
      (recur (option/->None) operators values))

    (and (instance? gleam.option.None next) (empty? operators) (= (count values) 1))
    (let [expression (first values)]
      [(option/->Some expression) operators values])

    (and (instance? gleam.option.None next) (empty? operators) (empty? values))
    [(option/->None) operators values]

    :else
    (throw (ex-info "parser bug, expression not full reduced" {:gleam/panic true}))))

(defn- binary-operator [token]
  (cond
    (instance? glexer.token.AmperAmper token) (p/->Ok (->And))
    (instance? glexer.token.EqualEqual token) (p/->Ok (->Eq))
    (instance? glexer.token.Greater token) (p/->Ok (->GtInt))
    (instance? glexer.token.GreaterDot token) (p/->Ok (->GtFloat))
    (instance? glexer.token.GreaterEqual token) (p/->Ok (->GtEqInt))
    (instance? glexer.token.GreaterEqualDot token) (p/->Ok (->GtEqFloat))
    (instance? glexer.token.Less token) (p/->Ok (->LtInt))
    (instance? glexer.token.LessDot token) (p/->Ok (->LtFloat))
    (instance? glexer.token.LessEqual token) (p/->Ok (->LtEqInt))
    (instance? glexer.token.LessEqualDot token) (p/->Ok (->LtEqFloat))
    (instance? glexer.token.LessGreater token) (p/->Ok (->Concatenate))
    (instance? glexer.token.Minus token) (p/->Ok (->SubInt))
    (instance? glexer.token.MinusDot token) (p/->Ok (->SubFloat))
    (instance? glexer.token.NotEqual token) (p/->Ok (->NotEq))
    (instance? glexer.token.Percent token) (p/->Ok (->RemainderInt))
    (instance? glexer.token.VBarVBar token) (p/->Ok (->Or))
    (instance? glexer.token.Pipe token) (p/->Ok (->Pipe))
    (instance? glexer.token.Plus token) (p/->Ok (->AddInt))
    (instance? glexer.token.PlusDot token) (p/->Ok (->AddFloat))
    (instance? glexer.token.Slash token) (p/->Ok (->DivInt))
    (instance? glexer.token.SlashDot token) (p/->Ok (->DivFloat))
    (instance? glexer.token.Star token) (p/->Ok (->MultInt))
    (instance? glexer.token.StarDot token) (p/->Ok (->MultFloat))
    :else (p/->Error nil)))

(defn- pop-binary-operator [tokens]
  (if (seq tokens)
    (let [token (nth (first tokens) 0) tokens (rest tokens)]
      (p/with-use [[op] (result/map (binary-operator token))]
        [op tokens]))
    (p/->Error nil)))

(defn- field [tokens parser]
  (if (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Colon (nth (nth tokens 1) 0)))
    (let [name (:value (nth (first tokens) 0)) start (nth (first tokens) 1) end (nth (nth tokens 1) 1) tokens (nthrest tokens 2)]
      (if (or (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0))) (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0))))
        (p/->Ok [(->ShorthandField name
                                   (->Span (:byte-offset start)
                                           (+' (:byte-offset end) 1))) tokens])
        (p/with-use [[_use0] (result/try* (parser tokens))]
          (let [[t tokens] _use0]
            (p/->Ok [(->LabelledField name
                                      (->Span (:byte-offset start)
                                              (+' (:byte-offset end) 1))
                                      t) tokens])))))
    (p/with-use [[_use0] (result/try* (parser tokens))]
      (let [[t tokens] _use0]
        (p/->Ok [(->UnlabelledField t) tokens])))))

(defn- string-offset [start string]
  (+' start (string/byte-size string)))

(defn- span-from-string [start string]
  (->Span start (+' start (string/byte-size string))))

(defn- comma-delimited [items tokens parser final]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.Position (nth (first tokens) 1)) (= (nth (first tokens) 0) final))
    (let [token (nth (first tokens) 0) token-start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok [(list/reverse items) (string-offset token-start (t/to-source token)) tokens]))

    :else
    (p/with-use [[_use0] (result/try* (parser tokens))]
      (let [[element tokens] _use0]
        (cond
          (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0)))
          (let [tokens (rest tokens)]
            (comma-delimited (list* element items) tokens parser final))

          (and (seq tokens) (instance? glexer.Position (nth (first tokens) 1)) (= (nth (first tokens) 0) final))
          (let [token (nth (first tokens) 0) token-start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) offset (string-offset token-start (t/to-source token))]
            (p/->Ok [(list/reverse (list* element items)) offset tokens]))

          (seq tokens)
          (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
            (p/->Error (->UnexpectedToken other position)))

          (empty? tokens)
          (p/->Error (->UnexpectedEndOfInput)))))))

(defn- bit-string-segment-options [size-parser options tokens]
  (p/with-use [[_use0] (result/try* (cond
                                      (and (seq tokens) (instance? glexer.token.Int (nth (first tokens) 0)))
                                      (let [i (:value (nth (first tokens) 0)) position (nth (first tokens) 1) tokens (rest tokens) subject (int/parse i)]
                                        (if (instance? Ok subject)
                                          (let [i (:value subject)]
                                            (p/->Ok [(->SizeOption i) tokens]))
                                          (p/->Error (->UnexpectedToken (t/->Int i)
                                                                        position))))

                                      (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "size") (instance? glexer.token.LeftParen (nth (nth tokens 1) 0)))
                                      (let [tokens (nthrest tokens 2)]
                                        (p/with-use [[_use0] (result/try* (size-parser tokens))]
                                          (let [[value tokens] _use0]
                                            (p/with-use [[_ tokens] (expect (t/->RightParen)
                                                                            tokens)]
                                              (p/->Ok [(->SizeValueOption value) tokens])))))

                                      (and (<= 4 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "unit") (instance? glexer.token.LeftParen (nth (nth tokens 1) 0)) (instance? glexer.token.Int (nth (nth tokens 2) 0)) (instance? glexer.token.RightParen (nth (nth tokens 3) 0)))
                                      (let [position (nth (first tokens) 1) i (:value (nth (nth tokens 2) 0)) tokens (nthrest tokens 4) subject (int/parse i)]
                                        (if (instance? Ok subject)
                                          (let [i (:value subject)]
                                            (p/->Ok [(->UnitOption i) tokens]))
                                          (p/->Error (->UnexpectedToken (t/->Int i)
                                                                        position))))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "bytes"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->BytesOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "binary"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->BytesOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "int"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->IntOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "float"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->FloatOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "bits"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->BitsOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "bit_string"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->BitsOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "utf8"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->Utf8Option) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "utf16"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->Utf16Option) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "utf32"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->Utf32Option) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "utf8_codepoint"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->Utf8CodepointOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "utf16_codepoint"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->Utf16CodepointOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "utf32_codepoint"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->Utf32CodepointOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "signed"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->SignedOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "unsigned"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->UnsignedOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "big"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->BigOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "little"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->LittleOption) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "native"))
                                      (let [tokens (rest tokens)]
                                        (p/->Ok [(->NativeOption) tokens]))

                                      (seq tokens)
                                      (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
                                        (p/->Error (->UnexpectedToken other
                                                                      position)))

                                      (empty? tokens)
                                      (p/->Error (->UnexpectedEndOfInput))))]
    (let [[option tokens] _use0
          options (list* option options)]
      (if (and (seq tokens) (instance? glexer.token.Minus (nth (first tokens) 0)))
        (let [tokens (rest tokens)]
          (bit-string-segment-options size-parser options tokens))
        (p/->Ok [(list/reverse options) tokens])))))

(defn- optional-bit-string-segment-options [size-parser tokens]
  (if (and (seq tokens) (instance? glexer.token.Colon (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (bit-string-segment-options size-parser (list) tokens))
    (p/->Ok [(list) tokens])))

(defn- bit-string-segment [parser size-parser tokens]
  (p/with-use [[_use0] (result/try* (parser tokens))]
    (let [[value tokens] _use0
          result (optional-bit-string-segment-options size-parser tokens)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[options tokens] _use0]
          (p/->Ok [[value options] tokens]))))))

(defn- delimited [acc tokens parser delimeter]
  (p/with-use [[_use0] (result/try* (parser tokens))]
    (let [[t tokens] _use0
          acc (list* t acc)]
      (if (and (seq tokens) (= (nth (first tokens) 0) delimeter))
        (let [tokens (rest tokens)]
          (delimited acc tokens parser delimeter))
        (p/->Ok [(list/reverse acc) tokens])))))

(defn- bit-array-size-precedence [operator]
  (if (or (instance? BitArraySizeAdd operator) (instance? BitArraySizeSubtract operator))
    7
    8))

(defn- handle-bit-array-size-operator [next operators values]
  (cond
    (and (instance? gleam.option.Some next) (empty? operators))
    (let [operator (:value next)]
      [(option/->None) (list operator) values])

    (and (instance? gleam.option.Some next) (seq operators) (<= 2 (count values)))
    (let [next (:value next) previous (first operators) a (first values) b (nth values 1) rest-values (nthrest values 2) operators (rest operators) subject (>= (bit-array-size-precedence previous) (bit-array-size-precedence next))]
      (if subject
        (let [span (->Span (:start (:location b)) (:end (:location a)))
              size (->BitArraySizeBinaryOperator span previous b a)
              values (list* size rest-values)]
          (recur (option/->Some next) operators values))
        [(option/->None) (list* next previous operators) values]))

    (and (instance? gleam.option.None next) (seq operators) (<= 2 (count values)))
    (let [operator (first operators) a (first values) b (nth values 1) operators (rest operators) values (nthrest values 2) values (list* (->BitArraySizeBinaryOperator (->Span (:start (:location b)) (:end (:location a))) operator b a) values)]
      (recur (option/->None) operators values))

    (and (instance? gleam.option.None next) (empty? operators) (= (count values) 1))
    (let [size (first values)]
      [(option/->Some size) operators values])

    (and (instance? gleam.option.None next) (empty? operators) (empty? values))
    [(option/->None) operators values]

    :else
    (throw (ex-info "parser bug, bit array size not fully reduced" {:gleam/panic true}))))

(defn- bit-array-size-operator [token]
  (cond
    (instance? glexer.token.Plus token) (p/->Ok (->BitArraySizeAdd))
    (instance? glexer.token.Minus token) (p/->Ok (->BitArraySizeSubtract))
    (instance? glexer.token.Star token) (p/->Ok (->BitArraySizeMultiply))
    (instance? glexer.token.Slash token) (p/->Ok (->BitArraySizeDivide))
    (instance? glexer.token.Percent token) (p/->Ok (->BitArraySizeRemainder))
    :else (p/->Error nil)))

(defn- pop-bit-array-size-operator [tokens]
  (if (seq tokens)
    (let [token (nth (first tokens) 0) tokens (rest tokens)]
      (p/with-use [[operator] (result/map (bit-array-size-operator token))]
        [operator tokens]))
    (p/->Error nil)))

(declare bit-array-size-unit bit-array-size-loop bit-array-size)

(defn- bit-array-size-unit [tokens]
  (cond
    (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [name (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok [(->BitArraySizeVariable (span-from-string start name) name) tokens]))

    (and (seq tokens) (instance? glexer.token.Int (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [value (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok [(->BitArraySizeInt (span-from-string start value) value) tokens]))

    (and (seq tokens) (instance? glexer.token.LeftBrace (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/with-use [[_use0] (result/try* (bit-array-size tokens))]
        (let [[inner tokens] _use0]
          (p/with-use [[_use0 tokens] (expect (t/->RightBrace) tokens)]
            (let [{end :byte-offset} _use0]
              (p/->Ok [(->BitArraySizeBlock (->Span start (+' end 1)) inner) tokens]))))))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))

    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))))

(defn- bit-array-size-loop [tokens operators values]
  (p/with-use [[_use0] (result/try* (bit-array-size-unit tokens))]
    (let [[size tokens] _use0
          values (list* size values) subject (pop-bit-array-size-operator tokens)]
      (if (instance? Ok subject)
        (let [operator (nth (:value subject) 0) tokens (nth (:value subject) 1) subject (handle-bit-array-size-operator (option/->Some operator) operators values)]
          (if (instance? gleam.option.Some (nth subject 0))
            (let [size (:value (nth subject 0))]
              (p/->Ok [size tokens]))
            (let [operators (nth subject 1) values (nth subject 2)]
              (bit-array-size-loop tokens operators values))))
        (let [subject (nth (handle-bit-array-size-operator (option/->None) operators values) 0)]
          (if (instance? gleam.option.None subject)
            (unexpected-error tokens)
            (let [size (:value subject)]
              (p/->Ok [size tokens]))))))))

(defn- bit-array-size [tokens]
  (bit-array-size-loop tokens (list) (list)))

(declare pattern-constructor-arguments pattern-constructor pattern)

(defn- pattern-constructor-arguments [arguments tokens]
  (cond
    (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok (->PatternConstructorArguments arguments
                                             false
                                             (+' end 1)
                                             tokens)))

    (and (<= 3 (count tokens)) (instance? glexer.token.DotDot (nth (first tokens) 0)) (instance? glexer.token.Comma (nth (nth tokens 1) 0)) (instance? glexer.token.RightParen (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
    (let [end (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3)]
      (p/->Ok (->PatternConstructorArguments arguments true (+' end 1) tokens)))

    (and (<= 2 (count tokens)) (instance? glexer.token.DotDot (nth (first tokens) 0)) (instance? glexer.token.RightParen (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [end (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
      (p/->Ok (->PatternConstructorArguments arguments true (+' end 1) tokens)))

    :else
    (let [tokens tokens]
      (p/with-use [[_use0] (result/try* (field tokens pattern))]
        (let [[pattern tokens] _use0
              arguments (list* pattern arguments)]
          (cond
            (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
            (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
              (p/->Ok (->PatternConstructorArguments arguments
                                                     false
                                                     (+' end 1)
                                                     tokens)))

            (and (<= 3 (count tokens)) (instance? glexer.token.Comma (nth (first tokens) 0)) (instance? glexer.token.DotDot (nth (nth tokens 1) 0)) (instance? glexer.token.RightParen (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
            (let [end (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3)]
              (p/->Ok (->PatternConstructorArguments arguments
                                                     true
                                                     (+' end 1)
                                                     tokens)))

            (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0)))
            (let [tokens (rest tokens)]
              (pattern-constructor-arguments arguments tokens))

            (seq tokens)
            (let [token (nth (first tokens) 0) position (nth (first tokens) 1)]
              (p/->Error (->UnexpectedToken token position)))

            (empty? tokens)
            (p/->Error (->UnexpectedEndOfInput))))))))

(defn- pattern-constructor [module constructor tokens start name-start]
  (if (and (seq tokens) (instance? glexer.token.LeftParen (nth (first tokens) 0)))
    (let [tokens (rest tokens) result (pattern-constructor-arguments (list) tokens)]
      (p/with-use [[_use0] (result/try* result)]
        (let [{patterns :fields spread :spread end :end tokens :remaining-tokens} _use0
              arguments (list/reverse patterns)
              pattern (->PatternVariant (->Span start end)
                                        module
                                        constructor
                                        arguments
                                        spread)]
          (p/->Ok [pattern tokens]))))
    (let [span (->Span start (string-offset name-start constructor))
          pattern (->PatternVariant span module constructor (list) false)]
      (p/->Ok [pattern tokens]))))

(defn- pattern [tokens]
  (p/with-use [[_use0] (result/try* (cond
                                      (and (seq tokens) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [name (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (pattern-constructor (option/->None)
                                                             name
                                                             tokens
                                                             start
                                                             start))

                                      (and (<= 3 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.Dot (nth (nth tokens 1) 0)) (instance? glexer.token.UpperName (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
                                      (let [module (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) name (:value (nth (nth tokens 2) 0)) name-start (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3)]
                                        (pattern-constructor (option/->Some module)
                                                             name
                                                             tokens
                                                             start
                                                             name-start))

                                      (and (<= 5 (count tokens)) (instance? glexer.token.String (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.As (nth (nth tokens 1) 0)) (instance? glexer.token.Name (nth (nth tokens 2) 0)) (instance? glexer.token.LessGreater (nth (nth tokens 3) 0)) (instance? glexer.token.Name (nth (nth tokens 4) 0)) (instance? glexer.Position (nth (nth tokens 4) 1)))
                                      (let [v (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) l (:value (nth (nth tokens 2) 0)) r (:value (nth (nth tokens 4) 0)) name-start (:byte-offset (nth (nth tokens 4) 1)) tokens (nthrest tokens 5) span (->Span start (string-offset name-start r)) pattern (->PatternConcatenate span v (option/->Some (->Named l)) (->Named r))]
                                        (p/->Ok [pattern tokens]))

                                      (and (<= 5 (count tokens)) (instance? glexer.token.String (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.As (nth (nth tokens 1) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 2) 0)) (instance? glexer.token.LessGreater (nth (nth tokens 3) 0)) (instance? glexer.token.Name (nth (nth tokens 4) 0)) (instance? glexer.Position (nth (nth tokens 4) 1)))
                                      (let [v (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) l (:value (nth (nth tokens 2) 0)) r (:value (nth (nth tokens 4) 0)) name-start (:byte-offset (nth (nth tokens 4) 1)) tokens (nthrest tokens 5) span (->Span start (string-offset name-start r)) pattern (->PatternConcatenate span v (option/->Some (->Discarded l)) (->Named r))]
                                        (p/->Ok [pattern tokens]))

                                      (and (<= 5 (count tokens)) (instance? glexer.token.String (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.As (nth (nth tokens 1) 0)) (instance? glexer.token.Name (nth (nth tokens 2) 0)) (instance? glexer.token.LessGreater (nth (nth tokens 3) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 4) 0)) (instance? glexer.Position (nth (nth tokens 4) 1)))
                                      (let [v (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) l (:value (nth (nth tokens 2) 0)) r (:value (nth (nth tokens 4) 0)) name-start (:byte-offset (nth (nth tokens 4) 1)) tokens (nthrest tokens 5) span (->Span start (+' (string-offset name-start r) 1)) pattern (->PatternConcatenate span v (option/->Some (->Named l)) (->Discarded r))]
                                        (p/->Ok [pattern tokens]))

                                      (and (<= 3 (count tokens)) (instance? glexer.token.String (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.LessGreater (nth (nth tokens 1) 0)) (instance? glexer.token.Name (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
                                      (let [v (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) n (:value (nth (nth tokens 2) 0)) name-start (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3) span (->Span start (string-offset name-start n)) pattern (->PatternConcatenate span v (option/->None) (->Named n))]
                                        (p/->Ok [pattern tokens]))

                                      (and (<= 3 (count tokens)) (instance? glexer.token.String (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.LessGreater (nth (nth tokens 1) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
                                      (let [v (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) n (:value (nth (nth tokens 2) 0)) name-start (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3) span (->Span start (+' (string-offset name-start n) 1)) pattern (->PatternConcatenate span v (option/->None) (->Discarded n))]
                                        (p/->Ok [pattern tokens]))

                                      (and (seq tokens) (instance? glexer.token.Int (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [value (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (p/->Ok [(->PatternInt (span-from-string start
                                                                                 value)
                                                               value) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Float (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [value (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (p/->Ok [(->PatternFloat (span-from-string start
                                                                                   value)
                                                                 value) tokens]))

                                      (and (seq tokens) (instance? glexer.token.String (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [value (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (p/->Ok [(->PatternString (->Span start
                                                                          (+' (string-offset start
                                                                                         value) 2))
                                                                  value) tokens]))

                                      (and (seq tokens) (instance? glexer.token.DiscardName (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [name (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (p/->Ok [(->PatternDiscard (->Span start
                                                                           (+' (string-offset start
                                                                                          name) 1))
                                                                   name) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [name (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (p/->Ok [(->PatternVariable (span-from-string start
                                                                                      name)
                                                                    name) tokens]))

                                      (and (seq tokens) (instance? glexer.token.LeftSquare (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) result (list' pattern (option/->Some (fn [_capture] (->PatternDiscard _capture ""))) (list) tokens)]
                                        (p/with-use [[_use0] (result/map result)]
                                          (let [{elements :values rest' :spread tokens :remaining-tokens end :end} _use0]
                                            [(->PatternList (->Span start
                                                                    end)
                                                            elements
                                                            rest') tokens])))

                                      (and (<= 2 (count tokens)) (instance? glexer.token.Hash (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.LeftParen (nth (nth tokens 1) 0)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 2) result (comma-delimited (list) tokens pattern (t/->RightParen))]
                                        (p/with-use [[_use0] (result/try* result)]
                                          (let [[patterns end tokens] _use0]
                                            (p/->Ok [(->PatternTuple (->Span start
                                                                             end)
                                                                     patterns) tokens]))))

                                      (and (seq tokens) (instance? glexer.token.LessLess (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) parser (fn [_capture] (bit-string-segment pattern bit-array-size _capture)) result (comma-delimited (list) tokens parser (t/->GreaterGreater))]
                                        (p/with-use [[_use0] (result/try* result)]
                                          (let [[segments end tokens] _use0]
                                            (p/->Ok [(->PatternBitString (->Span start
                                                                                 end)
                                                                         segments) tokens]))))

                                      (seq tokens)
                                      (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
                                        (p/->Error (->UnexpectedToken other
                                                                      position)))

                                      (empty? tokens)
                                      (p/->Error (->UnexpectedEndOfInput))))]
    (let [[pattern tokens] _use0]
      (if (and (<= 2 (count tokens)) (instance? glexer.token.As (nth (first tokens) 0)) (instance? glexer.token.Name (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
        (let [name (:value (nth (nth tokens 1) 0)) name-start (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2) span (->Span (:start (:location pattern)) (string-offset name-start name)) pattern (->PatternAssignment span pattern name)]
          (p/->Ok [pattern tokens]))
        (p/->Ok [pattern tokens])))))

(declare named-type tuple-type fn-type type-)

(defn- named-type [name module tokens start name-start]
  (p/with-use [[_use0] (result/try* (if (and (seq tokens) (instance? glexer.token.LeftParen (nth (first tokens) 0)))
                                      (let [tokens (rest tokens)]
                                        (comma-delimited (list)
                                                         tokens
                                                         type-
                                                         (t/->RightParen)))
                                      (let [end (+' name-start (string/byte-size name))]
                                        (p/->Ok [(list) end tokens]))))]
    (let [[parameters end tokens] _use0
          t (->NamedType (->Span start end) name module parameters)]
      (p/->Ok [t tokens]))))

(defn- tuple-type [start tokens]
  (let [result (comma-delimited (list) tokens type- (t/->RightParen))]
    (p/with-use [[_use0] (result/try* result)]
      (let [[types end tokens] _use0
            span (->Span start end)]
        (p/->Ok [(->TupleType span types) tokens])))))

(defn- fn-type [start tokens]
  (let [result (comma-delimited (list) tokens type- (t/->RightParen))]
    (p/with-use [[_use0] (result/try* result)]
      (let [[parameters _ tokens] _use0]
        (p/with-use [[_ tokens] (expect (t/->RightArrow) tokens)
                     [_use0] (result/try* (type- tokens))]
          (let [[return tokens] _use0
                span (->Span start (:end (:location return)))]
            (p/->Ok [(->FunctionType span parameters return) tokens])))))))

(defn- type- [tokens]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (<= 2 (count tokens)) (instance? glexer.token.Fn (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.LeftParen (nth (nth tokens 1) 0)))
    (let [i (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 2)]
      (fn-type i tokens))

    (and (<= 2 (count tokens)) (instance? glexer.token.Hash (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.LeftParen (nth (nth tokens 1) 0)))
    (let [i (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 2)]
      (tuple-type i tokens))

    (and (<= 3 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.Dot (nth (nth tokens 1) 0)) (instance? glexer.token.UpperName (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
    (let [module (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) name (:value (nth (nth tokens 2) 0)) end (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3)]
      (named-type name (option/->Some module) tokens start end))

    (and (seq tokens) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [name (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (named-type name (option/->None) tokens start start))

    (and (seq tokens) (instance? glexer.token.DiscardName (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [name (:value (nth (first tokens) 0)) i (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) value (->HoleType (->Span i (+' (string-offset i name) 1)) name)]
      (p/->Ok [value tokens]))

    (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [name (:value (nth (first tokens) 0)) i (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) value (->VariableType (span-from-string i name) name)]
      (p/->Ok [value tokens]))

    (seq tokens)
    (let [token (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken token position)))))

(defn- optional-return-annotation [end tokens]
  (if (and (seq tokens) (instance? glexer.token.RightArrow (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/try* (type- tokens))]
        (let [[return-type tokens] _use0]
          (p/->Ok [(option/->Some return-type) (:end (:location return-type)) tokens]))))
    (p/->Ok [(option/->None) end tokens])))

(defn- optional-type-annotation [tokens]
  (if (and (seq tokens) (instance? glexer.token.Colon (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/map (type- tokens))]
        (let [[annotation tokens] _use0]
          [(option/->Some annotation) tokens])))
    (p/->Ok [(option/->None) tokens])))

(defn- fn-parameter [tokens]
  (p/with-use [[_use0] (result/try* (cond
                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)))
                                      (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
                                        (p/->Ok [(->Named name) tokens]))

                                      (and (seq tokens) (instance? glexer.token.DiscardName (nth (first tokens) 0)))
                                      (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
                                        (p/->Ok [(->Discarded name) tokens]))

                                      (seq tokens)
                                      (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
                                        (p/->Error (->UnexpectedToken other
                                                                      position)))

                                      (empty? tokens)
                                      (p/->Error (->UnexpectedEndOfInput))))]
    (let [[name tokens] _use0]
      (p/with-use [[_use0] (result/try* (optional-type-annotation tokens))]
        (let [[type- tokens] _use0]
          (p/->Ok [(->FnParameter name type-) tokens]))))))

(defn- use-pattern [tokens]
  (p/with-use [[_use0] (result/try* (pattern tokens))]
    (let [[pattern tokens] _use0]
      (p/with-use [[_use0] (result/try* (optional-type-annotation tokens))]
        (let [[annotation tokens] _use0]
          (p/->Ok [(->UsePattern pattern annotation) tokens]))))))

(declare fn-capture call after-expression todo-panic optional-clause-guard case-clause case-clauses case-subjects case- fn- record-update-field record-update expression-unit expression-loop expression assert- use- assignment statement statements)

(defn- fn-capture [label function before after tokens]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (->Span (:start (:location function)) (+' end 1)) capture (->FnCapture span label function before (list/reverse after))]
      (after-expression capture tokens))

    :else
    (p/with-use [[_use0] (result/try* (field tokens expression))]
      (let [[argument tokens] _use0
            after (list* argument after)]
        (cond
          (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0)))
          (let [tokens (rest tokens)]
            (fn-capture label function before after tokens))

          (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
          (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (->Span (:start (:location function)) (+' end 1)) call (->FnCapture span label function before (list/reverse after))]
            (after-expression call tokens))

          (seq tokens)
          (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
            (p/->Error (->UnexpectedToken other position)))

          (empty? tokens)
          (p/->Error (->UnexpectedEndOfInput)))))))

(defn- call [arguments function tokens]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (->Span (:start (:location function)) (+' end 1)) call (->Call span function (list/reverse arguments))]
      (after-expression call tokens))

    (and (<= 5 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Colon (nth (nth tokens 1) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 2) 0)) (= (:value (nth (nth tokens 2) 0)) "") (instance? glexer.token.Comma (nth (nth tokens 3) 0)) (instance? glexer.token.RightParen (nth (nth tokens 4) 0)) (instance? glexer.Position (nth (nth tokens 4) 1)))
    (let [label (:value (nth (first tokens) 0)) end (:byte-offset (nth (nth tokens 4) 1)) tokens (nthrest tokens 5) span (->Span (:start (:location function)) (+' end 1)) capture (->FnCapture span (option/->Some label) function (list/reverse arguments) (list))]
      (after-expression capture tokens))

    (and (<= 4 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Colon (nth (nth tokens 1) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 2) 0)) (= (:value (nth (nth tokens 2) 0)) "") (instance? glexer.token.RightParen (nth (nth tokens 3) 0)) (instance? glexer.Position (nth (nth tokens 3) 1)))
    (let [label (:value (nth (first tokens) 0)) end (:byte-offset (nth (nth tokens 3) 1)) tokens (nthrest tokens 4) span (->Span (:start (:location function)) (+' end 1)) capture (->FnCapture span (option/->Some label) function (list/reverse arguments) (list))]
      (after-expression capture tokens))

    (and (<= 4 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Colon (nth (nth tokens 1) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 2) 0)) (= (:value (nth (nth tokens 2) 0)) "") (instance? glexer.token.Comma (nth (nth tokens 3) 0)))
    (let [label (:value (nth (first tokens) 0)) tokens (nthrest tokens 4)]
      (fn-capture (option/->Some label)
                  function
                  (list/reverse arguments)
                  (list)
                  tokens))

    (and (<= 3 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Colon (nth (nth tokens 1) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 2) 0)) (= (:value (nth (nth tokens 2) 0)) ""))
    (let [label (:value (nth (first tokens) 0)) tokens (nthrest tokens 3)]
      (fn-capture (option/->Some label)
                  function
                  (list/reverse arguments)
                  (list)
                  tokens))

    (and (<= 3 (count tokens)) (instance? glexer.token.DiscardName (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "") (instance? glexer.token.Comma (nth (nth tokens 1) 0)) (instance? glexer.token.RightParen (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
    (let [end (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3) span (->Span (:start (:location function)) (+' end 1)) capture (->FnCapture span (option/->None) function (list/reverse arguments) (list))]
      (after-expression capture tokens))

    (and (<= 2 (count tokens)) (instance? glexer.token.DiscardName (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "") (instance? glexer.token.RightParen (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [end (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2) span (->Span (:start (:location function)) (+' end 1)) capture (->FnCapture span (option/->None) function (list/reverse arguments) (list))]
      (after-expression capture tokens))

    (and (<= 2 (count tokens)) (instance? glexer.token.DiscardName (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) "") (instance? glexer.token.Comma (nth (nth tokens 1) 0)))
    (let [tokens (nthrest tokens 2)]
      (fn-capture (option/->None)
                  function
                  (list/reverse arguments)
                  (list)
                  tokens))

    (and (seq tokens) (instance? glexer.token.DiscardName (nth (first tokens) 0)) (= (:value (nth (first tokens) 0)) ""))
    (let [tokens (rest tokens)]
      (fn-capture (option/->None)
                  function
                  (list/reverse arguments)
                  (list)
                  tokens))

    :else
    (p/with-use [[_use0] (result/try* (field tokens expression))]
      (let [[argument tokens] _use0
            arguments (list* argument arguments)]
        (cond
          (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0)))
          (let [tokens (rest tokens)]
            (call arguments function tokens))

          (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
          (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (->Span (:start (:location function)) (+' end 1)) call (->Call span function (list/reverse arguments))]
            (after-expression call tokens))

          (seq tokens)
          (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
            (p/->Error (->UnexpectedToken other position)))

          (empty? tokens)
          (p/->Error (->UnexpectedEndOfInput)))))))

(defn- after-expression [parsed tokens]
  (cond
    (and (<= 2 (count tokens)) (instance? glexer.token.Dot (nth (first tokens) 0)) (instance? glexer.token.Name (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [label (:value (nth (nth tokens 1) 0)) label-start (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2) span (->Span (:start (:location parsed)) (string-offset label-start label)) expression (->FieldAccess span parsed label)]
      (recur expression tokens))

    (and (<= 2 (count tokens)) (instance? glexer.token.Dot (nth (first tokens) 0)) (instance? glexer.token.UpperName (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [label (:value (nth (nth tokens 1) 0)) label-start (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2) span (->Span (:start (:location parsed)) (string-offset label-start label)) expression (->FieldAccess span parsed label)]
      (recur expression tokens))

    (and (<= 2 (count tokens)) (instance? glexer.token.Dot (nth (first tokens) 0)) (instance? glexer.token.Int (nth (nth tokens 1) 0)))
    (let [token (nth (nth tokens 1) 0) value (:value (nth (nth tokens 1) 0)) position (nth (nth tokens 1) 1) tokens (nthrest tokens 2) subject (int/parse value)]
      (if (instance? Ok subject)
        (let [i (:value subject) end (string-offset (:byte-offset position) value) span (->Span (:start (:location parsed)) end) expression (->TupleIndex span parsed i)]
          (recur expression tokens))
        (p/->Error (->UnexpectedToken token position))))

    (and (seq tokens) (instance? glexer.token.LeftParen (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (call (list) parsed tokens))

    :else
    (p/->Ok [parsed tokens])))

(defn- todo-panic [tokens constructor start keyword-name]
  (if (and (seq tokens) (instance? glexer.token.As (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/try* (expression tokens))]
        (let [[reason tokens] _use0
              span (->Span start (:end (:location reason)))
              expression (constructor span (option/->Some reason))]
          (p/->Ok [(option/->Some expression) tokens]))))
    (let [span (span-from-string start keyword-name)
          expression (constructor span (option/->None))]
      (p/->Ok [(option/->Some expression) tokens]))))

(defn- optional-clause-guard [tokens]
  (if (and (seq tokens) (instance? glexer.token.If (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/try* (expression tokens))]
        (let [[expression tokens] _use0]
          (p/->Ok [(option/->Some expression) tokens]))))
    (p/->Ok [(option/->None) tokens])))

(defn- case-clause [tokens]
  (let [multipatterns (fn [_capture]
                        (delimited (list) _capture pattern (t/->Comma)))
        result (delimited (list) tokens multipatterns (t/->VBar))]
    (p/with-use [[_use0] (result/try* result)]
      (let [[patterns tokens] _use0]
        (p/with-use [[_use0] (result/try* (optional-clause-guard tokens))]
          (let [[guard tokens] _use0]
            (p/with-use [[_ tokens] (expect (t/->RightArrow) tokens)
                         [_use0] (result/map (expression tokens))]
              (let [[expression tokens] _use0]
                [(->Clause patterns guard expression) tokens]))))))))

(defn- case-clauses [clauses tokens]
  (p/with-use [[_use0] (result/try* (case-clause tokens))]
    (let [[clause tokens] _use0
          clauses (list* clause clauses)]
      (if (and (seq tokens) (instance? glexer.token.RightBrace (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
        (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
          (p/->Ok [(list/reverse clauses) tokens (+' end 1)]))
        (case-clauses clauses tokens)))))

(defn- case-subjects [subjects tokens]
  (p/with-use [[_use0] (result/try* (expression tokens))]
    (let [[subject tokens] _use0
          subjects (list* subject subjects)]
      (if (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0)))
        (let [tokens (rest tokens)]
          (case-subjects subjects tokens))
        (p/->Ok [(list/reverse subjects) tokens])))))

(defn- case- [tokens start]
  (p/with-use [[_use0] (result/try* (case-subjects (list) tokens))]
    (let [[subjects tokens] _use0]
      (p/with-use [[_ tokens] (expect (t/->LeftBrace) tokens)
                   [_use0] (result/try* (case-clauses (list) tokens))]
        (let [[clauses tokens end] _use0]
          (p/->Ok [(option/->Some (->Case (->Span start end) subjects clauses)) tokens]))))))

(defn- fn- [tokens start]
  (p/with-use [[_ tokens] (expect (t/->LeftParen) tokens)]
    (let [result (comma-delimited (list) tokens fn-parameter (t/->RightParen))]
      (p/with-use [[_use0] (result/try* result)]
        (let [[parameters _ tokens] _use0]
          (p/with-use [[_use0] (result/try* (optional-return-annotation 0
                                                                        tokens))]
            (let [[return _ tokens] _use0]
              (p/with-use [[_ tokens] (expect (t/->LeftBrace) tokens)
                           [_use0] (result/try* (statements (list) tokens))]
                (let [[body end tokens] _use0]
                  (p/->Ok [(option/->Some (->Fn (->Span start end)
                                                parameters
                                                return
                                                body)) tokens]))))))))))

(defn- record-update-field [tokens]
  (cond
    (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Colon (nth (nth tokens 1) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (nthrest tokens 2)]
      (if (or (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0))) (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0))))
        (p/->Ok [(->RecordUpdateField name (option/->None)) tokens])
        (p/with-use [[_use0] (result/try* (expression tokens))]
          (let [[expression tokens] _use0]
            (p/->Ok [(->RecordUpdateField name (option/->Some expression)) tokens])))))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))

    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))))

(defn- record-update [module constructor tokens start]
  (p/with-use [[_use0] (result/try* (expression tokens))]
    (let [[record tokens] _use0]
      (cond
        (and (seq tokens) (instance? glexer.token.RightParen (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
        (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (->Span start (+' end 1)) expression (->RecordUpdate span module constructor record (list))]
          (p/->Ok [(option/->Some expression) tokens]))

        (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0)))
        (let [tokens (rest tokens) result (comma-delimited (list) tokens record-update-field (t/->RightParen))]
          (p/with-use [[_use0] (result/try* result)]
            (let [[fields end tokens] _use0
                  span (->Span start end)
                  expression (->RecordUpdate span
                                             module
                                             constructor
                                             record
                                             fields)]
              (p/->Ok [(option/->Some expression) tokens]))))

        :else
        (p/->Ok [(option/->None) tokens])))))

(defn- expression-unit [tokens context]
  (p/with-use [[_use0] (result/try* (cond
                                      (and (<= 5 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.Dot (nth (nth tokens 1) 0)) (instance? glexer.token.UpperName (nth (nth tokens 2) 0)) (instance? glexer.token.LeftParen (nth (nth tokens 3) 0)) (instance? glexer.token.DotDot (nth (nth tokens 4) 0)))
                                      (let [module (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) constructor (:value (nth (nth tokens 2) 0)) tokens (nthrest tokens 5)]
                                        (record-update (option/->Some module)
                                                       constructor
                                                       tokens
                                                       start))

                                      (and (<= 3 (count tokens)) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.LeftParen (nth (nth tokens 1) 0)) (instance? glexer.token.DotDot (nth (nth tokens 2) 0)))
                                      (let [constructor (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 3)]
                                        (record-update (option/->None)
                                                       constructor
                                                       tokens
                                                       start))

                                      (and (seq tokens) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [name (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (p/->Ok [(option/->Some (->Variable (span-from-string start
                                                                                              name)
                                                                            name)) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Int (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [value (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (span-from-string start value)]
                                        (p/->Ok [(option/->Some (->Int span
                                                                       value)) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Float (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [value (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (span-from-string start value)]
                                        (p/->Ok [(option/->Some (->Float span
                                                                         value)) tokens]))

                                      (and (seq tokens) (instance? glexer.token.String (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [value (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (->Span start (+' (string-offset start value) 2))]
                                        (p/->Ok [(option/->Some (->String span
                                                                          value)) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [name (:value (nth (first tokens) 0)) start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) span (span-from-string start name)]
                                        (p/->Ok [(option/->Some (->Variable span
                                                                            name)) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Fn (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (fn- tokens start))

                                      (and (seq tokens) (instance? glexer.token.Case (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (case- tokens start))

                                      (and (seq tokens) (instance? glexer.token.Panic (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (todo-panic tokens
                                                    ->Panic
                                                    start
                                                    "panic"))

                                      (and (seq tokens) (instance? glexer.token.Todo (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (todo-panic tokens
                                                    ->Todo
                                                    start
                                                    "todo"))

                                      (and (seq tokens) (instance? glexer.token.LeftSquare (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) result (list' expression (option/->None) (list) tokens)]
                                        (p/with-use [[_use0] (result/map result)]
                                          (let [{elements :values rest' :spread tokens :remaining-tokens end :end} _use0]
                                            [(option/->Some (->List (->Span start
                                                                            end)
                                                                    elements
                                                                    rest')) tokens])))

                                      (and (<= 2 (count tokens)) (instance? glexer.token.Hash (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.LeftParen (nth (nth tokens 1) 0)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 2) result (comma-delimited (list) tokens expression (t/->RightParen))]
                                        (p/with-use [[_use0] (result/map result)]
                                          (let [[expressions end tokens] _use0]
                                            [(option/->Some (->Tuple (->Span start
                                                                             end)
                                                                     expressions)) tokens])))

                                      (and (seq tokens) (instance? glexer.token.Bang (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) unit (expression-unit tokens (->RegularExpressionUnit))]
                                        (p/with-use [[_use0] (result/try* unit)]
                                          (let [[maybe-expression tokens] _use0]
                                            (if (instance? gleam.option.Some maybe-expression)
                                              (let [expression (:value maybe-expression) span (->Span start (:end (:location expression)))]
                                                (p/->Ok [(option/->Some (->NegateBool span
                                                                                      expression)) tokens]))
                                              (unexpected-error tokens)))))

                                      (and (seq tokens) (instance? glexer.token.Minus (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) unit (expression-unit tokens (->RegularExpressionUnit))]
                                        (p/with-use [[_use0] (result/try* unit)]
                                          (let [[maybe-expression tokens] _use0]
                                            (if (instance? gleam.option.Some maybe-expression)
                                              (let [expression (:value maybe-expression) span (->Span start (:end (:location expression)))]
                                                (p/->Ok [(option/->Some (->NegateInt span
                                                                                     expression)) tokens]))
                                              (unexpected-error tokens)))))

                                      (and (seq tokens) (instance? glexer.token.LeftBrace (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
                                        (p/with-use [[_use0] (result/map (statements (list)
                                                                                     tokens))]
                                          (let [[statements end tokens] _use0]
                                            [(option/->Some (->Block (->Span start
                                                                             end)
                                                                     statements)) tokens])))

                                      (and (seq tokens) (instance? glexer.token.LessLess (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) parser (fn [_capture] (bit-string-segment expression expression _capture)) result (comma-delimited (list) tokens parser (t/->GreaterGreater))]
                                        (p/with-use [[_use0] (result/map result)]
                                          (let [[segments end tokens] _use0]
                                            [(option/->Some (->BitString (->Span start
                                                                                 end)
                                                                         segments)) tokens])))

                                      (and (seq tokens) (instance? glexer.token.Echo (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
                                      (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) result (if (instance? ExpressionUnitAfterPipe context) (let [span (span-from-string start "echo")] (p/->Ok [span (option/->None) tokens])) (result/map (expression tokens) (fn [expression-and-tokens] (let [[expression tokens] expression-and-tokens span (->Span start (:end (:location expression)))] [span (option/->Some expression) tokens]))))]
                                        (p/with-use [[_use0] (result/try* result)]
                                          (let [[span echo-expression tokens] _use0]
                                            (if (and (seq tokens) (instance? glexer.token.As (nth (first tokens) 0)))
                                              (let [tokens (rest tokens)]
                                                (p/with-use [[_use0] (result/map (expression tokens))]
                                                  (let [[message tokens] _use0
                                                        span (->Span (:start span)
                                                                     (:end (:location message)))]
                                                    [(option/->Some (->Echo span
                                                                            echo-expression
                                                                            (option/->Some message))) tokens])))
                                              (p/->Ok [(option/->Some (->Echo span
                                                                              echo-expression
                                                                              (option/->None))) tokens])))))

                                      :else
                                      (p/->Ok [(option/->None) tokens])))]
    (let [[parsed tokens] _use0]
      (if (instance? gleam.option.Some parsed)
        (let [expression (:value parsed) subject (after-expression expression tokens)]
          (if (instance? Ok subject)
            (let [expression (nth (:value subject) 0) tokens (nth (:value subject) 1)]
              (p/->Ok [(option/->Some expression) tokens]))
            (let [error (:value subject)]
              (p/->Error error))))
        (p/->Ok [(option/->None) tokens])))))

(defn- expression-loop [tokens operators values context]
  (p/with-use [[_use0] (result/try* (expression-unit tokens context))]
    (let [[expression tokens] _use0]
      (if (instance? gleam.option.None expression)
        (unexpected-error tokens)
        (let [e (:value expression) values (list* e values) subject (pop-binary-operator tokens)]
          (if (instance? Ok subject)
            (let [operator (nth (:value subject) 0) tokens (nth (:value subject) 1) subject (handle-operator (option/->Some operator) operators values)]
              (if (instance? gleam.option.Some (nth subject 0))
                (let [expression (:value (nth subject 0))]
                  (p/->Ok [expression tokens]))
                (let [operators (nth subject 1) values (nth subject 2)]
                  (expression-loop tokens
                                   operators
                                   values
                                   (if (instance? Pipe operator)
                                     (->ExpressionUnitAfterPipe)
                                     (->RegularExpressionUnit))))))
            (let [subject (nth (handle-operator (option/->None) operators values) 0)]
              (if (instance? gleam.option.None subject)
                (unexpected-error tokens)
                (let [expression (:value subject)]
                  (p/->Ok [expression tokens]))))))))))

(defn- expression [tokens]
  (expression-loop tokens (list) (list) (->RegularExpressionUnit)))

(defn- assert- [tokens start]
  (p/with-use [[_use0] (result/try* (expression tokens))]
    (let [[subject tokens] _use0]
      (if (and (seq tokens) (instance? glexer.token.As (nth (first tokens) 0)))
        (let [tokens (rest tokens) subject (expression tokens)]
          (if (instance? gleam.prelude.Error subject)
            (let [error (:value subject)]
              (p/->Error error))
            (let [message (nth (:value subject) 0) tokens (nth (:value subject) 1) statement (->Assert (->Span start (:end (:location message))) subject (option/->Some message))]
              (p/->Ok [statement tokens]))))
        (let [statement (->Assert (->Span start (:end (:location subject)))
                                  subject
                                  (option/->None))]
          (p/->Ok [statement tokens]))))))

(defn- use- [tokens start]
  (p/with-use [[_use0] (result/try* (if (and (seq tokens) (instance? glexer.token.LeftArrow (nth (first tokens) 0)))
                                      (p/->Ok [(list) tokens])
                                      (delimited (list)
                                                 tokens
                                                 use-pattern
                                                 (t/->Comma))))]
    (let [[patterns tokens] _use0]
      (p/with-use [[_ tokens] (expect (t/->LeftArrow) tokens)
                   [_use0] (result/try* (expression tokens))]
        (let [[function tokens] _use0]
          (p/->Ok [(->Use (->Span start (:end (:location function)))
                          patterns
                          function) tokens]))))))

(defn- assignment [kind tokens start]
  (p/with-use [[_use0] (result/try* (pattern tokens))]
    (let [[pattern tokens] _use0]
      (p/with-use [[_use0] (result/try* (optional-type-annotation tokens))]
        (let [[annotation tokens] _use0]
          (p/with-use [[_ tokens] (expect (t/->Equal) tokens)
                       [_use0] (result/try* (expression tokens))]
            (let [[value tokens] _use0]
              (p/with-use [[_use0] (result/try* (if (and (instance? LetAssert kind) (instance? gleam.option.None (:message kind)) (seq tokens) (instance? glexer.token.As (nth (first tokens) 0)))
                                                  (let [tokens (rest tokens)]
                                                    (p/with-use [[_use0] (result/map (expression tokens))]
                                                      (let [[message tokens] _use0]
                                                        [(->LetAssert (option/->Some message)) tokens (:end (:location message))])))
                                                  (p/->Ok [kind tokens (:end (:location value))])))]
                (let [[kind tokens end] _use0
                      statement (->Assignment (->Span start end)
                                              kind
                                              pattern
                                              annotation
                                              value)]
                  (p/->Ok [statement tokens]))))))))))

(defn- statement [tokens]
  (cond
    (and (<= 2 (count tokens)) (instance? glexer.token.Let (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.Assert (nth (nth tokens 1) 0)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 2)]
      (assignment (->LetAssert (option/->None)) tokens start))

    (and (seq tokens) (instance? glexer.token.Let (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (assignment (->Let) tokens start))

    (and (seq tokens) (instance? glexer.token.Use (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (use- tokens start))

    (and (seq tokens) (instance? glexer.token.Assert (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (assert- tokens start))

    :else
    (let [tokens tokens]
      (p/with-use [[_use0] (result/try* (expression tokens))]
        (let [[expression tokens] _use0]
          (p/->Ok [(->Expression expression) tokens]))))))

(defn- statements [acc tokens]
  (if (and (seq tokens) (instance? glexer.token.RightBrace (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok [(list/reverse acc) (+' end 1) tokens]))
    (p/with-use [[_use0] (result/try* (statement tokens))]
      (let [[statement tokens] _use0]
        (statements (list* statement acc) tokens)))))

(defn- function-parameter [tokens]
  (p/with-use [[_use0] (result/try* (cond
                                      (empty? tokens)
                                      (p/->Error (->UnexpectedEndOfInput))

                                      (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 1) 0)))
                                      (let [label (:value (nth (first tokens) 0)) name (:value (nth (nth tokens 1) 0)) tokens (nthrest tokens 2)]
                                        (p/->Ok [(option/->Some label) (->Discarded name) tokens]))

                                      (and (seq tokens) (instance? glexer.token.DiscardName (nth (first tokens) 0)))
                                      (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
                                        (p/->Ok [(option/->None) (->Discarded name) tokens]))

                                      (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Name (nth (nth tokens 1) 0)))
                                      (let [label (:value (nth (first tokens) 0)) name (:value (nth (nth tokens 1) 0)) tokens (nthrest tokens 2)]
                                        (p/->Ok [(option/->Some label) (->Named name) tokens]))

                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)))
                                      (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
                                        (p/->Ok [(option/->None) (->Named name) tokens]))

                                      (seq tokens)
                                      (let [token (nth (first tokens) 0) position (nth (first tokens) 1)]
                                        (p/->Error (->UnexpectedToken token
                                                                      position)))))]
    (let [[label parameter tokens] _use0]
      (p/with-use [[_use0] (result/try* (optional-type-annotation tokens))]
        (let [[type- tokens] _use0]
          (p/->Ok [(->FunctionParameter label parameter type-) tokens]))))))

(defn- function-definition [module attributes publicity name start tokens]
  (p/with-use [[_ tokens] (expect (t/->LeftParen) tokens)]
    (let [result (comma-delimited (list)
                                  tokens
                                  function-parameter
                                  (t/->RightParen))]
      (p/with-use [[_use0] (result/try* result)]
        (let [[parameters end tokens] _use0
              result (optional-return-annotation end tokens)]
          (p/with-use [[_use0] (result/try* result)]
            (let [[return-type end tokens] _use0]
              (p/with-use [[_use0] (result/try* (if (and (seq tokens) (instance? glexer.token.LeftBrace (nth (first tokens) 0)))
                                                  (let [tokens (rest tokens)]
                                                    (statements (list) tokens))
                                                  (p/->Ok [(list) end tokens])))]
                (let [[body end tokens] _use0
                      location (->Span start end)
                      function (->Function location
                                           name
                                           publicity
                                           parameters
                                           return-type
                                           body)
                      module (push-function module attributes function)]
                  (p/->Ok [module tokens]))))))))))

(defn- push-constant [module attributes constant]
  (->Module (:imports module) (:custom-types module) (:type-aliases module) (list* (->Definition (list/reverse attributes) constant) (:constants module)) (:functions module)))

(defn- expect-name [tokens next]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
      (next name tokens))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))))

(defn- const-definition [module attributes publicity tokens start]
  (p/with-use [[name tokens] (expect-name tokens)
               [_use0] (result/try* (optional-type-annotation tokens))]
    (let [[annotation tokens] _use0]
      (p/with-use [[_ tokens] (expect (t/->Equal) tokens)
                   [_use0] (result/try* (expression tokens))]
        (let [[expression tokens] _use0
              constant (->Constant (->Span start
                                           (:end (:location expression)))
                                   name
                                   publicity
                                   annotation
                                   expression)
              module (push-constant module attributes constant)]
          (p/->Ok [module tokens]))))))

(defn- push-custom-type [module attributes custom-type]
  (let [custom-type (->CustomType (:location custom-type) (:name custom-type) (:publicity custom-type) (:opaque- custom-type) (:parameters custom-type) (list/reverse (:variants custom-type)))]
    (->Module (:imports module) (list* (->Definition (list/reverse attributes) custom-type) (:custom-types module)) (:type-aliases module) (:constants module) (:functions module))))

(defn- push-variant [custom-type variant]
  (->CustomType (:location custom-type) (:name custom-type) (:publicity custom-type) (:opaque- custom-type) (:parameters custom-type) (list* variant (:variants custom-type))))

(defn- variant-field [tokens]
  (if (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Colon (nth (nth tokens 1) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (nthrest tokens 2)]
      (p/with-use [[_use0] (result/try* (type- tokens))]
        (let [[type- tokens] _use0]
          (p/->Ok [(->LabelledVariantField type- name) tokens]))))
    (let [tokens tokens]
      (p/with-use [[_use0] (result/try* (type- tokens))]
        (let [[type- tokens] _use0]
          (p/->Ok [(->UnlabelledVariantField type-) tokens]))))))

(defn- expect-upper-name [tokens next]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [name (:value (nth (first tokens) 0)) end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (next name end tokens))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))))

(defn- attribute [tokens]
  (p/with-use [[_use0] (result/try* (cond
                                      (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)))
                                      (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
                                        (p/->Ok [name tokens]))

                                      (seq tokens)
                                      (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
                                        (p/->Error (->UnexpectedToken other
                                                                      position)))

                                      (empty? tokens)
                                      (p/->Error (->UnexpectedEndOfInput))))]
    (let [[name tokens] _use0]
      (if (and (seq tokens) (instance? glexer.token.LeftParen (nth (first tokens) 0)))
        (let [tokens (rest tokens) result (comma-delimited (list) tokens expression (t/->RightParen))]
          (p/with-use [[_use0] (result/try* result)]
            (let [[parameters _ tokens] _use0]
              (p/->Ok [(->Attribute name parameters) tokens]))))
        (p/->Ok [(->Attribute name (list)) tokens])))))

(defn- attributes [accumulated-attributes tokens]
  (if (and (seq tokens) (instance? glexer.token.At (nth (first tokens) 0)))
    (let [tokens (rest tokens) subject (attribute tokens)]
      (if (instance? gleam.prelude.Error subject)
        (let [error (:value subject)]
          (p/->Error error))
        (let [attribute (nth (:value subject) 0) tokens (nth (:value subject) 1)]
          (recur (list* attribute accumulated-attributes) tokens))))
    (p/->Ok [(list/reverse accumulated-attributes) tokens])))

(defn- until [limit acc tokens callback]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.Position (nth (first tokens) 1)) (= (nth (first tokens) 0) limit))
    (let [token (nth (first tokens) 0) i (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok [acc (string-offset i (t/to-source token)) tokens]))

    (seq tokens)
    (let [subject (callback acc tokens)]
      (if (instance? Ok subject)
        (let [acc (nth (:value subject) 0) tokens (nth (:value subject) 1)]
          (recur limit acc tokens callback))
        (let [error (:value subject)]
          (p/->Error error))))))

(defn- variants [ct tokens]
  (p/with-use [[ct tokens] (until (t/->RightBrace) ct tokens)
               [_use0] (result/try* (attributes (list) tokens))]
    (let [[attributes tokens] _use0]
      (p/with-use [[name _ tokens] (expect-upper-name tokens)
                   [_use0] (result/try* (cond
                                          (and (<= 2 (count tokens)) (instance? glexer.token.LeftParen (nth (first tokens) 0)) (instance? glexer.token.RightParen (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
                                          (let [i (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
                                            (p/->Ok [(list) i tokens]))

                                          (and (seq tokens) (instance? glexer.token.LeftParen (nth (first tokens) 0)))
                                          (let [tokens (rest tokens)]
                                            (comma-delimited (list)
                                                             tokens
                                                             variant-field
                                                             (t/->RightParen)))

                                          :else
                                          (p/->Ok [(list) 0 tokens])))]
        (let [[fields _ tokens] _use0
              ct (push-variant ct (->Variant name fields attributes))]
          (p/->Ok [ct tokens]))))))

(defn- custom-type [module attributes name parameters publicity opaque- tokens start]
  (let [ct (->CustomType (->Span 0 0)
                         name
                         publicity
                         opaque-
                         parameters
                         (list))]
    (p/with-use [[_use0] (result/try* (variants ct tokens))]
      (let [[ct end tokens] _use0
            ct (->CustomType (->Span start end) (:name ct) (:publicity ct) (:opaque- ct) (:parameters ct) (:variants ct))
            module (push-custom-type module attributes ct)]
        (p/->Ok [module tokens])))))

(defn- push-type-alias [module attributes type-alias]
  (->Module (:imports module) (:custom-types module) (list* (->Definition (list/reverse attributes) type-alias) (:type-aliases module)) (:constants module) (:functions module)))

(defn- type-alias [module attributes name parameters publicity start tokens]
  (p/with-use [[_use0] (result/try* (type- tokens))]
    (let [[type- tokens] _use0
          span (->Span start (:end (:location type-)))
          alias (->TypeAlias span name publicity parameters type-)
          module (push-type-alias module attributes alias)]
      (p/->Ok [module tokens]))))

(defn- name [tokens]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
      (p/->Ok [name tokens]))

    (seq tokens)
    (let [token (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken token position)))))

(defn- type-definition [module attributes publicity opaque- tokens start]
  (p/with-use [[name-value name-start tokens] (expect-upper-name tokens)
               [_use0] (result/try* (if (and (seq tokens) (instance? glexer.token.LeftParen (nth (first tokens) 0)))
                                      (let [tokens (rest tokens)]
                                        (comma-delimited (list)
                                                         tokens
                                                         name
                                                         (t/->RightParen)))
                                      (p/->Ok [(list) (string-offset name-start
                                                              name-value) tokens])))]
    (let [[parameters end tokens] _use0]
      (cond
        (and (seq tokens) (instance? glexer.token.Equal (nth (first tokens) 0)))
        (let [tokens (rest tokens)]
          (type-alias module
                      attributes
                      name-value
                      parameters
                      publicity
                      start
                      tokens))

        (and (seq tokens) (instance? glexer.token.LeftBrace (nth (first tokens) 0)))
        (let [tokens (rest tokens)]
          (-> module
              (custom-type attributes
                           name-value
                           parameters
                           publicity
                           opaque-
                           tokens
                           start)))

        :else
        (let [span (->Span start end)
              ct (->CustomType span
                               name-value
                               publicity
                               opaque-
                               parameters
                               (list))
              module (push-custom-type module attributes ct)]
          (p/->Ok [module tokens]))))))

(defn- optional-module-alias [tokens end]
  (cond
    (and (<= 2 (count tokens)) (instance? glexer.token.As (nth (first tokens) 0)) (instance? glexer.token.Name (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [alias (:value (nth (nth tokens 1) 0)) alias-start (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
      [(option/->Some (->Named alias)) (string-offset alias-start alias) tokens])

    (and (<= 2 (count tokens)) (instance? glexer.token.As (nth (first tokens) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [alias (:value (nth (nth tokens 1) 0)) alias-start (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
      [(option/->Some (->Discarded alias)) (+' (string-offset alias-start alias) 1) tokens])

    :else
    [(option/->None) end tokens]))

(defn- unqualified-imports [types values tokens]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.RightBrace (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok (->UnqualifiedImports (list/reverse types)
                                    (list/reverse values)
                                    (+' end 1)
                                    tokens)))

    (and (<= 4 (count tokens)) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.token.As (nth (nth tokens 1) 0)) (instance? glexer.token.UpperName (nth (nth tokens 2) 0)) (instance? glexer.token.Comma (nth (nth tokens 3) 0)))
    (let [name (:value (nth (first tokens) 0)) alias (:value (nth (nth tokens 2) 0)) tokens (nthrest tokens 4) import- (->UnqualifiedImport name (option/->Some alias))]
      (recur types (list* import- values) tokens))

    (and (<= 4 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.As (nth (nth tokens 1) 0)) (instance? glexer.token.Name (nth (nth tokens 2) 0)) (instance? glexer.token.Comma (nth (nth tokens 3) 0)))
    (let [name (:value (nth (first tokens) 0)) alias (:value (nth (nth tokens 2) 0)) tokens (nthrest tokens 4) import- (->UnqualifiedImport name (option/->Some alias))]
      (recur types (list* import- values) tokens))

    (and (<= 4 (count tokens)) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.token.As (nth (nth tokens 1) 0)) (instance? glexer.token.UpperName (nth (nth tokens 2) 0)) (instance? glexer.token.RightBrace (nth (nth tokens 3) 0)) (instance? glexer.Position (nth (nth tokens 3) 1)))
    (let [name (:value (nth (first tokens) 0)) alias (:value (nth (nth tokens 2) 0)) end (:byte-offset (nth (nth tokens 3) 1)) tokens (nthrest tokens 4) import- (->UnqualifiedImport name (option/->Some alias))]
      (p/->Ok (->UnqualifiedImports (list/reverse types)
                                    (list/reverse (list* import- values))
                                    (+' end 1)
                                    tokens)))

    (and (<= 4 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.As (nth (nth tokens 1) 0)) (instance? glexer.token.Name (nth (nth tokens 2) 0)) (instance? glexer.token.RightBrace (nth (nth tokens 3) 0)) (instance? glexer.Position (nth (nth tokens 3) 1)))
    (let [name (:value (nth (first tokens) 0)) alias (:value (nth (nth tokens 2) 0)) end (:byte-offset (nth (nth tokens 3) 1)) tokens (nthrest tokens 4) import- (->UnqualifiedImport name (option/->Some alias))]
      (p/->Ok (->UnqualifiedImports (list/reverse types)
                                    (list/reverse (list* import- values))
                                    (+' end 1)
                                    tokens)))

    (and (<= 2 (count tokens)) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.token.Comma (nth (nth tokens 1) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (nthrest tokens 2) import- (->UnqualifiedImport name (option/->None))]
      (recur types (list* import- values) tokens))

    (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Comma (nth (nth tokens 1) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (nthrest tokens 2) import- (->UnqualifiedImport name (option/->None))]
      (recur types (list* import- values) tokens))

    (and (<= 2 (count tokens)) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.token.RightBrace (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [name (:value (nth (first tokens) 0)) end (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2) import- (->UnqualifiedImport name (option/->None))]
      (p/->Ok (->UnqualifiedImports (list/reverse types)
                                    (list/reverse (list* import- values))
                                    (+' end 1)
                                    tokens)))

    (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.RightBrace (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [name (:value (nth (first tokens) 0)) end (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2) import- (->UnqualifiedImport name (option/->None))]
      (p/->Ok (->UnqualifiedImports (list/reverse types)
                                    (list/reverse (list* import- values))
                                    (+' end 1)
                                    tokens)))

    (and (<= 5 (count tokens)) (instance? glexer.token.Type (nth (first tokens) 0)) (instance? glexer.token.UpperName (nth (nth tokens 1) 0)) (instance? glexer.token.As (nth (nth tokens 2) 0)) (instance? glexer.token.UpperName (nth (nth tokens 3) 0)) (instance? glexer.token.Comma (nth (nth tokens 4) 0)))
    (let [name (:value (nth (nth tokens 1) 0)) alias (:value (nth (nth tokens 3) 0)) tokens (nthrest tokens 5) import- (->UnqualifiedImport name (option/->Some alias))]
      (recur (list* import- types) values tokens))

    (and (<= 5 (count tokens)) (instance? glexer.token.Type (nth (first tokens) 0)) (instance? glexer.token.UpperName (nth (nth tokens 1) 0)) (instance? glexer.token.As (nth (nth tokens 2) 0)) (instance? glexer.token.UpperName (nth (nth tokens 3) 0)) (instance? glexer.token.RightBrace (nth (nth tokens 4) 0)) (instance? glexer.Position (nth (nth tokens 4) 1)))
    (let [name (:value (nth (nth tokens 1) 0)) alias (:value (nth (nth tokens 3) 0)) end (:byte-offset (nth (nth tokens 4) 1)) tokens (nthrest tokens 5) import- (->UnqualifiedImport name (option/->Some alias))]
      (p/->Ok (->UnqualifiedImports (list/reverse (list* import- types))
                                    (list/reverse values)
                                    (+' end 1)
                                    tokens)))

    (and (<= 3 (count tokens)) (instance? glexer.token.Type (nth (first tokens) 0)) (instance? glexer.token.UpperName (nth (nth tokens 1) 0)) (instance? glexer.token.Comma (nth (nth tokens 2) 0)))
    (let [name (:value (nth (nth tokens 1) 0)) tokens (nthrest tokens 3) import- (->UnqualifiedImport name (option/->None))]
      (recur (list* import- types) values tokens))

    (and (<= 3 (count tokens)) (instance? glexer.token.Type (nth (first tokens) 0)) (instance? glexer.token.UpperName (nth (nth tokens 1) 0)) (instance? glexer.token.RightBrace (nth (nth tokens 2) 0)) (instance? glexer.Position (nth (nth tokens 2) 1)))
    (let [name (:value (nth (nth tokens 1) 0)) end (:byte-offset (nth (nth tokens 2) 1)) tokens (nthrest tokens 3) import- (->UnqualifiedImport name (option/->None))]
      (p/->Ok (->UnqualifiedImports (list/reverse (list* import- types))
                                    (list/reverse values)
                                    (+' end 1)
                                    tokens)))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))))

(defn- optional-unqualified-imports [tokens end]
  (if (and (<= 2 (count tokens)) (instance? glexer.token.Dot (nth (first tokens) 0)) (instance? glexer.token.LeftBrace (nth (nth tokens 1) 0)))
    (let [tokens (nthrest tokens 2)]
      (unqualified-imports (list) (list) tokens))
    (p/->Ok (->UnqualifiedImports (list) (list) end tokens))))

(defn- module-name [name end tokens]
  (cond
    (and (<= 2 (count tokens)) (instance? glexer.token.Slash (nth (first tokens) 0)) (instance? glexer.token.Name (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)) (not= name ""))
    (let [i (:byte-offset (nth (nth tokens 1) 1)) s (:value (nth (nth tokens 1) 0)) tokens (nthrest tokens 2) end (+' i (string/byte-size s))]
      (recur (str (str name "/") s) end tokens))

    (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (= name ""))
    (let [i (:byte-offset (nth (first tokens) 1)) s (:value (nth (first tokens) 0)) tokens (rest tokens) end (+' i (string/byte-size s))]
      (recur s end tokens))

    (and (empty? tokens) (= name ""))
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (= name ""))
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))

    :else
    (p/->Ok [name end tokens])))

(defn- import-statement [module attributes tokens start]
  (p/with-use [[_use0] (result/try* (module-name "" 0 tokens))]
    (let [[module-name end tokens] _use0]
      (p/with-use [[_use0] (result/try* (optional-unqualified-imports tokens
                                                                      end))]
        (let [{ts :types vs :values end :end tokens :remaining-tokens} _use0
              [alias end tokens] (optional-module-alias tokens end)
              span (->Span start end)
              import- (->Import span module-name alias ts vs)
              definition (->Definition (list/reverse attributes) import-)
              module (->Module (list* definition (:imports module)) (:custom-types module) (:type-aliases module) (:constants module) (:functions module))]
          (p/->Ok [module tokens]))))))

(defn- slurp [module attributes tokens]
  (cond
    (and (seq tokens) (instance? glexer.token.At (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/try* (attribute tokens))]
        (let [[attribute tokens] _use0]
          (slurp module (list* attribute attributes) tokens))))

    (and (seq tokens) (instance? glexer.token.Import (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) result (import-statement module attributes tokens start)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[module tokens] _use0]
          (slurp module (list) tokens))))

    (and (<= 2 (count tokens)) (instance? glexer.token.Pub (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.Type (nth (nth tokens 1) 0)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 2) result (type-definition module attributes (->Public) false tokens start)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[module tokens] _use0]
          (slurp module (list) tokens))))

    (and (<= 3 (count tokens)) (instance? glexer.token.Pub (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.Opaque (nth (nth tokens 1) 0)) (instance? glexer.token.Type (nth (nth tokens 2) 0)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 3) result (type-definition module attributes (->Public) true tokens start)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[module tokens] _use0]
          (slurp module (list) tokens))))

    (and (seq tokens) (instance? glexer.token.Type (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) result (type-definition module attributes (->Private) false tokens start)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[module tokens] _use0]
          (slurp module (list) tokens))))

    (and (<= 2 (count tokens)) (instance? glexer.token.Pub (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)) (instance? glexer.token.Const (nth (nth tokens 1) 0)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (nthrest tokens 2) result (const-definition module attributes (->Public) tokens start)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[module tokens] _use0]
          (slurp module (list) tokens))))

    (and (seq tokens) (instance? glexer.token.Const (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [start (:byte-offset (nth (first tokens) 1)) tokens (rest tokens) result (const-definition module attributes (->Private) tokens start)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[module tokens] _use0]
          (slurp module (list) tokens))))

    (and (<= 3 (count tokens)) (instance? glexer.token.Pub (nth (first tokens) 0)) (instance? glexer.token.Fn (nth (nth tokens 1) 0)) (instance? glexer.token.Name (nth (nth tokens 2) 0)))
    (let [start (nth (first tokens) 1) name (:value (nth (nth tokens 2) 0)) tokens (nthrest tokens 3) {start :byte-offset} start result (function-definition module attributes (->Public) name start tokens)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[module tokens] _use0]
          (slurp module (list) tokens))))

    (and (<= 2 (count tokens)) (instance? glexer.token.Fn (nth (first tokens) 0)) (instance? glexer.token.Name (nth (nth tokens 1) 0)))
    (let [start (nth (first tokens) 1) name (:value (nth (nth tokens 1) 0)) tokens (nthrest tokens 2) {start :byte-offset} start result (function-definition module attributes (->Private) name start tokens)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[module tokens] _use0]
          (slurp module (list) tokens))))

    (empty? tokens)
    (p/->Ok module)

    :else
    (let [tokens tokens]
      (unexpected-error tokens))))

(defn module
  {:malli/schema [:=> [:cat :string] [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [src]
  (-> (glexer/new* src)
      glexer/discard-comments
      glexer/discard-whitespace
      glexer/lex
      ((fn [_capture]
        (slurp (->Module (list) (list) (list) (list) (list)) (list) _capture)))))
