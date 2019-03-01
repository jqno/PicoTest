package nl.jqno.picotest.engine;

import org.junit.platform.engine.UniqueId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCollector {
    private final UniqueId uniqueId;
    private final List<PicoTestDescriptor> tests = new ArrayList<>();

    public TestCollector(UniqueId uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void accept(String description, Runnable test) {
        var testcaseId = uniqueId.append("case", description);
        var d = new PicoTestDescriptor(testcaseId, description, test);
        tests.add(d);
    }

    public List<PicoTestDescriptor> getTests() {
        return Collections.unmodifiableList(tests);
    }
}
