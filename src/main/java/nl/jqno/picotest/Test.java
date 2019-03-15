package nl.jqno.picotest;

import nl.jqno.picotest.engine.Modifier;
import nl.jqno.picotest.engine.TestCollector;
import org.junit.platform.commons.annotation.Testable;

public abstract class Test {

    private TestCollector collector = null;

    @Testable
    public final void test(String description, Runnable test) {
        collector.accept(description, test);
    }

    @Testable
    public final void skip(String reason, String description, Runnable test) {
        collector.acceptSkip(description, reason, test);
    }

    public final void beforeEach(Runnable block) {
        collector.acceptBlock(Modifier.BEFORE_EACH, block);
    }

    public final void afterEach(Runnable block) {
        collector.acceptBlock(Modifier.AFTER_EACH, block);
    }

    public void setCollector(TestCollector collector) {
        this.collector = collector;
    }
}
