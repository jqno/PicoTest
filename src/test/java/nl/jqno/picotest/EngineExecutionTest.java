package nl.jqno.picotest;

import nl.jqno.picotest.examples.BeforeAndAfterAllTest;
import nl.jqno.picotest.examples.BeforeAndAfterEachTest;
import nl.jqno.picotest.examples.ExampleTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void verifyBeforeAndAfterEach() {
        BeforeAndAfterEachTest.log.clear();
        var results = EngineTestKit
                .engine("picotest")
                .selectors(selectClass(BeforeAndAfterEachTest.class))
                .execute();
        results.containers().assertStatistics(stats -> stats
                .started(5)
                .succeeded(5));
        results.tests().assertStatistics(stats -> stats
                .started(5)
                .succeeded(3)
                .failed(2));
        assertEquals(List.of("before", "A", "after", "before", "B", "after", "before", "C", "after"), BeforeAndAfterEachTest.log);
    }

    @Test
    void verifyBeforeAndAfterAll() {
        BeforeAndAfterAllTest.log.clear();
        EngineTestKit
                .engine("picotest")
                .selectors(selectClass(BeforeAndAfterAllTest.class))
                .execute()
                .containers()
                .assertStatistics(stats -> stats
                        .started(5)
                        .succeeded(3)
                        .failed(2));
        assertEquals(List.of("before", "A", "B", "C", "after"), BeforeAndAfterAllTest.log);
    }
}
