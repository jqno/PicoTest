package nl.jqno.picotest.engine;

import nl.jqno.picotest.Test;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.filter.ClasspathScanningSupport;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PicoTestEngine implements TestEngine {
    private static final String ID = "picotest";
    private static final String DISPLAY_NAME = "PicoTest (JUnit Platform)";
    private static final Objenesis OBJENESIS = new ObjenesisStd();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
        var descriptor = new EngineDescriptor(uniqueId, DISPLAY_NAME);

        for (Class<?> testClass : allSelectedClasses(request)) {
            var classUniqueId = uniqueId.append("class", testClass.getCanonicalName());
            var classTestDescriptor = new PicoTestContainerDescriptor(classUniqueId, testClass);
            descriptor.addChild(classTestDescriptor);
            discoverTestCases(classTestDescriptor, testClass);
        }
        return descriptor;
    }

    private List<Class<?>> allSelectedClasses(EngineDiscoveryRequest request) {
        Predicate<Class<?>> classPredicate = (Class<?> c) -> Test.class.isAssignableFrom(c) && !Modifier.isPrivate(c.getModifiers());
        var classNamePredicate = ClasspathScanningSupport.buildClassNamePredicate(request);

        var result = new ArrayList<Class<?>>();
        request.getSelectorsByType(ModuleSelector.class).forEach(s -> {
            result.addAll(ReflectionSupport.findAllClassesInModule(s.getModuleName(), classPredicate, classNamePredicate));
        });
        request.getSelectorsByType(ClasspathRootSelector.class).forEach(s -> {
            result.addAll(ReflectionSupport.findAllClassesInClasspathRoot(s.getClasspathRoot(), classPredicate, classNamePredicate));
        });
        request.getSelectorsByType(PackageSelector.class).forEach(s -> {
            result.addAll(ReflectionSupport.findAllClassesInPackage(s.getPackageName(), classPredicate, classNamePredicate));
        });
        request.getSelectorsByType(ClassSelector.class).forEach(s -> {
            var c = s.getJavaClass();
            if (classPredicate.test(c) && classNamePredicate.test(c.getCanonicalName())) {
                result.add(s.getJavaClass());
            }
        });
        return result;
    }

    private void discoverTestCases(TestDescriptor descriptor, Class<?> testClass) {
        if (Test.class.isAssignableFrom(testClass)) {
            Test test = OBJENESIS.newInstance((Class<Test>)testClass);
            TestCollector collector = new TestCollector(descriptor);
            test.setCollector(collector);
            test.fixture();
        }
    }

    @Override
    public void execute(ExecutionRequest request) {
        var root = request.getRootTestDescriptor();
        var listener = request.getEngineExecutionListener();
        listener.executionStarted(root);
        executeContainer(root, listener);
        listener.executionFinished(root, TestExecutionResult.successful());
    }

    private void executeContainer(TestDescriptor testDescriptor, EngineExecutionListener listener) {
        listener.executionStarted(testDescriptor);
        testDescriptor.getChildren().forEach(d -> {
            if (d instanceof PicoTestContainerDescriptor) {
                executeContainer(d, listener);
            }
            if (d instanceof PicoTestDescriptor) {
                executeTest((PicoTestDescriptor)d, listener);
            }
        });
        listener.executionFinished(testDescriptor, TestExecutionResult.successful());
    }

    private void executeTest(PicoTestDescriptor testDescriptor, EngineExecutionListener listener) {
        listener.executionStarted(testDescriptor);
        try {
            testDescriptor.getTest().run();
        }
        catch (AssertionError e) {
            listener.executionFinished(testDescriptor, TestExecutionResult.failed(e));
            return;
        }
        catch (Throwable e) {
            listener.executionFinished(testDescriptor, TestExecutionResult.aborted(e));
            return;
        }
        listener.executionFinished(testDescriptor, TestExecutionResult.successful());
    }
}
