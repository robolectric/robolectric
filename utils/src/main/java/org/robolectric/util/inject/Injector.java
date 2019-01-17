package org.robolectric.util.inject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * A tiny dependency injection and plugin helper for Robolectric.
 *
 * Dependencies may be retrieved explicitly by calling {@link #getInstance(Class)}; transitive
 * dependencies will be automatically injected as needed.
 *
 * Dependencies are identified by an interface or class.
 *
 * When a dependency is requested, the injector looks for any instance that has been previously
 * found for the given interface, or that has been explicitly registered with
 * {@link #register(Class, Object)}. Failing that, the injector searches for an implementing class
 * from the following sources, in order:
 *
 * * Explicitly-registered implementations registered with {@link #register(Class, Class)}.
 * * Plugin implementations published as {@link java.util.ServiceLoader} services under the
 *   dependency type (see also {@link PluginFinder#findPlugin(Class)}).
 * * Fallback default implementation classes registered with {@link #registerDefault(Class, Class)}.
 * * If the dependency type is a concrete class, then the dependency type itself.
 * * If the dependency type is an array or {@link java.util.Collection}, then the component type
 *   of the array or collection is recursively sought using {@link PluginFinder#findPlugins(Class)}
 *   and an array or collection of those instances is returned.
 * * If no implementation has yet been found, the injector will throw an exception.
 *
 * When the injector has determined an implementing class, it attempts to instantiate it. It
 * searches for a constructor in the following order:
 *
 * * A singular public constructor annotated {@link Inject}. (If multiple constructors are
 *   `@Inject` annotated, the injector will throw an exception.)
 * * A singular public constructor of any arity.
 * * If no constructor has yet been found, the Injector will throw an exception.
 *
 * Any constructor parameters are seen as further dependencies, and the injector will recursively
 * attempt to resolve an implementation for each before invoking the constructor and thereby
 * instantiating the original dependency implementation.
 *
 * The implementation is then stored in the injector and returned to the requestor.
 */
@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
public class Injector {

  private final PluginFinder pluginFinder = new PluginFinder();
  private final Map<Key, Provider<?>> providers = new HashMap<>();
  private final Map<Key, Class<?>> defaultImpls = new HashMap<>();

  public synchronized <T> Injector register(@Nonnull Class<T> type, @Nonnull T instance) {
    providers.put(new Key(type), () -> instance);
    return this;
  }

  public synchronized <T> Injector register(
      @Nonnull Class<T> type, @Nonnull Class<? extends T> implementingClass) {
    registerMemoized(new Key(type), implementingClass);
    return this;
  }

  public synchronized <T> Injector registerDefault(
      @Nonnull Class<T> type, @Nonnull Class<? extends T> defaultClass) {
    defaultImpls.put(new Key(type), defaultClass);
    return this;
  }

  private <T> Provider<T> registerMemoized(
      @Nonnull Key key, @Nonnull Class<? extends T> defaultClass) {
    return registerMemoized(key, () -> inject(defaultClass));
  }

  private synchronized <T> Provider<T> registerMemoized(
      @Nonnull Key key, Provider<T> tProvider) {
    Provider<T> provider = new MemoizingProvider<>(tProvider);
    providers.put(key, provider);
    return provider;
  }

  @SuppressWarnings("unchecked")
  private <T> T inject(@Nonnull Class<? extends T> clazz) {
    try {
      Constructor<T> defaultCtor = null;
      Constructor<T> injectCtor = null;

      for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
        if (ctor.getParameterCount() == 0) {
          defaultCtor = (Constructor<T>) ctor;
        } else if (ctor.isAnnotationPresent(Inject.class)) {
          if (injectCtor != null) {
            throw new InjectionException(clazz, "multiple @Inject constructors");
          }
          injectCtor = (Constructor<T>) ctor;
        }
      }

      if (defaultCtor != null) {
        return defaultCtor.newInstance();
      }

      if (injectCtor != null) {
        final Object[] params = new Object[injectCtor.getParameterCount()];

        Class<?>[] paramTypes = injectCtor.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
          Class<?> paramType = paramTypes[i];
          try {
            params[i] = getInstance(paramType);
          } catch (InjectionException e) {
            throw new InjectionException(clazz,
                "failed to inject " + paramType.getName() + " param", e);
          }
        }

        return injectCtor.newInstance(params);
      }

      throw new InjectionException(clazz, "no default or @Inject constructor");
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new InjectionException(clazz, e);
    }
  }

  @SuppressWarnings("unchecked")
  private synchronized <T> Provider<T> getProvider(Class<T> clazz) {
    Key key = new Key(clazz);
    Provider<?> provider = providers.computeIfAbsent(key, k -> new Provider<T>() {
      @Override
      public synchronized T get() {
        Class<? extends T> implClass = pluginFinder.findPlugin(clazz);

        if (implClass == null) {
          synchronized (Injector.this) {
            implClass = (Class<? extends T>) defaultImpls.get(key);
          }
        }

        if (implClass == null && clazz.isArray()) {
          Provider<T> tProvider = new MultiProvider(clazz.getComponentType());
          return registerMemoized(new Key(clazz), tProvider).get();
        }

        if (implClass == null) {
          throw new InjectionException(clazz, "no provider found");
        }

        // replace this with the found provider for future lookups...
        return registerMemoized(new Key(clazz), implClass).get();
      }
    });
    return (Provider<T>) provider;
  }

  public <T> T getInstance(Class<T> clazz) {
    Provider<T> provider = getProvider(clazz);

    if (provider == null) {
      throw new InjectionException(clazz, "no provider registered");
    }

    return provider.get();
  }

  private static class Key {

    @Nonnull
    private final Class<?> theInterface;

    private <T> Key(@Nonnull Class<T> theInterface) {
      this.theInterface = theInterface;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key)) {
        return false;
      }
      Key key = (Key) o;
      return theInterface.equals(key.theInterface);
    }

    @Override
    public int hashCode() {
      return theInterface.hashCode();
    }
  }

  private static class MemoizingProvider<T> implements Provider<T> {

    private Provider<T> delegate;
    private T instance;

    private MemoizingProvider(Provider<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public synchronized T get() {
      if (instance == null) {
        instance = delegate.get();
        delegate = null;
      }
      return instance;
    }
  }

  private class MultiProvider<T> implements Provider<T[]> {

    private final Class<T> clazz;

    MultiProvider(Class<T> clazz) {
      this.clazz = clazz;
    }

    @Override
    public T[] get() {
      List<T> plugins = new ArrayList<>();
      for (Class<? extends T> pluginClass : pluginFinder.findPlugins(clazz)) {
        plugins.add(inject(pluginClass));
      }
      return plugins.toArray((T[]) Array.newInstance(clazz, 0));
    }
  }
}