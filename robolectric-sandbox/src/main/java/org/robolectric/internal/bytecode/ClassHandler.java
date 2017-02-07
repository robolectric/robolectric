package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public interface ClassHandler {
  void classInitializing(Class clazz);

  Object initializing(Object instance);

  Plan methodInvoked(String signature, boolean isStatic, Class<?> theClass);

  MethodHandle getShadowCreator(Class<?> caller);

  MethodHandle findShadowMethod(Class<?> theClass, String name, MethodType type,
      boolean isStatic)
      throws IllegalAccessException;

  Object intercept(String signature, Object instance, Object[] params, Class theClass) throws Throwable;

  <T extends Throwable> T stripStackTrace(T throwable);

  public interface Plan {
    Object run(Object instance, Object roboData, Object[] params) throws Throwable;

    String describe();
  }
}
