-module(ref_test).
-compile([no_auto_import, nowarn_unused_vars, nowarn_unused_function, nowarn_nomatch, inline]).
-define(FILEPATH, "test/ref_test.gleam").
-export([main/0, hello_world_test/0]).

-file("test/ref_test.gleam", 3).
-spec main() -> nil.
main() ->
    gleeunit:main().

-file("test/ref_test.gleam", 8).
-spec hello_world_test() -> nil.
hello_world_test() ->
    Name = <<"Joe"/utf8>>,
    Greeting = <<<<"Hello, "/utf8, Name/binary>>/binary, "!"/utf8>>,
    _assert_subject = <<"Hello, Joe!"/utf8>>,
    case Greeting =:= _assert_subject of
        true -> nil;
        false -> erlang:error(#{gleam_error => assert,
                message => <<"Assertion failed."/utf8>>,
                file => <<?FILEPATH/utf8>>,
                module => <<"ref_test"/utf8>>,
                function => <<"hello_world_test"/utf8>>,
                line => 12,
                kind => binary_operator,
                operator => '==',
                left => #{kind => expression,
                    value => Greeting,
                    start => 202,
                    'end' => 210
                    },
                right => #{kind => literal,
                    value => _assert_subject,
                    start => 214,
                    'end' => 227
                    },
                start => 195,
                'end' => 227,
                expression_start => 202})
    end.
