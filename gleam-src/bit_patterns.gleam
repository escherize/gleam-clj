import gleam/int
import gleam/io

fn show(r: Result(String, Nil)) -> String {
  case r {
    Ok(s) -> s
    Error(_) -> "no-match"
  }
}

fn classify(b: BitArray) -> Result(String, Nil) {
  case b {
    <<>> -> Ok("empty")
    <<"--", rest:bits>> -> Ok("dashes+" <> int.to_string(bit_size(rest) / 8))
    <<1, x, rest:bytes>> ->
      Ok("one," <> int.to_string(x) <> "," <> int.to_string(bit_size(rest) / 8))
    <<n:size(16), _:bits>> -> Ok("u16=" <> int.to_string(n))
    _ -> Error(Nil)
  }
}

fn bit_size(b: BitArray) -> Int {
  case b {
    <<>> -> 0
    <<_, rest:bytes>> -> 8 + bit_size(rest)
    _ -> 0
  }
}

fn take(b: BitArray, len: Int) -> Result(String, Nil) {
  case b {
    <<chunk:size(len)-bytes, rest:bits>> ->
      Ok(int.to_string(bit_size(chunk) / 8) <> "+" <> int.to_string(bit_size(rest) / 8))
    _ -> Error(Nil)
  }
}

pub fn main() {
  io.println(show(classify(<<>>)))
  io.println(show(classify(<<"--stuff":utf8>>)))
  io.println(show(classify(<<1, 42, 9, 9, 9>>)))
  io.println(show(classify(<<200, 1, 7>>)))
  io.println(show(take(<<1, 2, 3, 4, 5>>, 2)))
  io.println(show(take(<<1, 2>>, 5)))
  io.println(show(take(<<1, 2>>, -1)))
}
