package nl.jqno.picotest;

import nl.jqno.picotest.engine.TestCollector;
import org.junit.platform.commons.annotation.Testable;

public abstract class Test {

    public abstract void fixture();
    private TestCollector collector = null;

    @Testable
    public final void test(String description, Runnable test) {
        collector.accept(description, test);
    }

    public void setCollector(TestCollector collector) {
        this.collector = collector;
    }
}
