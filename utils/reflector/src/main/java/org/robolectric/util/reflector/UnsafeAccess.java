package org.robolectric.util.reflector;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import org.robolectric.util.Util;
import sun.misc.Unsafe;

/** Access to sun.misc.Unsafe and the various scary things within. */
@SuppressWarnings("NewApi")
public class UnsafeAccess {

  private static final Danger DANGER =
      Util.getJavaVersion() < 11 ? new DangerPre11() : new Danger11Plus();

  interface Danger {
    <T> Class<?> defineClass(Class<T> iClass, String reflectorClassName, byte[] bytecode);
  }

  static <T> Class<?> defineClass(Class<T> iClass, String reflectorClassName, byte[] bytecode) {
    return DANGER.defineClass(iClass, reflectorClassName, bytecode);
  }

  @SuppressWarnings("RethrowReflectiveOperationExceptionAsLinkageError")
  private static class DangerPre11 implements Danger {
    private final Unsafe unsafe;
    private final Method defineClassMethod;

    {
      try {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        unsafe = (Unsafe) unsafeField.get(null);
        defineClassMethod =
            Unsafe.class.getMethod(
                "defineClass",
                String.class,
                byte[].class,
                int.class,
                int.class,
                ClassLoader.class,
                ProtectionDomain.class);
      } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
        throw new AssertionError(e);
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<?> defineClass(Class<T> iClass, String reflectorClassName, byte[] bytecode) {
      // use reflection to call since this method does not exist on JDK11
      try {
        return (Class<?>)
            defineClassMethod.invoke(
                unsafe,
                reflectorClassName,
                bytecode,
                0,
                bytecode.length,
                iClass.getClassLoader(),
                null);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new AssertionError(e);
      }
    }
  }

  @SuppressWarnings("RethrowReflectiveOperationExceptionAsLinkageError")
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
}
