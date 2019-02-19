package nl.jqno.picotest;

public abstract class Test {

    public abstract void fixture();

    public final void test(String description, Runnable test) {
        // TODO
    }
}
