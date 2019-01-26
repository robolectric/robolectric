package org.robolectric.android.interceptors;

import static java.lang.invoke.MethodType.methodType;

import com.google.auto.service.AutoService;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.util.Function;

@AutoService(Interceptor.class)
@SuppressWarnings("NewApi")
public class SystemLogEInterceptor extends Interceptor {

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public SystemLogEInterceptor() {
    super(new MethodRef(System.class.getName(), "logE"));
  }

  static void logE(Object... params) {
    String message = "System.logE: ";
    for (Object param : params) {
      message += param.toString();
    }
    System.err.println(message);
  }

  @Override
  public Function<Object, Object> handle(MethodSignature methodSignature) {
    return (theClass, value, params) -> {
      logE(params);
      return null;
    };
  }

  @Override
  public MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    return lookup.findStatic(getClass(), "logE",
        methodType(void.class, Object[].class));
  }
}
