package org.robolectric.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;

/**
 * A super-simple dependency injection and plugin framework.
 *
 * Register default implementation classes using {@link #registerDefault(Class, Class)}.
 *
 * For interfaces lacking a default implementation, the injector will look for an implementation
 * registered in the same way as {@link java.util.ServiceLoader} does.
 */
@SuppressWarnings("NewApi")
public class Injector {

  private static final String PREFIX = "META-INF/services/";

  private final Map<Key, Provider<?>> providers = new HashMap<>();
  private final Map<Key, Class<?>> defaultImpls = new HashMap<>();

  synchronized public <T> void register(Class<T> type, T instance) {
    providers.put(new Key(type), () -> instance);
  }

  synchronized public <T> void register(Class<T> type, Class<? extends T> defaultClass) {
    registerInternal(new Key(type), defaultClass);
  }

  synchronized private <T> Provider<T> registerInternal(Key key, Class<? extends T> defaultClass) {
    Provider<T> provider = new MemoizingProvider<>(() -> inject(defaultClass));
    providers.put(key, provider);
    return provider;
  }

  synchronized public <T> void registerDefault(Class<T> type,
      Class<? extends T> defaultClass) {
    defaultImpls.put(new Key(type), defaultClass);
  }

  private <T> T inject(Class<? extends T> clazz) {
    try {
      Constructor<T> defaultCtor = null;
      Constructor<T> injectCtor = null;

      for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
        if (ctor.getParameterCount() == 0) {
          defaultCtor = (Constructor<T>) ctor;
        } else if (ctor.getAnnotation(Inject.class) != null) {
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
          params[i] = getInstance(paramType);
        }

        return injectCtor.newInstance(params);
      }

      throw new InjectionException(clazz, "no default or @Inject constructor");
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new InjectionException(clazz, e);
    }
  }

  synchronized private <T> Provider<T> getProvider(Class<T> clazz) {
    Key key = new Key(clazz);
    Provider<?> provider = providers.computeIfAbsent(key, k -> new Provider<T>() {
      @Override
      synchronized public T provide() {
        Class<? extends T> implClass = findService(clazz);

        if (implClass == null) {
          synchronized (Injector.this) {
            implClass = (Class<? extends T>) defaultImpls.get(key);
          }
        }

        if (implClass == null) {
          throw new InjectionException(clazz, "no provider found");
        }

        // replace this with the found provider for future lookups...
        Provider<T> tProvider;
        tProvider = registerInternal(new Key(clazz), implClass);
        return tProvider.provide();
      }
    });
    return (Provider<T>) provider;
  }

  public <T> T getInstance(Class<T> clazz) {
    Provider<?> provider = getProvider(clazz);

    if (provider == null) {
      throw new InjectionException(clazz, "no provider registered");
    }

    return ((Provider<T>) provider).provide();
  }

  private <T> Class<? extends T> findService(Class<T> serviceType) {
    ClassLoader loader = serviceType.getClassLoader();
    Enumeration<URL> configs;
    try {
      String fullName = PREFIX + serviceType.getName();
      if (loader == null) {
        configs = ClassLoader.getSystemResources(fullName);
      } else {
        configs = loader.getResources(fullName);
      }
    } catch (IOException e) {
      throw new InjectionException(serviceType, "Error locating configuration files", e);
    }

    List<URL> urls = new ArrayList<>();
    while (configs.hasMoreElements()) {
      URL url = configs.nextElement();
      urls.add(url);
    }

    if (urls.isEmpty()) {
      return null;
    } else if (urls.size() > 1) {
      throw new InjectionException(serviceType, "too many implementations: " + urls);
    }

    URL url = urls.get(0);
    String className = readOnlyLine(serviceType, url);

    try {
      return (Class<T>) loader.loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new InjectionException(serviceType, "no such implementation class", e);
    }
  }

  private <T> String readOnlyLine(Class<T> serviceType, URL url) {
    String className = null;
    try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(),
        StandardCharsets.UTF_8))) {
      for (String line = in.readLine(); line != null; line = in.readLine()) {
        line = line.trim();
        if (!line.isEmpty() && !line.startsWith("#")) {
          if (className != null) {
            throw new InjectionException(serviceType, "too many implementations in " + url);
          }

          className = line;
        }
      }
    } catch (IOException x) {
      throw new InjectionException(serviceType, "Error reading configuration file", x);
    }
    return className;
  }

  private static class Key {

    private Class<?> theInterface;

    public <T> Key(Class<T> theInterface) {
      this.theInterface = theInterface;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Key key = (Key) o;
      return Objects.equals(theInterface, key.theInterface);
    }

    @Override
    public int hashCode() {
      return Objects.hash(theInterface);
    }
  }

  private interface Provider<T> {

    T provide();
  }

  private static class MemoizingProvider<T> implements Provider<T> {

    private Provider<T> delegate;
    private T instance;

    public MemoizingProvider(Provider<T> delegate) {
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

  public static class InjectionException extends RuntimeException {
    public InjectionException(Class<?> clazz, String message, Throwable cause) {
      super(clazz + ": " + message, cause);
    }

    public InjectionException(Class<?> clazz, String message) {
      super(clazz + ": " + message);
    }

    public InjectionException(Class<?> clazz, Throwable cause) {
      super(clazz + ": failed to inject");
    }
  }
}
