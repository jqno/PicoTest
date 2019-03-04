package nl.jqno.picotest.examples;

import nl.jqno.picotest.Test;

public class ExampleTest extends Test {

    public void demo() {
        test("a succeeding test", () -> {
            assert 1 + 1 == 2;
        });

        test("a failing test", () -> {
            assert 1 + 1 == 3;
        });

        test("an aborted test", () -> {
            throw new IllegalStateException();
        });

        skip("sometimes you want to skip things", "a skipped test", () -> {
           assert 1 + 1 == 2;
        });
    }

    public void anotherContainer() {
        test("1 + 1 == 2", () -> {
            assert 1 + 1 == 2;
        });
    }
}
