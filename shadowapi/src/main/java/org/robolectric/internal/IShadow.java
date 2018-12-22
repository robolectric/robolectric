package org.robolectric.internal;

import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings("TypeParameterUnusedInFormals")
public interface IShadow {
  <T> T extract(Object instance);

  <T> T newInstanceOf(Class<T> clazz);

  <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params);

  <T> T directlyOn(T shadowedObject, Class<T> clazz);

  @SuppressWarnings("unchecked")
  <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ReflectionHelpers.ClassParameter... paramValues);

  <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues);

  <R, T> R directlyOn(Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues);

  <R> R invokeConstructor(Class<? extends R> clazz, R instance, ReflectionHelpers.ClassParameter... paramValues);

  String directMethodName(String className, String methodName);

  void directInitialize(Class<?> clazz);
}
