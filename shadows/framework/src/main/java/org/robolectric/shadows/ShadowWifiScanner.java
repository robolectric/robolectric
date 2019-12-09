package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.net.wifi.WifiScanner;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow for {@link android.net.wifi.WifiScanner}. */
@Config(minSdk = M)
@Implements(value = WifiScanner.class, looseSignatures = true, isInAndroidSdk = false)
public class ShadowWifiScanner {
  @RealObject protected WifiScanner realWifiScanner;

  @Implementation(minSdk = 23)
  public List<Integer> getAvailableChannels(int band) {
    return new ArrayList<>();
  }
}
