package org.robolectric.internal.bytecode;

import org.robolectric.internal.IShadow;
import org.robolectric.util.ReflectionHelpers;

public class ShadowImpl implements IShadow {

  private final ProxyMaker PROXY_MAKER =
      new ProxyMaker(
          new ProxyMaker.MethodMapper() {
            @Override
            public String getName(String className, String methodName) {
              return directMethodName(className, methodName);
            }
          });

  @Override
  @SuppressWarnings("TypeParameterUnusedInFormals")
  public <T> T extract(Object instance) {
    return (T) ((ShadowedObject) instance).$$robo$getData();
  }

  @Override public <T> T newInstanceOf(Class<T> clazz) {
    return ReflectionHelpers.callConstructor(clazz);
  }

  @Override public <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
    return ReflectionHelpers.callConstructor(clazz, ReflectionHelpers.ClassParameter.fromComponentLists(parameterTypes, params));
  }

  @Override public <T> T directlyOn(T shadowedObject, Class<T> clazz) {
    return createProxy(shadowedObject, clazz);
  }

  private <T> T createProxy(T shadowedObject, Class<T> clazz) {
    try {
      return PROXY_MAKER.createProxy(clazz, shadowedObject);
    } catch (Exception e) {
      throw new RuntimeException("error creating direct call proxy for " + clazz, e);
    }
  }

  @Override @SuppressWarnings(value = {"unchecked", "TypeParameterUnusedInFormals"})
  public <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    try {
      Class<Object> aClass = (Class<Object>) shadowedObject.getClass().getClassLoader().loadClass(clazzName);
      return directlyOn(shadowedObject, aClass, methodName, paramValues);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override @SuppressWarnings(value = {"unchecked", "TypeParameterUnusedInFormals"})
  public <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = directMethodName(clazz.getName(), methodName);
    return (R) ReflectionHelpers.callInstanceMethod(clazz, shadowedObject, directMethodName, paramValues);
  }

  @Override @SuppressWarnings(value = {"unchecked", "TypeParameterUnusedInFormals"})
  public <R, T> R directlyOn(Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName = directMethodName(clazz.getName(), methodName);
    return (R) ReflectionHelpers.callStaticMethod(clazz, directMethodName, paramValues);
  }

  @Override @SuppressWarnings(value = {"unchecked", "TypeParameterUnusedInFormals"})
  public <R> R invokeConstructor(Class<? extends R> clazz, R instance, ReflectionHelpers.ClassParameter... paramValues) {
    String directMethodName =
        directMethodName(clazz.getName(), ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    return (R) ReflectionHelpers.callInstanceMethod(clazz, instance, directMethodName, paramValues);
  }

  @Override
  public String directMethodName(String className, String methodName) {
     return ShadowConstants.ROBO_PREFIX
      + className.replace('.', '_').replace('$', '_')
      + "$" + methodName;
  }

}
