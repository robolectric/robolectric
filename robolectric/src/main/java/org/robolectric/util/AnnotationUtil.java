package org.robolectric.util;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AnnotationUtil {
  public static <A extends Annotation> A defaultsFor(Class<A> annotation) {
    //noinspection unchecked
    return (A) Proxy.newProxyInstance(annotation.getClassLoader(),
        new Class[]{annotation}, new InvocationHandler() {
      public Object invoke(Object proxy, @NotNull Method method, Object[] args)
          throws Throwable {
        return method.getDefaultValue();
      }
    });
  }

}
