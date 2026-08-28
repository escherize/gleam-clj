-module(bench_ffi).
-export([now_ms/0]).
now_ms() -> erlang:monotonic_time(millisecond).
