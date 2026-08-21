(ns gleam.uri
  "Utilities for working with URIs
  
  This module provides functions for working with URIs (for example, parsing
  URIs or encoding query strings). The functions in this module are implemented
  according to [RFC 3986](https://tools.ietf.org/html/rfc3986).
  
  Query encoding (Form encoding) is defined in the
  [W3C specification](https://www.w3.org/TR/html52/sec-forms.html#urlencoded-form-data)."
  (:refer-clojure :exclude [drop-last empty merge])
  (:require
   [gleam-ffi]
   [gleam.int :as int]
   [gleam.list :as list]
   [gleam.option :as option]
   [gleam.prelude :as p]
   [gleam.string :as string]))

;; type Uri
(defrecord Uri [scheme userinfo host port path query fragment])
(defn Uri? "True if `v` is a Uri value." [v] (instance? Uri v))

(def empty (->Uri (option/->None) (option/->None) (option/->None) (option/->None) "" (option/->None) (option/->None)))

(def pop-codeunit gleam-ffi/pop-codeunit)

(defn- parse-fragment [rest' pieces]
  (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (:port pieces) (:path pieces) (:query pieces) (option/->Some rest'))))

(def codeunit-slice gleam-ffi/codeunit-slice)

(defn- parse-query-with-question-mark-loop [original uri-string pieces size]
  (cond
    (and (.startsWith ^String uri-string "#") (= size 0))
    (let [rest' (subs uri-string 1)]
      (parse-fragment rest' pieces))

    (.startsWith ^String uri-string "#")
    (let [rest' (subs uri-string 1) query (codeunit-slice original 0 size) pieces (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (:port pieces) (:path pieces) (option/->Some query) (:fragment pieces))]
      (parse-fragment rest' pieces))

    (= uri-string "")
    (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (:port pieces) (:path pieces) (option/->Some original) (:fragment pieces)))

    :else
    (let [[_ rest'] (pop-codeunit uri-string)]
      (recur original rest' pieces (+' size 1)))))

(defn- parse-query-with-question-mark [uri-string pieces]
  (parse-query-with-question-mark-loop uri-string uri-string pieces 0))

(defn- parse-path-loop [original uri-string pieces size]
  (cond
    (.startsWith ^String uri-string "?")
    (let [rest' (subs uri-string 1) path (codeunit-slice original 0 size) pieces (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (:port pieces) path (:query pieces) (:fragment pieces))]
      (parse-query-with-question-mark rest' pieces))

    (.startsWith ^String uri-string "#")
    (let [rest' (subs uri-string 1) path (codeunit-slice original 0 size) pieces (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (:port pieces) path (:query pieces) (:fragment pieces))]
      (parse-fragment rest' pieces))

    (= uri-string "")
    (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (:port pieces) original (:query pieces) (:fragment pieces)))

    :else
    (let [[_ rest'] (pop-codeunit uri-string)]
      (recur original rest' pieces (+' size 1)))))

(defn- parse-path [uri-string pieces]
  (parse-path-loop uri-string uri-string pieces 0))

(defn- parse-port-loop [uri-string pieces port]
  (cond
    (.startsWith ^String uri-string "0")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (*' port 10)))

    (.startsWith ^String uri-string "1")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 1)))

    (.startsWith ^String uri-string "2")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 2)))

    (.startsWith ^String uri-string "3")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 3)))

    (.startsWith ^String uri-string "4")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 4)))

    (.startsWith ^String uri-string "5")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 5)))

    (.startsWith ^String uri-string "6")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 6)))

    (.startsWith ^String uri-string "7")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 7)))

    (.startsWith ^String uri-string "8")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 8)))

    (.startsWith ^String uri-string "9")
    (let [rest' (subs uri-string 1)]
      (recur rest' pieces (+' (*' port 10) 9)))

    (.startsWith ^String uri-string "?")
    (let [rest' (subs uri-string 1) pieces (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (option/->Some port) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-query-with-question-mark rest' pieces))

    (.startsWith ^String uri-string "#")
    (let [rest' (subs uri-string 1) pieces (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (option/->Some port) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-fragment rest' pieces))

    (.startsWith ^String uri-string "/")
    (let [pieces (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (option/->Some port) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-path uri-string pieces))

    (= uri-string "")
    (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (option/->Some port) (:path pieces) (:query pieces) (:fragment pieces)))

    :else
    (p/->Error nil)))

(defn- parse-port [uri-string pieces]
  (cond
    (.startsWith ^String uri-string ":0")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 0))

    (.startsWith ^String uri-string ":1")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 1))

    (.startsWith ^String uri-string ":2")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 2))

    (.startsWith ^String uri-string ":3")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 3))

    (.startsWith ^String uri-string ":4")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 4))

    (.startsWith ^String uri-string ":5")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 5))

    (.startsWith ^String uri-string ":6")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 6))

    (.startsWith ^String uri-string ":7")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 7))

    (.startsWith ^String uri-string ":8")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 8))

    (.startsWith ^String uri-string ":9")
    (let [rest' (subs uri-string 2)]
      (parse-port-loop rest' pieces 9))

    (or (= uri-string ":") (= uri-string ""))
    (p/->Ok pieces)

    (.startsWith ^String uri-string "?")
    (let [rest' (subs uri-string 1)]
      (parse-query-with-question-mark rest' pieces))

    (.startsWith ^String uri-string ":?")
    (let [rest' (subs uri-string 2)]
      (parse-query-with-question-mark rest' pieces))

    (.startsWith ^String uri-string "#")
    (let [rest' (subs uri-string 1)]
      (parse-fragment rest' pieces))

    (.startsWith ^String uri-string ":#")
    (let [rest' (subs uri-string 2)]
      (parse-fragment rest' pieces))

    (.startsWith ^String uri-string "/")
    (parse-path uri-string pieces)

    (.startsWith ^String uri-string ":")
    (let [rest' (subs uri-string 1)]
      (if (.startsWith ^String rest' "/")
        (parse-path rest' pieces)
        (p/->Error nil)))

    :else
    (p/->Error nil)))

(defn- parse-host-outside-of-brackets-loop [original uri-string pieces size]
  (cond
    (= uri-string "")
    (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some original) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces)))

    (.startsWith ^String uri-string ":")
    (let [host (codeunit-slice original 0 size)
          pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some host) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-port uri-string pieces))

    (.startsWith ^String uri-string "/")
    (let [host (codeunit-slice original 0 size)
          pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some host) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-path uri-string pieces))

    (.startsWith ^String uri-string "?")
    (let [rest' (subs uri-string 1) host (codeunit-slice original 0 size) pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some host) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-query-with-question-mark rest' pieces))

    (.startsWith ^String uri-string "#")
    (let [rest' (subs uri-string 1) host (codeunit-slice original 0 size) pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some host) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-fragment rest' pieces))

    :else
    (let [[_ rest'] (pop-codeunit uri-string)]
      (recur original rest' pieces (+' size 1)))))

(defn- parse-host-outside-of-brackets [uri-string pieces]
  (parse-host-outside-of-brackets-loop uri-string uri-string pieces 0))

(defn- is-valid-host-within-brackets-char [char]
  (or (or (or (or (and (>= 48 char) (<= char 57)) (and (>= 65 char) (<= char 90))) (and (>= 97 char) (<= char 122))) (= char 58)) (= char 46)))

(defn- parse-host-within-brackets-loop [original uri-string pieces size]
  (cond
    (= uri-string "")
    (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some uri-string) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces)))

    (and (.startsWith ^String uri-string "]") (= size 0))
    (let [rest' (subs uri-string 1)]
      (parse-port rest' pieces))

    (.startsWith ^String uri-string "]")
    (let [rest' (subs uri-string 1) host (codeunit-slice original 0 (+' size 1)) pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some host) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-port rest' pieces))

    (and (.startsWith ^String uri-string "/") (= size 0))
    (parse-path uri-string pieces)

    (.startsWith ^String uri-string "/")
    (let [host (codeunit-slice original 0 size)
          pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some host) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-path uri-string pieces))

    (and (.startsWith ^String uri-string "?") (= size 0))
    (let [rest' (subs uri-string 1)]
      (parse-query-with-question-mark rest' pieces))

    (.startsWith ^String uri-string "?")
    (let [rest' (subs uri-string 1) host (codeunit-slice original 0 size) pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some host) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-query-with-question-mark rest' pieces))

    (and (.startsWith ^String uri-string "#") (= size 0))
    (let [rest' (subs uri-string 1)]
      (parse-fragment rest' pieces))

    (.startsWith ^String uri-string "#")
    (let [rest' (subs uri-string 1) host (codeunit-slice original 0 size) pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some host) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-fragment rest' pieces))

    :else
    (let [[char rest'] (pop-codeunit uri-string) subject (is-valid-host-within-brackets-char char)]
      (if subject
        (recur original rest' pieces (+' size 1))
        (parse-host-outside-of-brackets-loop original original pieces 0)))))

(defn- parse-host-within-brackets [uri-string pieces]
  (parse-host-within-brackets-loop uri-string uri-string pieces 0))

(defn- parse-host [uri-string pieces]
  (cond
    (.startsWith ^String uri-string "[")
    (parse-host-within-brackets uri-string pieces)

    (.startsWith ^String uri-string ":")
    (let [pieces (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some "") (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-port uri-string pieces))

    (= uri-string "")
    (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some "") (:port pieces) (:path pieces) (:query pieces) (:fragment pieces)))

    :else
    (parse-host-outside-of-brackets uri-string pieces)))

(defn- parse-userinfo-loop [original uri-string pieces size]
  (cond
    (and (.startsWith ^String uri-string "@") (= size 0))
    (let [rest' (subs uri-string 1)]
      (parse-host rest' pieces))

    (.startsWith ^String uri-string "@")
    (let [rest' (subs uri-string 1) userinfo (codeunit-slice original 0 size) pieces (->Uri (:scheme pieces) (option/->Some userinfo) (:host pieces) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-host rest' pieces))

    (or (= uri-string "") (.startsWith ^String uri-string "/") (.startsWith ^String uri-string "?") (.startsWith ^String uri-string "#"))
    (parse-host original pieces)

    :else
    (let [[_ rest'] (pop-codeunit uri-string)]
      (recur original rest' pieces (+' size 1)))))

(defn- parse-authority-pieces [string pieces]
  (parse-userinfo-loop string string pieces 0))

(defn- parse-authority-with-slashes [uri-string pieces]
  (cond
    (= uri-string "//")
    (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (option/->Some "") (:port pieces) (:path pieces) (:query pieces) (:fragment pieces)))

    (.startsWith ^String uri-string "//")
    (let [rest' (subs uri-string 2)]
      (parse-authority-pieces rest' pieces))

    :else
    (parse-path uri-string pieces)))

(defn- parse-scheme-loop [original uri-string pieces size]
  (cond
    (and (.startsWith ^String uri-string "/") (= size 0))
    (parse-authority-with-slashes uri-string pieces)

    (.startsWith ^String uri-string "/")
    (let [scheme (codeunit-slice original 0 size)
          pieces (->Uri (option/->Some (string/lowercase scheme)) (:userinfo pieces) (:host pieces) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-authority-with-slashes uri-string pieces))

    (and (.startsWith ^String uri-string "?") (= size 0))
    (let [rest' (subs uri-string 1)]
      (parse-query-with-question-mark rest' pieces))

    (.startsWith ^String uri-string "?")
    (let [rest' (subs uri-string 1) scheme (codeunit-slice original 0 size) pieces (->Uri (option/->Some (string/lowercase scheme)) (:userinfo pieces) (:host pieces) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-query-with-question-mark rest' pieces))

    (and (.startsWith ^String uri-string "#") (= size 0))
    (let [rest' (subs uri-string 1)]
      (parse-fragment rest' pieces))

    (.startsWith ^String uri-string "#")
    (let [rest' (subs uri-string 1) scheme (codeunit-slice original 0 size) pieces (->Uri (option/->Some (string/lowercase scheme)) (:userinfo pieces) (:host pieces) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-fragment rest' pieces))

    (and (.startsWith ^String uri-string ":") (= size 0))
    (p/->Error nil)

    (.startsWith ^String uri-string ":")
    (let [rest' (subs uri-string 1) scheme (codeunit-slice original 0 size) pieces (->Uri (option/->Some (string/lowercase scheme)) (:userinfo pieces) (:host pieces) (:port pieces) (:path pieces) (:query pieces) (:fragment pieces))]
      (parse-authority-with-slashes rest' pieces))

    (= uri-string "")
    (p/->Ok (->Uri (:scheme pieces) (:userinfo pieces) (:host pieces) (:port pieces) original (:query pieces) (:fragment pieces)))

    :else
    (let [[_ rest'] (pop-codeunit uri-string)]
      (recur original rest' pieces (+' size 1)))))

(defn parse
  "Parses a compliant URI string into the `Uri` type.
  If the string is not a valid URI string then an error is returned.
  
  The opposite operation is `uri.to_string`.
  
  ## Examples
  
  ```gleam
  assert uri.parse(\"https://example.com:1234/a/b?query=true#fragment\")
  == Ok(Uri(
  scheme: Some(\"https\"),
  userinfo: None,
  host: Some(\"example.com\"),
  port: Some(1234),
  path: \"/a/b\",
  query: Some(\"query=true\"),
  fragment: Some(\"fragment\"),
  ))
  ```"
  {:malli/schema [:=> [:cat :string] [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [uri-string]
  (parse-scheme-loop uri-string uri-string empty 0))

(def ^{:malli/schema [:=> [:cat :string] [:or [:fn p/Ok?] [:fn p/Error?]]]} parse-query gleam-ffi/parse-query)

(def ^{:malli/schema [:=> [:cat :string] :string]} percent-encode gleam-ffi/percent-encode)

(defn- percent-encode-query [part]
  (-> (percent-encode part) (string/replace "+" "%2B")))

(defn- query-pair [pair]
  (str (str (percent-encode-query (nth pair 0)) "=") (percent-encode-query (nth pair 1))))

(defn query-to-string
  "Encodes a list of key value pairs as a URI query string.
  
  The opposite operation is `uri.parse_query`.
  
  ## Examples
  
  ```gleam
  assert uri.query_to_string([#(\"a\", \"1\"), #(\"b\", \"2\")]) == \"a=1&b=2\"
  ```"
  {:malli/schema [:=> [:cat [:sequential [:tuple :string :string]]] :string]}
  [query]
  (-> query (list/map query-pair) (string/join "&")))

(def ^{:malli/schema [:=> [:cat :string] [:or [:fn p/Ok?] [:fn p/Error?]]]} percent-decode gleam-ffi/percent-decode)

(defn- remove-dot-segments-loop [input accumulator]
  (if (empty? input)
    (list/reverse accumulator)
    (let [segment (first input) rest' (rest input) accumulator (cond (= segment "") (let [accumulator accumulator] accumulator)  (= segment ".") (let [accumulator accumulator] accumulator)  (and (= segment "..") (empty? accumulator)) (list)  (and (= segment "..") (seq accumulator)) (let [accumulator (rest accumulator)] accumulator)  :else (let [segment segment accumulator accumulator] (list* segment accumulator)))]
      (recur rest' accumulator))))

(defn- remove-dot-segments [input]
  (remove-dot-segments-loop input (list)))

(defn path-segments
  "Splits the path section of a URI into its constituent segments.
  
  Removes empty segments and resolves dot-segments as specified in
  [section 5.2](https://www.ietf.org/rfc/rfc3986.html#section-5.2) of the RFC.
  
  ## Examples
  
  ```gleam
  assert uri.path_segments(\"/users/1\") == [\"users\", \"1\"]
  ```"
  {:malli/schema [:=> [:cat :string] [:sequential :string]]}
  [path]
  (remove-dot-segments (string/split path "/")))

(defn to-string
  "Encodes a `Uri` value as a URI string.
  
  The opposite operation is `uri.parse`.
  
  ## Examples
  
  ```gleam
  let uri = Uri(..empty, scheme: Some(\"https\"), host: Some(\"example.com\"))
  assert uri.to_string(uri) == \"https://example.com\"
  ```"
  {:malli/schema [:=> [:cat [:fn Uri?]] :string]}
  [uri]
  (let [out (let [subject (:scheme uri)]
              (if (instance? gleam.option.Some subject)
                (let [scheme (:value subject)]
                  (str scheme ":"))
                ""))
        out (let [subject (:host uri)]
              (if (instance? gleam.option.None subject)
                (str out (:path uri))
                (let [host (:value subject) out (str out "//") out (let [subject (:userinfo uri)] (if (instance? gleam.option.Some subject) (let [userinfo (:value subject)] (str (str out userinfo) "@")) out)) out (str out host) out (let [subject (:port uri)] (if (instance? gleam.option.Some subject) (let [port (:value subject)] (str (str out ":") (int/to-string port))) out)) out (let [subject (:path uri)] (cond (= subject "") out  (.startsWith ^String subject "/") (str out (:path uri))  :else (str (str out "/") (:path uri))))]
                  out)))
        out (let [subject (:query uri)]
              (if (instance? gleam.option.Some subject)
                (let [query (:value subject)]
                  (str (str out "?") query))
                out))
        out (let [subject (:fragment uri)]
              (if (instance? gleam.option.Some subject)
                (let [fragment (:value subject)]
                  (str (str out "#") fragment))
                out))]
    out))

(defn origin
  "Fetches the origin of a URI.
  
  Returns the origin of a uri as defined in
  [RFC 6454](https://tools.ietf.org/html/rfc6454)
  
  The supported URI schemes are `http` and `https`.
  URLs without a scheme will return `Error`.
  
  ## Examples
  
  ```gleam
  let assert Ok(uri) = uri.parse(\"https://example.com/path?foo#bar\")
  assert uri.origin(uri) == Ok(\"https://example.com\")
  ```"
  {:malli/schema [:=> [:cat [:fn Uri?]] [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [uri]
  (let [{scheme :scheme host :host port :port} uri]
    (cond
      (and (instance? gleam.option.Some host) (instance? gleam.option.Some scheme) (= (:value scheme) "https") (= port (option/->Some 443)))
      (let [h (:value host)]
        (p/->Ok (str "https://" h)))

      (and (instance? gleam.option.Some host) (instance? gleam.option.Some scheme) (= (:value scheme) "http") (= port (option/->Some 80)))
      (let [h (:value host)]
        (p/->Ok (str "http://" h)))

      (and (instance? gleam.option.Some host) (instance? gleam.option.Some scheme) (or (= (:value scheme) "http") (= (:value scheme) "https")))
      (let [h (:value host) s (:value scheme)]
        (if (instance? gleam.option.Some port)
          (let [p (:value port)]
            (p/->Ok (str (str (str (str s "://") h) ":") (int/to-string p))))
          (p/->Ok (str (str s "://") h))))

      :else
      (p/->Error nil))))

(defn- join-segments [segments]
  (string/join (list* "" segments) "/"))

(defn- drop-last [elements]
  (list/take elements (-' (list/length elements) 1)))

(defn merge
  "Resolves a URI with respect to the given base URI.
  
  The base URI must be an absolute URI or this function will return an error.
  The algorithm for merging URIs is described in
  [RFC 3986](https://tools.ietf.org/html/rfc3986#section-5.2)."
  {:malli/schema [:=> [:cat [:fn Uri?] [:fn Uri?]]
                      [:or [:fn p/Ok?] [:fn p/Error?]]]}
  [base relative]
  (if (and (instance? Uri base) (instance? gleam.option.Some (:scheme base)) (instance? gleam.option.Some (:host base)))
    (if (and (instance? Uri relative) (instance? gleam.option.Some (:host relative)))
      (let [path (-> (:path relative)
                     (string/split "/")
                     remove-dot-segments
                     join-segments)
            resolved (->Uri (option/or (:scheme relative) (:scheme base))
                            (option/->None)
                            (:host relative)
                            (option/or (:port relative) (:port base))
                            path
                            (:query relative)
                            (:fragment relative))]
        (p/->Ok resolved))
      (let [[new-path new-query] (let [subject (:path relative)]
                                   (if (= subject "")
                                     [(:path base) (option/or (:query relative)
                                                 (:query base))]
                                     (let [path-segments (let [subject (string/starts-with (:path relative)
                                                                               "/")]
                                                           (if subject
                                                             (string/split (:path relative)
                                                                           "/")
                                                             (-> (:path base)
                                                                 (string/split "/")
                                                                 drop-last
                                                                 (list/append (string/split (:path relative)
                                                                                            "/")))))
                                           path (-> path-segments
                                                    remove-dot-segments
                                                    join-segments)]
                                       [path (:query relative)])))
            resolved (->Uri (:scheme base)
                            (option/->None)
                            (:host base)
                            (:port base)
                            new-path
                            new-query
                            (:fragment relative))]
        (p/->Ok resolved)))
    (p/->Error nil)))
