package org.robolectric.util.inject;

import java.util.List;

/** Gives {@code ClasspathScalingBenchmark} access to the package-private {@link PluginFinder}. */
public final class PluginFinderBenchmarkAccess {
  // Plugin types with no service file anywhere, which is what most Injector lookups resolve.
  private static final Class<?>[] TYPES = {
    Runnable.class,
    Comparable.class,
    CharSequence.class,
    Iterable.class,
    AutoCloseable.class,
    Cloneable.class,
    Appendable.class,
    Readable.class,
    java.io.Closeable.class,
    java.io.Flushable.class,
    java.io.Serializable.class,
    java.util.Collection.class,
    java.util.Comparator.class,
    java.util.Deque.class,
    java.util.Enumeration.class,
    java.util.EventListener.class,
    java.util.Formattable.class,
    java.util.Iterator.class,
    java.util.Map.class,
    java.util.Queue.class,
    java.util.RandomAccess.class,
    java.util.Set.class,
    java.util.concurrent.Callable.class,
    java.util.concurrent.Executor.class,
    java.util.function.Supplier.class,
  };

  private PluginFinderBenchmarkAccess() {}

  /** Warm-up helper: resolves one type through a fresh finder. */
  public static List<?> findPluginNames(ClassLoader classLoader, String unused) {
    return new PluginFinder(classLoader).findPlugins(TYPES[TYPES.length - 1]);
  }

  /**
   * Resolves {@code count} distinct plugin types through one new finder, as a new Injector does.
   */
  public static void findPluginNamesWithNewFinder(ClassLoader classLoader, int count) {
    PluginFinder finder = new PluginFinder(classLoader);
    for (int i = 0; i < count; i++) {
      finder.findPlugins(TYPES[i]);
    }
  }
}
