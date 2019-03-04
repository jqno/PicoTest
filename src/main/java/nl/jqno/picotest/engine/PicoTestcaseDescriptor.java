package nl.jqno.picotest.engine;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.Optional;

public class PicoTestcaseDescriptor extends AbstractTestDescriptor {
    private final TestDescriptor parent;
    private final boolean isSkipped;
    private final String skipReason;
    private final Runnable test;

    public PicoTestcaseDescriptor(TestDescriptor parent, String displayName, boolean isSkipped, String skipReason, Runnable test) {
        super(generateUniqueId(parent, displayName), displayName);
        this.parent = parent;
        this.setParent(parent);
        this.isSkipped = isSkipped;
        this.skipReason = skipReason;
        this.test = test;
    }

    private static UniqueId generateUniqueId(TestDescriptor parent, String description) {
        return parent.getUniqueId().append("case", description);
    }

    public Runnable getTest() {
        return test;
    }

    public boolean isSkipped() {
        return isSkipped;
    }

    public String getSkipReason() {
        return skipReason;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return parent.getSource();
    }
}
