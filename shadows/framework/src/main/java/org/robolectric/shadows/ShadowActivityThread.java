package org.robolectric.shadows;

import android.app.ActivityThread;
import android.content.pm.PackageManager;

import javax.annotation.Nonnull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Implements(value = ActivityThread.class, isInAndroidSdk = false)
public class ShadowActivityThread {
  public static final String CLASS_NAME = "android.app.ActivityThread";

  @Implementation
  public static Object getPackageManager() {
    ClassLoader classLoader = ShadowActivityThread.class.getClassLoader();
    Class<?> iPackageManagerClass;
    try {
      iPackageManagerClass = classLoader.loadClass("android.content.pm.IPackageManager");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return Proxy.newProxyInstance(classLoader, new Class[]{iPackageManagerClass}, new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, @Nonnull Method method, Object[] args) throws Exception {
        if (method.getName().equals("getApplicationInfo")) {
          String packageName = (String) args[0];
          int flags = (Integer) args[1];
          try {
            return RuntimeEnvironment.application.getPackageManager().getApplicationInfo(packageName, flags);
          } catch (PackageManager.NameNotFoundException e) {
            return null;
          }
        } else if (method.getName().equals("notifyPackageUse")) {
          return null;
        } else if (method.getName().equals("getPackageInstaller")) {
          return null;
        }
        throw new UnsupportedOperationException("sorry, not supporting " + method + " yet!");
      }
    });
  }

  @Implementation
  public static Object currentActivityThread() {
    return RuntimeEnvironment.getActivityThread();
  }
}
