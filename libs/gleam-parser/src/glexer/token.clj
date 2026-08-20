(ns glexer.token)

;; type Token
(defrecord Name [value])
(defn Name? [v] (instance? Name v))
(defrecord UpperName [value])
(defn UpperName? [v] (instance? UpperName v))
(defrecord DiscardName [value])
(defn DiscardName? [v] (instance? DiscardName v))
(defrecord Int [value])
(defn Int? [v] (instance? Int v))
(ns-unmap *ns* 'Float)
(defrecord Float [value])
(defn Float? [v] (instance? Float v))
(ns-unmap *ns* 'String)
(defrecord String [value])
(defn String? [v] (instance? String v))
(defrecord CommentDoc [value])
(defn CommentDoc? [v] (instance? CommentDoc v))
(defrecord CommentNormal [value])
(defn CommentNormal? [v] (instance? CommentNormal v))
(defrecord CommentModule [value])
(defn CommentModule? [v] (instance? CommentModule v))
(defrecord As [])
(defn As? [v] (instance? As v))
(defrecord Assert [])
(defn Assert? [v] (instance? Assert v))
(defrecord Auto [])
(defn Auto? [v] (instance? Auto v))
(defrecord Case [])
(defn Case? [v] (instance? Case v))
(defrecord Const [])
(defn Const? [v] (instance? Const v))
(defrecord Delegate [])
(defn Delegate? [v] (instance? Delegate v))
(defrecord Derive [])
(defn Derive? [v] (instance? Derive v))
(defrecord Echo [])
(defn Echo? [v] (instance? Echo v))
(defrecord Else [])
(defn Else? [v] (instance? Else v))
(defrecord Fn [])
(defn Fn? [v] (instance? Fn v))
(defrecord If [])
(defn If? [v] (instance? If v))
(defrecord Implement [])
(defn Implement? [v] (instance? Implement v))
(defrecord Import [])
(defn Import? [v] (instance? Import v))
(defrecord Let [])
(defn Let? [v] (instance? Let v))
(defrecord Macro [])
(defn Macro? [v] (instance? Macro v))
(defrecord Opaque [])
(defn Opaque? [v] (instance? Opaque v))
(defrecord Panic [])
(defn Panic? [v] (instance? Panic v))
(defrecord Pub [])
(defn Pub? [v] (instance? Pub v))
(defrecord Test [])
(defn Test? [v] (instance? Test v))
(defrecord Todo [])
(defn Todo? [v] (instance? Todo v))
(defrecord Type [])
(defn Type? [v] (instance? Type v))
(defrecord Use [])
(defn Use? [v] (instance? Use v))
(defrecord LeftParen [])
(defn LeftParen? [v] (instance? LeftParen v))
(defrecord RightParen [])
(defn RightParen? [v] (instance? RightParen v))
(defrecord LeftBrace [])
(defn LeftBrace? [v] (instance? LeftBrace v))
(defrecord RightBrace [])
(defn RightBrace? [v] (instance? RightBrace v))
(defrecord LeftSquare [])
(defn LeftSquare? [v] (instance? LeftSquare v))
(defrecord RightSquare [])
(defn RightSquare? [v] (instance? RightSquare v))
(defrecord Plus [])
(defn Plus? [v] (instance? Plus v))
(defrecord Minus [])
(defn Minus? [v] (instance? Minus v))
(defrecord Star [])
(defn Star? [v] (instance? Star v))
(defrecord Slash [])
(defn Slash? [v] (instance? Slash v))
(defrecord Less [])
(defn Less? [v] (instance? Less v))
(defrecord Greater [])
(defn Greater? [v] (instance? Greater v))
(defrecord LessEqual [])
(defn LessEqual? [v] (instance? LessEqual v))
(defrecord GreaterEqual [])
(defn GreaterEqual? [v] (instance? GreaterEqual v))
(defrecord Percent [])
(defn Percent? [v] (instance? Percent v))
(defrecord PlusDot [])
(defn PlusDot? [v] (instance? PlusDot v))
(defrecord MinusDot [])
(defn MinusDot? [v] (instance? MinusDot v))
(defrecord StarDot [])
(defn StarDot? [v] (instance? StarDot v))
(defrecord SlashDot [])
(defn SlashDot? [v] (instance? SlashDot v))
(defrecord LessDot [])
(defn LessDot? [v] (instance? LessDot v))
(defrecord GreaterDot [])
(defn GreaterDot? [v] (instance? GreaterDot v))
(defrecord LessEqualDot [])
(defn LessEqualDot? [v] (instance? LessEqualDot v))
(defrecord GreaterEqualDot [])
(defn GreaterEqualDot? [v] (instance? GreaterEqualDot v))
(defrecord LessGreater [])
(defn LessGreater? [v] (instance? LessGreater v))
(defrecord At [])
(defn At? [v] (instance? At v))
(defrecord Colon [])
(defn Colon? [v] (instance? Colon v))
(defrecord Comma [])
(defn Comma? [v] (instance? Comma v))
(defrecord Hash [])
(defn Hash? [v] (instance? Hash v))
(defrecord Bang [])
(defn Bang? [v] (instance? Bang v))
(defrecord Equal [])
(defn Equal? [v] (instance? Equal v))
(defrecord EqualEqual [])
(defn EqualEqual? [v] (instance? EqualEqual v))
(defrecord NotEqual [])
(defn NotEqual? [v] (instance? NotEqual v))
(defrecord VBar [])
(defn VBar? [v] (instance? VBar v))
(defrecord VBarVBar [])
(defn VBarVBar? [v] (instance? VBarVBar v))
(defrecord AmperAmper [])
(defn AmperAmper? [v] (instance? AmperAmper v))
(defrecord LessLess [])
(defn LessLess? [v] (instance? LessLess v))
(defrecord GreaterGreater [])
(defn GreaterGreater? [v] (instance? GreaterGreater v))
(defrecord Pipe [])
(defn Pipe? [v] (instance? Pipe v))
(defrecord Dot [])
(defn Dot? [v] (instance? Dot v))
(defrecord DotDot [])
(defn DotDot? [v] (instance? DotDot v))
(defrecord LeftArrow [])
(defn LeftArrow? [v] (instance? LeftArrow v))
(defrecord RightArrow [])
(defn RightArrow? [v] (instance? RightArrow v))
(defrecord EndOfFile [])
(defn EndOfFile? [v] (instance? EndOfFile v))
(defrecord Space [value])
(defn Space? [v] (instance? Space v))
(defrecord UnterminatedString [value])
(defn UnterminatedString? [v] (instance? UnterminatedString v))
(defrecord UnexpectedGrapheme [value])
(defn UnexpectedGrapheme? [v] (instance? UnexpectedGrapheme v))

(defn to-source
  "Turn a token back into its Gleam source representation."
  {:malli/schema [:=> [:cat [:or [:fn Name?] [:fn UpperName?] [:fn DiscardName?] [:fn Int?] [:fn Float?] [:fn String?] [:fn CommentDoc?] [:fn CommentNormal?] [:fn CommentModule?] [:fn As?] [:fn Assert?] [:fn Auto?] [:fn Case?] [:fn Const?] [:fn Delegate?] [:fn Derive?] [:fn Echo?] [:fn Else?] [:fn Fn?] [:fn If?] [:fn Implement?] [:fn Import?] [:fn Let?] [:fn Macro?] [:fn Opaque?] [:fn Panic?] [:fn Pub?] [:fn Test?] [:fn Todo?] [:fn Type?] [:fn Use?] [:fn LeftParen?] [:fn RightParen?] [:fn LeftBrace?] [:fn RightBrace?] [:fn LeftSquare?] [:fn RightSquare?] [:fn Plus?] [:fn Minus?] [:fn Star?] [:fn Slash?] [:fn Less?] [:fn Greater?] [:fn LessEqual?] [:fn GreaterEqual?] [:fn Percent?] [:fn PlusDot?] [:fn MinusDot?] [:fn StarDot?] [:fn SlashDot?] [:fn LessDot?] [:fn GreaterDot?] [:fn LessEqualDot?] [:fn GreaterEqualDot?] [:fn LessGreater?] [:fn At?] [:fn Colon?] [:fn Comma?] [:fn Hash?] [:fn Bang?] [:fn Equal?] [:fn EqualEqual?] [:fn NotEqual?] [:fn VBar?] [:fn VBarVBar?] [:fn AmperAmper?] [:fn LessLess?] [:fn GreaterGreater?] [:fn Pipe?] [:fn Dot?] [:fn DotDot?] [:fn LeftArrow?] [:fn RightArrow?] [:fn EndOfFile?] [:fn Space?] [:fn UnterminatedString?] [:fn UnexpectedGrapheme?]]]
                      :string]}
  [tok]
  (cond
    (instance? Name tok) (let [str' (:value tok)]
                           str')
    (instance? UpperName tok) (let [str' (:value tok)]
                                str')
    (instance? Int tok) (let [str' (:value tok)]
                          str')
    (instance? Float tok) (let [str' (:value tok)]
                            str')
    (instance? DiscardName tok) (let [str' (:value tok)]
                                  (str "_" str'))
    (instance? String tok) (let [str' (:value tok)]
                             (str (str "\"" str') "\""))
    (instance? CommentDoc tok) (let [str' (:value tok)]
                                 (str "///" str'))
    (instance? CommentNormal tok) (let [str' (:value tok)]
                                    (str "//" str'))
    (instance? CommentModule tok) (let [str' (:value tok)]
                                    (str "////" str'))
    (instance? As tok) "as"
    (instance? Assert tok) "assert"
    (instance? Auto tok) "auto"
    (instance? Case tok) "case"
    (instance? Const tok) "const"
    (instance? Delegate tok) "delegate"
    (instance? Derive tok) "derive"
    (instance? Echo tok) "echo"
    (instance? Else tok) "else"
    (instance? Fn tok) "fn"
    (instance? If tok) "if"
    (instance? Implement tok) "implement"
    (instance? Import tok) "import"
    (instance? Let tok) "let"
    (instance? Macro tok) "macro"
    (instance? Opaque tok) "opaque"
    (instance? Panic tok) "panic"
    (instance? Pub tok) "pub"
    (instance? Test tok) "test"
    (instance? Todo tok) "todo"
    (instance? Type tok) "type"
    (instance? Use tok) "use"
    (instance? LeftParen tok) "("
    (instance? RightParen tok) ")"
    (instance? LeftBrace tok) "{"
    (instance? RightBrace tok) "}"
    (instance? LeftSquare tok) "["
    (instance? RightSquare tok) "]"
    (instance? Plus tok) "+"
    (instance? Minus tok) "-"
    (instance? Star tok) "*"
    (instance? Slash tok) "/"
    (instance? Less tok) "<"
    (instance? Greater tok) ">"
    (instance? LessEqual tok) "<="
    (instance? GreaterEqual tok) ">="
    (instance? Percent tok) "%"
    (instance? PlusDot tok) "+."
    (instance? MinusDot tok) "-."
    (instance? StarDot tok) "*."
    (instance? SlashDot tok) "/."
    (instance? LessDot tok) "<."
    (instance? GreaterDot tok) ">."
    (instance? LessEqualDot tok) "<=."
    (instance? GreaterEqualDot tok) ">=."
    (instance? LessGreater tok) "<>"
    (instance? At tok) "@"
    (instance? Colon tok) ":"
    (instance? Comma tok) ","
    (instance? Hash tok) "#"
    (instance? Bang tok) "!"
    (instance? Equal tok) "="
    (instance? EqualEqual tok) "=="
    (instance? NotEqual tok) "!="
    (instance? VBar tok) "|"
    (instance? VBarVBar tok) "||"
    (instance? AmperAmper tok) "&&"
    (instance? LessLess tok) "<<"
    (instance? GreaterGreater tok) ">>"
    (instance? Pipe tok) "|>"
    (instance? Dot tok) "."
    (instance? DotDot tok) ".."
    (instance? LeftArrow tok) "<-"
    (instance? RightArrow tok) "->"
    (instance? EndOfFile tok) ""
    (instance? Space tok) (let [str' (:value tok)]
                            str')
    (instance? UnterminatedString tok) (let [str' (:value tok)]
                                         (str "\"" str'))
    (instance? UnexpectedGrapheme tok) (let [str' (:value tok)]
                                         str')))
