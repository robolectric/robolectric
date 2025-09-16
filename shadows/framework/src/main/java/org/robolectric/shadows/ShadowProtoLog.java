package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for ProtoLog. */
@Implements(
    className = "com.android.internal.protolog.ProtoLog",
    isInAndroidSdk = false,
    minSdk = BAKLAVA)
public class ShadowProtoLog {

  @Implementation
  protected static boolean logOnlyToLogcat() {
    // We don't want to initialize Perfetto data sources and have to deal with Perfetto
    // when running tests on the host side, instead just log everything to logcat
    return true;
  }
}
