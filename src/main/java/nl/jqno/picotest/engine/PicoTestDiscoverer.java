package nl.jqno.picotest.engine;

import nl.jqno.picotest.Test;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.*;
import org.junit.platform.engine.support.filter.ClasspathScanningSupport;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PicoTestDiscoverer {
    private static final Objenesis OBJENESIS = new ObjenesisStd();
    private final EngineDiscoveryRequest request;

    public PicoTestDiscoverer(EngineDiscoveryRequest request) {
        this.request = request;
    }

    public void discover(TestDescriptor rootDescriptor) {
        discoverSelectedClasses(request, rootDescriptor);
    }

    private void discoverSelectedClasses(EngineDiscoveryRequest request, TestDescriptor descriptor) {
        Predicate<Class<?>> classPredicate = (Class<?> c) -> Test.class.isAssignableFrom(c) && !Modifier.isPrivate(c.getModifiers());
        var classNamePredicate = ClasspathScanningSupport.buildClassNamePredicate(request);

        request.getSelectorsByType(ModuleSelector.class)
                .stream()
                .flatMap(s -> ReflectionSupport.findAllClassesInModule(s.getModuleName(), classPredicate, classNamePredicate).stream())
                .forEach(c -> resolveClass(descriptor, c));

        request.getSelectorsByType(ClasspathRootSelector.class)
                .stream()
                .flatMap(s -> ReflectionSupport.findAllClassesInClasspathRoot(s.getClasspathRoot(), classPredicate, classNamePredicate).stream())
                .forEach(c -> resolveClass(descriptor, c));

        request.getSelectorsByType(PackageSelector.class)
                .stream()
                .flatMap(s -> ReflectionSupport.findAllClassesInPackage(s.getPackageName(), classPredicate, classNamePredicate).stream())
                .forEach(c -> resolveClass(descriptor, c));

        request.getSelectorsByType(ClassSelector.class)
                .stream()
                .filter(s -> classPredicate.test(s.getJavaClass()) && classNamePredicate.test(s.getJavaClass().getCanonicalName()))
                .forEach(s -> resolveClass(descriptor, s.getJavaClass()));

        request.getSelectorsByType(MethodSelector.class)
                .stream()
                .filter(s -> classPredicate.test(s.getJavaClass()) && classNamePredicate.test(s.getJavaClass().getCanonicalName()))
                .filter(s -> s.getMethodParameterTypes().equals(""))
                .forEach(s -> resolveClassWithMethod(descriptor, s.getJavaClass(), s.getMethodName()));
    }

    private void resolveClass(TestDescriptor descriptor, Class<?> c) {
        var classTestDescriptor = classDescriptorFor(descriptor, c);
        var methods = findMethods(c);
        methods.forEach(m -> resolveMethod(classTestDescriptor, instantiate(c), m));
    }

    private List<Method> findMethods(Class<?> c) {
        return Arrays.stream(c.getDeclaredMethods())
                .filter(m -> !m.isSynthetic() && !Modifier.isPrivate(m.getModifiers()))
                .filter(m -> m.getParameterCount() == 0 && m.getReturnType().equals(void.class))
                .collect(Collectors.toList());
    }

    private void resolveClassWithMethod(TestDescriptor descriptor, Class<?> c, String method) {
        try {
            var classTestDescriptor = classDescriptorFor(descriptor, c);
            resolveMethod(classTestDescriptor, instantiate(c), c.getMethod(method));
        }
        catch (NoSuchMethodException ignored) {}
    }

    private void resolveMethod(TestDescriptor descriptor, Test instance, Method method) {
        var methodTestDescriptor = methodDescriptorFor(descriptor, method);
        resolveTestcase(methodTestDescriptor, instance, method);
    }

    private void resolveTestcase(TestDescriptor descriptor, Test instance, Method method) {
        instance.setCollector(new TestCollector(descriptor));
        try {
            method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println(method);
            throw new RuntimeException(e);
        }
    }

    private Test instantiate(Class<?> c) {
        return OBJENESIS.newInstance((Class<? extends Test>)c);
    }

    private TestDescriptor classDescriptorFor(TestDescriptor descriptor, Class<?> c) {
        return containerDescriptorFor(descriptor, c.getSimpleName(), "class", c.getName());
    }

    private TestDescriptor methodDescriptorFor(TestDescriptor descriptor, Method m) {
        return containerDescriptorFor(descriptor, m.getName(), "method", m.getName());
    }

    private TestDescriptor containerDescriptorFor(TestDescriptor descriptor, String displayName, String type, String value) {
        var classUniqueId = descriptor.getUniqueId().append(type, value);
        var result = new PicoTestContainerDescriptor(classUniqueId, displayName);
        descriptor.addChild(result);
        return result;
    }
}
