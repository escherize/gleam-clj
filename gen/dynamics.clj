(ns dynamics
  "Decoder regressions: shapes that exercised latent FFI-core bugs found
   while shimming gleam_json (decoder zero placeholders, decode_list,
   dynamic_string)."
  (:require
   [gleam.dynamic :as dynamic]
   [gleam.dynamic.decode :as decode]
   [gleam.io :as io]
   [gleam.string :as string]))

(defn main
  "main() -> Nil"
  {:malli/schema [:=> [:cat] :nil] :gleam/src "dynamics.gleam:10"}
  []
  (io/println (string/inspect (decode/run (dynamic/string "hi") decode/string)))
  (let [ints (dynamic/list' (list (dynamic/int 1) (dynamic/int 2)))]
    (io/println (string/inspect (decode/run ints (decode/list' decode/int))))
    (let [mixed (dynamic/list' (list (dynamic/int 1) (dynamic/string "x")))]
      (io/println (string/inspect (decode/run mixed (decode/list' decode/int))))
      (io/println (string/inspect (decode/run (dynamic/int 3)
                                              (decode/list' decode/int))))
      (let [shout (decode/map decode/string string/uppercase)]
        (io/println (string/inspect (decode/run (dynamic/int 9) shout)))
        (io/println (string/inspect (decode/run (dynamic/string "f")
                                                (decode/map decode/float
                                                            string/inspect))))
        (io/println (string/inspect (decode/run (dynamic/string "i")
                                                (decode/map decode/int
                                                            string/inspect))))))))

(defn -main [& _]
  (main))
