import gleam/int
import gleam/io

/// Money tagged with a phantom currency. No runtime cost:
/// the parameter exists only in the type checker.
pub opaque type Money(currency) {
  Money(cents: Int)
}

pub type Usd

pub type Eur

pub fn usd(cents: Int) -> Money(Usd) {
  Money(cents)
}

pub fn eur(cents: Int) -> Money(Eur) {
  Money(cents)
}

/// Same-currency arithmetic only: `add(usd(1), eur(1))`
/// refuses to compile.
pub fn add(a: Money(c), b: Money(c)) -> Money(c) {
  Money(a.cents + b.cents)
}

pub fn main() {
  let assert Money(300) = add(usd(100), usd(200))
  let assert Money(250) = add(eur(50), eur(200))
  io.println("balance: " <> int.to_string(300))
}
