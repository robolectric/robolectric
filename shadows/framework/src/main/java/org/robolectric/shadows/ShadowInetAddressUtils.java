package org.robolectric.shadows;

import android.os.Build;
import java.net.InetAddress;
import java.net.UnknownHostException;
import libcore.net.InetAddressUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow InetAddressUtils class that doesn't use native Libcore function. */
@Implements(value = InetAddressUtils.class, minSdk = Build.VERSION_CODES.Q, isInAndroidSdk = false)
public class ShadowInetAddressUtils {
  @Implementation
  protected static InetAddress parseNumericAddressNoThrow(String address) {
    try {
      return InetAddress.getByName(address);
    } catch (UnknownHostException e) {
      return null;
    }
  }
}
