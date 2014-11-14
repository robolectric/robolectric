package org.robolectric.bytecode;

import org.robolectric.util.ReflectionHelpers;

public class RobolectricInternals {
  public static final String ROBO_PREFIX = "$$robo$$";

  @SuppressWarnings("UnusedDeclaration")
  private static ClassHandler classHandler; // initialized via magic by SdkEnvironment

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

  @SuppressWarnings("UnusedDeclaration")
  public static Throwable cleanStackTrace(Throwable exception) throws Throwable {
    return classHandler.stripStackTrace(exception);
  }

  public static Object intercept(String signature, Object instance, Object[] params, Class theClass) throws Throwable {
    try {
      return classHandler.intercept(signature, instance, params, theClass);
    } catch (java.lang.LinkageError e) {
      throw new Exception(e);
    }
  }

  public static String directMethodName(String methodName) {
    return String.format(ROBO_PREFIX + "%s", methodName);
  }

  public static String directMethodName(String className, String methodName) {
    String simpleName = className;
    int lastDotIndex = simpleName.lastIndexOf(".");
    if (lastDotIndex != -1) simpleName = simpleName.substring(lastDotIndex + 1);
    int lastDollarIndex = simpleName.lastIndexOf("$");
    if (lastDollarIndex != -1) simpleName = simpleName.substring(lastDollarIndex + 1);
    return String.format(ROBO_PREFIX + "%s_%04x_%s", simpleName, className.hashCode() & 0xffff, methodName);
  }

  public static void performStaticInitialization(Class<?> clazz) {
    ReflectionHelpers.callStaticMethodReflectively(clazz, InstrumentingClassLoader.STATIC_INITIALIZER_METHOD_NAME);
  }
}
