package org.robolectric.android;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.asList;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.ReflectionHelpers.callStaticMethod;

import android.content.Context;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import org.robolectric.android.fakes.CleanerCompat;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.shadows.ShadowSystem;
import org.robolectric.shadows.ShadowWindow;
import org.robolectric.util.Function;
import org.robolectric.util.Util;

public class AndroidInterceptors {
  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public static Collection<Interceptor> all() {
    List<Interceptor> interceptors =
        new ArrayList<>(
            asList(
                new LinkedHashMapEldestInterceptor(),
                new PolicyManagerMakeNewWindowInterceptor(),
                new SystemTimeInterceptor(),
                new SystemArrayCopyInterceptor(),
                new LocaleAdjustLanguageCodeInterceptor(),
                new SystemLogInterceptor(),
                new NoOpInterceptor()));

    if (Util.getJavaVersion() >= 9) {
      interceptors.add(new CleanerInterceptor());
    }

    return interceptors;
  }

//  @Intercept(value = LinkedHashMap.class, method = "eldest")
  public static class LinkedHashMapEldestInterceptor extends Interceptor {
    public LinkedHashMapEldestInterceptor() {
      super(new MethodRef(LinkedHashMap.class, "eldest"));
    }

    @Nullable
    static Object eldest(LinkedHashMap map) {
      return map.isEmpty() ? null : map.entrySet().iterator().next();
    }

    @Override
    public Function<Object, Object> handle(MethodSignature methodSignature) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          return eldest((LinkedHashMap) value);
        }
      };
    }

    @Override
    public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException {
      return lookup.findStatic(getClass(), "eldest",
          methodType(Object.class, LinkedHashMap.class));
    }
  }

  public static class PolicyManagerMakeNewWindowInterceptor extends Interceptor {
    public PolicyManagerMakeNewWindowInterceptor() {
      super(new MethodRef("com.android.internal.policy.PolicyManager", "makeNewWindow"));
    }

    @Override
    public Function<Object, Object> handle(MethodSignature methodSignature) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          ClassLoader cl = theClass.getClassLoader();

          try {
            Class<?> shadowWindowClass = cl.loadClass("org.robolectric.shadows.ShadowWindow");
            Class<?> activityClass = cl.loadClass(Context.class.getName());
            Object context = params[0];
            return callStaticMethod(shadowWindowClass, "create", from(activityClass, context));
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        }
      };
    }

    @Override
    public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException {
      Class<?> shadowWindowClass;
      try {
        shadowWindowClass = type.returnType().getClassLoader().loadClass(ShadowWindow.class.getName());
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      return lookup.in(type.returnType()).findStatic(shadowWindowClass, "create", type);
    }
  }

  public static class SystemTimeInterceptor extends Interceptor {
    public SystemTimeInterceptor() {
      super(
          new MethodRef(System.class, "nanoTime"),
          new MethodRef(System.class, "currentTimeMillis"));
    }

    @Override
    public Function<Object, Object> handle(final MethodSignature methodSignature) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          ClassLoader cl = theClass.getClassLoader();
          try {
            Class<?> shadowSystemClockClass = cl.loadClass("org.robolectric.shadows.ShadowSystem");
            return callStaticMethod(shadowSystemClockClass, methodSignature.methodName);
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        }
      };
    }

    @Override
    public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException {
      switch (methodName) {
        case "nanoTime":
          return lookup.findStatic(ShadowSystem.class,
              "nanoTime", methodType(long.class));
        case "currentTimeMillis":
          return lookup.findStatic(ShadowSystem.class,
              "currentTimeMillis", methodType(long.class));
      }
      throw new UnsupportedOperationException();
    }
  }

  public static class SystemArrayCopyInterceptor extends Interceptor {
    public SystemArrayCopyInterceptor() {
      super(new MethodRef(System.class, "arraycopy"));
    }

    @Override
    public Function<Object, Object> handle(MethodSignature methodSignature) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          //noinspection SuspiciousSystemArraycopy
          System.arraycopy(params[0], (Integer) params[1], params[2], (Integer) params[3], (Integer) params[4]);
          return null;
        }
      };
    }

    @Override
    public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException {
      return lookup.findStatic(System.class, "arraycopy",
          methodType(void.class, Object.class, int.class, Object.class, int.class, int.class));
    }
  }

  public static class LocaleAdjustLanguageCodeInterceptor extends Interceptor {
    public LocaleAdjustLanguageCodeInterceptor() {
      super(new MethodRef(Locale.class, "adjustLanguageCode"));
    }

    static String adjustLanguageCode(String languageCode) {
      String adjusted = languageCode.toLowerCase(Locale.US);
      // Map new language codes to the obsolete language
      // codes so the correct resource bundles will be used.
      if (languageCode.equals("he")) {
        adjusted = "iw";
      } else if (languageCode.equals("id")) {
        adjusted = "in";
      } else if (languageCode.equals("yi")) {
        adjusted = "ji";
      }

      return adjusted;
    }

    @Override
    public Function<Object, Object> handle(MethodSignature methodSignature) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          return adjustLanguageCode((String) params[0]);
        }
      };
    }

    @Override
    public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException {
      return lookup.findStatic(getClass(), "adjustLanguageCode",
          methodType(String.class, String.class));
    }
  }

  /** AndroidInterceptor for System.logE and System.logW. */
  public static class SystemLogInterceptor extends Interceptor {
    public SystemLogInterceptor() {
      super(
          new MethodRef(System.class.getName(), "logE"),
          new MethodRef(System.class.getName(), "logW"));
    }

    static void logE(Object... params) {
      log("System.logE: ", params);
    }

    static void logW(Object... params) {
      log("System.logW: ", params);
    }

    static void log(String prefix, Object... params) {
      StringBuilder message = new StringBuilder(prefix);
      for (Object param : params) {
        message.append(param.toString());
      }
      System.err.println(message);
    }

    @Override
    public Function<Object, Object> handle(MethodSignature methodSignature) {
      return (theClass, value, params) -> {
        if ("logE".equals(methodSignature.methodName)) {
          logE(params);
        } else if ("logW".equals(methodSignature.methodName)) {
          logW(params);
        }
        return null;
      };
    }

    @Override
    public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException {
      return lookup.findStatic(getClass(), methodName, methodType(void.class, Object[].class));
    }
  }

  /**
   * Maps calls to Cleaner, which moved between Java 8 and 9:
   *
   * <ul>
   *   <li>{@code sun.misc.Cleaner.create()} -> {@code new java.lang.ref.Cleaner().register()}
   *   <li>{@code sun.misc.Cleaner.clean()} -> {@code java.lang.ref.Cleaner.Cleanable().clean()}
   * </ul>
   */
  public static class CleanerInterceptor extends Interceptor {

    public CleanerInterceptor() {
      super(
          new MethodRef("sun.misc.Cleaner", "create"),
          new MethodRef("sun.misc.Cleaner", "clean")
      );
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

  public static class NoOpInterceptor extends Interceptor {
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

    @Override public Function<Object, Object> handle(MethodSignature methodSignature) {
      return returnDefaultValue(methodSignature);
    }

    @Override public MethodHandle getMethodHandle(String methodName, MethodType type) throws NoSuchMethodException, IllegalAccessException {
      MethodHandle nothing = constant(Void.class, null).asType(methodType(void.class));

      if (type.parameterCount() != 0) {
        return dropArguments(nothing, 0, type.parameterArray());
      } else {
        return nothing;
      }
    }
  }
}
