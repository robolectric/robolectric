package org.robolectric.util.reflector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides accessor objects for efficiently calling otherwise inaccessible (non-public) methods.
 */
@SuppressWarnings("NewApi")
public class Reflector {

  private static final boolean DEBUG = false;
  private static final AtomicInteger COUNTER = new AtomicInteger();
  private static final Map<Class<?>, Constructor<?>> CACHE =
      Collections.synchronizedMap(new WeakerHashMap<>());

  /**
   * Returns an object which provides accessors for invoking otherwise inaccessible static methods
   * and fields.
   *
   * @param iClass an interface with methods matching private methods on the target
   */
  public static <T> T reflector(Class<T> iClass) {
    return reflector(iClass, null);
  }

  /**
   * Returns an object which provides accessors for invoking otherwise inaccessible methods and
   * fields.
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
    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
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

    if (DEBUG) {
      File file = new File("/tmp", reflectorClassName + ".class");
      System.out.println("Generated reflector: " + file.getAbsolutePath());
      try (OutputStream out = new FileOutputStream(file)) {
        out.write(bytecode);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    final Class<?> proxyClass;
    proxyClass = defineViaUnsafe(iClass, reflectorClassName, bytecode);
    // proxyClass = defineViaNewClassLoader(iClass, reflectorClassName, bytecode);

    return proxyClass.asSubclass(iClass);
  }

  private static <T> Class<?> defineViaUnsafe(
      Class<T> iClass, String reflectorClassName, byte[] bytecode) {
    return UnsafeAccess.defineClass(iClass, reflectorClassName, bytecode);
  }

  @SuppressWarnings("unused")
  private static <T> Class<?> defineViaNewClassLoader(
      Class<T> iClass, String reflectorClassName, byte[] bytecode) {
    Class<?> proxyClass;
    ClassLoader classLoader =
        new ClassLoader(iClass.getClassLoader()) {
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

  private static <T> byte[] getBytecode(
      Class<T> iClass, Class<?> targetClass, String reflectorClassName) {
    ReflectorClassWriter writer = new ReflectorClassWriter(iClass, targetClass, reflectorClassName);
    writer.write();

    return writer.toByteArray();
  }
}
