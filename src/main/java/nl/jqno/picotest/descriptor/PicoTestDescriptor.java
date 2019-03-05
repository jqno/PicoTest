package nl.jqno.picotest.descriptor;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

public abstract class PicoTestDescriptor extends AbstractTestDescriptor {
    private final Class<?> testClass;

    protected PicoTestDescriptor(TestDescriptor parent, UniqueId uniqueId, Class<?> testClass, String displayName, TestSource source) {
        super(uniqueId, displayName, source);
        parent.addChild(this);
        this.setParent(parent);
        this.testClass = testClass;
    }

    public Class<?> getTestClass() {
        return testClass;
    }
}
