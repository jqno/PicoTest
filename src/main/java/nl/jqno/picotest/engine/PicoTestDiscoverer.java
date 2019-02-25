package nl.jqno.picotest.engine;

import nl.jqno.picotest.Test;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.filter.ClasspathScanningSupport;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PicoTestDiscoverer {
    private static final Objenesis OBJENESIS = new ObjenesisStd();
    private final EngineDiscoveryRequest request;
    private final TestDescriptor rootDescriptor;

    public PicoTestDiscoverer(EngineDiscoveryRequest request, TestDescriptor rootDescriptor) {
        this.request = request;
        this.rootDescriptor = rootDescriptor;
    }

    public void discover() {
        for (Class<?> testClass : allSelectedClasses(request)) {
            var classUniqueId = rootDescriptor.getUniqueId().append("class", testClass.getCanonicalName());
            var classTestDescriptor = new PicoTestContainerDescriptor(classUniqueId, testClass);
            rootDescriptor.addChild(classTestDescriptor);
            discoverTestCases(classTestDescriptor, testClass);
        }
    }

    private List<Class<?>> allSelectedClasses(EngineDiscoveryRequest request) {
        Predicate<Class<?>> classPredicate = (Class<?> c) -> Test.class.isAssignableFrom(c) && !Modifier.isPrivate(c.getModifiers());
        var classNamePredicate = ClasspathScanningSupport.buildClassNamePredicate(request);

        var result = new ArrayList<Class<?>>();
        request.getSelectorsByType(ModuleSelector.class).forEach(s -> {
            result.addAll(ReflectionSupport.findAllClassesInModule(s.getModuleName(), classPredicate, classNamePredicate));
        });
        request.getSelectorsByType(ClasspathRootSelector.class).forEach(s -> {
            result.addAll(ReflectionSupport.findAllClassesInClasspathRoot(s.getClasspathRoot(), classPredicate, classNamePredicate));
        });
        request.getSelectorsByType(PackageSelector.class).forEach(s -> {
            result.addAll(ReflectionSupport.findAllClassesInPackage(s.getPackageName(), classPredicate, classNamePredicate));
        });
        request.getSelectorsByType(ClassSelector.class).forEach(s -> {
            var c = s.getJavaClass();
            if (classPredicate.test(c) && classNamePredicate.test(c.getCanonicalName())) {
                result.add(s.getJavaClass());
            }
        });
        return result;
    }

    private void discoverTestCases(TestDescriptor descriptor, Class<?> testClass) {
        if (Test.class.isAssignableFrom(testClass)) {
            Test test = OBJENESIS.newInstance((Class<Test>)testClass);
            TestCollector collector = new TestCollector(descriptor);
            test.setCollector(collector);
            test.fixture();
        }
    }
}
