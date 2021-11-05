package org.robolectric.internal;

import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings("TypeParameterUnusedInFormals")
public interface IShadow {
  <T> T extract(Object instance);

  <T> T newInstanceOf(Class<T> clazz);

  <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params);

  /**
   * Returns a proxy object that invokes the original $$robo$$-prefixed methods for {@code
   * shadowedObject}.
   *
   * @deprecated This is incompatible with JDK17+. Use a {@link
   *     org.robolectric.util.reflector.Reflector} interface with {@link
   *     org.robolectric.util.reflector.Direct}.
   */
  @Deprecated
  <T> T directlyOn(T shadowedObject, Class<T> clazz);

  @SuppressWarnings("unchecked")
  <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ReflectionHelpers.ClassParameter... paramValues);

  <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues);

  <R, T> R directlyOn(Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues);

  <R> R invokeConstructor(Class<? extends R> clazz, R instance, ReflectionHelpers.ClassParameter... paramValues);

  String directMethodName(String className, String methodName);

  void directInitialize(Class<?> clazz);
}
