//// A module containing some unusual functions and types.

/// A type where the value can never be constructed.
/// Can you work out why?
pub type Never {
  Never(Never)
}

/// Call a function twice with an initial value.
///
pub fn twice(argument: value, my_function: fn(value) -> value) -> value {
  my_function(my_function(argument))
}

/// Call a function three times with an initial value.
///
pub fn thrice(argument: value, my_function: fn(value) -> value) -> value {
  my_function(my_function(my_function(argument)))
}

// Synthetic main added for the gleam-clj harness: exercise twice/thrice.
import gleam/int
import gleam/io

pub fn main() {
  io.println(int.to_string(twice(1, fn(x) { x * 2 })))
  io.println(int.to_string(thrice(1, fn(x) { x * 2 })))
}
