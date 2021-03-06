package nl.jqno.picotest.engine;

import nl.jqno.picotest.Test;
import nl.jqno.picotest.descriptor.PicoTestClassContainerDescriptor;
import nl.jqno.picotest.descriptor.PicoTestDescriptor;
import nl.jqno.picotest.descriptor.PicoTestMethodContainerDescriptor;
import nl.jqno.picotest.descriptor.PicoTestcaseDescriptor;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.*;
import org.junit.platform.engine.support.filter.ClasspathScanningSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PicoTestDiscoverer {
    private final EngineDiscoveryRequest request;

    public PicoTestDiscoverer(EngineDiscoveryRequest request) {
        this.request = request;
    }

    public void discover(TestDescriptor rootDescriptor) {
        Predicate<Class<?>> classPredicate = (Class<?> c) -> Test.class.isAssignableFrom(c) && !Modifier.isPrivate(c.getModifiers());
        var classNamePredicate = ClasspathScanningSupport.buildClassNamePredicate(request);

        request.getSelectorsByType(ModuleSelector.class)
                .stream()
                .flatMap(s -> ReflectionSupport.findAllClassesInModule(s.getModuleName(), classPredicate, classNamePredicate).stream())
                .forEach(c -> resolveClass(rootDescriptor, c));

        request.getSelectorsByType(ClasspathRootSelector.class)
                .stream()
                .flatMap(s -> ReflectionSupport.findAllClassesInClasspathRoot(s.getClasspathRoot(), classPredicate, classNamePredicate).stream())
                .forEach(c -> resolveClass(rootDescriptor, c));

        request.getSelectorsByType(PackageSelector.class)
                .stream()
                .flatMap(s -> ReflectionSupport.findAllClassesInPackage(s.getPackageName(), classPredicate, classNamePredicate).stream())
                .forEach(c -> resolveClass(rootDescriptor, c));

        request.getSelectorsByType(ClassSelector.class)
                .stream()
                .filter(s -> classPredicate.test(s.getJavaClass()) && classNamePredicate.test(s.getJavaClass().getCanonicalName()))
                .forEach(s -> resolveClass(rootDescriptor, s.getJavaClass()));

        request.getSelectorsByType(MethodSelector.class)
                .stream()
                .filter(s -> classPredicate.test(s.getJavaClass()) && classNamePredicate.test(s.getJavaClass().getCanonicalName()))
                .filter(s -> s.getMethodParameterTypes().equals(""))
                .forEach(s -> resolveClassWithMethod(rootDescriptor, s.getJavaClass(), s.getMethodName()));

        request.getSelectorsByType(UniqueIdSelector.class)
                .forEach(s -> resolveUniqueId(rootDescriptor, s.getUniqueId()));
    }

    private void resolveClass(TestDescriptor descriptor, Class<?> c) {
        var classTestDescriptor = new PicoTestClassContainerDescriptor(descriptor, c);
        var methods = findMethods(c);
        var instance = instantiate(c);
        instance.ifPresent(i -> methods.forEach(m -> resolveMethod(classTestDescriptor, i, m)));
    }

    private List<Method> findMethods(Class<?> c) {
        return Arrays.stream(c.getDeclaredMethods())
                .filter(m -> !m.isSynthetic() && !Modifier.isPrivate(m.getModifiers()))
                .filter(m -> m.getParameterCount() == 0 && m.getReturnType().equals(void.class))
                .collect(Collectors.toList());
    }

    private void resolveClassWithMethod(TestDescriptor descriptor, Class<?> c, String methodName) {
        var classTestDescriptor = new PicoTestClassContainerDescriptor(descriptor, c);
        var method = methodFor(c, methodName);
        var instance = instantiate(c);
        method.ifPresent(m -> instance.ifPresent(i -> resolveMethod(classTestDescriptor, i, m)));
    }

    private void resolveMethod(PicoTestDescriptor descriptor, Test instance, Method method) {
        var methodTestDescriptor = new PicoTestMethodContainerDescriptor(descriptor, method);
        resolveTestcases(methodTestDescriptor, instance, method);
    }

    private void resolveTestcases(PicoTestDescriptor descriptor, Test instance, Method method) {
        discoverTestcases(descriptor, instance, method)
                .forEach(descriptor::addChild);
    }

    private void resolveUniqueId(TestDescriptor descriptor, UniqueId selectedUniqueId) {
        var segments = selectedUniqueId.getSegments();
        var klass = Optional.ofNullable(segments.get(1)).map(UniqueId.Segment::getValue).flatMap(this::classForName);
        var methodName = Optional.ofNullable(segments.get(2)).map(UniqueId.Segment::getValue);
        var testcaseName = Optional.ofNullable(segments.get(3)).map(UniqueId.Segment::getValue);

        if (testcaseName.isPresent() && methodName.isPresent() && klass.isPresent()) {
            var instance = instantiate(klass.get());
            var method = methodFor(klass.get(), methodName.get());
            method.ifPresent(m -> instance.ifPresent(i -> {
                var classDescriptor = new PicoTestClassContainerDescriptor(descriptor, klass.get());
                var methodDescriptor = new PicoTestMethodContainerDescriptor(classDescriptor, m);
                discoverTestcases(methodDescriptor, i, m)
                        .stream()
                        .filter(d -> d.getUniqueId().equals(selectedUniqueId))
                        .forEach(descriptor::addChild);
            }));
        } else if (methodName.isPresent() && klass.isPresent()) {
            resolveClassWithMethod(descriptor, klass.get(), methodName.get());
        } else if (klass.isPresent()) {
            resolveClass(descriptor, klass.get());
        }
    }

    private List<PicoTestcaseDescriptor> discoverTestcases(PicoTestDescriptor parent, Test instance, Method method) {
        var collector = new TestCollector(parent);
        instance.setCollector(collector);
        try {
            method.invoke(instance);
            return collector.getTests();
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println(method);
            throw new RuntimeException(e);
        }
    }

    private Optional<Class<?>> classForName(String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Method> methodFor(Class<?> c, String methodName) {
        try {
            return Optional.ofNullable(c.getMethod(methodName));
        } catch (NoSuchMethodException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Test> instantiate(Class<?> c) {
        try {
            var constructor = c.getConstructor();
            return Optional.of((Test)constructor.newInstance());
        }
        catch (ReflectiveOperationException | ClassCastException ignored) {
            return Optional.empty();
        }
    }
}
