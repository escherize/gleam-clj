import glance
import gleam/list

const source = "
import gleam/list
import gleam/option.{type Option, None, Some}

pub type Shape {
  Circle(radius: Float)
  Rect(width: Float, height: Float)
  Point
}

pub opaque type Id {
  Id(Int)
}

pub fn area(shape: Shape) -> Float {
  case shape {
    Circle(r) -> 3.14159 *. r *. r
    Rect(w, h) -> w *. h
    Point -> 0.0
  }
}

pub fn total(shapes: List(Shape)) -> Float {
  shapes
  |> list.map(area)
  |> list.fold(0.0, fn(a, b) { a +. b })
}

pub fn biggest(shapes: List(Shape)) -> Option(Shape) {
  case shapes {
    [] -> None
    [first, ..rest] ->
      Some(
        list.fold(rest, first, fn(acc, s) {
          case area(s) >. area(acc) {
            True -> s
            False -> acc
          }
        }),
      )
  }
}
"

pub fn parse_50() -> Int {
  list.repeat(0, 5000)
  |> list.fold(0, fn(acc, _) {
    case glance.module(source) {
      Ok(_) -> acc + 1
      Error(_) -> acc
    }
  })
}
