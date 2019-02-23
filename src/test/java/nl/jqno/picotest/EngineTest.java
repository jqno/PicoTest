package nl.jqno.picotest;

import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class EngineTest {

    @Test
    void verifyTestExecution() {
        var execution = EngineTestKit
                .engine("picotest")
                .selectors(selectClass(ExampleTestCase.class))
                .execute();
        execution.containers()
                .assertStatistics(stats -> stats.started(3).succeeded(3));
        execution.tests()
                .assertStatistics(stats -> stats.started(1).succeeded(1));
    }

}
