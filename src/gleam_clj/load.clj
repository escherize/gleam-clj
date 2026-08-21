(ns gleam-clj.load
  "Compile and load Gleam from a running Clojure REPL.

  gleam-to-clj is a native binary; this shells out to it, loads the emitted
  Clojure, and caches by source hash so re-loading unchanged source is free.

  The runtime the compiled code needs — `gleam.prelude`, the `gleam-ffi`
  core, and the compiled stdlib under `gleam.*` — must already be on the
  classpath (the gleam-clj `src/` and `stdlib-clj/`, or the packaged
  runtime dep). This namespace only handles compiling and loading your own
  Gleam modules on top of that.

  Usage:

    (require '[gleam-clj.load :as gl])
    (gl/require-gleam \"src/thing.gleam\")   ; => 'thing  (compiled + loaded)
    (thing/some-fn ...)

    (gl/eval-gleam \"pub fn double(x: Int) -> Int { x * 2 }\")
    ;; => a namespace symbol whose public fns are now callable"
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def ^:dynamic *binary*
  "Path to the gleam-to-clj binary. Override with the GLEAM_TO_CLJ env var,
  else looked up on PATH, else the repo's debug build."
  (or (System/getenv "GLEAM_TO_CLJ")
      (let [{:keys [exit out]} (shell/sh "sh" "-c" "command -v gleam-to-clj")]
        (when (zero? exit) (str/trim out)))
      "gleam-to-clj/target/debug/gleam-to-clj"))

(def ^:dynamic *stdlib-src*
  "Path to the vendored Gleam stdlib sources the compiler needs to typecheck.
  Override with GLEAM_CLJ_STDLIB; defaults to the repo layout."
  (or (System/getenv "GLEAM_CLJ_STDLIB") "stdlib-src"))

(def ^:private loaded
  "source-sha -> {:ns sym} for compiled modules already loaded this session."
  (atom {}))

(defn- sha [^String s]
  (let [d (.digest (java.security.MessageDigest/getInstance "SHA-256")
                   (.getBytes s "UTF-8"))]
    (apply str (map #(format "%02x" %) d))))

(defn- module-ns
  "Namespace symbol for a compiled file: the emitted (ns ...) form."
  [clj-path]
  (with-open [r (io/reader clj-path)]
    (let [form (read (java.io.PushbackReader. r))]
      (when-not (and (seq? form) (= 'ns (first form)))
        (throw (ex-info "emitted file has no ns form" {:path clj-path})))
      (second form))))

(defn- compile-file
  "Run gleam-to-clj over `gleam-path`, returning the emitted .clj path."
  [gleam-path out-path]
  (let [{:keys [exit err]}
        (shell/sh *binary* "typed" (str gleam-path) (str out-path) *stdlib-src*)]
    (when-not (zero? exit)
      (throw (ex-info (str "gleam-to-clj failed:\n" err)
                      {:gleam-path (str gleam-path) :exit exit})))
    out-path))

(defn require-gleam
  "Compile the Gleam file at `path` and load the result. Returns the loaded
  namespace symbol. Unchanged source (by content hash) is a no-op after the
  first load. Pass :reload true to force recompilation."
  [path & {:keys [reload]}]
  (let [src (slurp path)
        key (sha src)]
    (if-let [hit (and (not reload) (@loaded key))]
      (:ns hit)
      (let [tmp (java.io.File/createTempFile "gleam-clj-" ".clj")
            clj (compile-file path (.getPath tmp))
            ns-sym (module-ns clj)]
        (binding [*ns* (create-ns ns-sym)]
          (load-file clj))
        (.delete tmp)
        (swap! loaded assoc key {:ns ns-sym})
        ns-sym))))

(defn eval-gleam
  "Compile and load a string of Gleam source. The module name defaults to a
  content-derived `snippet_<hash>`; override with :module. Returns the loaded
  namespace symbol so callers can invoke its public fns."
  [source & {:keys [module]}]
  (let [name (or module (str "snippet_" (subs (sha source) 0 8)))
        tmp (java.io.File/createTempFile (str name "-") ".gleam")]
    (spit tmp source)
    (try
      ;; the single-file ns is the file stem, so name the temp file after it
      (let [named (io/file (.getParentFile tmp) (str name ".gleam"))]
        (io/copy tmp named)
        (try (require-gleam (.getPath named) :reload true)
             (finally (.delete named))))
      (finally (.delete tmp)))))
