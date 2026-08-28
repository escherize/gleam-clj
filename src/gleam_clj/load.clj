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

(defn- gleam-imports
  "Ordered distinct local module names imported by `source`. Stdlib modules
  (`gleam/*`, gleeunit) are excluded — the compiler pre-seeds them."
  [source]
  (->> (re-seq #"(?m)^import\s+([a-z][a-z0-9_]*(?:/[a-z][a-z0-9_]*)*)" source)
       (map second)
       (distinct)
       (remove #(or (= % "gleeunit") (str/starts-with? % "gleam/")))))

(defn- closure
  "Import closure of the file at `path`: topo-ordered {:module :path :source}
  maps, dependencies before dependents, the entry last. An import of `m` from
  a file in dir D resolves to D/m.gleam; imports that resolve to no file are
  left for the compiler's own diagnostic."
  [path]
  (let [seen (atom #{})
        out  (atom [])]
    (letfn [(visit [module ^java.io.File f]
              (let [canon (.getCanonicalPath f)]
                (when (and (.exists f) (not (@seen canon)))
                  (swap! seen conj canon)
                  (let [source (slurp f)]
                    ;; post-order: dependencies land before their importers
                    (doseq [m (gleam-imports source)]
                      (visit m (io/file (.getParentFile f) (str m ".gleam"))))
                    (swap! out conj {:module module :path f :source source})))))]
      (visit (str/replace (.getName (io/file path)) #"\.gleam$" "") (io/file path))
      @out)))

(defn- delete-tree
  "Recursively delete `^java.io.File f`."
  [^java.io.File f]
  (doseq [x (rseq (into [] (file-seq f)))] ; children before parents
    (.delete x)))

(defn- compile-closure
  "Stage `entries` into a temp project and run the real `build` pipeline over
  it once. Returns {:tmp <project dir> :out <compiled .clj dir>}; the caller
  owns deleting :tmp once it has loaded what it needs. Compile failure throws
  Gleam's own diagnostic verbatim."
  [entries gleam-path]
  (let [tmp (-> (java.nio.file.Files/createTempDirectory
                 "gleam-clj-" (make-array java.nio.file.attribute.FileAttribute 0))
                .toFile)]
    (try
      (doseq [{:keys [module source]} entries]
        (let [f (io/file tmp "src" (str module ".gleam"))]
          (some-> (.getParentFile f) .mkdirs)
          (spit f source)))
      (let [{:keys [exit err]}
            (shell/sh *binary* "build" (str tmp) (str (io/file tmp "out")) *stdlib-src*)]
        (when-not (zero? exit)
          ;; err is Gleam's own diagnostic (source span, expected/found) —
          ;; surface it verbatim, without a Rust-panic wrapper.
          (throw (ex-info (str "Gleam compile error:\n\n" (str/trim err))
                          {:gleam-path gleam-path}))))
      {:tmp tmp :out (io/file tmp "out")}
      (catch Throwable t
        (delete-tree tmp)
        (throw t)))))

(defn require-gleam
  "Compile the Gleam file at `path` plus every local file it imports, and load
  them in dependency order. Returns the entry namespace symbol. Closures are
  cached by a hash over every file in them — edit any file and its dependents
  recompile. Pass :reload true to force recompilation."
  [path & {:keys [reload]}]
  (let [entries (closure path)
        key (->> entries
                 (map #(str (:module %) \0 (:source %) \0))
                 (apply str)
                 sha)]
    (if-let [hit (and (not reload) (@loaded key))]
      (:ns hit)
      (let [{:keys [tmp out]} (compile-closure entries (str (io/file path)))
            clj #(io/file out (str (:module %) ".clj"))]
        (try
          (let [ns-sym (module-ns (clj (last entries)))]
            ;; deps first, so emitted (:require ...) forms become no-ops
            (doseq [e entries]
              (binding [*ns* (create-ns (module-ns (clj e)))]
                (load-file (str (clj e)))))
            (swap! loaded assoc key {:ns ns-sym})
            ns-sym)
          (finally (delete-tree tmp)))))))

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
