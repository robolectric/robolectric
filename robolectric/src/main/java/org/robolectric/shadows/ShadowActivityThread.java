package org.robolectric.shadows;

import org.jetbrains.annotations.NotNull;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.fest.reflect.core.Reflection.type;

@Implements(value = Robolectric.Anything.class, className = ShadowActivityThread.CLASS_NAME)
public class ShadowActivityThread {
  public static final String CLASS_NAME = "android.app.ActivityThread";

  @Implementation
  public static Object getPackageManager() {
    ClassLoader classLoader = ShadowActivityThread.class.getClassLoader();
    Class<?> iPackageManagerClass = type("android.content.pm.IPackageManager")
        .withClassLoader(classLoader)
        .load();
    return Proxy.newProxyInstance(classLoader, new Class[] {iPackageManagerClass}, new InvocationHandler() {
      @Override public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getApplicationInfo")) {
          String packageName = (String) args[0];
          int flags = (Integer) args[1];
          return Robolectric.packageManager.getApplicationInfo(packageName, flags);
        }
        throw new UnsupportedOperationException("sorry, not supporting " + method + " yet!");
      }
    });
  }

  @Implementation
  public static Object currentActivityThread() {
    return Robolectric.activityThread;
  }
}
