package org.robolectric.internal.bytecode;

import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interceptors {
  private final Map<MethodRef, Interceptor> interceptors = new HashMap<>();

  public Interceptors(List<Interceptor> interceptorList) {
    for (Interceptor interceptor : interceptorList) {
      for (MethodRef methodRef : interceptor.getMethodRefs()) {
        this.interceptors.put(methodRef, interceptor);
      }
    }
  }

  public Function<Object, Object> getInterceptionHandler(final MethodSignature methodSignature) {
    Interceptor interceptor = interceptors.get(new MethodRef(methodSignature.className, methodSignature.methodName));
    if (interceptor != null) {
      return interceptor.handle(methodSignature);
    }

    // nothing matched, return default
    return new Function<Object, Object>() {
      @Override
      public Object call(Class<?> theClass, Object value, Object[] params) {
        return ReflectionHelpers.PRIMITIVE_RETURN_VALUES.get(methodSignature.returnType);
      }
    };
  }
}
