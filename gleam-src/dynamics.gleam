//// Decoder regressions: shapes that exercised latent FFI-core bugs found
//// while shimming gleam_json (decoder zero placeholders, decode_list,
//// dynamic_string).

import gleam/dynamic
import gleam/dynamic/decode
import gleam/io
import gleam/string

pub fn main() {
  // dynamic_string: strings are not bit arrays on this runtime
  io.println(string.inspect(decode.run(dynamic.string("hi"), decode.string)))

  // decode_list: success, element failure (path carries the index), non-list
  let ints = dynamic.list([dynamic.int(1), dynamic.int(2)])
  io.println(string.inspect(decode.run(ints, decode.list(decode.int))))
  let mixed = dynamic.list([dynamic.int(1), dynamic.string("x")])
  io.println(string.inspect(decode.run(mixed, decode.list(decode.int))))
  io.println(string.inspect(decode.run(dynamic.int(3), decode.list(decode.int))))

  // failure placeholders must be the type's zero: a mapped decoder applies
  // its transform to the placeholder, which crashed when it was the raw value
  let shout = decode.map(decode.string, string.uppercase)
  io.println(string.inspect(decode.run(dynamic.int(9), shout)))
  io.println(string.inspect(decode.run(dynamic.string("f"), decode.map(decode.float, string.inspect))))
  io.println(string.inspect(decode.run(dynamic.string("i"), decode.map(decode.int, string.inspect))))
}
