package org.robolectric.android.interceptors;

import static java.lang.invoke.MethodType.methodType;

import com.google.auto.service.AutoService;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.robolectric.android.fakes.CleanerCompat;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.util.Function;
import org.robolectric.util.Util;

/**
 * Maps calls to Cleaner, which moved between Java 8 and 9:
 *
 * * `sun.misc.Cleaner.create()` -> `new java.lang.ref.Cleaner().register()`
 * * `sun.misc.Cleaner.clean()` -> `java.lang.ref.Cleaner.Cleanable().clean()`
 */
@AutoService(Interceptor.class)
@SuppressWarnings("NewApi")
public class CleanerInterceptor extends Interceptor {

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public CleanerInterceptor() {
    super(determineMethodRefs());
  }

  private static MethodRef[] determineMethodRefs() {
    if (Util.getJavaVersion() >= 9) {
      return new MethodRef[]{new MethodRef("sun.misc.Cleaner", "create"),
          new MethodRef("sun.misc.Cleaner", "clean")};
    } else {
      return new MethodRef[0];
    }
  }

  static Object create(Object obj, Runnable action) {
    System.out.printf("Cleaner.create(%s,%s)%n", obj, action);
    return CleanerCompat.register(obj, action);
  }

  static void clean(Object cleanable) {
    System.out.printf("Cleaner.clean(%s)%n", cleanable);
    CleanerCompat.clean(cleanable);
  }

  @Override
  public Function<Object, Object> handle(MethodSignature methodSignature) {
    switch (methodSignature.methodName) {
      case "create":
        return (theClass, value, params) -> create(params[0], (Runnable) params[1]);
      case "clean":
        return (theClass, value, params) -> {
          clean(value);
          return null;
        };
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    switch (methodName) {
      case "create":
        return lookup.findStatic(getClass(), "create",
            methodType(Object.class, Object.class, Runnable.class));
      case "clean":
        return lookup.findStatic(getClass(), "clean",
            methodType(void.class, Object.class));
      default:
        throw new IllegalStateException();
    }
  }
}
