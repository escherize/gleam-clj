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

(declare Definition? Definition-schema Attribute? Attribute-schema Module? Module-schema Function? Function-schema Span? Span-schema Use? Assignment? Assert? Expression? Statement? Statement-schema Let? LetAssert? AssignmentKind? AssignmentKind-schema UsePattern? UsePattern-schema PatternInt? PatternFloat? PatternString? PatternDiscard? PatternVariable? PatternTuple? PatternList? PatternAssignment? PatternConcatenate? PatternBitString? PatternVariant? Pattern? Pattern-schema Int? Float? String? Variable? NegateInt? NegateBool? Block? Panic? Todo? Tuple? List? Fn? RecordUpdate? FieldAccess? Call? TupleIndex? FnCapture? BitString? Case? BinaryOperator? Echo? Expression-schema Clause? Clause-schema BytesOption? IntOption? FloatOption? BitsOption? Utf8Option? Utf16Option? Utf32Option? Utf8CodepointOption? Utf16CodepointOption? Utf32CodepointOption? SignedOption? UnsignedOption? BigOption? LittleOption? NativeOption? SizeValueOption? SizeOption? UnitOption? BitStringSegmentOption? BitStringSegmentOption-schema BitArraySizeInt? BitArraySizeVariable? BitArraySizeBinaryOperator? BitArraySizeBlock? BitArraySize? BitArraySize-schema BitArraySizeAdd? BitArraySizeSubtract? BitArraySizeMultiply? BitArraySizeDivide? BitArraySizeRemainder? BitArraySizeOperator? BitArraySizeOperator-schema And? Or? Eq? NotEq? LtInt? LtEqInt? LtFloat? LtEqFloat? GtEqInt? GtInt? GtEqFloat? GtFloat? Pipe? AddInt? AddFloat? SubInt? SubFloat? MultInt? MultFloat? DivInt? DivFloat? RemainderInt? Concatenate? BinaryOperator-schema FnParameter? FnParameter-schema FunctionParameter? FunctionParameter-schema Named? Discarded? AssignmentName? AssignmentName-schema Import? Import-schema Constant? Constant-schema UnqualifiedImport? UnqualifiedImport-schema Public? Private? Publicity? Publicity-schema TypeAlias? TypeAlias-schema CustomType? CustomType-schema Variant? Variant-schema RecordUpdateField? RecordUpdateField-schema LabelledVariantField? UnlabelledVariantField? VariantField? VariantField-schema LabelledField? ShorthandField? UnlabelledField? Field? Field-schema NamedType? TupleType? FunctionType? VariableType? HoleType? Type? Type-schema UnexpectedEndOfInput? UnexpectedToken? Error-schema UnqualifiedImports? UnqualifiedImports-schema PatternConstructorArguments? PatternConstructorArguments-schema RegularExpressionUnit? ExpressionUnitAfterPipe? ParseExpressionUnitContext? ParseExpressionUnitContext-schema ParsedList? ParsedList-schema)

;; type Definition
(defprotocol IDefinition)
(defrecord Definition [attributes definition] IDefinition)
(defn Definition? "True if `v` is a Definition value." [v] (instance? Definition v))
(defn Definition-schema
  "Malli schema for Definition(definition)."
  [definition]
  [:and [:fn Definition?] [:map [:attributes [:sequential (Attribute-schema)]] [:definition definition]]])

;; type Attribute
(defprotocol IAttribute)
(defrecord Attribute [^java.lang.String name arguments] IAttribute)
(defn Attribute? "True if `v` is a Attribute value." [v] (instance? Attribute v))
(defn Attribute-schema
  "Malli schema for Attribute."
  []
  [:and [:fn Attribute?] [:map [:name :string] [:arguments [:sequential (Expression-schema)]]]])

;; type Module
(defprotocol IModule)
(defrecord Module [imports custom-types type-aliases constants functions] IModule)
(defn Module? "True if `v` is a Module value." [v] (instance? Module v))
(defn Module-schema
  "Malli schema for Module."
  []
  [:and [:fn Module?] [:map [:imports [:sequential (Definition-schema (Import-schema))]] [:custom-types [:sequential (Definition-schema (CustomType-schema))]] [:type-aliases [:sequential (Definition-schema (TypeAlias-schema))]] [:constants [:sequential (Definition-schema (Constant-schema))]] [:functions [:sequential (Definition-schema (Function-schema))]]]])

;; type Function
(defprotocol IFunction)
(defrecord Function [location ^java.lang.String name publicity parameters return body] IFunction)
(defn Function? "True if `v` is a Function value." [v] (instance? Function v))
(defn Function-schema
  "Malli schema for Function."
  []
  [:and [:fn Function?] [:map [:location (Span-schema)] [:name :string] [:publicity (Publicity-schema)] [:parameters [:sequential (FunctionParameter-schema)]] [:return (option/Option-schema (Type-schema))] [:body [:sequential (Statement-schema)]]]])

;; type Span
(defprotocol ISpan)
(defrecord Span [start end] ISpan)
(defn Span? "True if `v` is a Span value." [v] (instance? Span v))
(defn Span-schema
  "Malli schema for Span."
  []
  [:and [:fn Span?] [:map [:start :int] [:end :int]]])

;; type Statement
(defprotocol IStatement)
(defrecord Use [location patterns function] IStatement)
(defn Use? "True if `v` is a Use value." [v] (instance? Use v))
(defrecord Assignment [location kind pattern annotation value] IStatement)
(defn Assignment? "True if `v` is a Assignment value." [v] (instance? Assignment v))
(defrecord Assert [location expression message] IStatement)
(defn Assert? "True if `v` is a Assert value." [v] (instance? Assert v))
(defrecord Expression [value] IStatement)
(defn Expression? "True if `v` is a Expression value." [v] (instance? Expression v))
(defn Statement? "True if `v` is any Statement value." [v] (instance? glance.IStatement v))
(defn Statement-schema
  "Malli schema for Statement."
  []
  [:or
   [:and [:fn Use?] [:map [:location (Span-schema)] [:patterns [:sequential (UsePattern-schema)]] [:function [:fn Expression?]]]]
   [:and [:fn Assignment?] [:map [:location (Span-schema)] [:kind [:fn AssignmentKind?]] [:pattern (Pattern-schema)] [:annotation (option/Option-schema (Type-schema))] [:value [:fn Expression?]]]]
   [:and [:fn Assert?] [:map [:location (Span-schema)] [:expression [:fn Expression?]] [:message (option/Option-schema [:fn Expression?])]]]
   [:and [:fn Expression?] [:map [:value [:fn Expression?]]]]])

;; type AssignmentKind
(defprotocol IAssignmentKind)
(defrecord Let [] IAssignmentKind)
(defn Let? "True if `v` is a Let value." [v] (instance? Let v))
(defrecord LetAssert [message] IAssignmentKind)
(defn LetAssert? "True if `v` is a LetAssert value." [v] (instance? LetAssert v))
(defn AssignmentKind? "True if `v` is any AssignmentKind value." [v] (instance? glance.IAssignmentKind v))
(defn AssignmentKind-schema
  "Malli schema for AssignmentKind."
  []
  [:or
   [:fn Let?]
   [:and [:fn LetAssert?] [:map [:message (option/Option-schema [:fn Expression?])]]]])

;; type UsePattern
(defprotocol IUsePattern)
(defrecord UsePattern [pattern annotation] IUsePattern)
(defn UsePattern? "True if `v` is a UsePattern value." [v] (instance? UsePattern v))
(defn UsePattern-schema
  "Malli schema for UsePattern."
  []
  [:and [:fn UsePattern?] [:map [:pattern (Pattern-schema)] [:annotation (option/Option-schema (Type-schema))]]])

;; type Pattern
(defprotocol IPattern)
(defrecord PatternInt [location ^java.lang.String value] IPattern)
(defn PatternInt? "True if `v` is a PatternInt value." [v] (instance? PatternInt v))
(defrecord PatternFloat [location ^java.lang.String value] IPattern)
(defn PatternFloat? "True if `v` is a PatternFloat value." [v] (instance? PatternFloat v))
(defrecord PatternString [location ^java.lang.String value] IPattern)
(defn PatternString? "True if `v` is a PatternString value." [v] (instance? PatternString v))
(defrecord PatternDiscard [location ^java.lang.String name] IPattern)
(defn PatternDiscard? "True if `v` is a PatternDiscard value." [v] (instance? PatternDiscard v))
(defrecord PatternVariable [location ^java.lang.String name] IPattern)
(defn PatternVariable? "True if `v` is a PatternVariable value." [v] (instance? PatternVariable v))
(defrecord PatternTuple [location elements] IPattern)
(defn PatternTuple? "True if `v` is a PatternTuple value." [v] (instance? PatternTuple v))
(defrecord PatternList [location elements tail] IPattern)
(defn PatternList? "True if `v` is a PatternList value." [v] (instance? PatternList v))
(defrecord PatternAssignment [location pattern ^java.lang.String name] IPattern)
(defn PatternAssignment? "True if `v` is a PatternAssignment value." [v] (instance? PatternAssignment v))
(defrecord PatternConcatenate [location ^java.lang.String prefix prefix-name rest-name] IPattern)
(defn PatternConcatenate? "True if `v` is a PatternConcatenate value." [v] (instance? PatternConcatenate v))
(defrecord PatternBitString [location segments] IPattern)
(defn PatternBitString? "True if `v` is a PatternBitString value." [v] (instance? PatternBitString v))
(defrecord PatternVariant [location module ^java.lang.String constructor arguments with-spread] IPattern)
(defn PatternVariant? "True if `v` is a PatternVariant value." [v] (instance? PatternVariant v))
(defn Pattern? "True if `v` is any Pattern value." [v] (instance? glance.IPattern v))
(defn Pattern-schema
  "Malli schema for Pattern."
  []
  [:or
   [:and [:fn PatternInt?] [:map [:location (Span-schema)] [:value :string]]]
   [:and [:fn PatternFloat?] [:map [:location (Span-schema)] [:value :string]]]
   [:and [:fn PatternString?] [:map [:location (Span-schema)] [:value :string]]]
   [:and [:fn PatternDiscard?] [:map [:location (Span-schema)] [:name :string]]]
   [:and [:fn PatternVariable?] [:map [:location (Span-schema)] [:name :string]]]
   [:and [:fn PatternTuple?] [:map [:location (Span-schema)] [:elements [:sequential [:fn Pattern?]]]]]
   [:and [:fn PatternList?] [:map [:location (Span-schema)] [:elements [:sequential [:fn Pattern?]]] [:tail (option/Option-schema [:fn Pattern?])]]]
   [:and [:fn PatternAssignment?] [:map [:location (Span-schema)] [:pattern [:fn Pattern?]] [:name :string]]]
   [:and [:fn PatternConcatenate?] [:map [:location (Span-schema)] [:prefix :string] [:prefix-name (option/Option-schema (AssignmentName-schema))] [:rest-name (AssignmentName-schema)]]]
   [:and [:fn PatternBitString?] [:map [:location (Span-schema)] [:segments [:sequential [:tuple [:fn Pattern?] [:sequential (BitStringSegmentOption-schema (BitArraySize-schema))]]]]]]
   [:and [:fn PatternVariant?] [:map [:location (Span-schema)] [:module (option/Option-schema :string)] [:constructor :string] [:arguments [:sequential (Field-schema [:fn Pattern?])]] [:with-spread :boolean]]]])

;; type Expression
(defprotocol IExpression)
(defrecord Int [location ^java.lang.String value] IExpression)
(defn Int? "True if `v` is a Int value." [v] (instance? Int v))
(ns-unmap *ns* 'Float)
(defrecord Float [location ^java.lang.String value] IExpression)
(defn Float? "True if `v` is a Float value." [v] (instance? Float v))
(ns-unmap *ns* 'String)
(defrecord String [location ^java.lang.String value] IExpression)
(defn String? "True if `v` is a String value." [v] (instance? String v))
(defrecord Variable [location ^java.lang.String name] IExpression)
(defn Variable? "True if `v` is a Variable value." [v] (instance? Variable v))
(defrecord NegateInt [location value] IExpression)
(defn NegateInt? "True if `v` is a NegateInt value." [v] (instance? NegateInt v))
(defrecord NegateBool [location value] IExpression)
(defn NegateBool? "True if `v` is a NegateBool value." [v] (instance? NegateBool v))
(defrecord Block [location statements] IExpression)
(defn Block? "True if `v` is a Block value." [v] (instance? Block v))
(defrecord Panic [location message] IExpression)
(defn Panic? "True if `v` is a Panic value." [v] (instance? Panic v))
(defrecord Todo [location message] IExpression)
(defn Todo? "True if `v` is a Todo value." [v] (instance? Todo v))
(defrecord Tuple [location elements] IExpression)
(defn Tuple? "True if `v` is a Tuple value." [v] (instance? Tuple v))
(defrecord List [location elements rest] IExpression)
(defn List? "True if `v` is a List value." [v] (instance? List v))
(defrecord Fn [location arguments return-annotation body] IExpression)
(defn Fn? "True if `v` is a Fn value." [v] (instance? Fn v))
(defrecord RecordUpdate [location module ^java.lang.String constructor record fields] IExpression)
(defn RecordUpdate? "True if `v` is a RecordUpdate value." [v] (instance? RecordUpdate v))
(defrecord FieldAccess [location container ^java.lang.String label] IExpression)
(defn FieldAccess? "True if `v` is a FieldAccess value." [v] (instance? FieldAccess v))
(defrecord Call [location function arguments] IExpression)
(defn Call? "True if `v` is a Call value." [v] (instance? Call v))
(defrecord TupleIndex [location tuple index] IExpression)
(defn TupleIndex? "True if `v` is a TupleIndex value." [v] (instance? TupleIndex v))
(defrecord FnCapture [location label function arguments-before arguments-after] IExpression)
(defn FnCapture? "True if `v` is a FnCapture value." [v] (instance? FnCapture v))
(defrecord BitString [location segments] IExpression)
(defn BitString? "True if `v` is a BitString value." [v] (instance? BitString v))
(defrecord Case [location subjects clauses] IExpression)
(defn Case? "True if `v` is a Case value." [v] (instance? Case v))
(defrecord BinaryOperator [location name left right] IExpression)
(defn BinaryOperator? "True if `v` is a BinaryOperator value." [v] (instance? BinaryOperator v))
(defrecord Echo [location expression message] IExpression)
(defn Echo? "True if `v` is a Echo value." [v] (instance? Echo v))
(defn Expression-schema
  "Malli schema for Expression."
  []
  [:or
   [:and [:fn Int?] [:map [:location (Span-schema)] [:value :string]]]
   [:and [:fn Float?] [:map [:location (Span-schema)] [:value :string]]]
   [:and [:fn String?] [:map [:location (Span-schema)] [:value :string]]]
   [:and [:fn Variable?] [:map [:location (Span-schema)] [:name :string]]]
   [:and [:fn NegateInt?] [:map [:location (Span-schema)] [:value [:fn Expression?]]]]
   [:and [:fn NegateBool?] [:map [:location (Span-schema)] [:value [:fn Expression?]]]]
   [:and [:fn Block?] [:map [:location (Span-schema)] [:statements [:sequential [:fn Statement?]]]]]
   [:and [:fn Panic?] [:map [:location (Span-schema)] [:message (option/Option-schema [:fn Expression?])]]]
   [:and [:fn Todo?] [:map [:location (Span-schema)] [:message (option/Option-schema [:fn Expression?])]]]
   [:and [:fn Tuple?] [:map [:location (Span-schema)] [:elements [:sequential [:fn Expression?]]]]]
   [:and [:fn List?] [:map [:location (Span-schema)] [:elements [:sequential [:fn Expression?]]] [:rest (option/Option-schema [:fn Expression?])]]]
   [:and [:fn Fn?] [:map [:location (Span-schema)] [:arguments [:sequential (FnParameter-schema)]] [:return-annotation (option/Option-schema (Type-schema))] [:body [:sequential [:fn Statement?]]]]]
   [:and [:fn RecordUpdate?] [:map [:location (Span-schema)] [:module (option/Option-schema :string)] [:constructor :string] [:record [:fn Expression?]] [:fields [:sequential (RecordUpdateField-schema [:fn Expression?])]]]]
   [:and [:fn FieldAccess?] [:map [:location (Span-schema)] [:container [:fn Expression?]] [:label :string]]]
   [:and [:fn Call?] [:map [:location (Span-schema)] [:function [:fn Expression?]] [:arguments [:sequential (Field-schema [:fn Expression?])]]]]
   [:and [:fn TupleIndex?] [:map [:location (Span-schema)] [:tuple [:fn Expression?]] [:index :int]]]
   [:and [:fn FnCapture?] [:map [:location (Span-schema)] [:label (option/Option-schema :string)] [:function [:fn Expression?]] [:arguments-before [:sequential (Field-schema [:fn Expression?])]] [:arguments-after [:sequential (Field-schema [:fn Expression?])]]]]
   [:and [:fn BitString?] [:map [:location (Span-schema)] [:segments [:sequential [:tuple [:fn Expression?] [:sequential (BitStringSegmentOption-schema [:fn Expression?])]]]]]]
   [:and [:fn Case?] [:map [:location (Span-schema)] [:subjects [:sequential [:fn Expression?]]] [:clauses [:sequential [:fn Clause?]]]]]
   [:and [:fn BinaryOperator?] [:map [:location (Span-schema)] [:name (BinaryOperator-schema)] [:left [:fn Expression?]] [:right [:fn Expression?]]]]
   [:and [:fn Echo?] [:map [:location (Span-schema)] [:expression (option/Option-schema [:fn Expression?])] [:message (option/Option-schema [:fn Expression?])]]]])

;; type Clause
(defprotocol IClause)
(defrecord Clause [patterns guard body] IClause)
(defn Clause? "True if `v` is a Clause value." [v] (instance? Clause v))
(defn Clause-schema
  "Malli schema for Clause."
  []
  [:and [:fn Clause?] [:map [:patterns [:sequential [:sequential (Pattern-schema)]]] [:guard (option/Option-schema [:fn Expression?])] [:body [:fn Expression?]]]])

;; type BitStringSegmentOption
(defprotocol IBitStringSegmentOption)
(defrecord BytesOption [] IBitStringSegmentOption)
(defn BytesOption? "True if `v` is a BytesOption value." [v] (instance? BytesOption v))
(defrecord IntOption [] IBitStringSegmentOption)
(defn IntOption? "True if `v` is a IntOption value." [v] (instance? IntOption v))
(defrecord FloatOption [] IBitStringSegmentOption)
(defn FloatOption? "True if `v` is a FloatOption value." [v] (instance? FloatOption v))
(defrecord BitsOption [] IBitStringSegmentOption)
(defn BitsOption? "True if `v` is a BitsOption value." [v] (instance? BitsOption v))
(defrecord Utf8Option [] IBitStringSegmentOption)
(defn Utf8Option? "True if `v` is a Utf8Option value." [v] (instance? Utf8Option v))
(defrecord Utf16Option [] IBitStringSegmentOption)
(defn Utf16Option? "True if `v` is a Utf16Option value." [v] (instance? Utf16Option v))
(defrecord Utf32Option [] IBitStringSegmentOption)
(defn Utf32Option? "True if `v` is a Utf32Option value." [v] (instance? Utf32Option v))
(defrecord Utf8CodepointOption [] IBitStringSegmentOption)
(defn Utf8CodepointOption? "True if `v` is a Utf8CodepointOption value." [v] (instance? Utf8CodepointOption v))
(defrecord Utf16CodepointOption [] IBitStringSegmentOption)
(defn Utf16CodepointOption? "True if `v` is a Utf16CodepointOption value." [v] (instance? Utf16CodepointOption v))
(defrecord Utf32CodepointOption [] IBitStringSegmentOption)
(defn Utf32CodepointOption? "True if `v` is a Utf32CodepointOption value." [v] (instance? Utf32CodepointOption v))
(defrecord SignedOption [] IBitStringSegmentOption)
(defn SignedOption? "True if `v` is a SignedOption value." [v] (instance? SignedOption v))
(defrecord UnsignedOption [] IBitStringSegmentOption)
(defn UnsignedOption? "True if `v` is a UnsignedOption value." [v] (instance? UnsignedOption v))
(defrecord BigOption [] IBitStringSegmentOption)
(defn BigOption? "True if `v` is a BigOption value." [v] (instance? BigOption v))
(defrecord LittleOption [] IBitStringSegmentOption)
(defn LittleOption? "True if `v` is a LittleOption value." [v] (instance? LittleOption v))
(defrecord NativeOption [] IBitStringSegmentOption)
(defn NativeOption? "True if `v` is a NativeOption value." [v] (instance? NativeOption v))
(defrecord SizeValueOption [value] IBitStringSegmentOption)
(defn SizeValueOption? "True if `v` is a SizeValueOption value." [v] (instance? SizeValueOption v))
(defrecord SizeOption [value] IBitStringSegmentOption)
(defn SizeOption? "True if `v` is a SizeOption value." [v] (instance? SizeOption v))
(defrecord UnitOption [value] IBitStringSegmentOption)
(defn UnitOption? "True if `v` is a UnitOption value." [v] (instance? UnitOption v))
(defn BitStringSegmentOption? "True if `v` is any BitStringSegmentOption value." [v] (instance? glance.IBitStringSegmentOption v))
(defn BitStringSegmentOption-schema
  "Malli schema for BitStringSegmentOption(t)."
  [t]
  [:or
   [:fn BytesOption?]
   [:fn IntOption?]
   [:fn FloatOption?]
   [:fn BitsOption?]
   [:fn Utf8Option?]
   [:fn Utf16Option?]
   [:fn Utf32Option?]
   [:fn Utf8CodepointOption?]
   [:fn Utf16CodepointOption?]
   [:fn Utf32CodepointOption?]
   [:fn SignedOption?]
   [:fn UnsignedOption?]
   [:fn BigOption?]
   [:fn LittleOption?]
   [:fn NativeOption?]
   [:and [:fn SizeValueOption?] [:map [:value t]]]
   [:and [:fn SizeOption?] [:map [:value :int]]]
   [:and [:fn UnitOption?] [:map [:value :int]]]])

;; type BitArraySize
(defprotocol IBitArraySize)
(defrecord BitArraySizeInt [location ^java.lang.String value] IBitArraySize)
(defn BitArraySizeInt? "True if `v` is a BitArraySizeInt value." [v] (instance? BitArraySizeInt v))
(defrecord BitArraySizeVariable [location ^java.lang.String name] IBitArraySize)
(defn BitArraySizeVariable? "True if `v` is a BitArraySizeVariable value." [v] (instance? BitArraySizeVariable v))
(defrecord BitArraySizeBinaryOperator [location operator left right] IBitArraySize)
(defn BitArraySizeBinaryOperator? "True if `v` is a BitArraySizeBinaryOperator value." [v] (instance? BitArraySizeBinaryOperator v))
(defrecord BitArraySizeBlock [location inner] IBitArraySize)
(defn BitArraySizeBlock? "True if `v` is a BitArraySizeBlock value." [v] (instance? BitArraySizeBlock v))
(defn BitArraySize? "True if `v` is any BitArraySize value." [v] (instance? glance.IBitArraySize v))
(defn BitArraySize-schema
  "Malli schema for BitArraySize."
  []
  [:or
   [:and [:fn BitArraySizeInt?] [:map [:location (Span-schema)] [:value :string]]]
   [:and [:fn BitArraySizeVariable?] [:map [:location (Span-schema)] [:name :string]]]
   [:and [:fn BitArraySizeBinaryOperator?] [:map [:location (Span-schema)] [:operator (BitArraySizeOperator-schema)] [:left [:fn BitArraySize?]] [:right [:fn BitArraySize?]]]]
   [:and [:fn BitArraySizeBlock?] [:map [:location (Span-schema)] [:inner [:fn BitArraySize?]]]]])

;; type BitArraySizeOperator
(defprotocol IBitArraySizeOperator)
(defrecord BitArraySizeAdd [] IBitArraySizeOperator)
(defn BitArraySizeAdd? "True if `v` is a BitArraySizeAdd value." [v] (instance? BitArraySizeAdd v))
(defrecord BitArraySizeSubtract [] IBitArraySizeOperator)
(defn BitArraySizeSubtract? "True if `v` is a BitArraySizeSubtract value." [v] (instance? BitArraySizeSubtract v))
(defrecord BitArraySizeMultiply [] IBitArraySizeOperator)
(defn BitArraySizeMultiply? "True if `v` is a BitArraySizeMultiply value." [v] (instance? BitArraySizeMultiply v))
(defrecord BitArraySizeDivide [] IBitArraySizeOperator)
(defn BitArraySizeDivide? "True if `v` is a BitArraySizeDivide value." [v] (instance? BitArraySizeDivide v))
(defrecord BitArraySizeRemainder [] IBitArraySizeOperator)
(defn BitArraySizeRemainder? "True if `v` is a BitArraySizeRemainder value." [v] (instance? BitArraySizeRemainder v))
(defn BitArraySizeOperator? "True if `v` is any BitArraySizeOperator value." [v] (instance? glance.IBitArraySizeOperator v))
(defn BitArraySizeOperator-schema
  "Malli schema for BitArraySizeOperator."
  []
  [:or
   [:fn BitArraySizeAdd?]
   [:fn BitArraySizeSubtract?]
   [:fn BitArraySizeMultiply?]
   [:fn BitArraySizeDivide?]
   [:fn BitArraySizeRemainder?]])

;; type BinaryOperator
(defprotocol IBinaryOperator)
(defrecord And [] IBinaryOperator)
(defn And? "True if `v` is a And value." [v] (instance? And v))
(defrecord Or [] IBinaryOperator)
(defn Or? "True if `v` is a Or value." [v] (instance? Or v))
(defrecord Eq [] IBinaryOperator)
(defn Eq? "True if `v` is a Eq value." [v] (instance? Eq v))
(defrecord NotEq [] IBinaryOperator)
(defn NotEq? "True if `v` is a NotEq value." [v] (instance? NotEq v))
(defrecord LtInt [] IBinaryOperator)
(defn LtInt? "True if `v` is a LtInt value." [v] (instance? LtInt v))
(defrecord LtEqInt [] IBinaryOperator)
(defn LtEqInt? "True if `v` is a LtEqInt value." [v] (instance? LtEqInt v))
(defrecord LtFloat [] IBinaryOperator)
(defn LtFloat? "True if `v` is a LtFloat value." [v] (instance? LtFloat v))
(defrecord LtEqFloat [] IBinaryOperator)
(defn LtEqFloat? "True if `v` is a LtEqFloat value." [v] (instance? LtEqFloat v))
(defrecord GtEqInt [] IBinaryOperator)
(defn GtEqInt? "True if `v` is a GtEqInt value." [v] (instance? GtEqInt v))
(defrecord GtInt [] IBinaryOperator)
(defn GtInt? "True if `v` is a GtInt value." [v] (instance? GtInt v))
(defrecord GtEqFloat [] IBinaryOperator)
(defn GtEqFloat? "True if `v` is a GtEqFloat value." [v] (instance? GtEqFloat v))
(defrecord GtFloat [] IBinaryOperator)
(defn GtFloat? "True if `v` is a GtFloat value." [v] (instance? GtFloat v))
(defrecord Pipe [] IBinaryOperator)
(defn Pipe? "True if `v` is a Pipe value." [v] (instance? Pipe v))
(defrecord AddInt [] IBinaryOperator)
(defn AddInt? "True if `v` is a AddInt value." [v] (instance? AddInt v))
(defrecord AddFloat [] IBinaryOperator)
(defn AddFloat? "True if `v` is a AddFloat value." [v] (instance? AddFloat v))
(defrecord SubInt [] IBinaryOperator)
(defn SubInt? "True if `v` is a SubInt value." [v] (instance? SubInt v))
(defrecord SubFloat [] IBinaryOperator)
(defn SubFloat? "True if `v` is a SubFloat value." [v] (instance? SubFloat v))
(defrecord MultInt [] IBinaryOperator)
(defn MultInt? "True if `v` is a MultInt value." [v] (instance? MultInt v))
(defrecord MultFloat [] IBinaryOperator)
(defn MultFloat? "True if `v` is a MultFloat value." [v] (instance? MultFloat v))
(defrecord DivInt [] IBinaryOperator)
(defn DivInt? "True if `v` is a DivInt value." [v] (instance? DivInt v))
(defrecord DivFloat [] IBinaryOperator)
(defn DivFloat? "True if `v` is a DivFloat value." [v] (instance? DivFloat v))
(defrecord RemainderInt [] IBinaryOperator)
(defn RemainderInt? "True if `v` is a RemainderInt value." [v] (instance? RemainderInt v))
(defrecord Concatenate [] IBinaryOperator)
(defn Concatenate? "True if `v` is a Concatenate value." [v] (instance? Concatenate v))
(defn BinaryOperator-schema
  "Malli schema for BinaryOperator."
  []
  [:or
   [:fn And?]
   [:fn Or?]
   [:fn Eq?]
   [:fn NotEq?]
   [:fn LtInt?]
   [:fn LtEqInt?]
   [:fn LtFloat?]
   [:fn LtEqFloat?]
   [:fn GtEqInt?]
   [:fn GtInt?]
   [:fn GtEqFloat?]
   [:fn GtFloat?]
   [:fn Pipe?]
   [:fn AddInt?]
   [:fn AddFloat?]
   [:fn SubInt?]
   [:fn SubFloat?]
   [:fn MultInt?]
   [:fn MultFloat?]
   [:fn DivInt?]
   [:fn DivFloat?]
   [:fn RemainderInt?]
   [:fn Concatenate?]])

;; type FnParameter
(defprotocol IFnParameter)
(defrecord FnParameter [name type-] IFnParameter)
(defn FnParameter? "True if `v` is a FnParameter value." [v] (instance? FnParameter v))
(defn FnParameter-schema
  "Malli schema for FnParameter."
  []
  [:and [:fn FnParameter?] [:map [:name (AssignmentName-schema)] [:type- (option/Option-schema (Type-schema))]]])

;; type FunctionParameter
(defprotocol IFunctionParameter)
(defrecord FunctionParameter [label name type-] IFunctionParameter)
(defn FunctionParameter? "True if `v` is a FunctionParameter value." [v] (instance? FunctionParameter v))
(defn FunctionParameter-schema
  "Malli schema for FunctionParameter."
  []
  [:and [:fn FunctionParameter?] [:map [:label (option/Option-schema :string)] [:name (AssignmentName-schema)] [:type- (option/Option-schema (Type-schema))]]])

;; type AssignmentName
(defprotocol IAssignmentName)
(defrecord Named [^java.lang.String value] IAssignmentName)
(defn Named? "True if `v` is a Named value." [v] (instance? Named v))
(defrecord Discarded [^java.lang.String value] IAssignmentName)
(defn Discarded? "True if `v` is a Discarded value." [v] (instance? Discarded v))
(defn AssignmentName? "True if `v` is any AssignmentName value." [v] (instance? glance.IAssignmentName v))
(defn AssignmentName-schema
  "Malli schema for AssignmentName."
  []
  [:or
   [:and [:fn Named?] [:map [:value :string]]]
   [:and [:fn Discarded?] [:map [:value :string]]]])

;; type Import
(defprotocol IImport)
(defrecord Import [location ^java.lang.String module alias unqualified-types unqualified-values] IImport)
(defn Import? "True if `v` is a Import value." [v] (instance? Import v))
(defn Import-schema
  "Malli schema for Import."
  []
  [:and [:fn Import?] [:map [:location (Span-schema)] [:module :string] [:alias (option/Option-schema (AssignmentName-schema))] [:unqualified-types [:sequential (UnqualifiedImport-schema)]] [:unqualified-values [:sequential (UnqualifiedImport-schema)]]]])

;; type Constant
(defprotocol IConstant)
(defrecord Constant [location ^java.lang.String name publicity annotation value] IConstant)
(defn Constant? "True if `v` is a Constant value." [v] (instance? Constant v))
(defn Constant-schema
  "Malli schema for Constant."
  []
  [:and [:fn Constant?] [:map [:location (Span-schema)] [:name :string] [:publicity (Publicity-schema)] [:annotation (option/Option-schema (Type-schema))] [:value (Expression-schema)]]])

;; type UnqualifiedImport
(defprotocol IUnqualifiedImport)
(defrecord UnqualifiedImport [^java.lang.String name alias] IUnqualifiedImport)
(defn UnqualifiedImport? "True if `v` is a UnqualifiedImport value." [v] (instance? UnqualifiedImport v))
(defn UnqualifiedImport-schema
  "Malli schema for UnqualifiedImport."
  []
  [:and [:fn UnqualifiedImport?] [:map [:name :string] [:alias (option/Option-schema :string)]]])

;; type Publicity
(defprotocol IPublicity)
(defrecord Public [] IPublicity)
(defn Public? "True if `v` is a Public value." [v] (instance? Public v))
(defrecord Private [] IPublicity)
(defn Private? "True if `v` is a Private value." [v] (instance? Private v))
(defn Publicity? "True if `v` is any Publicity value." [v] (instance? glance.IPublicity v))
(defn Publicity-schema
  "Malli schema for Publicity."
  []
  [:or
   [:fn Public?]
   [:fn Private?]])

;; type TypeAlias
(defprotocol ITypeAlias)
(defrecord TypeAlias [location ^java.lang.String name publicity parameters aliased] ITypeAlias)
(defn TypeAlias? "True if `v` is a TypeAlias value." [v] (instance? TypeAlias v))
(defn TypeAlias-schema
  "Malli schema for TypeAlias."
  []
  [:and [:fn TypeAlias?] [:map [:location (Span-schema)] [:name :string] [:publicity (Publicity-schema)] [:parameters [:sequential :string]] [:aliased (Type-schema)]]])

;; type CustomType
(defprotocol ICustomType)
(defrecord CustomType [location ^java.lang.String name publicity opaque- parameters variants] ICustomType)
(defn CustomType? "True if `v` is a CustomType value." [v] (instance? CustomType v))
(defn CustomType-schema
  "Malli schema for CustomType."
  []
  [:and [:fn CustomType?] [:map [:location (Span-schema)] [:name :string] [:publicity (Publicity-schema)] [:opaque- :boolean] [:parameters [:sequential :string]] [:variants [:sequential (Variant-schema)]]]])

;; type Variant
(defprotocol IVariant)
(defrecord Variant [^java.lang.String name fields attributes] IVariant)
(defn Variant? "True if `v` is a Variant value." [v] (instance? Variant v))
(defn Variant-schema
  "Malli schema for Variant."
  []
  [:and [:fn Variant?] [:map [:name :string] [:fields [:sequential (VariantField-schema)]] [:attributes [:sequential (Attribute-schema)]]]])

;; type RecordUpdateField
(defprotocol IRecordUpdateField)
(defrecord RecordUpdateField [^java.lang.String label item] IRecordUpdateField)
(defn RecordUpdateField? "True if `v` is a RecordUpdateField value." [v] (instance? RecordUpdateField v))
(defn RecordUpdateField-schema
  "Malli schema for RecordUpdateField(t)."
  [t]
  [:and [:fn RecordUpdateField?] [:map [:label :string] [:item (option/Option-schema t)]]])

;; type VariantField
(defprotocol IVariantField)
(defrecord LabelledVariantField [item ^java.lang.String label] IVariantField)
(defn LabelledVariantField? "True if `v` is a LabelledVariantField value." [v] (instance? LabelledVariantField v))
(defrecord UnlabelledVariantField [item] IVariantField)
(defn UnlabelledVariantField? "True if `v` is a UnlabelledVariantField value." [v] (instance? UnlabelledVariantField v))
(defn VariantField? "True if `v` is any VariantField value." [v] (instance? glance.IVariantField v))
(defn VariantField-schema
  "Malli schema for VariantField."
  []
  [:or
   [:and [:fn LabelledVariantField?] [:map [:item (Type-schema)] [:label :string]]]
   [:and [:fn UnlabelledVariantField?] [:map [:item (Type-schema)]]]])

;; type Field
(defprotocol IField)
(defrecord LabelledField [^java.lang.String label label-location item] IField)
(defn LabelledField? "True if `v` is a LabelledField value." [v] (instance? LabelledField v))
(defrecord ShorthandField [^java.lang.String label location] IField)
(defn ShorthandField? "True if `v` is a ShorthandField value." [v] (instance? ShorthandField v))
(defrecord UnlabelledField [item] IField)
(defn UnlabelledField? "True if `v` is a UnlabelledField value." [v] (instance? UnlabelledField v))
(defn Field? "True if `v` is any Field value." [v] (instance? glance.IField v))
(defn Field-schema
  "Malli schema for Field(t)."
  [t]
  [:or
   [:and [:fn LabelledField?] [:map [:label :string] [:label-location (Span-schema)] [:item t]]]
   [:and [:fn ShorthandField?] [:map [:label :string] [:location (Span-schema)]]]
   [:and [:fn UnlabelledField?] [:map [:item t]]]])

;; type Type
(defprotocol IType)
(defrecord NamedType [location ^java.lang.String name module parameters] IType)
(defn NamedType? "True if `v` is a NamedType value." [v] (instance? NamedType v))
(defrecord TupleType [location elements] IType)
(defn TupleType? "True if `v` is a TupleType value." [v] (instance? TupleType v))
(defrecord FunctionType [location parameters return] IType)
(defn FunctionType? "True if `v` is a FunctionType value." [v] (instance? FunctionType v))
(defrecord VariableType [location ^java.lang.String name] IType)
(defn VariableType? "True if `v` is a VariableType value." [v] (instance? VariableType v))
(defrecord HoleType [location ^java.lang.String name] IType)
(defn HoleType? "True if `v` is a HoleType value." [v] (instance? HoleType v))
(defn Type? "True if `v` is any Type value." [v] (instance? glance.IType v))
(defn Type-schema
  "Malli schema for Type."
  []
  [:or
   [:and [:fn NamedType?] [:map [:location (Span-schema)] [:name :string] [:module (option/Option-schema :string)] [:parameters [:sequential [:fn Type?]]]]]
   [:and [:fn TupleType?] [:map [:location (Span-schema)] [:elements [:sequential [:fn Type?]]]]]
   [:and [:fn FunctionType?] [:map [:location (Span-schema)] [:parameters [:sequential [:fn Type?]]] [:return [:fn Type?]]]]
   [:and [:fn VariableType?] [:map [:location (Span-schema)] [:name :string]]]
   [:and [:fn HoleType?] [:map [:location (Span-schema)] [:name :string]]]])

;; type Error
(defprotocol IError)
(defrecord UnexpectedEndOfInput [] IError)
(defn UnexpectedEndOfInput? "True if `v` is a UnexpectedEndOfInput value." [v] (instance? UnexpectedEndOfInput v))
(defrecord UnexpectedToken [token position] IError)
(defn UnexpectedToken? "True if `v` is a UnexpectedToken value." [v] (instance? UnexpectedToken v))
(defn Error-schema
  "Malli schema for Error."
  []
  [:or
   [:fn UnexpectedEndOfInput?]
   [:and [:fn UnexpectedToken?] [:map [:token (t/Token-schema)] [:position (glexer/Position-schema)]]]])

;; type UnqualifiedImports
(defprotocol IUnqualifiedImports)
(defrecord UnqualifiedImports [types values end remaining-tokens] IUnqualifiedImports)
(defn UnqualifiedImports? "True if `v` is a UnqualifiedImports value." [v] (instance? UnqualifiedImports v))
(defn UnqualifiedImports-schema
  "Malli schema for UnqualifiedImports."
  []
  [:and [:fn UnqualifiedImports?] [:map [:types [:sequential (UnqualifiedImport-schema)]] [:values [:sequential (UnqualifiedImport-schema)]] [:end :int] [:remaining-tokens [:sequential [:tuple (t/Token-schema) (glexer/Position-schema)]]]]])

;; type PatternConstructorArguments
(defprotocol IPatternConstructorArguments)
(defrecord PatternConstructorArguments [fields spread end remaining-tokens] IPatternConstructorArguments)
(defn PatternConstructorArguments? "True if `v` is a PatternConstructorArguments value." [v] (instance? PatternConstructorArguments v))
(defn PatternConstructorArguments-schema
  "Malli schema for PatternConstructorArguments."
  []
  [:and [:fn PatternConstructorArguments?] [:map [:fields [:sequential (Field-schema (Pattern-schema))]] [:spread :boolean] [:end :int] [:remaining-tokens [:sequential [:tuple (t/Token-schema) (glexer/Position-schema)]]]]])

;; type ParseExpressionUnitContext
(defprotocol IParseExpressionUnitContext)
(defrecord RegularExpressionUnit [] IParseExpressionUnitContext)
(defn RegularExpressionUnit? "True if `v` is a RegularExpressionUnit value." [v] (instance? RegularExpressionUnit v))
(defrecord ExpressionUnitAfterPipe [] IParseExpressionUnitContext)
(defn ExpressionUnitAfterPipe? "True if `v` is a ExpressionUnitAfterPipe value." [v] (instance? ExpressionUnitAfterPipe v))
(defn ParseExpressionUnitContext? "True if `v` is any ParseExpressionUnitContext value." [v] (instance? glance.IParseExpressionUnitContext v))
(defn ParseExpressionUnitContext-schema
  "Malli schema for ParseExpressionUnitContext."
  []
  [:or
   [:fn RegularExpressionUnit?]
   [:fn ExpressionUnitAfterPipe?]])

;; type ParsedList
(defprotocol IParsedList)
(defrecord ParsedList [values spread remaining-tokens end] IParsedList)
(defn ParsedList? "True if `v` is a ParsedList value." [v] (instance? ParsedList v))
(defn ParsedList-schema
  "Malli schema for ParsedList(ast_node)."
  [ast-node]
  [:and [:fn ParsedList?] [:map [:values [:sequential ast-node]] [:spread (option/Option-schema ast-node)] [:remaining-tokens [:sequential [:tuple (t/Token-schema) (glexer/Position-schema)]]] [:end :int]]])

(defn precedence
  "precedence(operator: BinaryOperator) -> Int"
  {:malli/schema [:=> [:cat (BinaryOperator-schema)] :int]
   :gleam/src "bench/build/packages/glance/src/glance.gleam:236"}
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

(defn- unexpected-error
  "unexpected_error(tokens: List(#(Token, Position))) -> Result(a, Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1093"}
  [tokens]
  (if (seq tokens)
    (let [token (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken token position)))
    (p/->Error (->UnexpectedEndOfInput))))

(defn- expect
  "expect(expected: Token, tokens: List(#(Token, Position)), next: fn(Position, List(#(Token, Position))) -> Result(a, Error)) -> Result(a, Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:419"}
  [expected tokens next]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (= (nth (first tokens) 0) expected))
    (let [position (nth (first tokens) 1) tokens (rest tokens)]
      (next position tokens))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))))

(defn- list'
  "list(parser: fn(List(#(Token, Position))) -> Result(#(a, List(#(Token, Position))), Error), discard: Option(fn(Span) -> a), acc: List(a), tokens: List(#(Token, Position))) -> Result(ParsedList(a), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1885"}
  [parser discard acc tokens]
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

(defn- push-function
  "push_function(module: Module, attributes: List(Attribute), function: Function) -> Module"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:380"}
  [module attributes function]
  (->Module (:imports module) (:custom-types module) (:type-aliases module) (:constants module) (list* (->Definition (list/reverse attributes) function) (:functions module))))

(defn- handle-operator
  "handle_operator(next: Option(BinaryOperator), operators: List(BinaryOperator), values: List(Expression)) -> #(Option(Expression), List(BinaryOperator), List(Expression))

   Simple-Precedence-Parser, handle seeing an operator or end"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1173"}
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

(defn- binary-operator
  "binary_operator(token: Token) -> Result(BinaryOperator, Nil)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1100"}
  [token]
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

(defn- pop-binary-operator
  "pop_binary_operator(tokens: List(#(Token, Position))) -> Result(#(BinaryOperator, List(#(Token, Position))), Nil)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1129"}
  [tokens]
  (if (seq tokens)
    (let [token (nth (first tokens) 0) tokens (rest tokens)]
      (p/with-use [[op] (result/map (binary-operator token))]
        [op tokens]))
    (p/->Error nil)))

(defn- field
  "field(tokens: List(#(Token, Position)), of parser: fn(List(#(Token, Position))) -> Result(#(a, List(#(Token, Position))), Error)) -> Result(#(Field(a), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2284"}
  [tokens parser]
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

(defn- string-offset
  "string_offset(start: Int, string: String) -> Int"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1582"}
  [start ^java.lang.String string]
  (+' start (string/byte-size string)))

(defn- span-from-string
  "span_from_string(start: Int, string: String) -> Span"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1214"}
  [start ^java.lang.String string]
  (->Span start (+' start (string/byte-size string))))

(defn- comma-delimited
  "comma_delimited(items: List(a), tokens: List(#(Token, Position)), parse parser: fn(List(#(Token, Position))) -> Result(#(a, List(#(Token, Position))), Error), until final: Token) -> Result(#(List(a), Int, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2038"}
  [items tokens parser final]
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

(defn- bit-string-segment-options
  "bit_string_segment_options(size_parser: fn(List(#(Token, Position))) -> Result(#(a, List(#(Token, Position))), Error), options: List(BitStringSegmentOption(a)), tokens: List(#(Token, Position))) -> Result(#(List(BitStringSegmentOption(a)), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1395"}
  [size-parser options tokens]
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

(defn- optional-bit-string-segment-options
  "optional_bit_string_segment_options(size_parser: fn(List(#(Token, Position))) -> Result(#(a, List(#(Token, Position))), Error), tokens: List(#(Token, Position))) -> Result(#(List(BitStringSegmentOption(a)), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1384"}
  [size-parser tokens]
  (if (and (seq tokens) (instance? glexer.token.Colon (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (bit-string-segment-options size-parser (list) tokens))
    (p/->Ok [(list) tokens])))

(defn- bit-string-segment
  "bit_string_segment(parser: fn(List(#(Token, Position))) -> Result(#(a, List(#(Token, Position))), Error), size_parser: fn(List(#(Token, Position))) -> Result(#(b, List(#(Token, Position))), Error), tokens: List(#(Token, Position))) -> Result(#(#(a, List(BitStringSegmentOption(b))), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1373"}
  [parser size-parser tokens]
  (p/with-use [[_use0] (result/try* (parser tokens))]
    (let [[value tokens] _use0
          result (optional-bit-string-segment-options size-parser tokens)]
      (p/with-use [[_use0] (result/try* result)]
        (let [[options tokens] _use0]
          (p/->Ok [[value options] tokens]))))))

(defn- delimited
  "delimited(acc: List(a), tokens: List(#(Token, Position)), parser: fn(List(#(Token, Position))) -> Result(#(a, List(#(Token, Position))), Error), delimeter: Token) -> Result(#(List(a), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1842"}
  [acc tokens parser delimeter]
  (p/with-use [[_use0] (result/try* (parser tokens))]
    (let [[t tokens] _use0
          acc (list* t acc)]
      (if (and (seq tokens) (= (nth (first tokens) 0) delimeter))
        (let [tokens (rest tokens)]
          (delimited acc tokens parser delimeter))
        (p/->Ok [(list/reverse acc) tokens])))))

(defn- bit-array-size-precedence
  "bit_array_size_precedence(operator: BitArraySizeOperator) -> Int"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1522"}
  [operator]
  (if (or (instance? BitArraySizeAdd operator) (instance? BitArraySizeSubtract operator))
    7
    8))

(defn- handle-bit-array-size-operator
  "handle_bit_array_size_operator(next: Option(BitArraySizeOperator), operators: List(BitArraySizeOperator), values: List(BitArraySize)) -> #(Option(BitArraySize), List(BitArraySizeOperator), List(BitArraySize))"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1541"}
  [next operators values]
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

(defn- bit-array-size-operator
  "bit_array_size_operator(token: Token) -> Result(BitArraySizeOperator, Nil)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1511"}
  [token]
  (cond
    (instance? glexer.token.Plus token) (p/->Ok (->BitArraySizeAdd))
    (instance? glexer.token.Minus token) (p/->Ok (->BitArraySizeSubtract))
    (instance? glexer.token.Star token) (p/->Ok (->BitArraySizeMultiply))
    (instance? glexer.token.Slash token) (p/->Ok (->BitArraySizeDivide))
    (instance? glexer.token.Percent token) (p/->Ok (->BitArraySizeRemainder))
    :else (p/->Error nil)))

(defn- pop-bit-array-size-operator
  "pop_bit_array_size_operator(tokens: List(#(Token, Position))) -> Result(#(BitArraySizeOperator, List(#(Token, Position))), Nil)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1529"}
  [tokens]
  (if (seq tokens)
    (let [token (nth (first tokens) 0) tokens (rest tokens)]
      (p/with-use [[operator] (result/map (bit-array-size-operator token))]
        [operator tokens]))
    (p/->Error nil)))

(declare bit-array-size-unit bit-array-size-loop bit-array-size)

(defn- bit-array-size-unit
  "bit_array_size_unit(tokens: List(#(Token, Position))) -> Result(#(BitArraySize, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1492"}
  [tokens]
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

(defn- bit-array-size-loop
  "bit_array_size_loop(tokens: List(#(Token, Position)), operators: List(BitArraySizeOperator), values: List(BitArraySize)) -> Result(#(BitArraySize, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1468"}
  [tokens operators values]
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

(defn- bit-array-size
  "bit_array_size(tokens: List(#(Token, Position))) -> Result(#(BitArraySize, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1464"}
  [tokens]
  (bit-array-size-loop tokens (list) (list)))

(declare pattern-constructor-arguments pattern-constructor pattern)

(defn- pattern-constructor-arguments
  "pattern_constructor_arguments(arguments: List(Field(Pattern)), tokens: List(#(Token, Position))) -> Result(PatternConstructorArguments, Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:938"}
  [arguments tokens]
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

(defn- pattern-constructor
  "pattern_constructor(module: Option(String), constructor: String, tokens: List(#(Token, Position)), start: Int, name_start: Int) -> Result(#(Pattern, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:903"}
  [module ^java.lang.String constructor tokens start name-start]
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

(defn- pattern
  "pattern(tokens: List(#(Token, Position))) -> Result(#(Pattern, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:971"}
  [tokens]
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

(defn- named-type
  "named_type(name: String, module: Option(String), tokens: List(#(Token, Position)), start: Int, name_start: Int) -> Result(#(Type, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2173"}
  [^java.lang.String name module tokens start name-start]
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

(defn- tuple-type
  "tuple_type(start: Int, tokens: List(#(Token, Position))) -> Result(#(Type, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2202"}
  [start tokens]
  (let [result (comma-delimited (list) tokens type- (t/->RightParen))]
    (p/with-use [[_use0] (result/try* result)]
      (let [[types end tokens] _use0
            span (->Span start end)]
        (p/->Ok [(->TupleType span types) tokens])))))

(defn- fn-type
  "fn_type(start: Int, tokens: List(#(Token, Position))) -> Result(#(Type, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2193"}
  [start tokens]
  (let [result (comma-delimited (list) tokens type- (t/->RightParen))]
    (p/with-use [[_use0] (result/try* result)]
      (let [[parameters _ tokens] _use0]
        (p/with-use [[_ tokens] (expect (t/->RightArrow) tokens)
                     [_use0] (result/try* (type- tokens))]
          (let [[return tokens] _use0
                span (->Span start (:end (:location return)))]
            (p/->Ok [(->FunctionType span parameters return) tokens])))))))

(defn- type-
  "type_(tokens: List(#(Token, Position))) -> Result(#(Type, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2139"}
  [tokens]
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

(defn- optional-return-annotation
  "optional_return_annotation(end: Int, tokens: List(#(Token, Position))) -> Result(#(Option(Type), Int, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:802"}
  [end tokens]
  (if (and (seq tokens) (instance? glexer.token.RightArrow (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/try* (type- tokens))]
        (let [[return-type tokens] _use0]
          (p/->Ok [(option/->Some return-type) (:end (:location return-type)) tokens]))))
    (p/->Ok [(option/->None) end tokens])))

(defn- optional-type-annotation
  "optional_type_annotation(tokens: List(#(Token, Position))) -> Result(#(Option(Type), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2026"}
  [tokens]
  (if (and (seq tokens) (instance? glexer.token.Colon (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/map (type- tokens))]
        (let [[annotation tokens] _use0]
          [(option/->Some annotation) tokens])))
    (p/->Ok [(option/->None) tokens])))

(defn- fn-parameter
  "fn_parameter(tokens: List(#(Token, Position))) -> Result(#(FnParameter, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1954"}
  [tokens]
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

(defn- use-pattern
  "use_pattern(tokens: List(#(Token, Position))) -> Result(#(UsePattern, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:873"}
  [tokens]
  (p/with-use [[_use0] (result/try* (pattern tokens))]
    (let [[pattern tokens] _use0]
      (p/with-use [[_use0] (result/try* (optional-type-annotation tokens))]
        (let [[annotation tokens] _use0]
          (p/->Ok [(->UsePattern pattern annotation) tokens]))))))

(declare fn-capture call after-expression todo-panic optional-clause-guard case-clause case-clauses case-subjects case- fn- record-update-field record-update expression-unit expression-loop expression assert- use- assignment statement statements)

(defn- fn-capture
  "fn_capture(label: Option(String), function: Expression, before: List(Field(Expression)), after: List(Field(Expression)), tokens: List(#(Token, Position))) -> Result(#(Expression, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1700"}
  [label function before after tokens]
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

(defn- call
  "call(arguments: List(Field(Expression)), function: Expression, tokens: List(#(Token, Position))) -> Result(#(Expression, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1621"}
  [arguments function tokens]
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

(defn- after-expression
  "after_expression(parsed: Expression, tokens: List(#(Token, Position))) -> Result(#(Expression, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1586"}
  [parsed tokens]
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

(defn- todo-panic
  "todo_panic(tokens: List(#(Token, Position)), constructor: fn(Span, Option(Expression)) -> Expression, start: Int, keyword_name: String) -> Result(#(Option(Expression), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1352"}
  [tokens constructor start ^java.lang.String keyword-name]
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

(defn- optional-clause-guard
  "optional_clause_guard(tokens: List(#(Token, Position))) -> Result(#(Option(Expression), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1830"}
  [tokens]
  (if (and (seq tokens) (instance? glexer.token.If (nth (first tokens) 0)))
    (let [tokens (rest tokens)]
      (p/with-use [[_use0] (result/try* (expression tokens))]
        (let [[expression tokens] _use0]
          (p/->Ok [(option/->Some expression) tokens]))))
    (p/->Ok [(option/->None) tokens])))

(defn- case-clause
  "case_clause(tokens: List(#(Token, Position))) -> Result(#(Clause, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1820"}
  [tokens]
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

(defn- case-clauses
  "case_clauses(clauses: List(Clause), tokens: List(#(Token, Position))) -> Result(#(List(Clause), List(#(Token, Position)), Int), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1807"}
  [clauses tokens]
  (p/with-use [[_use0] (result/try* (case-clause tokens))]
    (let [[clause tokens] _use0
          clauses (list* clause clauses)]
      (if (and (seq tokens) (instance? glexer.token.RightBrace (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
        (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
          (p/->Ok [(list/reverse clauses) tokens (+' end 1)]))
        (case-clauses clauses tokens)))))

(defn- case-subjects
  "case_subjects(subjects: List(Expression), tokens: List(#(Token, Position))) -> Result(#(List(Expression), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1795"}
  [subjects tokens]
  (p/with-use [[_use0] (result/try* (expression tokens))]
    (let [[subject tokens] _use0
          subjects (list* subject subjects)]
      (if (and (seq tokens) (instance? glexer.token.Comma (nth (first tokens) 0)))
        (let [tokens (rest tokens)]
          (case-subjects subjects tokens))
        (p/->Ok [(list/reverse subjects) tokens])))))

(defn- case-
  "case_(tokens: List(#(Token, Position)), start: Int) -> Result(#(Option(Expression), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1785"}
  [tokens start]
  (p/with-use [[_use0] (result/try* (case-subjects (list) tokens))]
    (let [[subjects tokens] _use0]
      (p/with-use [[_ tokens] (expect (t/->LeftBrace) tokens)
                   [_use0] (result/try* (case-clauses (list) tokens))]
        (let [[clauses tokens end] _use0]
          (p/->Ok [(option/->Some (->Case (->Span start end) subjects clauses)) tokens]))))))

(defn- fn-
  "fn_(tokens: List(#(Token, Position)), start: Int) -> Result(#(Option(Expression), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1857"}
  [tokens start]
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

(defn- record-update-field
  "record_update_field(tokens: List(#(Token, Position))) -> Result(#(RecordUpdateField(Expression), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1765"}
  [tokens]
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

(defn- record-update
  "record_update(module: Option(String), constructor: String, tokens: List(#(Token, Position)), start: Int) -> Result(#(Option(Expression), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1739"}
  [module ^java.lang.String constructor tokens start]
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

(defn- expression-unit
  "expression_unit(tokens: List(#(Token, Position)), context: ParseExpressionUnitContext) -> Result(#(Option(Expression), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1218"}
  [tokens context]
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

(defn- expression-loop
  "expression_loop(tokens: List(#(Token, Position)), operators: List(BinaryOperator), values: List(Expression), context: ParseExpressionUnitContext) -> Result(#(Expression, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1139"}
  [tokens operators values context]
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

(defn- expression
  "expression(tokens: List(#(Token, Position))) -> Result(#(Expression, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1089"}
  [tokens]
  (expression-loop tokens (list) (list) (->RegularExpressionUnit)))

(defn- assert-
  "assert_(tokens: List(#(Token, Position)), start: Int) -> Result(#(Statement, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:843"}
  [tokens start]
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

(defn- use-
  "use_(tokens: List(#(Token, Position)), start: Int) -> Result(#(Statement, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:862"}
  [tokens start]
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

(defn- assignment
  "assignment(kind: AssignmentKind, tokens: List(#(Token, Position)), start: Int) -> Result(#(Statement, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:881"}
  [kind tokens start]
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

(defn- statement
  "statement(tokens: List(#(Token, Position))) -> Result(#(Statement, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:829"}
  [tokens]
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

(defn- statements
  "statements(acc: List(Statement), tokens: List(#(Token, Position))) -> Result(#(List(Statement), Int, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:815"}
  [acc tokens]
  (if (and (seq tokens) (instance? glexer.token.RightBrace (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (p/->Ok [(list/reverse acc) (+' end 1) tokens]))
    (p/with-use [[_use0] (result/try* (statement tokens))]
      (let [[statement tokens] _use0]
        (statements (list* statement acc) tokens)))))

(defn- function-parameter
  "function_parameter(tokens: List(#(Token, Position))) -> Result(#(FunctionParameter, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1970"}
  [tokens]
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

(defn- function-definition
  "function_definition(module: Module, attributes: List(Attribute), publicity: Publicity, name: String, start: Int, tokens: List(#(Token, Position))) -> Result(#(Module, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:771"}
  [module attributes publicity ^java.lang.String name start tokens]
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

(defn- push-constant
  "push_constant(module: Module, attributes: List(Attribute), constant: Constant) -> Module"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:369"}
  [module attributes constant]
  (->Module (:imports module) (:custom-types module) (:type-aliases module) (list* (->Definition (list/reverse attributes) constant) (:constants module)) (:functions module)))

(defn- expect-name
  "expect_name(tokens: List(#(Token, Position)), next: fn(String, List(#(Token, Position))) -> Result(a, Error)) -> Result(a, Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:443"}
  [tokens next]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
      (next name tokens))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))))

(defn- const-definition
  "const_definition(module: Module, attributes: List(Attribute), publicity: Publicity, tokens: List(#(Token, Position)), start: Int) -> Result(#(Module, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:1996"}
  [module attributes publicity tokens start]
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

(defn- push-custom-type
  "push_custom_type(module: Module, attributes: List(Attribute), custom_type: CustomType) -> Module"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:391"}
  [module attributes custom-type]
  (let [custom-type (->CustomType (:location custom-type) (:name custom-type) (:publicity custom-type) (:opaque- custom-type) (:parameters custom-type) (list/reverse (:variants custom-type)))]
    (->Module (:imports module) (list* (->Definition (list/reverse attributes) custom-type) (:custom-types module)) (:type-aliases module) (:constants module) (:functions module))))

(defn- push-variant
  "push_variant(custom_type: CustomType, variant: Variant) -> CustomType"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:415"}
  [custom-type variant]
  (->CustomType (:location custom-type) (:name custom-type) (:publicity custom-type) (:opaque- custom-type) (:parameters custom-type) (list* variant (:variants custom-type))))

(defn- variant-field
  "variant_field(tokens: List(#(Token, Position))) -> Result(#(VariantField, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2271"}
  [tokens]
  (if (and (<= 2 (count tokens)) (instance? glexer.token.Name (nth (first tokens) 0)) (instance? glexer.token.Colon (nth (nth tokens 1) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (nthrest tokens 2)]
      (p/with-use [[_use0] (result/try* (type- tokens))]
        (let [[type- tokens] _use0]
          (p/->Ok [(->LabelledVariantField type- name) tokens]))))
    (let [tokens tokens]
      (p/with-use [[_use0] (result/try* (type- tokens))]
        (let [[type- tokens] _use0]
          (p/->Ok [(->UnlabelledVariantField type-) tokens]))))))

(defn- expect-upper-name
  "expect_upper_name(tokens: List(#(Token, Position)), next: fn(String, Int, List(#(Token, Position))) -> Result(a, Error)) -> Result(a, Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:432"}
  [tokens next]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.UpperName (nth (first tokens) 0)) (instance? glexer.Position (nth (first tokens) 1)))
    (let [name (:value (nth (first tokens) 0)) end (:byte-offset (nth (first tokens) 1)) tokens (rest tokens)]
      (next name end tokens))

    (seq tokens)
    (let [other (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken other position)))))

(defn- attribute
  "attribute(tokens: List(#(Token, Position))) -> Result(#(Attribute, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:473"}
  [tokens]
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

(defn- attributes
  "attributes(accumulated_attributes: List(Attribute), tokens: List(#(Token, Position))) -> Result(#(List(Attribute), List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2255"}
  [accumulated-attributes tokens]
  (if (and (seq tokens) (instance? glexer.token.At (nth (first tokens) 0)))
    (let [tokens (rest tokens) subject (attribute tokens)]
      (if (instance? gleam.prelude.Error subject)
        (let [error (:value subject)]
          (p/->Error error))
        (let [attribute (nth (:value subject) 0) tokens (nth (:value subject) 1)]
          (recur (list* attribute accumulated-attributes) tokens))))
    (p/->Ok [(list/reverse accumulated-attributes) tokens])))

(defn- until
  "until(limit: Token, acc: a, tokens: List(#(Token, Position)), callback: fn(a, List(#(Token, Position))) -> Result(#(a, List(#(Token, Position))), Error)) -> Result(#(a, Int, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:454"}
  [limit acc tokens callback]
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

(defn- variants
  "variants(ct: CustomType, tokens: List(#(Token, Position))) -> Result(#(CustomType, Int, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2237"}
  [ct tokens]
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

(defn- custom-type
  "custom_type(module: Module, attributes: List(Attribute), name: String, parameters: List(String), publicity: Publicity, opaque_: Bool, tokens: List(#(Token, Position)), start: Int) -> Result(#(Module, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2209"}
  [module attributes ^java.lang.String name parameters publicity opaque- tokens start]
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

(defn- push-type-alias
  "push_type_alias(module: Module, attributes: List(Attribute), type_alias: TypeAlias) -> Module"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:404"}
  [module attributes type-alias]
  (->Module (:imports module) (:custom-types module) (list* (->Definition (list/reverse attributes) type-alias) (:type-aliases module)) (:constants module) (:functions module)))

(defn- type-alias
  "type_alias(module: Module, attributes: List(Attribute), name: String, parameters: List(String), publicity: Publicity, start: Int, tokens: List(#(Token, Position))) -> Result(#(Module, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2123"}
  [module attributes ^java.lang.String name parameters publicity start tokens]
  (p/with-use [[_use0] (result/try* (type- tokens))]
    (let [[type- tokens] _use0
          span (->Span start (:end (:location type-)))
          alias (->TypeAlias span name publicity parameters type-)
          module (push-type-alias module attributes alias)]
      (p/->Ok [module tokens]))))

(defn- name
  "name(tokens: List(#(Token, Position))) -> Result(#(String, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2229"}
  [tokens]
  (cond
    (empty? tokens)
    (p/->Error (->UnexpectedEndOfInput))

    (and (seq tokens) (instance? glexer.token.Name (nth (first tokens) 0)))
    (let [name (:value (nth (first tokens) 0)) tokens (rest tokens)]
      (p/->Ok [name tokens]))

    (seq tokens)
    (let [token (nth (first tokens) 0) position (nth (first tokens) 1)]
      (p/->Error (->UnexpectedToken token position)))))

(defn- type-definition
  "type_definition(module: Module, attributes: List(Attribute), publicity: Publicity, opaque_: Bool, tokens: List(#(Token, Position)), start: Int) -> Result(#(Module, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:2074"}
  [module attributes publicity opaque- tokens start]
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

(defn- optional-module-alias
  "optional_module_alias(tokens: List(#(Token, Position)), end: Int) -> #(Option(AssignmentName), Int, List(#(Token, Position)))"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:603"}
  [tokens end]
  (cond
    (and (<= 2 (count tokens)) (instance? glexer.token.As (nth (first tokens) 0)) (instance? glexer.token.Name (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [alias (:value (nth (nth tokens 1) 0)) alias-start (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
      [(option/->Some (->Named alias)) (string-offset alias-start alias) tokens])

    (and (<= 2 (count tokens)) (instance? glexer.token.As (nth (first tokens) 0)) (instance? glexer.token.DiscardName (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)))
    (let [alias (:value (nth (nth tokens 1) 0)) alias-start (:byte-offset (nth (nth tokens 1) 1)) tokens (nthrest tokens 2)]
      [(option/->Some (->Discarded alias)) (+' (string-offset alias-start alias) 1) tokens])

    :else
    [(option/->None) end tokens]))

(defn- unqualified-imports
  "unqualified_imports(types: List(UnqualifiedImport), values: List(UnqualifiedImport), tokens: List(#(Token, Position))) -> Result(UnqualifiedImports, Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:642"}
  [types values tokens]
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

(defn- optional-unqualified-imports
  "optional_unqualified_imports(tokens: List(#(Token, Position)), end: Int) -> Result(UnqualifiedImports, Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:631"}
  [tokens end]
  (if (and (<= 2 (count tokens)) (instance? glexer.token.Dot (nth (first tokens) 0)) (instance? glexer.token.LeftBrace (nth (nth tokens 1) 0)))
    (let [tokens (nthrest tokens 2)]
      (unqualified-imports (list) (list) tokens))
    (p/->Ok (->UnqualifiedImports (list) (list) end tokens))))

(defn- module-name
  "module_name(name: String, end: Int, tokens: List(#(Token, Position))) -> Result(#(String, Int, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:580"}
  [^java.lang.String name end tokens]
  (cond
    (and (<= 2 (count tokens)) (instance? glexer.token.Slash (nth (first tokens) 0)) (instance? glexer.token.Name (nth (nth tokens 1) 0)) (instance? glexer.Position (nth (nth tokens 1) 1)) (not= name ""))
    (let [i (:byte-offset (nth (nth tokens 1) 1)) s (:value (nth (nth tokens 1) 0)) tokens (nthrest tokens 2) end (+' i (string/byte-size s))]
      (recur (str name "/" s) end tokens))

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

(defn- import-statement
  "import_statement(module: Module, attributes: List(Attribute), tokens: List(#(Token, Position)), start: Int) -> Result(#(Module, List(#(Token, Position))), Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:562"}
  [module attributes tokens start]
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

(defn- slurp
  "slurp(module: Module, attributes: List(Attribute), tokens: List(#(Token, Position))) -> Result(Module, Error)"
  {:gleam/src "bench/build/packages/glance/src/glance.gleam:491"}
  [module attributes tokens]
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
  "module(src: String) -> Result(Module, Error)"
  {:malli/schema [:=> [:cat :string]
                      (p/result-of (Module-schema) (Error-schema))]
   :gleam/src "bench/build/packages/glance/src/glance.gleam:361"}
  [^java.lang.String src]
  (-> (glexer/new* src)
      glexer/discard-comments
      glexer/discard-whitespace
      glexer/lex
      ((fn [_capture]
        (slurp (->Module (list) (list) (list) (list) (list)) (list) _capture)))))
