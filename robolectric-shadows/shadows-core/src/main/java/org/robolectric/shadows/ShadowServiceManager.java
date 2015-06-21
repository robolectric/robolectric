package org.robolectric.shadows;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Map;

/**
 * Shadow for {@link android.os.ServiceManager}.
 */
@Implements(value = ServiceManager.class, isInAndroidSdk = false)
public class ShadowServiceManager {

  @Implementation
  public static IBinder getService(String name) {
    return null;
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
