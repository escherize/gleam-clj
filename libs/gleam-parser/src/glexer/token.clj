(ns glexer.token)

;; type Token
(defprotocol IToken)
(defrecord Name [value] IToken)
(defn Name? "True if `v` is a Name value." [v] (instance? Name v))
(defrecord UpperName [value] IToken)
(defn UpperName? "True if `v` is a UpperName value." [v] (instance? UpperName v))
(defrecord DiscardName [value] IToken)
(defn DiscardName? "True if `v` is a DiscardName value." [v] (instance? DiscardName v))
(defrecord Int [value] IToken)
(defn Int? "True if `v` is a Int value." [v] (instance? Int v))
(ns-unmap *ns* 'Float)
(defrecord Float [value] IToken)
(defn Float? "True if `v` is a Float value." [v] (instance? Float v))
(ns-unmap *ns* 'String)
(defrecord String [value] IToken)
(defn String? "True if `v` is a String value." [v] (instance? String v))
(defrecord CommentDoc [value] IToken)
(defn CommentDoc? "True if `v` is a CommentDoc value." [v] (instance? CommentDoc v))
(defrecord CommentNormal [value] IToken)
(defn CommentNormal? "True if `v` is a CommentNormal value." [v] (instance? CommentNormal v))
(defrecord CommentModule [value] IToken)
(defn CommentModule? "True if `v` is a CommentModule value." [v] (instance? CommentModule v))
(defrecord As [] IToken)
(defn As? "True if `v` is a As value." [v] (instance? As v))
(defrecord Assert [] IToken)
(defn Assert? "True if `v` is a Assert value." [v] (instance? Assert v))
(defrecord Auto [] IToken)
(defn Auto? "True if `v` is a Auto value." [v] (instance? Auto v))
(defrecord Case [] IToken)
(defn Case? "True if `v` is a Case value." [v] (instance? Case v))
(defrecord Const [] IToken)
(defn Const? "True if `v` is a Const value." [v] (instance? Const v))
(defrecord Delegate [] IToken)
(defn Delegate? "True if `v` is a Delegate value." [v] (instance? Delegate v))
(defrecord Derive [] IToken)
(defn Derive? "True if `v` is a Derive value." [v] (instance? Derive v))
(defrecord Echo [] IToken)
(defn Echo? "True if `v` is a Echo value." [v] (instance? Echo v))
(defrecord Else [] IToken)
(defn Else? "True if `v` is a Else value." [v] (instance? Else v))
(defrecord Fn [] IToken)
(defn Fn? "True if `v` is a Fn value." [v] (instance? Fn v))
(defrecord If [] IToken)
(defn If? "True if `v` is a If value." [v] (instance? If v))
(defrecord Implement [] IToken)
(defn Implement? "True if `v` is a Implement value." [v] (instance? Implement v))
(defrecord Import [] IToken)
(defn Import? "True if `v` is a Import value." [v] (instance? Import v))
(defrecord Let [] IToken)
(defn Let? "True if `v` is a Let value." [v] (instance? Let v))
(defrecord Macro [] IToken)
(defn Macro? "True if `v` is a Macro value." [v] (instance? Macro v))
(defrecord Opaque [] IToken)
(defn Opaque? "True if `v` is a Opaque value." [v] (instance? Opaque v))
(defrecord Panic [] IToken)
(defn Panic? "True if `v` is a Panic value." [v] (instance? Panic v))
(defrecord Pub [] IToken)
(defn Pub? "True if `v` is a Pub value." [v] (instance? Pub v))
(defrecord Test [] IToken)
(defn Test? "True if `v` is a Test value." [v] (instance? Test v))
(defrecord Todo [] IToken)
(defn Todo? "True if `v` is a Todo value." [v] (instance? Todo v))
(defrecord Type [] IToken)
(defn Type? "True if `v` is a Type value." [v] (instance? Type v))
(defrecord Use [] IToken)
(defn Use? "True if `v` is a Use value." [v] (instance? Use v))
(defrecord LeftParen [] IToken)
(defn LeftParen? "True if `v` is a LeftParen value." [v] (instance? LeftParen v))
(defrecord RightParen [] IToken)
(defn RightParen? "True if `v` is a RightParen value." [v] (instance? RightParen v))
(defrecord LeftBrace [] IToken)
(defn LeftBrace? "True if `v` is a LeftBrace value." [v] (instance? LeftBrace v))
(defrecord RightBrace [] IToken)
(defn RightBrace? "True if `v` is a RightBrace value." [v] (instance? RightBrace v))
(defrecord LeftSquare [] IToken)
(defn LeftSquare? "True if `v` is a LeftSquare value." [v] (instance? LeftSquare v))
(defrecord RightSquare [] IToken)
(defn RightSquare? "True if `v` is a RightSquare value." [v] (instance? RightSquare v))
(defrecord Plus [] IToken)
(defn Plus? "True if `v` is a Plus value." [v] (instance? Plus v))
(defrecord Minus [] IToken)
(defn Minus? "True if `v` is a Minus value." [v] (instance? Minus v))
(defrecord Star [] IToken)
(defn Star? "True if `v` is a Star value." [v] (instance? Star v))
(defrecord Slash [] IToken)
(defn Slash? "True if `v` is a Slash value." [v] (instance? Slash v))
(defrecord Less [] IToken)
(defn Less? "True if `v` is a Less value." [v] (instance? Less v))
(defrecord Greater [] IToken)
(defn Greater? "True if `v` is a Greater value." [v] (instance? Greater v))
(defrecord LessEqual [] IToken)
(defn LessEqual? "True if `v` is a LessEqual value." [v] (instance? LessEqual v))
(defrecord GreaterEqual [] IToken)
(defn GreaterEqual? "True if `v` is a GreaterEqual value." [v] (instance? GreaterEqual v))
(defrecord Percent [] IToken)
(defn Percent? "True if `v` is a Percent value." [v] (instance? Percent v))
(defrecord PlusDot [] IToken)
(defn PlusDot? "True if `v` is a PlusDot value." [v] (instance? PlusDot v))
(defrecord MinusDot [] IToken)
(defn MinusDot? "True if `v` is a MinusDot value." [v] (instance? MinusDot v))
(defrecord StarDot [] IToken)
(defn StarDot? "True if `v` is a StarDot value." [v] (instance? StarDot v))
(defrecord SlashDot [] IToken)
(defn SlashDot? "True if `v` is a SlashDot value." [v] (instance? SlashDot v))
(defrecord LessDot [] IToken)
(defn LessDot? "True if `v` is a LessDot value." [v] (instance? LessDot v))
(defrecord GreaterDot [] IToken)
(defn GreaterDot? "True if `v` is a GreaterDot value." [v] (instance? GreaterDot v))
(defrecord LessEqualDot [] IToken)
(defn LessEqualDot? "True if `v` is a LessEqualDot value." [v] (instance? LessEqualDot v))
(defrecord GreaterEqualDot [] IToken)
(defn GreaterEqualDot? "True if `v` is a GreaterEqualDot value." [v] (instance? GreaterEqualDot v))
(defrecord LessGreater [] IToken)
(defn LessGreater? "True if `v` is a LessGreater value." [v] (instance? LessGreater v))
(defrecord At [] IToken)
(defn At? "True if `v` is a At value." [v] (instance? At v))
(defrecord Colon [] IToken)
(defn Colon? "True if `v` is a Colon value." [v] (instance? Colon v))
(defrecord Comma [] IToken)
(defn Comma? "True if `v` is a Comma value." [v] (instance? Comma v))
(defrecord Hash [] IToken)
(defn Hash? "True if `v` is a Hash value." [v] (instance? Hash v))
(defrecord Bang [] IToken)
(defn Bang? "True if `v` is a Bang value." [v] (instance? Bang v))
(defrecord Equal [] IToken)
(defn Equal? "True if `v` is a Equal value." [v] (instance? Equal v))
(defrecord EqualEqual [] IToken)
(defn EqualEqual? "True if `v` is a EqualEqual value." [v] (instance? EqualEqual v))
(defrecord NotEqual [] IToken)
(defn NotEqual? "True if `v` is a NotEqual value." [v] (instance? NotEqual v))
(defrecord VBar [] IToken)
(defn VBar? "True if `v` is a VBar value." [v] (instance? VBar v))
(defrecord VBarVBar [] IToken)
(defn VBarVBar? "True if `v` is a VBarVBar value." [v] (instance? VBarVBar v))
(defrecord AmperAmper [] IToken)
(defn AmperAmper? "True if `v` is a AmperAmper value." [v] (instance? AmperAmper v))
(defrecord LessLess [] IToken)
(defn LessLess? "True if `v` is a LessLess value." [v] (instance? LessLess v))
(defrecord GreaterGreater [] IToken)
(defn GreaterGreater? "True if `v` is a GreaterGreater value." [v] (instance? GreaterGreater v))
(defrecord Pipe [] IToken)
(defn Pipe? "True if `v` is a Pipe value." [v] (instance? Pipe v))
(defrecord Dot [] IToken)
(defn Dot? "True if `v` is a Dot value." [v] (instance? Dot v))
(defrecord DotDot [] IToken)
(defn DotDot? "True if `v` is a DotDot value." [v] (instance? DotDot v))
(defrecord LeftArrow [] IToken)
(defn LeftArrow? "True if `v` is a LeftArrow value." [v] (instance? LeftArrow v))
(defrecord RightArrow [] IToken)
(defn RightArrow? "True if `v` is a RightArrow value." [v] (instance? RightArrow v))
(defrecord EndOfFile [] IToken)
(defn EndOfFile? "True if `v` is a EndOfFile value." [v] (instance? EndOfFile v))
(defrecord Space [value] IToken)
(defn Space? "True if `v` is a Space value." [v] (instance? Space v))
(defrecord UnterminatedString [value] IToken)
(defn UnterminatedString? "True if `v` is a UnterminatedString value." [v] (instance? UnterminatedString v))
(defrecord UnexpectedGrapheme [value] IToken)
(defn UnexpectedGrapheme? "True if `v` is a UnexpectedGrapheme value." [v] (instance? UnexpectedGrapheme v))
(defn Token? "True if `v` is any Token value." [v] (instance? glexer.token.IToken v))

(defn to-source
  "Turn a token back into its Gleam source representation."
  {:malli/schema [:=> [:cat [:fn Token?]] :string]}
  [tok]
  (cond
    (instance? Name tok)
    (let [str' (:value tok)]
      str')

    (instance? UpperName tok)
    (let [str' (:value tok)]
      str')

    (instance? Int tok)
    (let [str' (:value tok)]
      str')

    (instance? Float tok)
    (let [str' (:value tok)]
      str')

    (instance? DiscardName tok)
    (let [str' (:value tok)]
      (str "_" str'))

    (instance? String tok)
    (let [str' (:value tok)]
      (str (str "\"" str') "\""))

    (instance? CommentDoc tok)
    (let [str' (:value tok)]
      (str "///" str'))

    (instance? CommentNormal tok)
    (let [str' (:value tok)]
      (str "//" str'))

    (instance? CommentModule tok)
    (let [str' (:value tok)]
      (str "////" str'))

    (instance? As tok)
    "as"

    (instance? Assert tok)
    "assert"

    (instance? Auto tok)
    "auto"

    (instance? Case tok)
    "case"

    (instance? Const tok)
    "const"

    (instance? Delegate tok)
    "delegate"

    (instance? Derive tok)
    "derive"

    (instance? Echo tok)
    "echo"

    (instance? Else tok)
    "else"

    (instance? Fn tok)
    "fn"

    (instance? If tok)
    "if"

    (instance? Implement tok)
    "implement"

    (instance? Import tok)
    "import"

    (instance? Let tok)
    "let"

    (instance? Macro tok)
    "macro"

    (instance? Opaque tok)
    "opaque"

    (instance? Panic tok)
    "panic"

    (instance? Pub tok)
    "pub"

    (instance? Test tok)
    "test"

    (instance? Todo tok)
    "todo"

    (instance? Type tok)
    "type"

    (instance? Use tok)
    "use"

    (instance? LeftParen tok)
    "("

    (instance? RightParen tok)
    ")"

    (instance? LeftBrace tok)
    "{"

    (instance? RightBrace tok)
    "}"

    (instance? LeftSquare tok)
    "["

    (instance? RightSquare tok)
    "]"

    (instance? Plus tok)
    "+"

    (instance? Minus tok)
    "-"

    (instance? Star tok)
    "*"

    (instance? Slash tok)
    "/"

    (instance? Less tok)
    "<"

    (instance? Greater tok)
    ">"

    (instance? LessEqual tok)
    "<="

    (instance? GreaterEqual tok)
    ">="

    (instance? Percent tok)
    "%"

    (instance? PlusDot tok)
    "+."

    (instance? MinusDot tok)
    "-."

    (instance? StarDot tok)
    "*."

    (instance? SlashDot tok)
    "/."

    (instance? LessDot tok)
    "<."

    (instance? GreaterDot tok)
    ">."

    (instance? LessEqualDot tok)
    "<=."

    (instance? GreaterEqualDot tok)
    ">=."

    (instance? LessGreater tok)
    "<>"

    (instance? At tok)
    "@"

    (instance? Colon tok)
    ":"

    (instance? Comma tok)
    ","

    (instance? Hash tok)
    "#"

    (instance? Bang tok)
    "!"

    (instance? Equal tok)
    "="

    (instance? EqualEqual tok)
    "=="

    (instance? NotEqual tok)
    "!="

    (instance? VBar tok)
    "|"

    (instance? VBarVBar tok)
    "||"

    (instance? AmperAmper tok)
    "&&"

    (instance? LessLess tok)
    "<<"

    (instance? GreaterGreater tok)
    ">>"

    (instance? Pipe tok)
    "|>"

    (instance? Dot tok)
    "."

    (instance? DotDot tok)
    ".."

    (instance? LeftArrow tok)
    "<-"

    (instance? RightArrow tok)
    "->"

    (instance? EndOfFile tok)
    ""

    (instance? Space tok)
    (let [str' (:value tok)]
      str')

    (instance? UnterminatedString tok)
    (let [str' (:value tok)]
      (str "\"" str'))

    (instance? UnexpectedGrapheme tok)
    (let [str' (:value tok)]
      str')))
