package org.robolectric.util.inject;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Priority;

@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
class PluginFinder {

  private final ServiceFinderAdapter serviceFinderAdapter;

  public PluginFinder() {
    this(new ServiceFinderAdapter(null));
  }

  /**
   * @param classLoader the classloader to be used to load provider-configuration files and provider
   *     classes, or null if the system classloader (or, failing that, the bootstrap classloader) is
   *     to be used
   */
  public PluginFinder(ClassLoader classLoader) {
    this(new ServiceFinderAdapter(classLoader));
  }

  PluginFinder(ServiceFinderAdapter serviceFinderAdapter) {
    this.serviceFinderAdapter = serviceFinderAdapter;
  }

  /**
   * Returns an implementation class for the specified plugin.
   *
   * <p>If there is more than such one candidate, the classes will be sorted by {@link Priority} and
   * the one with the highest priority will be returned. If multiple classes claim the same
   * priority, a {@link ServiceConfigurationError} will be thrown. Classes without a Priority are
   * treated as {@code @Priority(0)}.
   *
   * @param pluginType the class of the plugin type
   * @param <T> the class of the plugin type
   * @return the implementing class with the highest priority
   */
  @Nullable
  <T> Class<? extends T> findPlugin(Class<T> pluginType) {
    return best(pluginType, findPlugins(pluginType));
  }

  /**
   * Returns a list of implementation classes for the specified plugin, ordered from highest to
   * lowest priority. If no implementing classes can be found, an empty list is returned.
   *
   * @param pluginType the class of the plugin type
   * @param <T> the class of the plugin type
   * @return a prioritized list of implementation classes
   */
  @Nonnull
  <T> List<Class<? extends T>> findPlugins(Class<T> pluginType) {
    return prioritize(filter(serviceFinderAdapter.load(pluginType)));
  }

  private <T> Iterable<Class<? extends T>> filter(Iterable<Class<? extends T>> classes) {
    Set<Class<?>> superseded = new HashSet<>();
    for (Class<? extends T> clazz : classes) {
      Supersedes supersedes = clazz.getAnnotation(Supersedes.class);
      if (supersedes != null) {
        superseded.add(supersedes.value());
      }
    }
    if (superseded.isEmpty()) {
      return classes;
    } else {
      return () -> new Filterator<>(classes.iterator(), o -> !superseded.contains(o));
    }
  }

  @Nullable
  private <T> Class<? extends T> best(Class<T> pluginType,
      List<Class<? extends T>> serviceClasses) {
    if (serviceClasses.isEmpty()) {
      return null;
    }

    Class<? extends T> first = serviceClasses.get(0);
    if (serviceClasses.size() == 1) {
      return first;
    }

    int topPriority = priority(first);
    serviceClasses = serviceClasses.stream()
        .filter(it -> priority(it) == topPriority)
        .collect(Collectors.toList());

    if (serviceClasses.size() == 1) {
      return serviceClasses.get(0);
    } else {
      throw new InjectionException(pluginType, "too many implementations: " + serviceClasses);
    }
  }

  static class ServiceFinderAdapter {

    private final ClassLoader classLoader;

    ServiceFinderAdapter(ClassLoader classLoader) {
      this.classLoader = classLoader;
    }

    @Nonnull
    <T> Iterable<Class<? extends T>> load(Class<T> pluginType) {
      if (classLoader == null) {
        return ServiceFinder.load(pluginType);
      } else {
        return ServiceFinder.load(pluginType, classLoader);
      }
    }
  }

  @Nonnull
  private <T> List<Class<? extends T>> prioritize(Iterable<Class<? extends T>> iterable) {
    List<Class<? extends T>> serviceClasses = new ArrayList<>();

    for (Class<? extends T> serviceClass : iterable) {
      serviceClasses.add(serviceClass);
    }

    Comparator<Class<? extends T>> c = reverseOrder(comparing(PluginFinder::priority));
    c = c.thenComparing(Class::getName);
    serviceClasses.sort(c);

    return serviceClasses;
  }

  private static <T> int priority(Class<? extends T> pluginClass) {
    Priority priority = pluginClass.getAnnotation(Priority.class);
    return priority == null ? 0 : priority.value();
  }

  private static class Filterator<T> implements Iterator<T> {

    private final Iterator<T> delegate;
    private final Predicate<T> predicate;
    private T next;

    public Filterator(Iterator<T> delegate, Predicate<T> predicate) {
      this.delegate = delegate;
      this.predicate = predicate;
      findNext();
    }

    void findNext() {
      while (delegate.hasNext()) {
        next = delegate.next();
        if (predicate.test(next)) {
          return;
        }
      }
      next = null;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public T next() {
      try {
        return next;
      } finally {
        findNext();
      }
    }
  }
}
