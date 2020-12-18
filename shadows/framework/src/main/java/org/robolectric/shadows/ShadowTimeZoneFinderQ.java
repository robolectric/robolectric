package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.shadows.ShadowTimeZoneFinder.readTzlookup;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow for TimeZoneFinder on Q after Developer Preview 1. */
@Implements(
    className = "libcore.timezone.TimeZoneFinder",
    minSdk = Q,
    maxSdk = R,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowTimeZoneFinderQ {

  @Implementation
  protected static Object getInstance() {
    try {
      return ReflectionHelpers.callStaticMethod(
          Class.forName("libcore.timezone.TimeZoneFinder"),
          "createInstanceForTests",
          ClassParameter.from(String.class, readTzlookup()));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
