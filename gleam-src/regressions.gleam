//// Wild-code sweep regressions: shapes that once crashed the emitter.

import gleam/int
import gleam/io
import gleam/list

/// `use Nil, ...` — zero-arity constructor as a use pattern (from iv).
pub fn print_all(items: List(Int)) -> Nil {
  use Nil, item <- list.fold(items, Nil)
  io.println(int.to_string(item))
}

/// Empty bit-array pattern (from gleam_http).
pub fn describe(b: BitArray) -> String {
  case b {
    <<>> -> "empty"
    _ -> "bytes"
  }
}

/// let-assert destructure whose collapsed let binds bracket-heavy strings
/// (from gap): the binding-vector scanner must ignore [ ] inside strings.
pub fn bracket_label(n: Int) -> String {
  let assert Ok(head) = list.first([n])
  let open = "["
  let close = "]"
  open <> int.to_string(head) <> close
}

pub fn main() {
  print_all([1, 2])
  io.println(describe(<<>>))
  io.println(describe(<<7>>))
  io.println(bracket_label(42))
}
