import gleam/dict.{type Dict}
import gleam/int
import gleam/list

/// Fewest coins summing to `amount`. Error(Nil) if unreachable.
pub fn min_coins(coins: List(Int), amount: Int) -> Result(Int, Nil) {
  case amount {
    0 -> Ok(0)
    a if a < 0 -> Error(Nil)
    _ -> {
      int.range(1, amount + 1, dict.from_list([#(0, 0)]), fn(table, a) {
        step(coins, table, a)
      })
      |> dict.get(amount)
    }
  }
}

fn step(coins: List(Int), table: Dict(Int, Int), a: Int) -> Dict(Int, Int) {
  echo #("step", "a: ", a, ", table: ", table)
  let best =
    coins
    |> list.filter_map(fn(c) { dict.get(table, a - c) })
    |> list.reduce(int.min)
  case best {
    Ok(b) -> dict.insert(table, a, b + 1)
    Error(_) -> table
  }
}

pub fn main() {
  let assert Ok(0) = min_coins([1, 5, 10], 0)
  let assert Ok(1) = min_coins([1, 5, 10], 10)
  let assert Ok(2) = min_coins([1, 5, 10], 15)
  let assert Ok(4) = min_coins([1, 5, 10], 13)
  // greedy trap: greedy picks 4+1+1 = 3 coins, optimal is 3+3 = 2
  let assert Ok(2) = min_coins([1, 3, 4], 6)
  let assert Error(Nil) = min_coins([5, 10], 3)
  let assert Error(Nil) = min_coins([], 7)
}
