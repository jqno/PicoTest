package nl.jqno.picotest.engine;

import org.junit.platform.engine.TestDescriptor;

public class TestCollector {
    private TestDescriptor descriptor;

    public TestCollector(TestDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public void accept(String description, Runnable test) {
        var uniqueId = descriptor.getUniqueId().append("case", description);
        var d = new PicoTestDescriptor(uniqueId, description, test);
        descriptor.addChild(d);
    }
}
