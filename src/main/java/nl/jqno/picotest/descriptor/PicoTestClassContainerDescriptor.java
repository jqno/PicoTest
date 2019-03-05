package nl.jqno.picotest.descriptor;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

public class PicoTestClassContainerDescriptor extends PicoTestDescriptor {
    public PicoTestClassContainerDescriptor(TestDescriptor parent, Class<?> klass) {
        super(parent, generateUniqueId(parent, klass.getCanonicalName()), klass, klass.getSimpleName(), ClassSource.from(klass));
    }

    private static UniqueId generateUniqueId(TestDescriptor parent, String description) {
        return parent.getUniqueId().append("class", description);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }
}
