package org.robolectric.internal.bytecode;

import org.robolectric.util.ReflectionHelpers;
import org.robolectric.internal.ShadowConstants;

public class RobolectricInternals {

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

  public static void performStaticInitialization(Class<?> clazz) {
    ReflectionHelpers.callStaticMethod(clazz, ShadowConstants.STATIC_INITIALIZER_METHOD_NAME);
  }
}
