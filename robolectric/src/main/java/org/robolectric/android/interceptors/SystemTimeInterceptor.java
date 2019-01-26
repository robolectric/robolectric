package org.robolectric.android.interceptors;

import static java.lang.invoke.MethodType.methodType;

import com.google.auto.service.AutoService;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

@AutoService(Interceptor.class)
@SuppressWarnings("NewApi")
public class SystemTimeInterceptor extends Interceptor {

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public SystemTimeInterceptor() {
    super(new MethodRef(System.class, "nanoTime"),
        new MethodRef(System.class, "currentTimeMillis"));
  }

  @Override
  public Function<Object, Object> handle(final MethodSignature methodSignature) {
    return (theClass, value, params) -> {
      ClassLoader cl = theClass.getClassLoader();
      try {
        Class<?> shadowSystemClockClass = cl.loadClass("org.robolectric.shadows.ShadowSystemClock");
        return ReflectionHelpers
            .callStaticMethod(shadowSystemClockClass, methodSignature.methodName);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Override
  public MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    switch (methodName) {
      case "nanoTime":
        return lookup.findStatic(ShadowSystemClock.class,
            "nanoTime", methodType(long.class));
      case "currentTimeMillis":
        return lookup.findStatic(ShadowSystemClock.class,
            "currentTimeMillis", methodType(long.class));
    }
    throw new UnsupportedOperationException();
  }
}
