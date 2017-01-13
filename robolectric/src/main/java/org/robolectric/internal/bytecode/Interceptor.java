package org.robolectric.internal.bytecode;

import org.robolectric.util.Function;

public abstract class Interceptor {
  private MethodRef[] methodRefs;

  public Interceptor(MethodRef... methodRefs) {
    this.methodRefs = methodRefs;
  }

  public MethodRef[] getMethodRefs() {
    return methodRefs;
  }

  public abstract Function<Object, Object> handle(MethodSignature methodSignature);
}
