import gleam/list

pub type Shape {
  Circle(Float)
  Rect(width: Float, height: Float)
  Point
}

/// Area with pi = 3.0, engineering approximation.
pub fn area(shape: Shape) -> Float {
  case shape {
    Circle(r) -> 3.0 *. r *. r
    Rect(w, h) -> w *. h
    Point -> 0.0
  }
}

fn sum(xs: List(Float)) -> Float {
  case xs {
    [] -> 0.0
    [x, ..rest] -> x +. sum(rest)
  }
}

pub fn total_area(shapes: List(Shape)) -> Float {
  shapes
  |> list.map(area)
  |> sum
}

pub fn main() {
  let assert 12.0 = area(Circle(2.0))
  let assert 6.0 = area(Rect(2.0, 3.0))
  let assert 0.0 = area(Point)
  let assert 18.0 = total_area([Circle(2.0), Rect(2.0, 3.0), Point])
  let assert 0.0 = total_area([])
}
