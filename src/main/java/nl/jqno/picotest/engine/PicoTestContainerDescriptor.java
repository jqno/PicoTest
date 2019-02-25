package nl.jqno.picotest.engine;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

public class PicoTestContainerDescriptor extends AbstractTestDescriptor {
    public PicoTestContainerDescriptor(UniqueId uniqueId, Class<?> testClass) {
        super(uniqueId, testClass.getSimpleName());
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }
}
