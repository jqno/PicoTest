package nl.jqno.picotest.descriptor;

import nl.jqno.picotest.engine.Modifier;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.HashMap;
import java.util.Map;

public abstract class PicoTestDescriptor extends AbstractTestDescriptor {
    private static final Runnable EMPTY_BLOCK = () -> {};

    private final Class<?> testClass;
    private final Map<Modifier, Runnable> modifiers = new HashMap<>();

    protected PicoTestDescriptor(TestDescriptor parent, UniqueId uniqueId, Class<?> testClass, String displayName, TestSource source) {
        super(uniqueId, displayName, source);
        parent.addChild(this);
        this.setParent(parent);
        this.testClass = testClass;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public void acceptModifier(Modifier modifier, Runnable block) {
        modifiers.put(modifier, block);
    }

    public Runnable getBlock(Modifier modifier) {
        return modifiers.getOrDefault(modifier, EMPTY_BLOCK);
    }
}
