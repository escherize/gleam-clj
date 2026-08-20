pub type Option(inner) {
  Some(inner)
  None
}

// An option of string
pub const name: Option(String) = Some("Annah")

// An option of int
pub const level: Option(Int) = Some(10)

// Synthetic main added for the gleam-clj harness: exercise the generic type.
import gleam/io

pub fn main() {
  case name {
    Some(n) -> io.println(n)
    None -> io.println("nobody")
  }
  case level {
    Some(l) if l > 5 -> io.println("high level")
    _ -> io.println("low level")
  }
}
