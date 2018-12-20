package org.robolectric.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import sun.misc.Unsafe;

@SuppressWarnings("NewApi")
public class Reflector {

  private static final AtomicInteger COUNTER = new AtomicInteger();
  private static final Map<Class<?>, Constructor<?>> CACHE =
      Collections.synchronizedMap(new WeakerHashMap<>());
  private static final Unsafe UNSAFE;

  static {
    try {
      Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      UNSAFE = (Unsafe) unsafeField.get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Returns an object which provides accessors for invoking otherwise inaccessible methods.
   *
   * @param iClass an interface with methods matching private methods on the target
   * @param target the target object
   */
  public static <T> T reflector(Class<T> iClass, Object target) {
    Class<?> targetClass = determineTargetClass(iClass);

    Constructor<? extends T> ctor = (Constructor<? extends T>) CACHE.get(iClass);
    try {
      if (ctor == null) {
        Class<? extends T> reflectorClass = createReflectorClass(iClass, targetClass);
        ctor = reflectorClass.getConstructor(targetClass);
        ctor.setAccessible(true);
      }

      CACHE.put(iClass, ctor);

      return ctor.newInstance(target);
    } catch (NoSuchMethodException | InstantiationException
        | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  private static <T> Class<?> determineTargetClass(Class<T> iClass) {
    ForType forType = iClass.getAnnotation(ForType.class);
    if (forType == null) {
      throw new IllegalArgumentException("no @ForType annotation found for " + iClass);
    }
    Class<?> targetClass = forType.value();
    if (targetClass.equals(void.class)) {
      String forClassName = forType.className();
      if (forClassName.isEmpty()) {
        throw new IllegalArgumentException(
            "no value or className given for @ForType for " + iClass);
      }

      try {
        targetClass = Class.forName(forClassName, false, iClass.getClassLoader());
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("failed to resolve @ForType class for " + iClass, e);
      }
    }
    return targetClass;
  }

  private static <T> Class<? extends T> createReflectorClass(
      Class<T> iClass, Class<?> targetClass) {
    String reflectorClassName = iClass.getName() + "$$Reflector" + COUNTER.getAndIncrement();
    byte[] bytecode = getBytecode(iClass, targetClass, reflectorClassName);

    final Class<?> proxyClass;
    proxyClass = defineViaUnsafe(iClass, reflectorClassName, bytecode);
    // proxyClass = defineViaNewClassLoader(iClass, reflectorClassName, bytecode);

    return proxyClass.asSubclass(iClass);
  }

  private static <T> Class<?> defineViaUnsafe(Class<T> iClass, String reflectorClassName,
      byte[] bytecode) {
    return UNSAFE.defineClass(reflectorClassName, bytecode, 0, bytecode.length,
        iClass.getClassLoader(), null);
  }

  @SuppressWarnings("unused")
  private static <T> Class<?> defineViaNewClassLoader(
      Class<T> iClass, String reflectorClassName, byte[] bytecode) {
    Class<?> proxyClass;
    ClassLoader classLoader = new ClassLoader(iClass.getClassLoader()) {
      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        return defineClass(name, bytecode, 0, bytecode.length);
      }
    };
    try {
      proxyClass = classLoader.loadClass(reflectorClassName);
    } catch (ClassNotFoundException e) {
      throw new AssertionError(e);
    }
    return proxyClass;
  }

  private static <T> byte[] getBytecode(Class<T> iClass, Class<?> targetClass,
      String reflectorClassName) {
    ReflectorClassWriter writer = new ReflectorClassWriter(iClass, targetClass, reflectorClassName);
    writer.write();

    return writer.toByteArray();
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface ForType {

    Class<?> value() default void.class;

    String className() default "";
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface WithType {

    String value();
  }

  /**
   * Values are held via {@link WeakReference}, so if any class becomes otherwise unreachable it can
   * be garbage collected.
   */
  private static class WeakerHashMap<K, V> implements Map<K, V> {

    public final Map<K, WeakReference<V>> map = new WeakHashMap<>();

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
      return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V get(Object key) {
      WeakReference<V> ref = map.get(key);
      if (ref != null) {
        V v = ref.get();
        if (v == null) {
          map.remove(key);
        }
        return v;
      }
      return null;
    }

    @Override
    public V put(K key, V value) {
      WeakReference<V> oldV = map.put(key, new WeakReference<>(value));
      return oldV == null ? null : oldV.get();
    }

    @Override
    public V remove(Object key) {
      WeakReference<V> oldV = map.remove(key);
      return oldV == null ? null : oldV.get();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      map.clear();
    }

    @Override
    public Set<K> keySet() {
      return map.keySet();
    }

    @Override
    public Collection<V> values() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      throw new UnsupportedOperationException();
    }
  }

}
