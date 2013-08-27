package org.robolectric.bytecode;

import org.fest.reflect.method.Invoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.fest.reflect.core.Reflection.method;

public class RobolectricInternals {
  public static final String ROBO_PREFIX = "$$robo$$";

  @SuppressWarnings({"UnusedDeclaration"})
  private static ClassHandler classHandler; // initialized via magic by SdkEnvironment

  public static ClassHandler getClassHandler() {
    return classHandler;
  }

  public static <T> T newInstanceOf(Class<T> clazz) {
    try {
      Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
      defaultConstructor.setAccessible(true);
      return defaultConstructor.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
    try {
      Constructor<T> declaredConstructor = clazz.getDeclaredConstructor(parameterTypes);
      declaredConstructor.setAccessible(true);
      return declaredConstructor.newInstance(params);
    } catch (InstantiationException e) {
      throw new RuntimeException("error instantiating " + clazz.getName(), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
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
    } catch(java.lang.LinkageError e) {
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
    try {
      Method originalStaticInitializer = clazz.getDeclaredMethod(InstrumentingClassLoader.STATIC_INITIALIZER_METHOD_NAME);
      originalStaticInitializer.setAccessible(true);
      originalStaticInitializer.invoke(null);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static Invoker<Void> getConstructor(Class<?> clazz, Object instance, String... parameterTypes) {
    Class[] parameterClasses = new Class[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      try {
        parameterClasses[i] = clazz.getClassLoader().loadClass(parameterTypes[i]);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return getConstructor(clazz, instance, parameterClasses);
  }

  public static Invoker<Void> getConstructor(Class<?> clazz, Object instance, Class... parameterTypes) {
    String name = directMethodName(clazz.getName(), InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME);
    return method(name).withParameterTypes(parameterTypes).in(instance);
  }
}
