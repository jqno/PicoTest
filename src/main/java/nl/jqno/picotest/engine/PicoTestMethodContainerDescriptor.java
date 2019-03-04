package nl.jqno.picotest.engine;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;

import java.lang.reflect.Method;
import java.util.Optional;

public class PicoTestMethodContainerDescriptor extends AbstractTestDescriptor {
    private final Method method;

    public PicoTestMethodContainerDescriptor(TestDescriptor parent, Method method) {
        super(generateUniqueId(parent, method.getName()), method.getName());
        this.setParent(parent);
        this.method = method;
    }

    private static UniqueId generateUniqueId(TestDescriptor parent, String description) {
        return parent.getUniqueId().append("method", description);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(MethodSource.from(method));
    }
}
