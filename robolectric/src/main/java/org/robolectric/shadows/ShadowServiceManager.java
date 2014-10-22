package org.robolectric.shadows;

import android.os.IBinder;
import android.os.RemoteException;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;

import java.util.Map;

@Implements(value = Robolectric.Anything.class, className = "android.os.ServiceManager")
public class ShadowServiceManager {
  public static IBinder getService(String name) {
    return null;
  }

  public static void addService(String name, IBinder service) {
    // pass
  }

  public static IBinder checkService(String name) {
    return null;
  }

  public static String[] listServices() throws RemoteException {
    // actual implementation returns null sometimes, so it's ok
    // to return null instead of an empty list.
    return null;
  }

  public static void initServiceCache(Map<String, IBinder> cache) {
    // pass
  }
}
