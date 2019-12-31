package org.robolectric.util.reflector;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

/** Access to sun.misc.Unsafe and the various scary things within. */
public class UnsafeAccess {

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

  static <T> Class<?> defineClass(Class<T> iClass, String reflectorClassName, byte[] bytecode) {
    return UNSAFE.defineClass(
        reflectorClassName, bytecode, 0, bytecode.length, iClass.getClassLoader(), null);
  }

}
