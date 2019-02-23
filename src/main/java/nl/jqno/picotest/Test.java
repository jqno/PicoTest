package nl.jqno.picotest;

import nl.jqno.picotest.engine.TestCollector;

public abstract class Test {

    public abstract void fixture();
    private TestCollector collector = null;

    public final void test(String description, Runnable test) {
        collector.accept(description, test);
    }

    public void setCollector(TestCollector collector) {
        this.collector = collector;
    }
}
