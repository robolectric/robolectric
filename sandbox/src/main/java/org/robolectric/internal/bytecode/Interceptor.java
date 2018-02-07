package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import javax.annotation.Nonnull;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

public abstract class Interceptor {
  private MethodRef[] methodRefs;

  public Interceptor(MethodRef... methodRefs) {
    this.methodRefs = methodRefs;
  }

  public MethodRef[] getMethodRefs() {
    return methodRefs;
  }

  abstract public Function<Object, Object> handle(MethodSignature methodSignature);

  abstract public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException;

  @Nonnull
  protected static Function<Object, Object> returnDefaultValue(final MethodSignature methodSignature) {
    return new Function<Object, Object>() {
      @Override
      public Object call(Class<?> theClass, Object value, Object[] params) {
        return ReflectionHelpers.defaultValueForType(methodSignature.returnType);
      }
    };
  }
}
