package nl.jqno.picotest.descriptor;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;

public class PicoTestMethodContainerDescriptor extends PicoTestDescriptor {
    public PicoTestMethodContainerDescriptor(PicoTestDescriptor parent, Method method) {
        super(parent, generateUniqueId(parent, method.getName()), parent.getTestClass(), method.getName(), MethodSource.from(method));
    }

    private static UniqueId generateUniqueId(TestDescriptor parent, String description) {
        return parent.getUniqueId().append("method", description);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }
}
