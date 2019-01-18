package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RobolectricInternals {

  @SuppressWarnings("UnusedDeclaration")
  private static ClassHandler classHandler; // initialized via magic by SdkEnvironment

  @SuppressWarnings("UnusedDeclaration")
  private static ShadowInvalidator shadowInvalidator;

  @SuppressWarnings("UnusedDeclaration")
  private static ClassLoader classLoader;

  @SuppressWarnings("UnusedDeclaration")
  public static void classInitializing(Class clazz) throws Exception {
    classHandler.classInitializing(clazz);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static Object initializing(Object instance) throws Exception {
    return classHandler.initializing(instance);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static ClassHandler.Plan methodInvoked(String signature, boolean isStatic, Class<?> theClass) {
    return classHandler.methodInvoked(signature, isStatic, theClass);
  }

  public static MethodHandle getShadowCreator(Class<?> caller) {
    return classHandler.getShadowCreator(caller);
  }

  public static MethodHandle findShadowMethodHandle(Class<?> theClass, String name,
      MethodType methodType, boolean isStatic) throws IllegalAccessException {
    return classHandler.findShadowMethodHandle(theClass, name, methodType, isStatic);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static Throwable cleanStackTrace(Throwable exception) {
    return classHandler.stripStackTrace(exception);
  }

  public static Object intercept(String signature, Object instance, Object[] params, Class theClass) throws Throwable {
    try {
      return classHandler.intercept(signature, instance, params, theClass);
    } catch (java.lang.LinkageError e) {
      throw new Exception(e);
    }
  }

  public static void performStaticInitialization(Class<?> clazz)
      throws InvocationTargetException, IllegalAccessException {
    try {
      Method clinitMethod = clazz.getDeclaredMethod(ShadowConstants.STATIC_INITIALIZER_METHOD_NAME);
      clinitMethod.setAccessible(true);
      clinitMethod.invoke(null);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(clazz + " not instrumented?", e);
    }
  }

  public static ShadowInvalidator getShadowInvalidator() {
    return shadowInvalidator;
  }

  public static ClassLoader getClassLoader() {
    return classLoader;
  }
}
