package nl.jqno.picotest.engine;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;

import java.util.Optional;

public class PicoTestClassContainerDescriptor extends AbstractTestDescriptor {
    private Class<?> klass;

    public PicoTestClassContainerDescriptor(TestDescriptor parent, Class<?> klass) {
        super(generateUniqueId(parent, klass.getCanonicalName()), klass.getSimpleName());
        this.setParent(parent);
        this.klass = klass;
    }

    private static UniqueId generateUniqueId(TestDescriptor parent, String description) {
        return parent.getUniqueId().append("class", description);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(ClassSource.from(klass));
    }
}
