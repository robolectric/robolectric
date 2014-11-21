package org.robolectric.util;

import org.robolectric.bytecode.DirectObjectMarker;

public class ShadowThingy {
  public static <T> T newInstanceOf(Class<T> clazz) {
    return ReflectionHelpers.callConstructorReflectively(clazz);
  }

  public static Object newInstanceOf(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      if (clazz != null) {
        return newInstanceOf(clazz);
      }
    } catch (ClassNotFoundException e) {
    }
    return null;
  }

  public static <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
    return ReflectionHelpers.callConstructorReflectively(clazz, ReflectionHelpers.ClassParameter.fromComponentLists(parameterTypes, params));
  }

  public static <T> T directlyOn(T shadowedObject, Class<T> clazz) {
    return ReflectionHelpers.callConstructorReflectively(clazz, ReflectionHelpers.ClassParameter.fromComponentLists(new Class[]{DirectObjectMarker.class, clazz}, new Object[]{DirectObjectMarker.INSTANCE, shadowedObject}));
  }

  public static <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    try {
      Class<Object> aClass = (Class<Object>) shadowedObject.getClass().getClassLoader().loadClass(clazzName);
      return directlyOn(shadowedObject, aClass, methodName, paramValues);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = directMethodName(clazz.getName(), methodName);
    return ReflectionHelpers.callInstanceMethodReflectively(shadowedObject, directMethodName, paramValues);
  }

  public static <R, T> R directlyOn(Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = directMethodName(clazz.getName(), methodName);
    return ReflectionHelpers.callStaticMethodReflectively(clazz, directMethodName, paramValues);
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
    String directMethodName = directMethodName(clazz.getName(), ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    return ReflectionHelpers.callInstanceMethodReflectively(instance, directMethodName, paramValues);
  }

  public static String directMethodName(String methodName) {
    return String.format(ShadowConstants.ROBO_PREFIX + "%s", methodName);
  }

  public static String directMethodName(String className, String methodName) {
    String simpleName = className;
    int lastDotIndex = simpleName.lastIndexOf(".");
    if (lastDotIndex != -1) simpleName = simpleName.substring(lastDotIndex + 1);
    int lastDollarIndex = simpleName.lastIndexOf("$");
    if (lastDollarIndex != -1) simpleName = simpleName.substring(lastDollarIndex + 1);
    return String.format(ShadowConstants.ROBO_PREFIX + "%s_%04x_%s", simpleName, className.hashCode() & 0xffff, methodName);
  }
}
