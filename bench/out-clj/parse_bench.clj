(ns parse-bench
  (:require
   [glance :as glance]
   [gleam.list :as list])
  (:import (gleam.prelude Ok)))

(def ^:private source "\nimport gleam/list\nimport gleam/option.{type Option, None, Some}\n\npub type Shape {\n  Circle(radius: Float)\n  Rect(width: Float, height: Float)\n  Point\n}\n\npub opaque type Id {\n  Id(Int)\n}\n\npub fn area(shape: Shape) -> Float {\n  case shape {\n    Circle(r) -> 3.14159 *. r *. r\n    Rect(w, h) -> w *. h\n    Point -> 0.0\n  }\n}\n\npub fn total(shapes: List(Shape)) -> Float {\n  shapes\n  |> list.map(area)\n  |> list.fold(0.0, fn(a, b) { a +. b })\n}\n\npub fn biggest(shapes: List(Shape)) -> Option(Shape) {\n  case shapes {\n    [] -> None\n    [first, ..rest] ->\n      Some(\n        list.fold(rest, first, fn(acc, s) {\n          case area(s) >. area(acc) {\n            True -> s\n            False -> acc\n          }\n        }),\n      )\n  }\n}\n")

(defn parse-50
  "parse_50() -> Int"
  {:malli/schema [:=> [:cat] :int]
   :gleam/src "bench/src/parse_bench.gleam:48"}
  []
  (-> (list/repeat 0 5000)
      (list/fold 0
                 (fn [acc _]
                   (let [subject (glance/module source)]
                     (if (instance? Ok subject) (+' acc 1) acc))))))
