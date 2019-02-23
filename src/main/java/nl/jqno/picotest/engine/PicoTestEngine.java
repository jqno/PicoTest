package nl.jqno.picotest.engine;

import nl.jqno.picotest.Test;
import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

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
        try {
            var testClassName = "nl.jqno.picotest.ExampleTestCase";
            var exampleUniqueId = uniqueId.append("class", testClassName);
            var exampleTestCase = new PicoTestContainerDescriptor(exampleUniqueId, testClassName);
            descriptor.addChild(exampleTestCase);
            discoverTestCases(exampleTestCase, Class.forName(testClassName));
        } catch (ClassNotFoundException e) {
            // ¯\_(ツ)_/¯
        }
        return descriptor;
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
