package org.robolectric.internal.bytecode;

import android.content.Context;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

import java.util.LinkedHashMap;

public class Interceptors {
  @SuppressWarnings("UnnecessaryBoxing")
  public Function<Object, Object> getInterceptionHandler(final MethodSignature methodSignature) {
    // TODO: move these somewhere else!
    if (methodSignature.matches(LinkedHashMap.class.getName(), "eldest")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          LinkedHashMap map = (LinkedHashMap) value;
          return map.isEmpty() ? null : map.entrySet().iterator().next();
        }
      };
    } else if (methodSignature.matches("com.android.internal.policy.PolicyManager", "makeNewWindow")) {
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
    } else if (methodSignature.matches("java.lang.System", "nanoTime") || methodSignature.matches("java.lang.System", "currentTimeMillis")) {
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
    } else if (methodSignature.matches("java.lang.System", "arraycopy")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          //noinspection SuspiciousSystemArraycopy
          System.arraycopy(params[0], (Integer) params[1], params[2], (Integer) params[3], (Integer) params[4]);
          return null;
        }
      };
    } else if (methodSignature.matches("java.util.Locale", "adjustLanguageCode")) {
      return new Function<Object, Object>() {
        @Override
        public Object call(Class<?> theClass, Object value, Object[] params) {
          return params[0];
        }
      };
    } else if (methodSignature.matches("java.lang.System", "logE")) {
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

    return new Function<Object, Object>() {
      @Override
      public Object call(Class<?> theClass, Object value, Object[] params) {
        return ReflectionHelpers.PRIMITIVE_RETURN_VALUES.get(methodSignature.returnType);
      }
    };
  }
}
