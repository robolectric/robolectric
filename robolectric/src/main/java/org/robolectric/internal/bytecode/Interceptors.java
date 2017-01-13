package org.robolectric.internal.bytecode;

import org.robolectric.util.Function;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodType.methodType;

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

  private final MethodHandle NOTHING = constant(Void.class, null).asType(methodType(void.class));

  public MethodHandle getMethodHandle(String className, String methodName, MethodType type) {
    Interceptor mh = interceptors.get(new MethodRef(className, methodName));
    if (mh == null) mh = interceptors.get(new MethodRef(className, "*"));
    if (mh != null) {
      try {
        return mh.getMethodHandle(methodName, type);
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    if (type.parameterCount() != 0) {
      return dropArguments(NOTHING, 0, type.parameterArray());
    } else {
      return NOTHING;
    }
  }
}
