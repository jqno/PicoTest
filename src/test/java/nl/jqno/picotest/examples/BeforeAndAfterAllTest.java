package nl.jqno.picotest.examples;

import nl.jqno.picotest.Test;

import java.util.ArrayList;
import java.util.List;

public class BeforeAndAfterAllTest extends Test {

    public static final List<String> log = new ArrayList<>();

    public void happyPath() {
        beforeAll(() -> log.add("before"));

        afterAll(() -> log.add("after"));

        test("A", () -> log.add("A"));

        test("B", () -> log.add("B"));

        test("C", () -> log.add("C"));
    }

    public void brokenBeforeAll() {
        beforeAll(() -> {
            assert false;
        });

        test("unexecuted", () -> {});
    }

    public void brokenAfterAll() {
        afterAll(() -> {
            assert false;
        });

        test("unexecuted", () -> {});
    }
}
