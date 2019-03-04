package nl.jqno.picotest.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestCollector {
    private final TestDescriptor parent;
    private final List<PicoTestcaseDescriptor> tests = new ArrayList<>();

    public TestCollector(TestDescriptor parent) {
        this.parent = parent;
    }

    public void accept(String description, Runnable test) {
        accept(description, false, "", test);
    }

    public void acceptSkip(String description, String reason, Runnable test) {
        accept(description, true, reason, test);
    }

    private void accept(String description, boolean isSkipped, String reason, Runnable test) {
        tests.add(new PicoTestcaseDescriptor(parent, description, isSkipped, reason, test));
    }

    public List<PicoTestcaseDescriptor> getTests() {
        return Collections.unmodifiableList(tests);
    }
}
