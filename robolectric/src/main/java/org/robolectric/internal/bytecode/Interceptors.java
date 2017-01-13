package org.robolectric.internal.bytecode;

import android.content.Context;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class Interceptors {
  private final List<Interceptor> interceptors = new ArrayList<>();

  {
    interceptors.add(new Interceptor() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches(LinkedHashMap.class.getName(), "eldest");
      }

      @Override
      public Function<Object, Object> handle(MethodSignature methodSignature) {
        return new Function<Object, Object>() {
          @Override
          public Object call(Class<?> theClass, Object value, Object[] params) {
            LinkedHashMap map = (LinkedHashMap) value;
            return map.isEmpty() ? null : map.entrySet().iterator().next();
          }
        };
      }
    });

    interceptors.add(new Interceptor() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches("com.android.internal.policy.PolicyManager", "makeNewWindow");
      }

      @Override
      public Function<Object, Object> handle(MethodSignature methodSignature) {
        return new Function<Object, Object>() {
          @Override
          public Object call(Class<?> theClass, Object value, Object[] params) {
            ClassLoader cl = theClass.getClassLoader();
            Class<?> shadowWindowClass;

            try {
              shadowWindowClass = cl.loadClass("org.robolectric.shadows.ShadowWindow");
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }

            Class<?> activityClass;

            try {
              activityClass = cl.loadClass(Context.class.getName());
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }

            Object context = params[0];
            return ReflectionHelpers.callStaticMethod(shadowWindowClass, "create", ReflectionHelpers.ClassParameter.from(activityClass, context));
          }
        };
      }
    });

    interceptors.add(new Interceptor() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches(System.class.getName(), "nanoTime") || methodSignature.matches("java.lang.System", "currentTimeMillis");
      }

      @Override
      public Function<Object, Object> handle(final MethodSignature methodSignature) {
        return new Function<Object, Object>() {
          @Override
          public Object call(Class<?> theClass, Object value, Object[] params) {
            ClassLoader cl = theClass.getClassLoader();
            Class<?> shadowSystemClockClass;
            try {
              shadowSystemClockClass = cl.loadClass("org.robolectric.shadows.ShadowSystemClock");
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }

            return ReflectionHelpers.callStaticMethod(shadowSystemClockClass, methodSignature.methodName);
          }
        };
      }
    });

    interceptors.add(new Interceptor() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches(System.class.getName(), "arraycopy");
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
    });

    interceptors.add(new Interceptor() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches(Locale.class.getName(), "adjustLanguageCode");
      }

      @Override
      public Function<Object, Object> handle(MethodSignature methodSignature) {
        return new Function<Object, Object>() {
          @Override
          public Object call(Class<?> theClass, Object value, Object[] params) {
            return params[0];
          }
        };
      }
    });

    interceptors.add(new Interceptor() {
      @Override
      public boolean matches(MethodSignature methodSignature) {
        return methodSignature.matches(System.class.getName(), "logE");
      }

      @Override
      public Function<Object, Object> handle(MethodSignature methodSignature) {
        return new Function<Object, Object>() {
          @Override
          public Object call(Class<?> theClass, Object value, Object[] params) {
            String message = "System.logE: ";
            for (Object param : params) {
              message += param.toString();
            }
            System.err.println(message);
            return null;
          }
        };
      }
    });
  }

  @SuppressWarnings("UnnecessaryBoxing")

  public Function<Object, Object> getInterceptionHandler(final MethodSignature methodSignature) {
    for (Interceptor interceptor : interceptors) {
      if (interceptor.matches(methodSignature)) {
        return interceptor.handle(methodSignature);
      }
    }

    // nothing matched, return default
    return new Function<Object, Object>() {
      @Override
      public Object call(Class<?> theClass, Object value, Object[] params) {
        return ReflectionHelpers.PRIMITIVE_RETURN_VALUES.get(methodSignature.returnType);
      }
    };
  }

  public interface Interceptor {
    boolean matches(MethodSignature methodSignature);

    Function<Object, Object> handle(MethodSignature methodSignature);
  }
}
