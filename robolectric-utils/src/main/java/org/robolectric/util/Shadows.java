package org.robolectric.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Shadows {
  // todo: share with AsmInstrumentingClassLoader
  private static final String GET_ROBO_DATA_METHOD_NAME = "$$robo$getData";

  public static Object shadowOf(Object instance) {
    if (instance == null) {
      throw new NullPointerException("can't get a shadow for null");
    }

    Object roboData;
    try {
      Method getRoboData = null;
      Class<?> clazz = instance.getClass();
      while (clazz != null) {
        try {
          getRoboData = clazz.getDeclaredMethod(GET_ROBO_DATA_METHOD_NAME);
          break;
        } catch (NoSuchMethodException e) { }
        clazz = clazz.getSuperclass();
      }
      if (getRoboData == null) {
        throw new RuntimeException("can't get a shadow for " + instance);
      }
      getRoboData.setAccessible(true);
      roboData = getRoboData.invoke(instance);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    return roboData;
  }
}
