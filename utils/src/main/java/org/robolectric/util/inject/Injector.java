package org.robolectric.util.inject;

import com.google.common.annotations.VisibleForTesting;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.robolectric.util.Util;

/**
 * A tiny dependency injection and plugin helper for Robolectric.
 *
 * <p>Dependencies may be retrieved explicitly by calling {@link #getInstance}; transitive
 * dependencies will be automatically injected as needed. For a given injector, all calls to {@link
 * #getInstance} are idempotent.
 *
 * <p>Dependencies are identified by an interface or class, and optionally by a name specified with
 * &#064;{@link Named}.
 *
 * <h3>Dependency Resolution</h3>
 *
 * When a dependency is requested, an implementation is sought.
 *
 * <p>The injector looks for any instance that has been previously found for the given interface, or
 * that has been explicitly registered with {@link Builder#bind(Class, Object)} or {@link
 * Builder#bind(Key, Object)}. If none is found, the injector searches for an implementing class
 * from the following sources, in order:
 *
 * <ol>
 *   <li>Explicitly-registered implementations registered with {@link Builder#bind(Class, Class)}.
 *   <li>If the dependency type is an array or {@link Collection}, then its component type is
 *       recursively sought using {@link PluginFinder#findPlugins(Class)} and an array or collection
 *       of those instances is returned.
 *   <li>Plugin implementations published as {@link java.util.ServiceLoader} services under the
 *       dependency type (see also {@link PluginFinder#findPlugin(Class)}).
 *   <li>Fallback default implementation classes registered with {@link Builder#bindDefault(Class,
 *       Class)}.
 *   <li>If the dependency type is a concrete class, then the dependency type itself.
 * </ol>
 *
 * If the injector has a superinjector, it is always consulted first (with the exception of
 * interfaces annotated &#064;{@link AutoFactory}; see <a href="#Scopes">Scopes</a> below).
 *
 * <p>If no implementing class is found in the injector or any superinjector, an exception is
 * thrown.
 *
 * <p>
 *
 * <h3>Injection</h3>
 *
 * When the injector has determined an implementing class, it attempts to instantiate it. It
 * searches for a constructor in the following order:
 *
 * <ol>
 *   <li>A singular public constructor annotated &#064;{@link Inject}. (If multiple constructors are
 *       &#064;{@link Inject} annotated, the injector will throw an exception.)
 *   <li>A singular public constructor of any arity.
 *   <li>If no constructor has yet been found, the injector will throw an exception.
 * </ol>
 *
 * Any constructor parameters are treated as further dependencies, and the injector will recursively
 * attempt to resolve an implementation for each before invoking the constructor and thereby
 * instantiating the original dependency implementation.
 *
 * <h3 id="Scopes">Scopes</h3>
 *
 * If the dependency type is an interface annotated &#064;{@link AutoFactory}, then a factory object
 * implementing that interface is created; a new scoped injector is created for every method call to
 * the factory, with parameter arguments registered on the scoped injector.
 *
 * <h3>Thread Safety</h3>
 *
 * All methods are MT-safe.
 */
@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
public class Injector {

  private static final Key<Object> INJECTOR_KEY = new Key<>(Injector.class);

  private final Injector superInjector;
  private final PluginFinder pluginFinder;

  @GuardedBy("this")
  private final Map<Key<?>, Provider<?>> providers;
  private final Map<Key<?>, Class<?>> defaultImpls;

  /** Creates a new empty injector. */
  public Injector() {
    this(new PluginFinder());
  }

  @VisibleForTesting
  Injector(PluginFinder pluginFinder) {
    this(null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
        pluginFinder);
  }

  /** Creates a new injector based on values from a Builder. */
  private Injector(Injector superInjector, Map<Key<?>, Provider<?>> providers,
      Map<Key<?>, Class<?>> explicitImpls, Map<Key<?>, Class<?>> defaultImpls,
      PluginFinder pluginFinder) {
    this.superInjector = superInjector;

    this.providers = new HashMap<>(providers);
    for (Map.Entry<Key<?>, Class<?>> e : explicitImpls.entrySet()) {
      this.providers.put(e.getKey(), memoized(() -> inject(e.getValue())));
    }

    this.defaultImpls = new HashMap<>(defaultImpls);
    this.pluginFinder = pluginFinder;
  }

  /** Builder for {@link Injector}. */
  public static class Builder {
    private final Injector superInjector;
    private final Map<Key<?>, Provider<?>> providers = new HashMap<>();
    private final Map<Key<?>, Class<?>> explicitImpls = new HashMap<>();
    private final Map<Key<?>, Class<?>> defaultImpls = new HashMap<>();
    private final PluginFinder pluginFinder;

    /** Creates a new builder. */
    public Builder() {
      this(null, (ClassLoader) null);
    }

    /** Creates a new builder using the specified ClassLoader for plugin loading. */
    public Builder(ClassLoader classLoader) {
      this(null, classLoader);
    }

    /** Creates a new builder with a parent injector. */
    public Builder(Injector superInjector) {
      this(superInjector, (ClassLoader) null);
    }

    /**
     * Creates a new builder with a parent injector and the specified ClassLoader for plugin
     * loading.
     */
    public Builder(Injector superInjector, ClassLoader classLoader) {
      this(superInjector, new PluginFinder(classLoader));
    }

    @VisibleForTesting
    Builder(Injector superInjector, PluginFinder pluginFinder) {
      this.superInjector = superInjector;
      this.pluginFinder = pluginFinder;
    }

    /** Registers an instance for the given dependency type. */
    public <T> Builder bind(@Nonnull Class<T> type, @Nonnull T instance) {
      return bind(new Key<>(type), instance);
    }

    /** Registers an instance for the given key. */
    public <T> Builder bind(Key<T> key, @Nonnull T instance) {
      providers.put(key, () -> instance);
      return this;
    }

    /** Registers an implementing class for the given dependency type. */
    public <T> Builder bind(@Nonnull Class<T> type, @Nonnull Class<? extends T> implementingClass) {
      explicitImpls.put(new Key<>(type), implementingClass);
      return this;
    }

    /** Registers a fallback implementing class for the given dependency type. */
    public <T> Builder bindDefault(
        @Nonnull Class<T> type, @Nonnull Class<? extends T> defaultImplementingClass) {
      defaultImpls.put(new Key<>(type), defaultImplementingClass);
      return this;
    }

    /** Builds an injector as previously configured. */
    public Injector build() {
      return new Injector(superInjector, providers, explicitImpls, defaultImpls, pluginFinder);
    }
  }

  /** Finds an instance for the given class. Calls are guaranteed idempotent. */
  @Nonnull public <T> T getInstance(@Nonnull Class<T> type) {
    return getInstance(new Key<>(type));
  }

  /** Finds an instance for the given key. Calls are guaranteed idempotent. */
  @Nonnull private <T> T getInstance(@Nonnull Key<T> key) {
    try {
      return getInstanceInternal(key);
    } catch (UnsatisfiedDependencyException e) {
      throw e.asInjectionException(key);
    }
  }

  private <T> T getInstanceInternal(@Nonnull Key<T> key) {
    // If we have a superinjector, check it for a provider first.
    if (superInjector != null) {
      try {
        return superInjector.getInstanceInternal(key);
      } catch (UnsatisfiedDependencyException | InjectionException e) {
        // that's fine, we'll try locally next...
      }
    }

    return getProvider(key).get();
  }

  public Injector.Builder newScopeBuilder(ClassLoader classLoader) {
    return new Injector.Builder(this, classLoader);
  }

  @Nonnull private <T> Provider<T> memoized(@Nonnull Class<? extends T> implementingClass) {
    return memoized(() -> inject(implementingClass));
  }

  @Nonnull private <T> Provider<T> memoized(@Nonnull Provider<T> tProvider) {
    return new MemoizingProvider<>(tProvider);
  }

  @SuppressWarnings("unchecked")
  @Nonnull private <T> T inject(@Nonnull Class<? extends T> implementingClass) {
    Constructor<T> ctor;
    try {
      ctor = findConstructor(implementingClass);
    } catch (IllegalArgumentException e) {
      throw new InjectionException(implementingClass, e);
    }

    Object[] params = resolveDependencies(ctor);
    try {
      return ctor.newInstance(params);
    } catch (IllegalAccessException e) {
      throw Util.sneakyThrow(e);
    } catch (InstantiationException | InvocationTargetException e) {
      throw Util.sneakyThrow(e.getCause());
    }
  }

  private <T> Constructor<T> findConstructor(@Nonnull Class<? extends T> implementingClass) {
    List<Constructor<T>> injectCtors = new ArrayList<>();
    List<Constructor<T>> otherCtors = new ArrayList<>();

    for (Constructor<?> ctor : implementingClass.getConstructors()) {
      if (ctor.isAnnotationPresent(Inject.class)) {
        injectCtors.add((Constructor<T>) ctor);
      } else {
        otherCtors.add((Constructor<T>) ctor);
      }
    }

    if (injectCtors.size() > 1) { // ambiguous @Inject constructors
      throw new InjectionException(implementingClass, "multiple public @Inject constructors");
    } else if (injectCtors.size() == 1) { // single @Inject constructor, bingo!
      return injectCtors.get(0);
    } else if (otherCtors.size() > 1) { // ambiguous non-@Inject constructors
      throw new InjectionException(implementingClass, "multiple public constructors");
    } else if (otherCtors.size() == 1) { // single public constructor, bingo!
      return otherCtors.get(0);
    } else {
      throw new InjectionException(implementingClass, "no public constructor");
    }
  }

  private Object[] resolveDependencies(Executable ctor) {
    final Object[] params = new Object[ctor.getParameterCount()];

    AnnotatedType[] paramTypes = ctor.getAnnotatedParameterTypes();
    Annotation[][] parameterAnnotations = ctor.getParameterAnnotations();
    for (int i = 0; i < paramTypes.length; i++) {
      AnnotatedType paramType = paramTypes[i];
      String name = findName(parameterAnnotations[i]);
      Key<?> key = new Key<>(paramType.getType(), name);
      if (key.equals(INJECTOR_KEY)) {
        params[i] = this;
      } else {
        try {
          params[i] = getInstanceInternal(key);
        } catch (UnsatisfiedDependencyException e) {
          throw new UnsatisfiedDependencyException(new Key<>(ctor.getDeclaringClass()), e);
        }
      }
    }
    return params;
  }

  private String findName(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof Named) {
        return ((Named) annotation).value();
      }
    }
    return null;
  }

  /**
   * Finds a provider for the given key.
   *
   * Calls are guaranteed idempotent and non-blocking.
   */
  @SuppressWarnings("unchecked")
  @Nonnull
  private synchronized <T> Provider<T> getProvider(final Key<T> key) {
    // Previously-gotten providers (including those from subinjectors) will already be present.
    return (Provider<T>) providers.computeIfAbsent(key, k -> {
      // @AutoFactory requests are always handled by the top-level injector.
      if (key.isAutoFactory()) {
        return memoized(new ScopeBuilderProvider<>(key.getDependencyClass()));
      }

      // Find a provider locally.
      return findLocalProvider(key);
    });
  }

  private <T> Provider<T> findLocalProvider(Key<T> key) throws UnsatisfiedDependencyException {
    // If it's an array or collection, look for plugins.
    if (key.isArray()) {
      Provider<T> tProvider = new ArrayProvider(key.getComponentType());
      return memoized(tProvider);
    } else if (key.isCollection()) {
      Provider<T> tProvider = new ListProvider(key.getComponentType());
      return memoized(tProvider);
    }

    // Attempt to resolve an implementation class.
    Class<T> dependencyClass = key.getDependencyClass();

    // Try to find a solitary plugin...
    Class<? extends T> implClass = pluginFinder.findPlugin(dependencyClass);

    // ... or a default implementation class, if configured...
    if (implClass == null) {
      implClass = getDefaultImpl(key);
    }

    // ... otherwise if the dependency class is concrete (and not primitive or java.*), just use it.
    if (implClass == null && isConcrete(dependencyClass) && !isSystem(dependencyClass)) {
      implClass = dependencyClass;
    }

    if (implClass != null) {
      // Found an implementation class!
      return memoized(implClass);
    }

    // No luck.
    throw new UnsatisfiedDependencyException(key, null);
  }

  private <T> boolean isConcrete(Class<T> clazz) {
    return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
  }

  private <T> Class<? extends T> getDefaultImpl(Key<T> key) {
    Class<?> aClass = defaultImpls.get(key);
    return (Class<? extends T>) aClass;
  }

  private boolean isSystem(Class<?> clazz) {
    if (clazz.isPrimitive()) {
      return true;
    }
    Package aPackage = clazz.getPackage();
    return aPackage == null || aPackage.getName().startsWith("java.");
  }

  /** Identifies an injection point. */
  public static class Key<T> {

    @Nonnull
    private final Type theInterface;
    private final String name;

    private Key(@Nonnull Type theInterface) {
      this(theInterface, null);
    }

    public Key(Type theInterface, String name) {
      this.theInterface = theInterface;
      this.name = name;
    }

    Class<T> getDependencyClass() {
      //noinspection unchecked
      return (Class<T>) theInterface;
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
      return theInterface.equals(key.theInterface) && Objects.equals(name, key.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(theInterface, name);
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append("Key<").append(theInterface);
      if (name != null) {
        buf.append(" named \"")
            .append(name)
            .append("\"");
      }
      buf.append(">");
      return buf.toString();
    }

    String toShortString() {
      StringBuilder buf = new StringBuilder();
      buf.append(theInterface instanceof Class
          ? ((Class) theInterface).getSimpleName()
          : theInterface.getTypeName());
      if (name != null) {
        buf.append(" \"")
            .append(name)
            .append("\"");
      }
      return buf.toString();
    }

    public boolean isArray() {
      return (theInterface instanceof Class && ((Class) theInterface).isArray())
          || theInterface instanceof GenericArrayType;
    }

    public boolean isCollection() {
      if (theInterface instanceof ParameterizedType) {
        Type rawType = ((ParameterizedType) theInterface).getRawType();
        return Collection.class.isAssignableFrom((Class<?>) rawType);
      }
      return false;
    }

    Class<?> getComponentType() {
      if (isArray()) {
        if (theInterface instanceof Class) {
          return ((Class) theInterface).getComponentType();
        } else if (theInterface instanceof GenericArrayType) {
          Type genericComponentType = ((GenericArrayType) theInterface).getGenericComponentType();
          return (Class<?>) ((ParameterizedType) genericComponentType).getRawType();
        } else {
          throw new InjectionException(this, new IllegalArgumentException());
        }
      } else if (isCollection() && theInterface instanceof ParameterizedType) {
        return (Class) ((ParameterizedType) theInterface).getActualTypeArguments()[0];
      } else {
        throw new IllegalStateException(theInterface + "...?");
      }
    }

    boolean isAutoFactory() {
      return theInterface instanceof Class
          && ((Class) theInterface).isAnnotationPresent(AutoFactory.class);
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

  private class ListProvider<T> implements Provider<List<T>> {
    private final Class<T> clazz;

    ListProvider(Class<T> clazz) {
      this.clazz = clazz;
    }

    @Override
    public List<T> get() {
      List<T> plugins = new ArrayList<>();
      for (Class<? extends T> pluginClass : pluginFinder.findPlugins(clazz)) {
        plugins.add(inject(pluginClass));
      }
      return Collections.unmodifiableList(plugins);
    }
  }

  private class ArrayProvider<T> implements Provider<T[]> {
    private final ListProvider<T> listProvider;

    ArrayProvider(Class<T> clazz) {
      this.listProvider = new ListProvider<>(clazz);
    }

    @Override
    public T[] get() {
      T[] emptyArray = (T[]) Array.newInstance(listProvider.clazz, 0);
      return listProvider.get().toArray(emptyArray);
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
      Builder subBuilder = new Injector.Builder(Injector.this, pluginFinder);
      if (method.getParameterCount() > 0) {
        AnnotatedType[] parameterTypes = method.getAnnotatedParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
          Type paramType = parameterTypes[i].getType();
          String name = findName(parameterAnnotations[i]);
          Object arg = args[i];
          subBuilder.bind(new Key<>(paramType, name), arg);
        }
      }

      Class<?> returnType = method.getReturnType();
      return subBuilder.build().getInstance(new Key<T>(returnType));
    }
  }

  private static class UnsatisfiedDependencyException extends RuntimeException {

    private final Key<?> key;
    private final UnsatisfiedDependencyException inner;

    UnsatisfiedDependencyException(Key<?> key, UnsatisfiedDependencyException inner) {
      super(key.toString());
      this.key = key;
      this.inner = inner;
    }

    <T> InjectionException asInjectionException(Key<T> key) {
      StringBuilder buf = new StringBuilder("Failed to resolve dependency: ");
      UnsatisfiedDependencyException current = this;
      while (current != null) {
        buf.append(current.key.toShortString());
        current = current.inner;
        if (current != null) {
          buf.append("/");
        }
      }
      return new InjectionException(key, buf.toString(), this);
    }
  }
}
