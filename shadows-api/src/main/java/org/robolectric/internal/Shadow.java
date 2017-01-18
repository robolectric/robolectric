package org.robolectric.internal;

import org.robolectric.util.ReflectionHelpers.ClassParameter;

public class Shadow {
  @SuppressWarnings("unused")
  private static IShadow SHADOW_IMPL;

  public static <T> T newInstanceOf(Class<T> clazz) {
    return SHADOW_IMPL.newInstanceOf(clazz);
  }

  public static Object newInstanceOf(String className) {
    return SHADOW_IMPL.newInstanceOf(className);
  }

  public static <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
    return SHADOW_IMPL.newInstance(clazz, parameterTypes, params);
  }

  public static <T> T directlyOn(T shadowedObject, Class<T> clazz) {
    return SHADOW_IMPL.directlyOn(shadowedObject, clazz);
  }

  @SuppressWarnings("unchecked")
  public static <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ClassParameter... paramValues) {
    return SHADOW_IMPL.directlyOn(shadowedObject, clazzName, methodName, paramValues);
  }

  public static <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ClassParameter... paramValues) {
    return SHADOW_IMPL.directlyOn(shadowedObject, clazz, methodName, paramValues);
  }

  public static <R, T> R directlyOn(Class<T> clazz, String methodName, ClassParameter... paramValues) {
    return SHADOW_IMPL.directlyOn(clazz, methodName, paramValues);
  }

  public static <R> R invokeConstructor(Class<? extends R> clazz, R instance, ClassParameter... paramValues) {
    return SHADOW_IMPL.invokeConstructor(clazz, instance, paramValues);
  }

  public static String directMethodName(String methodName) {
    return SHADOW_IMPL.directMethodName(methodName);
  }
}
