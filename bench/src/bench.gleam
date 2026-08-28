//// Cross-VM benchmarks. Timed in-process on both VMs (startup excluded);
//// each workload runs once as warmup, then three timed rounds.

import gleam/dict.{type Dict}
import parse_bench
import gleam/int
import gleam/io
import gleam/list
import gleam/string

@external(erlang, "bench_ffi", "now_ms")
fn now_ms() -> Int

fn time(name: String, work: fn() -> Int) -> Nil {
  let _warmup = work()
  let rounds =
    list.map([1, 2, 3], fn(_) {
      let start = now_ms()
      let _ = work()
      now_ms() - start
    })
  io.println(
    name
    <> ": "
    <> string.join(list.map(rounds, int.to_string), " ")
    <> " ms",
  )
}

// -- coin change: dict-heavy dynamic programming, arbitrary-precision ints
fn min_coins(coins: List(Int), amount: Int) -> Int {
  let table =
    int.range(1, amount + 1, dict.from_list([#(0, 0)]), fn(table, a) {
      step(coins, table, a)
    })
  case dict.get(table, amount) {
    Ok(n) -> n
    Error(_) -> -1
  }
}

fn step(coins: List(Int), table: Dict(Int, Int), a: Int) -> Dict(Int, Int) {
  let best =
    coins
    |> list.filter_map(fn(c) { dict.get(table, a - c) })
    |> list.reduce(int.min)
  case best {
    Ok(b) -> dict.insert(table, a, b + 1)
    Error(_) -> table
  }
}

fn coin_work() -> Int {
  min_coins([1, 3, 4, 5, 17, 29], 500_000)
}

// -- list/string pipeline: map, filter, fold, to_string, join
fn build_range(n: Int, acc: List(Int)) -> List(Int) {
  case n {
    0 -> acc
    _ -> build_range(n - 1, [n, ..acc])
  }
}

fn pipeline_work() -> Int {
  build_range(2_000_000, [])
  |> list.map(fn(n) { n * 3 })
  |> list.filter(fn(n) { n % 2 == 0 })
  |> list.map(int.to_string)
  |> string.join(",")
  |> string.length()
}

pub fn main() {
  time("coin_change dp (amount 500k)", coin_work)
  time("list+string pipeline (2M)", pipeline_work)
  time("glance parse (5000x)", fn() { parse_bench.parse_50() })
}
