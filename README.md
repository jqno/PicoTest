# PicoTest

An ultra-minimal unit-testing framework, based on the JUnit 5 Platform.

How to use:

```java
import nl.jqno.picotest.Test;

public class ExampleTest extends Test {

    public void demo() {
        test("a succeeding test", () -> {
            assert 1 + 1 == 2;
        });

        test("a failing test", () -> {
            assert 1 + 1 == 3;
        });

        test("an errored test", () -> {
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
```