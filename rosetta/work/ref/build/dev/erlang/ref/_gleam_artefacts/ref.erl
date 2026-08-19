-module(ref).
-compile([no_auto_import, nowarn_unused_vars, nowarn_unused_function, nowarn_nomatch, inline]).
-define(FILEPATH, "src/ref.gleam").
-export([water_area/1, main/0]).

-file("src/ref.gleam", 19).
-spec water_area(list(integer())) -> integer().
water_area(Towers) ->
    _pipe = Towers,
    _pipe@1 = gleam@list:scan(_pipe, 0, fun gleam@int:max/2),
    _pipe@5 = gleam@list:map2(
        _pipe@1,
        begin
            _pipe@2 = Towers,
            _pipe@3 = lists:reverse(_pipe@2),
            _pipe@4 = gleam@list:scan(_pipe@3, 0, fun gleam@int:max/2),
            lists:reverse(_pipe@4)
        end,
        fun gleam@int:min/2
    ),
    _pipe@6 = gleam@list:map2(_pipe@5, Towers, fun gleam@int:subtract/2),
    gleam@int:sum(_pipe@6).

-file("src/ref.gleam", 5).
-spec main() -> nil.
main() ->
    Cases = [[1, 5, 3, 7, 2],
        [5, 3, 7, 2, 6, 4, 5, 9, 1, 2],
        [2, 6, 3, 5, 2, 8, 1, 4, 2, 2, 5, 3, 5, 7, 4, 1],
        [5, 5, 5, 5],
        [5, 6, 7, 8],
        [8, 7, 7, 6],
        [6, 7, 10, 7, 6]],
    gleam@list:each(Cases, fun(Towers) -> _pipe = Towers,
            _pipe@1 = water_area(_pipe),
            _pipe@2 = erlang:integer_to_binary(_pipe@1),
            gleam_stdlib:println(_pipe@2) end).
