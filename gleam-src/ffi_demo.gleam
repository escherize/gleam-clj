import gleam/io

@external(javascript, "clojure.string", "upper-case")
fn shout(s: String) -> String

pub fn main() {
  io.println(shout("hello from clojure interop"))
}
