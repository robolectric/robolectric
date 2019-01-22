package org.robolectric.util.inject;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Priority;

@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
class PluginFinder {

  private final ServiceFinderAdapter serviceFinderAdapter;

  public PluginFinder() {
    this(new ServiceFinderAdapter());
  }

  PluginFinder(ServiceFinderAdapter serviceFinderAdapter) {
    this.serviceFinderAdapter = serviceFinderAdapter;
  }

  /**
   * Returns an implementation class for the specified plugin.
   *
   * If there is more than such one candidate, the classes will be sorted by {@link Priority}
   * and the one with the highest priority will be returned. If multiple classes claim the same
   * priority, a {@link ServiceConfigurationError} will be thrown. Classes without a Priority
   * are treated as `@Priority(0)`.
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
   * Returns an implementation class for the specified plugin.
   *
   * If there is more than such one candidate, the classes will be sorted by {@link Priority}
   * and the one with the highest priority will be returned. If multiple classes claim the same
   * priority, a {@link ServiceConfigurationError} will be thrown. Classes without a Priority
   * are treated as `@Priority(0)`.
   *
   * @param pluginType the class of the plugin type
   * @param classLoader
   *         the classloader to be used to load provider-configuration files
   *         and provider classes, or `null` if the system classloader (or,
   *         failing that, the bootstrap classloader) is to be used
   * @param <T> the class of the plugin type
   * @return the implementing class with the highest priority, or `null` if none could be found
   */
  @Nullable
  <T> Class<? extends T> findPlugin(Class<T> pluginType, ClassLoader classLoader) {
    return best(pluginType, findPlugins(pluginType, classLoader));
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
    return prioritize(serviceFinderAdapter.load(pluginType));
  }

  /**
   * Returns a list of implementation classes for the specified plugin, ordered from highest to
   * lowest priority. If no implementing classes can be found, an empty list is returned.
   *
   * @param pluginType the class of the plugin type
   * @param classLoader
   *         the classloader to be used to load provider-configuration files
   *         and provider classes, or `null` if the system classloader (or,
   *         failing that, the bootstrap classloader) is to be used
   * @param <T> the class of the plugin type
   * @return a prioritized list of implementation classes
   */
  @Nonnull
  <T> List<Class<? extends T>> findPlugins(Class<T> pluginType, ClassLoader classLoader) {
    return prioritize(serviceFinderAdapter.load(pluginType, classLoader));
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

    @Nonnull
    <T> Iterable<Class<? extends T>> load(Class<T> pluginType) {
      return ServiceFinder.load(pluginType);
    }

    @Nonnull
    <T> Iterable<Class<? extends T>> load(Class<T> pluginType, ClassLoader classLoader) {
      return ServiceFinder.load(pluginType, classLoader);
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

}
