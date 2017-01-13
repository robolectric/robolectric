package org.robolectric.internal.bytecode;

import org.robolectric.util.Function;

import java.util.Collection;
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
    return Interceptor.returnDefaultValue(methodSignature);
  }

  public Collection<MethodRef> getAllMethodRefs() {
    return interceptors.keySet();
  }
}
