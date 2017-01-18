package org.robolectric.internal.bytecode;

import org.jetbrains.annotations.NotNull;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodType.methodType;

public class Interceptor {
  private MethodRef[] methodRefs;

  public Interceptor(MethodRef... methodRefs) {
    this.methodRefs = methodRefs;
  }

  public MethodRef[] getMethodRefs() {
    return methodRefs;
  }

  public Function<Object, Object> handle(MethodSignature methodSignature) {
    return returnDefaultValue(methodSignature);
  }

  public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException {
    MethodHandle nothing = constant(Void.class, null).asType(methodType(void.class));

    if (type.parameterCount() != 0) {
      return dropArguments(nothing, 0, type.parameterArray());
    } else {
      return nothing;
    }
  }

  @NotNull
  protected static Function<Object, Object> returnDefaultValue(final MethodSignature methodSignature) {
    return new Function<Object, Object>() {
      @Override
      public Object call(Class<?> theClass, Object value, Object[] params) {
        return ReflectionHelpers.PRIMITIVE_RETURN_VALUES.get(methodSignature.returnType);
      }
    };
  }
}
