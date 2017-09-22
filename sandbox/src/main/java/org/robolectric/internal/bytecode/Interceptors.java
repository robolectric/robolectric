package org.robolectric.internal.bytecode;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.util.Function;

public class Interceptors {
  private final Map<MethodRef, Interceptor> interceptors = new HashMap<>();

  public Interceptors(Interceptor... interceptors) {
    this(asList(interceptors));
  }

  public Interceptors(Collection<Interceptor> interceptorList) {
    for (Interceptor interceptor : interceptorList) {
      for (MethodRef methodRef : interceptor.getMethodRefs()) {
        this.interceptors.put(methodRef, interceptor);
      }
    }
  }

  public Collection<MethodRef> getAllMethodRefs() {
    return interceptors.keySet();
  }

  public Function<Object, Object> getInterceptionHandler(final MethodSignature methodSignature) {
    Interceptor interceptor = findInterceptor(methodSignature.className, methodSignature.methodName);
    if (interceptor != null) {
      return interceptor.handle(methodSignature);
    }

    // nothing matched, return default
    return Interceptor.returnDefaultValue(methodSignature);
  }

  public Interceptor findInterceptor(String className, String methodName) {
    Interceptor mh = interceptors.get(new MethodRef(className, methodName));
    if (mh == null) {
      mh = interceptors.get(new MethodRef(className, "*"));
    }
    return mh;
  }
}
