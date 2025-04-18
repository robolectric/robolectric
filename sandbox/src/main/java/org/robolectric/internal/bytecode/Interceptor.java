package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import javax.annotation.Nonnull;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

public abstract class Interceptor {
  private final MethodRef[] methodRefs;

  public Interceptor(MethodRef... methodRefs) {
    this.methodRefs = methodRefs;
  }

  public MethodRef[] getMethodRefs() {
    return methodRefs;
  }

  public abstract Function<Object, Object> handle(MethodSignature methodSignature);

  public abstract MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException;

  @Nonnull
  protected static Function<Object, Object> returnDefaultValue(
      final MethodSignature methodSignature) {
    return (theClass, value, params) ->
        ReflectionHelpers.defaultValueForType(methodSignature.returnType);
  }
}
