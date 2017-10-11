package org.robolectric.internal;

import com.google.common.annotations.VisibleForTesting;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.Plugin;
import org.robolectric.Plugin.UnsuitablePluginException;

public class PluginLoader<T extends Plugin> {

  private final Class<T> pluginInterface;

  @GuardedBy("this")
  private List<T> services;

  public PluginLoader(Class<T> pluginInterface) {
    this.pluginInterface = pluginInterface;
  }

  public <R> R invoke(Function<? super T, R> o) throws UnsuitablePluginException {
    List<T> services = findServices();

    if (services.isEmpty()) {
      throw new UnsuitablePluginException("no suitable plugins found for " + pluginInterface.getName());
    }

    for (T service : services) {
      try {
        return o.apply(service);
      } catch (UnsuitablePluginException e) {
        // ok
      }
    }

    throw new UnsuitablePluginException("no suitable plugin found for " + pluginInterface.getName());
  }

  synchronized private List<T> findServices() {
    if (services == null) {
      services = new ArrayList<>();
      for (T t : loadServices(pluginInterface)) {
        services.add(t);
      }
      Collections.sort(services, Comparator.comparing(Plugin::getPriority).reversed());
    }
    return services;
  }

  @VisibleForTesting
  Iterable<T> loadServices(Class<T> pluginClass) {
    return ServiceLoader.load(pluginClass);
  }
}
