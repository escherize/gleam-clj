/// Sum 1..n with a tail-recursive accumulator.
fn sum_to(n: Int, acc: Int) -> Int {
  case n {
    0 -> acc
    _ -> sum_to(n - 1, acc + n)
  }
}

pub fn main() {
  let assert 55 = sum_to(10, 0)
  let assert 500_000_500_000 = sum_to(1_000_000, 0)
}
