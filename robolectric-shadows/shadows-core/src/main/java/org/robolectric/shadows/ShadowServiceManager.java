package org.robolectric.shadows;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IPermissionController;
import android.os.IServiceManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceManagerNative;
import android.view.IWindowManager;
import android.view.accessibility.IAccessibilityManager;
import com.android.internal.view.IInputMethodManager;
import com.android.server.InputMethodManagerService;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.wm.WindowManagerService;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.robolectric.util.ReflectionHelpers.PRIMITIVE_RETURN_VALUES;

/**
 * Shadow for {@link android.os.ServiceManager}.
 */
@Implements(value = ServiceManager.class, isInAndroidSdk = false)
public class ShadowServiceManager {

  @Implementation
  public static IServiceManager getIServiceManager() {
    return new ServiceManagerNative() {
      private final Map<String, IBinder> services = new HashMap<>();

      @Override
      public IBinder getService(String name) throws RemoteException {
        return services.get(name);
      }

      @Override
      public IBinder checkService(String name) throws RemoteException {
        return null;
      }

      @Override
      public void addService(String name, IBinder service, boolean allowIsolated) throws RemoteException {
        System.out.println("name = " + name);
        services.put(name, service);
      }

      @Override
      public String[] listServices() throws RemoteException {
        return new String[0];
      }

      @Override
      public void setPermissionController(IPermissionController controller) throws RemoteException {

      }
    };
  }

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
//  public static IBinder getService(String name) {
//    Binder binder = null;
//    IInterface owner;
//    switch (name) {
//      case Context.WINDOW_SERVICE:
//        InputManagerService inputManagerService = new InputManagerService(RuntimeEnvironment.application);
//        return WindowManagerService.main(RuntimeEnvironment.application, inputManagerService, true, true, true);
////        binder = createNullProxy(IWindowManager.Stub.class);
////        owner = createNullProxy(IWindowManager.class);
////        binder.attachInterface(owner, WindowManagerService.class.getName());
////        break;
//      case Context.INPUT_METHOD_SERVICE:
//        binder = createNullProxy(IInputMethodManager.Stub.class);
//        owner = createNullProxy(IInputMethodManager.class);
//        binder.attachInterface(owner, InputMethodManagerService.class.getName());
//        break;
////      case Context.ACCESSIBILITY_SERVICE:
////        owner = createNullProxy(IAccessibilityManager.class);
////        binder.attachInterface(owner, AccessibilityManagerService.class.getName());
////        break;
//    }
//    return binder;
////    return null;
//  }

  @Implementation
  public static void addService(String name, IBinder service) {
  }

  @Implementation
  public static IBinder checkService(String name) {
    return null;
  }

  @Implementation
  public static String[] listServices() throws RemoteException {
    return null;
  }

  @Implementation
  public static void initServiceCache(Map<String, IBinder> cache) {
  }
}
