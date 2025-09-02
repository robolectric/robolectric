package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.Baklava;

/** Shadow for ProtoLog. */
@Implements(
    className = "com.android.internal.protolog.ProtoLog",
    isInAndroidSdk = false,
    minSdk = Baklava.SDK_INT)
public class ShadowProtoLog {

  @Implementation
  protected static boolean logOnlyToLogcat() {
    // We don't want to initialize Perfetto data sources and have to deal with Perfetto
    // when running tests on the host side, instead just log everything to logcat
    return true;
  }
}
