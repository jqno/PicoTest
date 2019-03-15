package nl.jqno.picotest;

import nl.jqno.picotest.examples.selectors.SelectorTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

public class EngineSelectorTest {

    private static final Class<?> testToSelect = SelectorTest.class;
    private final EngineTestKit.Builder engine = EngineTestKit.engine("picotest");

    @Test
    void moduleSelector() {
        // Running the tests on the classpath, so only the root container will trigger
        verifySelector(1, 1, selectModule("nl.jqno.picotest"));
    }

    @Test
    void classpathRootSelector() throws URISyntaxException {
        var classpathRoot = Paths.get(testToSelect.getProtectionDomain().getCodeSource().getLocation().toURI());
        verifySelector(14, 12, selectClasspathRoots(Collections.singleton(classpathRoot)).get(0));
    }

    @Test
    void packageSelector() {
        verifySelector(3, 3, selectPackage(testToSelect.getPackageName()));
    }

    @Test
    void classSelector() {
        verifySelector(3, 3, selectClass(testToSelect));
    }

    @Test
    void methodSelector() {
        verifySelector(3, 3, selectMethod(testToSelect, "test"));
    }

    @Test
    void uniqueIdSelector() {
        var uniqueId = UniqueId.forEngine("picotest")
                .append("class", testToSelect.getCanonicalName())
                .append("method", "test")
                .append("case", "test");
        verifySelector(3, 3, selectUniqueId(uniqueId));
    }

    private void verifySelector(int total, int successful, DiscoverySelector... selectors) {
        engine
                .selectors(selectors)
                .execute()
                .containers()
                .assertStatistics(stats -> stats.started(total).succeeded(successful).finished(total));
    }

}
