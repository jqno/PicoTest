package nl.jqno.picotest.examples;

import nl.jqno.picotest.Test;

import java.util.ArrayList;
import java.util.List;

public class BeforeAndAfterEachTest extends Test {

    public static final List<String> log = new ArrayList<>();

    public void happyPath() {
        beforeEach(() -> log.add("before"));

        afterEach(() -> log.add("after"));

        test("A", () -> log.add("A"));

        test("B", () -> log.add("B"));

        test("C", () -> log.add("C"));
    }

    public void testBrokenBeforeEach() {
        beforeEach(() -> {
            assert false;
        });

        test("unexecuted", () -> {});
    }

    public void testBrokenAfterEach() {
        afterEach(() -> {
            assert false;
        });

        test("unexecuted", () -> {});
    }
}
