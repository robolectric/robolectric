package org.robolectric.shadow.api;

import org.robolectric.internal.IShadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public class Shadow {
  @SuppressWarnings("unused")
  private final static IShadow SHADOW_IMPL;

  static {
    try {
      SHADOW_IMPL = Class.forName("org.robolectric.internal.bytecode.ShadowImpl")
          .asSubclass(IShadow.class).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Retrieve corresponding Shadow of the object.
   * @since 3.3
   */
  @SuppressWarnings("TypeParameterUnusedInFormals")
  public static <T> T extract(Object instance) {
    return SHADOW_IMPL.extract(instance);
  }

  public static <T> T newInstanceOf(Class<T> clazz) {
    return SHADOW_IMPL.newInstanceOf(clazz);
  }

  public static Object newInstanceOf(String className) {
    try {
      Class<?> aClass = Shadow.class.getClassLoader().loadClass(className);
      return SHADOW_IMPL.newInstanceOf(aClass);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
    return SHADOW_IMPL.newInstance(clazz, parameterTypes, params);
  }

  public static <T> T directlyOn(T shadowedObject, Class<T> clazz) {
    return SHADOW_IMPL.directlyOn(shadowedObject, clazz);
  }

  @SuppressWarnings(value = {"unchecked", "TypeParameterUnusedInFormals"})
  public static <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ClassParameter... paramValues) {
    return SHADOW_IMPL.directlyOn(shadowedObject, clazzName, methodName, paramValues);
  }

  @SuppressWarnings("TypeParameterUnusedInFormals")
  public static <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ClassParameter... paramValues) {
    return SHADOW_IMPL.directlyOn(shadowedObject, clazz, methodName, paramValues);
  }

  @SuppressWarnings("TypeParameterUnusedInFormals")
  public static <R, T> R directlyOn(Class<T> clazz, String methodName, ClassParameter... paramValues) {
    return SHADOW_IMPL.directlyOn(clazz, methodName, paramValues);
  }

  public static <R> R invokeConstructor(Class<? extends R> clazz, R instance, ClassParameter... paramValues) {
    return SHADOW_IMPL.invokeConstructor(clazz, instance, paramValues);
  }

  public static String directMethodName(String className, String methodName) {
    return SHADOW_IMPL.directMethodName(className, methodName);
  }

  public static void directInitialize(Class<?> clazz) {
    SHADOW_IMPL.directInitialize(clazz);
  }
}
