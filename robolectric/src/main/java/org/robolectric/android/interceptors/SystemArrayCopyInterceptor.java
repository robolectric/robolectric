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
public class SystemArrayCopyInterceptor extends Interceptor {

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public SystemArrayCopyInterceptor() {
    super(new MethodRef(System.class, "arraycopy"));
  }

  @Override
  public Function<Object, Object> handle(MethodSignature methodSignature) {
    return (theClass, value, params) -> {
      //noinspection SuspiciousSystemArraycopy
      System.arraycopy(params[0], (Integer) params[1], params[2], (Integer) params[3],
          (Integer) params[4]);
      return null;
    };
  }

  @Override
  public MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    return lookup.findStatic(System.class, "arraycopy",
        methodType(void.class, Object.class, int.class, Object.class, int.class, int.class));
  }
}
