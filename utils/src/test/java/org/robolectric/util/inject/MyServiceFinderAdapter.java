package org.robolectric.util.inject;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.util.inject.PluginFinder.ServiceFinderAdapter;

class MyServiceFinderAdapter extends ServiceFinderAdapter {

  private List<Class<?>> pluginClasses;

  MyServiceFinderAdapter(List<Class<?>> pluginClasses) {
    this.pluginClasses = pluginClasses;
  }

  @Nonnull
  @Override
  <T> Iterable<Class<? extends T>> load(Class<T> pluginType) {
    return fill();
  }

  @Nonnull
  @Override
  <T> Iterable<Class<? extends T>> load(Class<T> pluginType, ClassLoader classLoader) {
    return fill();
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  private <T> Iterable<Class<? extends T>> fill() {
    List<Class<? extends T>> classes = new ArrayList<>();
    for (Class<?> pluginClass : pluginClasses) {
      classes.add((Class<? extends T>) pluginClass);
    }
    return classes;
  }
}
