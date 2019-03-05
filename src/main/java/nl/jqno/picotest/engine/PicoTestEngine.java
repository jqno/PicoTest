package nl.jqno.picotest.engine;

import nl.jqno.picotest.descriptor.PicoTestcaseDescriptor;
import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

public class PicoTestEngine implements TestEngine {
    private static final String ID = "picotest";
    private static final String DISPLAY_NAME = "PicoTest (JUnit Platform)";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
        var descriptor = new EngineDescriptor(uniqueId, DISPLAY_NAME);
        var discoverer = new PicoTestDiscoverer(request);
        discoverer.discover(descriptor);
        return descriptor;
    }

    @Override
    public void execute(ExecutionRequest request) {
        var root = request.getRootTestDescriptor();
        var listener = request.getEngineExecutionListener();
        executeContainer(root, listener);
    }

    private void executeContainer(TestDescriptor testDescriptor, EngineExecutionListener listener) {
        listener.executionStarted(testDescriptor);
        testDescriptor.getChildren().forEach(d -> {
            if (d.isContainer()) {
                executeContainer(d, listener);
            }
            if (d.isTest()) {
                executeTest((PicoTestcaseDescriptor)d, listener);
            }
        });
        listener.executionFinished(testDescriptor, TestExecutionResult.successful());
    }

    private void executeTest(PicoTestcaseDescriptor testDescriptor, EngineExecutionListener listener) {
        if (testDescriptor.isSkipped()) {
            listener.executionSkipped(testDescriptor, testDescriptor.getSkipReason());
        }
        else {
            listener.executionStarted(testDescriptor);
            try {
                testDescriptor.getTest().run();
            }
            catch (Throwable e) {
                listener.executionFinished(testDescriptor, TestExecutionResult.failed(e));
                return;
            }
            listener.executionFinished(testDescriptor, TestExecutionResult.successful());
        }
    }
}
