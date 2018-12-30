package org.robolectric.util.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A super-simple dependency injection and plugin framework.
 *
 * Register default implementation classes using {@link #registerDefault(Class, Class)}.
 *
 * For interfaces lacking a default implementation, the injector will look for an implementation
 * registered in the same way as {@link java.util.ServiceLoader} does.
 */
@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
public class Injector {

  private final Injector superInjector;
  private final PluginFinder pluginFinder = new PluginFinder();
  private final Map<Key<?>, Provider<?>> providers = new HashMap<>();
  private final Map<Key<?>, Class<?>> defaultImpls = new HashMap<>();

  public Injector() {
    superInjector = null;
  }

  public Injector(Injector superInjector) {
    this.superInjector = superInjector;
  }

  synchronized public <T> Injector register(@Nonnull Class<T> type, @Nonnull T instance) {
    providers.put(new Key<>(type), () -> instance);
    return this;
  }

  synchronized public <T> Injector register(
      @Nonnull Class<T> type, @Nonnull Class<? extends T> defaultClass) {
    registerInternal(new Key<>(type), defaultClass);
    return this;
  }

  synchronized public <T> Injector registerDefault(
      @Nonnull Class<T> type, @Nonnull Class<? extends T> defaultClass) {
    defaultImpls.put(new Key<>(type), defaultClass);
    return this;
  }

  synchronized private <T> Provider<T> registerInternal(
      @Nonnull Key key, @Nonnull Class<? extends T> defaultClass) {
    Provider<T> provider = new MemoizingProvider<>(() -> inject(defaultClass));
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
            params[i] = get(paramType);
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

  private <T> Provider<T> getProvider(Class<T> clazz) {
    return getProvider(new Key<>(clazz));
  }

  @SuppressWarnings("unchecked")
  synchronized private <T> Provider<T> getProvider(Key<T> key) {
    Provider<T> provider = (Provider<T>) providers.get(key);
    if (provider == null) {
      if (superInjector != null) {
        return superInjector.getProvider(key);
      } else {
        provider = () -> findProvider(key);
        providers.put(key, provider);
      }
    }
    return provider;
  }

  synchronized private <T> T findProvider(Key<T> key) {
    Class<? extends T> implClass = pluginFinder.findPlugin(key.theInterface);

    if (implClass == null) {
      synchronized (Injector.this) {
        implClass = (Class<? extends T>) defaultImpls.get(key);
      }
    }

    if (implClass == null) {
      throw new InjectionException(key.theInterface, "no provider found");
    }

    // replace this with the found provider for future lookups...
    Provider<T> tProvider;
    tProvider = registerInternal(key, implClass);
    return tProvider.provide();
  }

  public <T> T get(Class<T> clazz) {
    Provider<T> provider = getProvider(clazz);

    if (provider == null) {
      throw new InjectionException(clazz, "no provider registered");
    }

    return provider.provide();
  }

  private static class Key<T> {

    @Nonnull
    private final Class<T> theInterface;

    private Key(@Nonnull Class<T> theInterface) {
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

  private interface Provider<T> {

    T provide();
  }

  private static class MemoizingProvider<T> implements Provider<T> {

    private Provider<T> delegate;
    private T instance;

    private MemoizingProvider(Provider<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    synchronized public T provide() {
      if (instance == null) {
        instance = delegate.provide();
        delegate = null;
      }
      return instance;
    }
  }
}