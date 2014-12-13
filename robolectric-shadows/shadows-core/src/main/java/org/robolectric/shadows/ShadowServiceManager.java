package org.robolectric.shadows;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import org.robolectric.annotation.Implements;

import java.util.Map;

@Implements(value = ServiceManager.class, isInAndroidSdk = false)
public class ShadowServiceManager {
  public static IBinder getService(String name) {
    return null;
  }

  public static void addService(String name, IBinder service) {
  }

  public static IBinder checkService(String name) {
    return null;
  }

  public static String[] listServices() throws RemoteException {
    return null;
  }

  public static void initServiceCache(Map<String, IBinder> cache) {
  }
}
