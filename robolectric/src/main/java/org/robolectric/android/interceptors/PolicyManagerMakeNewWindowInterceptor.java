package org.robolectric.android.interceptors;

import android.content.Context;
import com.google.auto.service.AutoService;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.shadows.ShadowWindow;
import org.robolectric.util.Function;
import org.robolectric.util.ReflectionHelpers;

@AutoService(Interceptor.class)
@SuppressWarnings("NewApi")
public class PolicyManagerMakeNewWindowInterceptor extends Interceptor {

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public PolicyManagerMakeNewWindowInterceptor() {
    super(new MethodRef("com.android.internal.policy.PolicyManager", "makeNewWindow"));
  }

  @Override
  public Function<Object, Object> handle(MethodSignature methodSignature) {
    return (theClass, value, params) -> {
      ClassLoader cl = theClass.getClassLoader();

      try {
        Class<?> shadowWindowClass = cl.loadClass("org.robolectric.shadows.ShadowWindow");
        Class<?> activityClass = cl.loadClass(Context.class.getName());
        Object context = params[0];
        return ReflectionHelpers.callStaticMethod(shadowWindowClass, "create",
            ReflectionHelpers.ClassParameter.from(activityClass, context));
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Override
  public MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    Class<?> shadowWindowClass;
    try {
      shadowWindowClass = type.returnType().getClassLoader()
          .loadClass(ShadowWindow.class.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return lookup.in(type.returnType())
        .findStatic(shadowWindowClass, "create", type);
  }
}
