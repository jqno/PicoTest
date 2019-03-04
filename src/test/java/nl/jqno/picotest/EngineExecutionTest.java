package nl.jqno.picotest;

import nl.jqno.picotest.examples.ExampleTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class EngineExecutionTest {

    @Test
    void verifyTestExecution() {
        EngineTestKit
                .engine("picotest")
                .selectors(selectClass(ExampleTest.class))
                .execute()
                .tests()
                .assertStatistics(stats -> stats
                        .started(4)
                        .succeeded(2)
                        .failed(1)
                        .aborted(1)
                        .finished(4));
    }

}
