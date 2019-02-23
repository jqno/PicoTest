package nl.jqno.picotest.engine;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

public class PicoTestDescriptor extends AbstractTestDescriptor {
    private final Runnable test;

    public PicoTestDescriptor(UniqueId uniqueId, String displayName, Runnable test) {
        super(uniqueId, displayName);
        this.test = test;
    }

    public Runnable getTest() {
        return test;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }
}
