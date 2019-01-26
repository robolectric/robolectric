package org.robolectric.android.interceptors;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodType.methodType;

import com.google.auto.service.AutoService;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.util.Function;

@AutoService(Interceptor.class)
@SuppressWarnings("NewApi")
public class NoOpInterceptor extends Interceptor {

  public NoOpInterceptor() {
    super(
        new MethodRef("java.lang.System", "loadLibrary"),
        new MethodRef("android.os.StrictMode", "trackActivity"),
        new MethodRef("android.os.StrictMode", "incrementExpectedActivityCount"),
        new MethodRef("android.util.LocaleUtil", "getLayoutDirectionFromLocale"),
        new MethodRef("android.view.FallbackEventHandler", "*"),
        new MethodRef("android.view.IWindowSession", "*")
    );
  }

  @Override
  public Function<Object, Object> handle(MethodSignature methodSignature) {
    return returnDefaultValue(methodSignature);
  }

  @Override
  public MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    MethodHandle nothing = constant(Void.class, null).asType(methodType(void.class));

    if (type.parameterCount() != 0) {
      return dropArguments(nothing, 0, type.parameterArray());
    } else {
      return nothing;
    }
  }
}
