package org.robolectric.internal;

import org.robolectric.util.ReflectionHelpers;

public class ShadowImpl implements IShadow {

  private final ProxyMaker PROXY_MAKER = new ProxyMaker(new ProxyMaker.MethodMapper() {
    @Override public String getName(String className, String methodName) {
      return directMethodName(methodName);
    }
  });

  public <T> T newInstanceOf(Class<T> clazz) {
    return ReflectionHelpers.callConstructor(clazz);
  }

  @Deprecated
  public Object newInstanceOf(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      if (clazz != null) {
        return newInstanceOf(clazz);
      }
    } catch (ClassNotFoundException e) {
    }
    return null;
  }

  public <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
    return ReflectionHelpers.callConstructor(clazz, ReflectionHelpers.ClassParameter.fromComponentLists(parameterTypes, params));
  }

  public <T> T directlyOn(T shadowedObject, Class<T> clazz) {
    return createProxy(shadowedObject, clazz);
  }

  private <T> T createProxy(T shadowedObject, Class<T> clazz) {
    try {
      if (InvokeDynamic.ENABLED) {
        return PROXY_MAKER.createProxy(clazz, shadowedObject);
      } else {
        return ReflectionHelpers.callConstructor(clazz,
            ReflectionHelpers.ClassParameter.fromComponentLists(new Class[] { DirectObjectMarker.class, clazz },
                new Object[] { DirectObjectMarker.INSTANCE, shadowedObject }));
      }
    } catch (Exception e) {
      throw new RuntimeException("error creating direct call proxy for " + clazz, e);
    }
  }

  @SuppressWarnings("unchecked")
  public <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    try {
      Class<Object> aClass = (Class<Object>) shadowedObject.getClass().getClassLoader().loadClass(clazzName);
      return directlyOn(shadowedObject, aClass, methodName, paramValues);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = directMethodName(methodName);
    return (R) ReflectionHelpers.callInstanceMethod(clazz, shadowedObject, directMethodName, paramValues);
  }

  public <R, T> R directlyOn(Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = directMethodName(methodName);
    return (R) ReflectionHelpers.callStaticMethod(clazz, directMethodName, paramValues);
  }

  public <R> R invokeConstructor(Class<? extends R> clazz, R instance, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = directMethodName(ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    return (R) ReflectionHelpers.callInstanceMethod(clazz, instance, directMethodName, paramValues);
  }

  public String directMethodName(String methodName) {
    return ShadowConstants.ROBO_PREFIX + methodName;
  }

}
