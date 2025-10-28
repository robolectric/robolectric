package org.robolectric.util.reflector;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Access to sun.misc.Unsafe and the various scary things within. */
@SuppressWarnings("NewApi")
public class UnsafeAccess {
  private static final Method privateLookupInMethod;
  private static final Method defineClassMethod;

  static {
      try {
        privateLookupInMethod =
            MethodHandles.class.getMethod(
                "privateLookupIn", Class.class, MethodHandles.Lookup.class);
        defineClassMethod = MethodHandles.Lookup.class.getMethod("defineClass", byte[].class);
      } catch (NoSuchMethodException e) {
        throw new LinkageError("Failed to find defineClass method", e);
      }
    }

  public static <T> Class<?> defineClass(
      Class<T> iClass, String reflectorClassName, byte[] bytecode) {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      try {
        MethodHandles.Lookup privateLookup =
            (Lookup) privateLookupInMethod.invoke(lookup, iClass, lookup);
        return (Class<?>) defineClassMethod.invoke(privateLookup, bytecode);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new AssertionError(e);
      }
    }
}
