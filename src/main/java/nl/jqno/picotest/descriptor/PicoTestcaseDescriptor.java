package nl.jqno.picotest.descriptor;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.FilePosition;

import java.util.Arrays;
import java.util.Optional;

public class PicoTestcaseDescriptor extends PicoTestDescriptor {
    private final boolean isSkipped;
    private final String skipReason;
    private final Runnable test;

    public PicoTestcaseDescriptor(PicoTestDescriptor parent, String displayName, boolean isSkipped, String skipReason, Runnable test) {
        super(parent, generateUniqueId(parent, displayName), parent.getTestClass(), displayName, determineSource(parent.getTestClass()));
        this.isSkipped = isSkipped;
        this.skipReason = skipReason;
        this.test = test;
    }

    private static UniqueId generateUniqueId(TestDescriptor parent, String description) {
        return parent.getUniqueId().append("case", description);
    }

    private static TestSource determineSource(Class<?> klass) {
        return Arrays.stream(new Throwable().getStackTrace())
                .filter(elt -> elt.getClassName().equals(klass.getCanonicalName()))
                .findFirst()
                .map(StackTraceElement::getLineNumber)
                .map(FilePosition::from)
                .map(pos -> ClassSource.from(klass, pos))
                .orElse(ClassSource.from(klass));
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
        return Optional.of(ClassSource.from("nl.jqno.picotest.examples.ExampleTest", FilePosition.from(10)));
    }
}
