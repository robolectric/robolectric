package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.os.Build;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(
  className = "android.app.SystemServiceRegistry",
  isInAndroidSdk = false,
  looseSignatures = true,
  minSdk = Build.VERSION_CODES.M
)
public class ShadowSystemServiceRegistry {

  @Resetter
  public static void reset() {
    Map<String, Object> fetchers =
        ReflectionHelpers.getStaticField(
            classForName("android.app.SystemServiceRegistry"), "SYSTEM_SERVICE_FETCHERS");

    Class staticApplicationServiceFetcherClass = null;
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      staticApplicationServiceFetcherClass =
          classForName("android.app.SystemServiceRegistry$StaticApplicationContextServiceFetcher");
    } else if (RuntimeEnvironment.getApiLevel() == Build.VERSION_CODES.M) {
      staticApplicationServiceFetcherClass =
          classForName("android.app.SystemServiceRegistry$StaticOuterContextServiceFetcher");
    }

    Class staticServiceFetcherClass =
        classForName("android.app.SystemServiceRegistry$StaticServiceFetcher");

    for (Object o : fetchers.values()) {
      if (staticApplicationServiceFetcherClass.isInstance(o)) {
        ReflectionHelpers.setField(
            staticApplicationServiceFetcherClass, o, "mCachedInstance", null);
      } else if (staticServiceFetcherClass.isInstance(o)) {
        ReflectionHelpers.setField(staticServiceFetcherClass, o, "mCachedInstance", null);
      }
    }
  }

  @Implementation(minSdk = O)
  protected static void onServiceNotFound(/* ServiceNotFoundException */ Object e0) {
    // otherwise the full stacktrace might be swallowed...
    Exception e = (Exception) e0;
    e.printStackTrace();
  }

  private static Class classForName(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
