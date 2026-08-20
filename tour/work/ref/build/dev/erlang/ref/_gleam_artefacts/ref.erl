-module(ref).
-compile([no_auto_import, nowarn_unused_vars, nowarn_unused_function, nowarn_nomatch, inline]).
-define(FILEPATH, "src/ref.gleam").
-export([main/0]).

-file("src/ref.gleam", 11).
-spec add(integer(), integer()) -> integer().
add(A, B) ->
    A + B.

-file("src/ref.gleam", 1).
-spec main() -> nil.
main() ->
    _assert_subject = add(1, 2),
    _assert_subject@1 = 3,
    case _assert_subject =:= _assert_subject@1 of
        true -> nil;
        false -> erlang:error(#{gleam_error => assert,
                message => <<"Assertion failed."/utf8>>,
                file => <<?FILEPATH/utf8>>,
                module => <<"ref"/utf8>>,
                function => <<"main"/utf8>>,
                line => 2,
                kind => binary_operator,
                operator => '==',
                left => #{kind => expression,
                    value => _assert_subject,
                    start => 25,
                    'end' => 34
                    },
                right => #{kind => literal,
                    value => _assert_subject@1,
                    start => 38,
                    'end' => 39
                    },
                start => 18,
                'end' => 39,
                expression_start => 25})
    end,
    _assert_subject@2 = add(1, 2),
    _assert_subject@3 = add(1, 3),
    case _assert_subject@2 < _assert_subject@3 of
        true -> nil;
        false -> erlang:error(#{gleam_error => assert,
                message => <<"Assertion failed."/utf8>>,
                file => <<?FILEPATH/utf8>>,
                module => <<"ref"/utf8>>,
                function => <<"main"/utf8>>,
                line => 4,
                kind => binary_operator,
                operator => '<',
                left => #{kind => expression,
                    value => _assert_subject@2,
                    start => 50,
                    'end' => 59
                    },
                right => #{kind => expression,
                    value => _assert_subject@3,
                    start => 62,
                    'end' => 71
                    },
                start => 43,
                'end' => 71,
                expression_start => 50})
    end,
    _assert_subject@4 = add(6, 2),
    _assert_subject@5 = add(2, 6),
    case _assert_subject@4 =:= _assert_subject@5 of
        true -> nil;
        false -> erlang:error(#{gleam_error => assert,
                message => <<"Addition should be commutative"/utf8>>,
                file => <<?FILEPATH/utf8>>,
                module => <<"ref"/utf8>>,
                function => <<"main"/utf8>>,
                line => 6,
                kind => binary_operator,
                operator => '==',
                left => #{kind => expression,
                    value => _assert_subject@4,
                    start => 82,
                    'end' => 91
                    },
                right => #{kind => expression,
                    value => _assert_subject@5,
                    start => 95,
                    'end' => 104
                    },
                start => 75,
                'end' => 104,
                expression_start => 82})
    end,
    _assert_subject@6 = add(2, 2),
    _assert_subject@7 = 5,
    case _assert_subject@6 =:= _assert_subject@7 of
        true -> nil;
        false -> erlang:error(#{gleam_error => assert,
                message => <<"Assertion failed."/utf8>>,
                file => <<?FILEPATH/utf8>>,
                module => <<"ref"/utf8>>,
                function => <<"main"/utf8>>,
                line => 8,
                kind => binary_operator,
                operator => '==',
                left => #{kind => expression,
                    value => _assert_subject@6,
                    start => 151,
                    'end' => 160
                    },
                right => #{kind => literal,
                    value => _assert_subject@7,
                    start => 164,
                    'end' => 165
                    },
                start => 144,
                'end' => 165,
                expression_start => 151})
    end.
