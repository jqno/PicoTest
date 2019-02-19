package nl.jqno.picotest.engine;

import org.junit.platform.engine.*;

public class PicoTestEngine implements TestEngine {
    @Override
    public String getId() {
        return "picotest";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        return null;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {

    }
}
