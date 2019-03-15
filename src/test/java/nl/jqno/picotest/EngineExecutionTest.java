package nl.jqno.picotest;

import nl.jqno.picotest.examples.AfterEachTest;
import nl.jqno.picotest.examples.BeforeEachTest;
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
                        .failed(2)
                        .aborted(0)
                        .skipped(1)
                        .finished(4));
    }

    @Test
    void verifyBeforeEach() {
        EngineTestKit
                .engine("picotest")
                .selectors(selectClass(BeforeEachTest.class))
                .execute()
                .tests()
                .assertStatistics(stats -> stats
                        .started(4)
                        .succeeded(3)
                        .failed(1));
    }

    @Test
    void verifyAfterEach() {
        EngineTestKit
                .engine("picotest")
                .selectors(selectClass(AfterEachTest.class))
                .execute()
                .tests()
                .assertStatistics(stats -> stats
                        .started(4)
                        .succeeded(3)
                        .failed(1));
    }

}
