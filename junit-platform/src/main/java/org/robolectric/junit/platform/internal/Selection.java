package org.robolectric.junit.platform.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.NestedClassSelector;
import org.junit.platform.engine.discovery.NestedMethodSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * The test classes and tests selected by a discovery request, in terms of top-level classes.
 *
 * <p>Unique ID selectors are resolved to their top-level class; the resulting test plan is then
 * pruned to the selected unique IDs (see {@link #prune}).
 */
public final class Selection {
  /** Selects tests of one top-level class. */
  interface Item {
    boolean selects(TestMethod test);
  }

  private static final Item WHOLE_CLASS = test -> true;

  private final Map<Class<?>, List<Item>> itemsByClass = new LinkedHashMap<>();
  private final List<UniqueId> uniqueIds = new ArrayList<>();

  public static Selection of(
      EngineDiscoveryRequest request, UniqueId engineId, Predicate<Class<?>> isCandidate) {
    Selection selection = new Selection();
    Predicate<String> classNameFilter = classNameFilter(request);
    Predicate<Class<?>> accept = c -> isCandidate.test(c) && classNameFilter.test(c.getName());

    for (ClassSelector selector : request.getSelectorsByType(ClassSelector.class)) {
      tryLoad(selector::getJavaClass).filter(accept).ifPresent(c -> selection.add(c, WHOLE_CLASS));
    }
    for (MethodSelector selector : request.getSelectorsByType(MethodSelector.class)) {
      Optional<Class<?>> testClass = tryLoad(selector::getJavaClass).filter(accept);
      Optional<Method> method = tryLoad(selector::getJavaMethod);
      if (testClass.isPresent() && method.isPresent()) {
        selection.add(
            testClass.get(), methodItem(Collections.emptyList(), testClass.get(), method.get()));
      }
    }
    for (NestedClassSelector selector : request.getSelectorsByType(NestedClassSelector.class)) {
      List<Class<?>> chain = new ArrayList<>(selector.getEnclosingClasses());
      chain.add(selector.getNestedClass());
      Class<?> topLevel = chain.get(0);
      if (accept.test(topLevel)) {
        selection.add(topLevel, nestedClassItem(chain));
      }
    }
    for (NestedMethodSelector selector : request.getSelectorsByType(NestedMethodSelector.class)) {
      List<Class<?>> enclosing = selector.getEnclosingClasses();
      if (accept.test(enclosing.get(0))) {
        selection.add(
            enclosing.get(0),
            methodItem(enclosing, selector.getNestedClass(), selector.getMethod()));
      }
    }
    for (PackageSelector selector : request.getSelectorsByType(PackageSelector.class)) {
      for (Class<?> c :
          ReflectionSupport.findAllClassesInPackage(
              selector.getPackageName(), accept, unused -> true)) {
        selection.add(c, WHOLE_CLASS);
      }
    }
    for (ClasspathRootSelector selector : request.getSelectorsByType(ClasspathRootSelector.class)) {
      for (Class<?> c :
          ReflectionSupport.findAllClassesInClasspathRoot(
              selector.getClasspathRoot(), accept, unused -> true)) {
        selection.add(c, WHOLE_CLASS);
      }
    }
    for (UniqueIdSelector selector : request.getSelectorsByType(UniqueIdSelector.class)) {
      UniqueId uniqueId = selector.getUniqueId();
      if (!uniqueId.hasPrefix(engineId)) {
        continue;
      }
      selection.uniqueIds.add(uniqueId);
      for (UniqueId.Segment segment : uniqueId.getSegments()) {
        if (segment.getType().equals("class")) {
          ReflectionSupport.tryToLoadClass(segment.getValue())
              .toOptional()
              .filter(accept)
              .ifPresent(c -> selection.add(c, WHOLE_CLASS));
          break;
        }
      }
    }
    return selection;
  }

  Map<Class<?>, List<Item>> itemsByClass() {
    return itemsByClass;
  }

  static boolean selectsWholeClass(List<Item> items) {
    return items.contains(WHOLE_CLASS);
  }

  static boolean selects(List<Item> items, TestMethod test) {
    for (Item item : items) {
      if (item.selects(test)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Removes the descriptors under {@code root} that are neither an ancestor nor a descendant of a
   * selected unique ID, if any unique IDs were selected.
   */
  void prune(TestDescriptor root) {
    if (uniqueIds.isEmpty()) {
      return;
    }
    for (TestDescriptor child : new ArrayList<>(root.getChildren())) {
      pruneRecursively(child);
    }
  }

  private void pruneRecursively(TestDescriptor descriptor) {
    UniqueId id = descriptor.getUniqueId();
    boolean isSelectedOrDescendant = false;
    boolean isAncestor = false;
    for (UniqueId selected : uniqueIds) {
      if (id.hasPrefix(selected)) {
        isSelectedOrDescendant = true;
      } else if (selected.hasPrefix(id)) {
        isAncestor = true;
      }
    }
    if (isSelectedOrDescendant) {
      return;
    }
    if (!isAncestor) {
      descriptor.removeFromHierarchy();
      return;
    }
    for (TestDescriptor child : new ArrayList<>(descriptor.getChildren())) {
      pruneRecursively(child);
    }
  }

  private void add(Class<?> topLevelClass, Item item) {
    itemsByClass.computeIfAbsent(topLevelClass, unused -> new ArrayList<>()).add(item);
  }

  private static Item methodItem(List<Class<?>> enclosing, Class<?> testClass, Method method) {
    return test ->
        test.enclosingClasses.equals(enclosing)
            && test.testClass.equals(testClass)
            && test.method.equals(method);
  }

  private static Item nestedClassItem(List<Class<?>> selectedChain) {
    return test -> {
      List<Class<?>> chain = new ArrayList<>(test.enclosingClasses);
      chain.add(test.testClass);
      return chain.size() >= selectedChain.size()
          && chain.subList(0, selectedChain.size()).equals(selectedChain);
    };
  }

  private interface ThrowingSupplier<T> {
    T get() throws Exception;
  }

  /** Selectors referring to classes that cannot be loaded are left to other engines. */
  private static <T> Optional<T> tryLoad(ThrowingSupplier<T> supplier) {
    try {
      return Optional.of(supplier.get());
    } catch (Exception | LinkageError e) {
      return Optional.empty();
    }
  }

  private static Predicate<String> classNameFilter(EngineDiscoveryRequest request) {
    List<Predicate<String>> filters = new ArrayList<>();
    for (ClassNameFilter filter : request.getFiltersByType(ClassNameFilter.class)) {
      filters.add(name -> filter.apply(name).included());
    }
    for (PackageNameFilter filter : request.getFiltersByType(PackageNameFilter.class)) {
      filters.add(name -> filter.apply(name).included());
    }
    return name -> filters.stream().allMatch(filter -> filter.test(name));
  }
}
