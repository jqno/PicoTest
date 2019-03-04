package nl.jqno.picotest.engine;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

public class PicoTestDescriptor extends AbstractTestDescriptor {
    private final boolean isSkipped;
    private final String skipReason;
    private final Runnable test;

    public PicoTestDescriptor(UniqueId uniqueId, String displayName, boolean isSkipped, String skipReason, Runnable test) {
        super(uniqueId, displayName);
        this.isSkipped = isSkipped;
        this.skipReason = skipReason;
        this.test = test;
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
}
