package org.robolectric.bytecode;

import org.robolectric.internal.ReflectionHelpers;

public class RobolectricInternals {
  public static final String ROBO_PREFIX = "$$robo$$";

  @SuppressWarnings({"UnusedDeclaration"})
  private static ClassHandler classHandler; // initialized via magic by SdkEnvironment

  public static ClassHandler getClassHandler() {
    return classHandler;
  }

  public static <T> T newInstanceOf(Class<T> clazz) {
    return ReflectionHelpers.callConstructorReflectively(clazz);
  }

  public static <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
    return ReflectionHelpers.callConstructorReflectively(clazz, ReflectionHelpers.ClassParameter.fromComponentLists(parameterTypes, params));
  }

  public static <T> T directlyOn(T shadowedObject, Class<T> clazz) {
    return newInstance(clazz, new Class[]{DirectObjectMarker.class, clazz},
        new Object[]{DirectObjectMarker.INSTANCE, shadowedObject});
  }

  private static String desc(Object o) {
    return o == null ? "null" : (
        (o instanceof Class)
            ? "class " + ((Class) o).getName()
            : "instance 0x" + Integer.toHexString(System.identityHashCode(o)) + " of " + o.getClass().getName());
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static void classInitializing(Class clazz) throws Exception {
    classHandler.classInitializing(clazz);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object initializing(Object instance) throws Exception {
    return classHandler.initializing(instance);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static ClassHandler.Plan methodInvoked(String signature, boolean isStatic, Class<?> theClass) {
    return classHandler.methodInvoked(signature, isStatic, theClass);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object intercept(String signature, Object instance, Object[] params, Class theClass) throws Throwable {
    try {
      return classHandler.intercept(signature, instance, params, theClass);
    } catch (java.lang.LinkageError e) {
      throw new Exception(e);
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Throwable cleanStackTrace(Throwable exception) throws Throwable {
    return classHandler.stripStackTrace(exception);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(Object o) {
    return o;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(boolean o) {
    return o;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(byte o) {
    return o;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(char o) {
    return o;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(short o) {
    return o;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(int o) {
    return o;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(long o) {
    return o;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(float o) {
    return o;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static Object autobox(double o) {
    return o;
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

  // we need a better spot for these methods that don't rely on being in the same classloader as their operands
  public static void performStaticInitialization(Class<?> clazz) {
    ReflectionHelpers.callStaticMethodReflectively(clazz, InstrumentingClassLoader.STATIC_INITIALIZER_METHOD_NAME);
  }

  public static <R> R invokeConstructor(Class<? extends R> clazz, R instance, ReflectionHelpers.StringParameter paramValue0, ReflectionHelpers.StringParameter... paramValues) {
    ReflectionHelpers.ClassParameter[] classParamValues = new ReflectionHelpers.ClassParameter[paramValues.length + 1];
    try {
      Class<?> paramClass = clazz.getClassLoader().loadClass(paramValue0.className);
      classParamValues[0] = new ReflectionHelpers.ClassParameter(paramClass, paramValue0.val);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    for (int i = 0; i < paramValues.length; i++) {
      try {
        Class<?> paramClass = clazz.getClassLoader().loadClass(paramValues[i].className);
        classParamValues[i + 1] = new ReflectionHelpers.ClassParameter(paramClass, paramValues[i].val);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return invokeConstructor(clazz, instance, classParamValues);
  }

  public static <R> R invokeConstructor(Class<? extends R> clazz, R instance, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = directMethodName(clazz.getName(), InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME);
    return ReflectionHelpers.callInstanceMethodReflectively(instance, directMethodName, paramValues);
  }
}
