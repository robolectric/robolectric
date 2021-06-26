package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = WindowManagerGlobal.class, isInAndroidSdk = false,
    minSdk = JELLY_BEAN_MR1, looseSignatures = true)
public class ShadowWindowManagerGlobal {

  @Resetter
  public static void reset() {
    reflector(WindowManagerGlobalReflector.class).setDefaultWindowManager(null);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public static Object getWindowSession() {
    return null;
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  public static Object getWindowSession(Looper looper) {
    return null;
  }

  @Implementation
  public static Object getWindowManagerService() throws RemoteException {
    IWindowManager service =
        reflector(WindowManagerGlobalReflector.class).getWindowManagerService();
    if (service == null) {
      service = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
      reflector(WindowManagerGlobalReflector.class).setWindowManagerService(service);
      if (VERSION.SDK_INT >= 30) {
        reflector(WindowManagerGlobalReflector.class).setUseBlastAdapter(service.useBLAST());
      }
    }
    return service;
  }

  @ForType(WindowManagerGlobal.class)
  interface WindowManagerGlobalReflector {
    @Accessor("sDefaultWindowManager")
    @Static
    void setDefaultWindowManager(WindowManagerGlobal global);

    @Static
    @Accessor("sWindowManagerService")
    IWindowManager getWindowManagerService();

    @Static
    @Accessor("sWindowManagerService")
    void setWindowManagerService(IWindowManager service);

    @Static
    @Accessor("sUseBLASTAdapter")
    void setUseBlastAdapter(boolean useBlastAdapter);
  }
}
