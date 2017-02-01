package org.robolectric.shadows;

import android.os.Binder;
import android.os.IInterface;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.WindowId;
import android.view.WindowManagerGlobal;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static org.robolectric.util.ReflectionHelpers.PRIMITIVE_RETURN_VALUES;

/**
 * Shadow for {@link android.view.WindowManagerGlobal}.
 */
@Implements(value = WindowManagerGlobal.class, isInAndroidSdk = false, minSdk = JELLY_BEAN_MR1)
public class ShadowWindowManagerGlobal {

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(WindowManagerGlobal.class, "sDefaultWindowManager", null);
  }

  @Implementation
  public static Object getWindowSession() {
    return (IWindowSession) Proxy.newProxyInstance(IWindowSession.class.getClassLoader(),
        new Class[]{IWindowSession.class}, new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("proxy = " + IWindowSession.class + " " + method.getName());
            switch (method.getName()) {
              case "getWindowId":
                return createNullServiceProxy(IWindowId.class);
            }

            return PRIMITIVE_RETURN_VALUES.get(method.getReturnType().getName());
          }
        });
  }

  private static <T extends IInterface> T createNullServiceProxy(final Class<T> clazz) {
    return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
        new Class[]{clazz}, new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("proxy = " + clazz + " " + method.getName());

            switch (method.getName()) {
              case "asBinder":
                return new Binder();
            }

            return PRIMITIVE_RETURN_VALUES.get(method.getReturnType().getName());
          }
        });
  }
//
//  @Implementation
//  public static Object getWindowSession(Looper looper) {
//    return null;
//  }

  public static <T> T createNullProxy(final Class<T> clazz) {
    return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
        new Class[]{clazz}, new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("proxy = " + clazz + " " + method.getName());
            return PRIMITIVE_RETURN_VALUES.get(method.getReturnType().getName());
          }
        });
  }


//  @Implementation
//  public static Object getWindowManagerService() {
//    return Proxy.newProxyInstance(IWindowManager.class.getClassLoader(),
//        new Class[]{IWindowManager.class}, new InvocationHandler() {
//          @Override
//          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            System.out.println("proxy = " + IWindowManager.class + " " + method.getName());
//            switch (method.getName()) {
//              case "openSession":
//                return createNullProxy(IWindowSession.class);
//            }
//            return PRIMITIVE_RETURN_VALUES.get(method.getReturnType().getName());
//          }
//        });
//  }

}