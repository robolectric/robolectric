package org.robolectric.util.inject;

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import javax.annotation.Priority;

@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
class PluginFinder {

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
  <T> List<Class<? extends T>> findPlugins(Class<T> pluginType) {
    return prioritize(ServiceFinder.load(pluginType));
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
  <T> List<Class<? extends T>> findPlugins(Class<T> pluginType, ClassLoader classLoader) {
    return prioritize(ServiceFinder.load(pluginType, classLoader));
  }

  private <T> Class<? extends T> best(Class<T> pluginType,
      List<Class<? extends T>> serviceClasses) {
    if (serviceClasses.isEmpty()) {
      return null;
    } else if (serviceClasses.size() > 1) {
      throw new InjectionException(pluginType, "too many implementations: " + serviceClasses);
    } else {
      return serviceClasses.get(0);
    }
  }

  private <T> List<Class<? extends T>> prioritize(ServiceFinder<T> serviceFinder) {
    List<Class<? extends T>> serviceClasses = new ArrayList<>();

    for (Class<T> serviceClass : serviceFinder) {
      serviceClasses.add(serviceClass);
    }

    serviceClasses.sort(comparing(pluginClass -> {
      Priority priority = pluginClass.getAnnotation(Priority.class);
      return priority == null ? 0 : priority.value();
    }));

    return serviceClasses;
  }

}
