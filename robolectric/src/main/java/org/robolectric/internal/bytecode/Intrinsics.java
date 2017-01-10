package org.robolectric.internal.bytecode;

import android.content.Context;
import android.view.Window;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.shadows.ShadowWindow;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodType.methodType;

public enum Intrinsics {
  ELDEST(LinkedHashMap.class, "eldest"),
  LOAD_LIBRARY(System.class, "loadLibrary"),
  TRACK_ACTIVITY("android.os.StrictMode", "trackActivity"),
  INCREMENT_EXPECTED_ACTIVITY_COUNT("android.os.StrictMode", "incrementExpectedActivityCount"),
  AUTO_CLOSEABLE("java.lang.AutoCloseable", "*"),
  GET_LAYOUT_DIRECTION_FROM_LOCALE("android.util.LocaleUtil", "getLayoutDirectionFromLocale"),
  MAKE_NEW_WINDOW("com.android.internal.policy.PolicyManager", "makeNewWindow"),
  POLICY_MANAGER("com.android.internal.policy.PolicyManager", "*"),
  FALLBACK_EVENT_HANDLER("android.view.FallbackEventHandler", "*"),
  I_WINDOW_SESSION("android.view.IWindowSession", "*"),
  NANO_TIME("java.lang.System", "nanoTime"),
  CURRENT_TIME_MILLIS("java.lang.System", "currentTimeMillis"),
  ARRAYCOPY("java.lang.System", "arraycopy"),
  LOG_E("java.lang.System", "logE"),
  ADJUST_LANGUAGE_CODE("java.util.Locale", "adjustLanguageCode");

  private final MethodRef ref;

  private Intrinsics(Class<?> type, String methodName) {
    ref = new MethodRef(type, methodName);
  }

  private Intrinsics(String className, String methodName) {
    ref = new MethodRef(className, methodName);
  }

  public MethodRef getRef() {
    return ref;
  }

  public static List<MethodRef> allRefs() {
    List<MethodRef> refs = new ArrayList<>();
    for (Intrinsics i : values()) {
      refs.add(i.getRef());
    }
    return refs;
  }

  public static class Impl implements InvokeDynamicSupport.IIntrinsics {
    private static final MethodHandle ELDEST;
    private static final MethodHandle NANO_TIME;
    private static final MethodHandle MILLIS_TIME;
    private static final MethodHandle ARRAY_COPY;
    private static final MethodHandle ADJUST_LANGUAGE_CODE;
    private static final MethodHandle LOG_E;
    private static final MethodHandle MAKE_NEW_WINDOW;
    private static final MethodHandle NOTHING = constant(Void.class, null).asType(methodType(void.class));

    private static final Map<MethodRef, MethodHandle> intrinsics;

    static {
      try {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        ELDEST = lookup.findStatic(Impl.class, "eldest", methodType(Object.class, LinkedHashMap.class));
        NANO_TIME = lookup.findStatic(ShadowSystemClock.class, "nanoTime", methodType(long.class));
        MILLIS_TIME = lookup.findStatic(ShadowSystemClock.class, "currentTimeMillis", methodType(long.class));
        ARRAY_COPY = lookup.findStatic(System.class, "arraycopy", methodType(void.class, Object.class, int.class, Object.class, int.class, int.class));
        ADJUST_LANGUAGE_CODE = identity(String.class);
        LOG_E = lookup.findStatic(Impl.class, "logE", methodType(void.class, Object[].class));
        MAKE_NEW_WINDOW = lookup.findStatic(ShadowWindow.class, "create", methodType(Window.class, Context.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }

      intrinsics = new Builder().add(Intrinsics.ELDEST, ELDEST)
          .add(Intrinsics.MAKE_NEW_WINDOW, MAKE_NEW_WINDOW)
          .add(Intrinsics.NANO_TIME, NANO_TIME)
          .add(Intrinsics.CURRENT_TIME_MILLIS, MILLIS_TIME)
          .add(Intrinsics.ARRAYCOPY, ARRAY_COPY)
          .add(Intrinsics.LOG_E, LOG_E)
          .add(Intrinsics.ADJUST_LANGUAGE_CODE, ADJUST_LANGUAGE_CODE)
          .build();
    }

    public MethodHandle getIntrinsic(String className, String methodName, MethodType type) {
      MethodHandle mh = intrinsics.get(new MethodRef(className, methodName));
      if (mh == null) mh = intrinsics.get(new MethodRef(className, "*"));

      if (mh == null && type.parameterCount() != 0) {
        mh = dropArguments(NOTHING, 0, type.parameterArray());
      } else if (mh == null) {
        mh = NOTHING;
      }
      return mh;
    }

    private static Object eldest(LinkedHashMap<?, ?> map) {
      return map.isEmpty() ? null : map.entrySet().iterator().next();
    }

    private static void logE(Object... params) {
      String message = "System.logE: ";
      for (Object param : params) {
        message += param.toString();
      }
      System.err.println(message);
    }

    private static class Builder {
      private final Map<MethodRef, MethodHandle> map;

      private Builder() {
        map = new HashMap<>();
      }

      public Builder add(Intrinsics intrinsics, MethodHandle mh) {
        map.put(intrinsics.getRef(), mh);
        return this;
      }

      public Builder add(String className, String method, MethodHandle mh) {
        map.put(new MethodRef(className, method), mh);
        return this;
      }

      public Map<MethodRef, MethodHandle> build() {
        return map;
      }
    }
  }
}
