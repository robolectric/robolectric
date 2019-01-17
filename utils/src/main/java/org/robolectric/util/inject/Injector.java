package org.robolectric.util.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Named;
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

  private final Injector superInjector;
  private final PluginFinder pluginFinder = new PluginFinder();

  @GuardedBy("this")
  private final Map<Key, Provider<?>> providers = new HashMap<>();

  @GuardedBy("this")
  private final Map<Key, Class<?>> defaultImpls = new HashMap<>();

  public Injector() {
    this.superInjector = null;
  }

  public Injector(Injector superInjector) {
    this.superInjector = superInjector;
  }

  public synchronized <T> Injector register(@Nonnull Class<T> type, @Nonnull T instance) {
    return register(new Key<>(type), instance);
  }

  public synchronized <T> Injector register(Key<T> key, @Nonnull T instance) {
    providers.put(key, () -> instance);
    return this;
  }

  public synchronized <T> Injector register(
      @Nonnull Class<T> type, @Nonnull Class<? extends T> implementingClass) {
    registerMemoized(new Key<>(type), implementingClass);
    return this;
  }

  public synchronized <T> Injector registerDefault(
      @Nonnull Class<T> type, @Nonnull Class<? extends T> defaultClass) {
    defaultImpls.put(new Key<>(type), defaultClass);
    return this;
  }

  public synchronized <T> Injector registerDefault(
      @Nonnull Class<T> type, String name, @Nonnull Class<? extends T> defaultClass) {
    defaultImpls.put(new Key<>(type, name), defaultClass);
    return this;
  }

  private <T> Provider<T> registerMemoized(
      @Nonnull Key<T> key, @Nonnull Class<? extends T> defaultClass) {
    return registerMemoized(key, () -> inject(defaultClass));
  }

  private synchronized <T> Provider<T> registerMemoized(
      @Nonnull Key<T> key, Provider<T> tProvider) {
    Provider<T> provider = new MemoizingProvider<>(tProvider);
    providers.put(key, provider);
    return provider;
  }

  @SuppressWarnings("unchecked")
  private <T> T inject(@Nonnull Class<? extends T> clazz) {
    try {
      List<Constructor<T>> injectCtors = new ArrayList<>();
      List<Constructor<T>> otherCtors = new ArrayList<>();

      for (Constructor<?> ctor : clazz.getConstructors()) {
        if (ctor.isAnnotationPresent(Inject.class)) {
          injectCtors.add((Constructor<T>) ctor);
        } else {
          otherCtors.add((Constructor<T>) ctor);
        }
      }

      Constructor<T> ctor;
      if (injectCtors.size() > 1) {
        throw new InjectionException(clazz, "multiple public @Inject constructors");
      } else if (injectCtors.size() == 1) {
        ctor = injectCtors.get(0);
      } else if (otherCtors.size() > 1 && !isSystem(clazz)) {
        throw new InjectionException(clazz, "multiple public constructors");
      } else if (otherCtors.size() == 1) {
        ctor = otherCtors.get(0);
      } else if (isSystem(clazz)) {
        throw new InjectionException(clazz, "nothing provided");
      } else {
        throw new InjectionException(clazz, "no public constructor");
      }

      final Object[] params = new Object[ctor.getParameterCount()];

      Class<?>[] paramTypes = ctor.getParameterTypes();
      Annotation[][] parameterAnnotations = ctor.getParameterAnnotations();
      for (int i = 0; i < paramTypes.length; i++) {
        Class<?> paramType = paramTypes[i];
        String name = findName(parameterAnnotations[i]);
        Key<?> key = new Key<>(paramType, name);
        try {
          params[i] = getInstance(key);
        } catch (InjectionException e) {
          throw new InjectionException(clazz,
              "failed to inject " + key + " param", e);
        }
      }

      return ctor.newInstance(params);

    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new InjectionException(clazz, e);
    }
  }

  private String findName(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof Named) {
        return ((Named) annotation).value();
      }
    }
    return null;
  }

  /** Finds a provider for the given key. Calls are guaranteed idempotent. */
  @SuppressWarnings("unchecked")
  private synchronized <T> Provider<T> getProvider(final Key<T> key) {
    Provider<?> provider = providers.computeIfAbsent(key, k -> new Provider<T>() {
      @Override
      public synchronized T get() {
        Class<T> clazz = key.theInterface;

        Class<? extends T> implClass = pluginFinder.findPlugin(clazz);

        if (implClass == null) {
          implClass = getDefaultImpl(key);
        }

        if (implClass == null && clazz.isArray()) {
          Provider<T> tProvider = new MultiProvider(clazz.getComponentType());
          return registerMemoized(key, tProvider).get();
        }

        if (clazz.isAnnotationPresent(AutoFactory.class)) {
          return registerMemoized(key, new ScopeBuilderProvider<>(clazz)).get();
        }

        if (isConcrete(clazz)) {
          implClass = clazz;
        }

        if (implClass == null && superInjector != null) {
          return superInjector.getInstance(clazz);
        }

        if (implClass == null) {
          throw new InjectionException(clazz, "no provider found");
        }

        // replace this with the found provider for future lookups...
        return registerMemoized(key, implClass).get();
      }

    });
    return (Provider<T>) provider;
  }

  private <T> boolean isConcrete(Class<T> clazz) {
    return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
  }

  private synchronized <T> Class<? extends T> getDefaultImpl(Key<T> key) {
    Class<?> aClass = defaultImpls.get(key);
    return (Class<? extends T>) aClass;
  }

  /** Finds an instance for the given class. Calls are guaranteed idempotent. */
  public <T> T getInstance(Class<T> clazz) {
    return getInstance(new Key<>(clazz));
  }

  /** Finds an instance for the given key. Calls are guaranteed idempotent. */
  private <T> T getInstance(Key<T> key) {
    Provider<T> provider = getProvider(key);

    if (provider == null) {
      throw new InjectionException(key, "no provider registered");
    }

    return provider.get();
  }

  private boolean isSystem(Class<?> clazz) {
    if (clazz.isPrimitive()) {
      return true;
    }
    Package aPackage = clazz.getPackage();
    return aPackage == null || aPackage.getName().startsWith("java.");
  }

  public static class Key<T> {

    @Nonnull
    private final Class<T> theInterface;
    private final String name;

    private Key(@Nonnull Class<T> theInterface) {
      this(theInterface, null);
    }

    public Key(Class<T> theInterface, String name) {
      this.theInterface = theInterface;
      this.name = name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Key)) {
        return false;
      }
      Key<?> key = (Key) o;
      return theInterface.equals(key.theInterface) &&
          Objects.equals(name, key.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(theInterface, name);
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append("Key<").append(theInterface.getName());
      if (name != null) {
        buf.append(" named \"")
            .append(name)
            .append("\"");
      }
      buf.append(">");
      return buf.toString();
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

  private class ScopeBuilderProvider<T> implements Provider<T> {

    private final Class<T> clazz;

    public ScopeBuilderProvider(Class<T> clazz) {
      this.clazz = clazz;
    }

    @Override
    public T get() {
      return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
          (proxy, method, args) -> create(method, args));
    }

    private Object create(Method method, Object[] args) {
      Injector subInjector = new Injector(Injector.this);
      Class<?>[] parameterTypes = method.getParameterTypes();
      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i = 0; i < args.length; i++) {
        Class<?> paramType = parameterTypes[i];
        String name = findName(parameterAnnotations[i]);
        Object arg = args[i];
        subInjector.register(new Key(paramType, name), arg);
      }
      return subInjector.getInstance(method.getReturnType());
    }
  }
}