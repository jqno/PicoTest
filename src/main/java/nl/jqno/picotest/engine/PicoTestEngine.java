package nl.jqno.picotest.engine;

import nl.jqno.picotest.descriptor.PicoTestDescriptor;
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

        boolean success = runBlock(testDescriptor, Modifier.BEFORE_ALL, listener);
        testDescriptor.getChildren().forEach(d -> {
            if (d.isContainer()) {
                executeContainer(d, listener);
            }
            if (d.isTest()) {
                executeTest((PicoTestcaseDescriptor)d, listener);
            }
        });
        success &= runBlock(testDescriptor, Modifier.AFTER_ALL, listener);

        if (success) {
            listener.executionFinished(testDescriptor, TestExecutionResult.successful());
        }
    }

    private boolean runBlock(TestDescriptor testDescriptor, Modifier modifier, EngineExecutionListener listener) {
       if (testDescriptor instanceof PicoTestDescriptor) {
           PicoTestDescriptor ptd = (PicoTestDescriptor)testDescriptor;
           try {
               ptd.getBlock(modifier).run();
           }
           catch (Throwable e) {
               listener.executionFinished(testDescriptor, TestExecutionResult.failed(e));
               return false;
           }
       }
       return true;
    }

    private void executeTest(PicoTestcaseDescriptor testDescriptor, EngineExecutionListener listener) {
        if (testDescriptor.isSkipped()) {
            listener.executionSkipped(testDescriptor, testDescriptor.getSkipReason());
        }
        else {
            listener.executionStarted(testDescriptor);
            try {
                testDescriptor.runFromParent(Modifier.BEFORE_EACH);
                testDescriptor.getTest().run();
                testDescriptor.runFromParent(Modifier.AFTER_EACH);
            }
            catch (Throwable e) {
                listener.executionFinished(testDescriptor, TestExecutionResult.failed(e));
                return;
            }
            listener.executionFinished(testDescriptor, TestExecutionResult.successful());
        }
    }
}
