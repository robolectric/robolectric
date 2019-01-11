package org.robolectric.util.reflector;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import sun.misc.Unsafe;

/** Access to sun.misc.Unsafe and the various scary things within. */
public class UnsafeAccess {

  private static final Danger DANGER =
      getJavaVersion() < 11
          ? new DangerPre11()
          : new Danger11Plus();

  interface Danger {
    <T> Class<?> defineClass(Class<T> iClass, String reflectorClassName, byte[] bytecode);
  }

  static <T> Class<?> defineClass(Class<T> iClass, String reflectorClassName, byte[] bytecode) {
    return DANGER.defineClass(iClass, reflectorClassName, bytecode);
  }

  private static class DangerPre11 implements Danger {
    private final Unsafe unsafe;

    {
      try {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        unsafe = (Unsafe) unsafeField.get(null);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    @Override
    public <T> Class<?> defineClass(Class<T> iClass, String reflectorClassName, byte[] bytecode) {
      return unsafe.defineClass(
          reflectorClassName, bytecode, 0, bytecode.length, iClass.getClassLoader(), null);
    }
  }

  private static class Danger11Plus implements Danger {
    private final Method privateLookupInMethod;
    private final Method defineClassMethod;

    {
      try {
        privateLookupInMethod =
            MethodHandles.class.getMethod(
                "privateLookupIn", Class.class, MethodHandles.Lookup.class);
        defineClassMethod =
            MethodHandles.Lookup.class.getMethod("defineClass", byte[].class);
      } catch (NoSuchMethodException e) {
        throw new AssertionError(e);
      }
    }

    @Override
    public <T> Class<?> defineClass(Class<T> iClass, String reflectorClassName, byte[] bytecode) {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      try {
        // MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(iClass, lookup);
        MethodHandles.Lookup privateLookup =
            (Lookup) privateLookupInMethod.invoke(lookup, iClass, lookup);

        // return privateLookup.defineClass(bytecode);
        return (Class<?>) defineClassMethod.invoke(privateLookup, bytecode);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new AssertionError(e);
      }
    }
  }

  /**
   * Returns the Java version as an int value.
   *
   * @return the Java version as an int value (8, 9, etc.)
   */
  private static int getJavaVersion() {
    String version = System.getProperty("java.version");
    assert version != null;
    if (version.startsWith("1.")) {
      version = version.substring(2);
    }
    // Allow these formats:
    // 1.8.0_72-ea
    // 9-ea
    // 9
    // 9.0.1
    int dotPos = version.indexOf('.');
    int dashPos = version.indexOf('-');
    return Integer.parseInt(
        version.substring(0, dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : 1));
  }
}
