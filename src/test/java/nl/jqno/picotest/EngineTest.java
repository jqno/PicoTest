package nl.jqno.picotest;

import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class EngineTest {

    @Test
    void verifyTestStats() {
        EngineTestKit
                .engine("picotest")
                .selectors(selectClass(ExampleTestCase.class))
                .execute()
                .containers()
                .assertStatistics(stats -> stats.succeeded(1).started(1));
    }
}
