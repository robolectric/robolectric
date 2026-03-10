package org.robolectric.shadows;

import com.google.auto.service.AutoService;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import org.robolectric.pluginapi.MethodHandleDecorator;
import org.robolectric.shadow.api.Shadow;

/**
 * A {@link MethodHandleDecorator} that enforces that all {@link android.view.View} methods are
 * called on the main thread.
 *
 * <p>This decorator is enabled if the system property {@code
 * robolectric.enforceViewMethodsCalledOnMainThread} is set to {@code true}, or if {@link
 * ShadowView#enableThreadCheck} is {@code true}.
 */
@SuppressWarnings("NewApi")
@AutoService(MethodHandleDecorator.class)
public class ViewThreadChecker implements MethodHandleDecorator {

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private MethodHandle threadCheckHandle;

  @Override
  public MethodHandle decorate(
      Class<?> definingClass,
      String methodName,
      MethodType methodType,
      boolean isStatic,
      MethodHandle original) {
    boolean prop = Boolean.getBoolean("robolectric.enforceViewMethodsCalledOnMainThread");
    if (!prop) {
      return null;
    }

    if (isStatic) {
      return null;
    }

    if (!isViewClass(definingClass)) {
      return null;
    }

    if (methodName.equals("__constructor__")
        || methodName.equals("getHandler")
        || methodName.equals("post")
        || methodName.equals("postDelayed")
        || methodName.equals("removeCallbacks")
        || methodName.equals("postOnAnimation")
        || methodName.equals("postOnAnimationDelayed")) {
      return null;
    }

    MethodHandle mh = original;
    if (mh == null) {
      // It's CALL_REAL_CODE. We need to look up the original method.
      try {
        Class<?>[] paramTypes = methodType.dropParameterTypes(0, 1).parameterArray();
        Method method =
            definingClass.getDeclaredMethod(
                Shadow.directMethodName(definingClass.getName(), methodName), paramTypes);
        method.setAccessible(true);
        mh = LOOKUP.unreflect(method);
      } catch (NoSuchMethodException | IllegalAccessException e) {
        return null;
      }
    }

    if (mh.type().parameterCount() == 0) {
      return null;
    }

    return addThreadCheck(mh, definingClass);
  }

  private boolean isViewClass(Class<?> definingClass) {
    if (definingClass.getName().equals("android.view.View")) {
      return true;
    }
    Class<?> superclass = definingClass.getSuperclass();
    if (superclass != null) {
      return isViewClass(superclass);
    }
    return false;
  }

  private MethodHandle addThreadCheck(MethodHandle mh, Class<?> definingClass) {
    try {
      ClassLoader cl = definingClass.getClassLoader();
      Class<?> shadowViewClass = cl.loadClass("org.robolectric.shadows.ShadowView");
      Method checkThreadMethod = shadowViewClass.getMethod("checkThread", Object.class);
      threadCheckHandle = LOOKUP.unreflect(checkThreadMethod);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    MethodType type = mh.type();
    MethodHandle checkAdapter =
        threadCheckHandle.asType(MethodType.methodType(void.class, type.parameterType(0)));
    return MethodHandles.foldArguments(mh, checkAdapter);
  }
}
