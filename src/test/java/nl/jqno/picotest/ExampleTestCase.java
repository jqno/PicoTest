package nl.jqno.picotest;

public class ExampleTestCase extends Test {

    @Override
    public void fixture() {
        test("1 + 1 == 2", () -> {
            assert 1 + 1 == 2;
        });
    }
}
