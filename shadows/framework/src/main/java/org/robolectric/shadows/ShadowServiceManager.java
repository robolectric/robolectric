package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.app.ISearchManager;
import android.app.trust.ITrustManager;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = ServiceManager.class, isInAndroidSdk = false)
public class ShadowServiceManager {

  private static Map<String, IBinder> SERVICES =
      new HashMap<String, IBinder>() {
        {
          put(
              Context.SEARCH_SERVICE,
              createBinder(ISearchManager.class, "android.app.ISearchManager"));
          put(
              Context.UI_MODE_SERVICE,
              createBinder(ISearchManager.class, "android.app.IUiModeManager"));
          if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
            put(
                Context.TRUST_SERVICE,
                createBinder(ITrustManager.class, "android.app.trust.ITrustManager"));
          }
        }
      };

  @Implementation
  public static IBinder getService(String name) {
    return SERVICES.get(name);
  }

  private static Binder createBinder(Class<? extends IInterface> clazz, String descriptor) {
    Binder binder = new Binder();
    binder.attachInterface(ReflectionHelpers.createNullProxy(clazz), descriptor);
    return binder;
  }

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
