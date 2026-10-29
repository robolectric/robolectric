package org.robolectric.junit.platform.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;

/**
 * A JUnit Jupiter test method ({@code @Test}, {@code @TestTemplate}, {@code @TestFactory}, ...) of
 * a top-level test class or of one of its {@code @Nested} classes.
 */
final class TestMethod {
  /** The classes enclosing {@link #testClass}, outermost first; empty for a top-level class. */
  final List<Class<?>> enclosingClasses;

  final Class<?> testClass;
  final Method method;

  private TestMethod(List<Class<?>> enclosingClasses, Class<?> testClass, Method method) {
    this.enclosingClasses = enclosingClasses;
    this.testClass = testClass;
    this.method = method;
  }

  /** The class whose configuration applies to this test: the top-level class. */
  Class<?> configurationClass() {
    return enclosingClasses.isEmpty() ? testClass : enclosingClasses.get(0);
  }

  DiscoverySelector selector() {
    return enclosingClasses.isEmpty()
        ? DiscoverySelectors.selectMethod(testClass, method)
        : DiscoverySelectors.selectNestedMethod(enclosingClasses, testClass, method);
  }

  /** Returns the tests of {@code topLevelClass} and its {@code @Nested} classes. */
  static List<TestMethod> collect(Class<?> topLevelClass) {
    List<TestMethod> result = new ArrayList<>();
    collect(Collections.emptyList(), topLevelClass, result);
    return result;
  }

  private static void collect(List<Class<?>> enclosing, Class<?> testClass, List<TestMethod> out) {
    for (Method method :
        ReflectionSupport.findMethods(
            testClass,
            m ->
                AnnotationSupport.isAnnotated(m, Testable.class)
                    && !Modifier.isStatic(m.getModifiers()),
            HierarchyTraversalMode.TOP_DOWN)) {
      out.add(new TestMethod(enclosing, testClass, method));
    }
    List<Class<?>> nestedEnclosing = new ArrayList<>(enclosing);
    nestedEnclosing.add(testClass);
    for (Class<?> nested :
        ReflectionSupport.findNestedClasses(
            testClass, c -> AnnotationSupport.isAnnotated(c, Nested.class))) {
      collect(Collections.unmodifiableList(nestedEnclosing), nested, out);
    }
  }
}
