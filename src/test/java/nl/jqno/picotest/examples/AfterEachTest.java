package nl.jqno.picotest.examples;

import nl.jqno.picotest.Test;

public class AfterEachTest extends Test {

    private static final String EXPECTED = "expected";
    public static String status = EXPECTED;

    public void testAfterEach() {
        afterEach(() -> {
            status = EXPECTED;
        });

        test("change the status to A", () -> {
            assert status.equals(EXPECTED);
            status = "A";
        });

        test("change the status to B", () -> {
            assert status.equals(EXPECTED);
            status = "B";
        });

        test("change the status to C", () -> {
            assert status.equals(EXPECTED);
            status = "C";
        });
    }

    public void testBrokenBeforeEach() {
        afterEach(() -> {
            assert false;
        });

        test("unexecuted", () -> {});
    }
}
