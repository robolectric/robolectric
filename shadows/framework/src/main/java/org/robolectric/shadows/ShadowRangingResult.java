package org.robolectric.shadows;

import android.net.MacAddress;
import android.net.wifi.rtt.RangingResult;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.net.wifi.rtt.RangingResult}.
 *
 * @deprecated Use {@link RangingResultBuilder} instead.
 */
@Implements(
    value = RangingResult.class,
    minSdk = VERSION_CODES.P,
    maxSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
@Deprecated
public class ShadowRangingResult {

  /**
   * @deprecated Use {@link RangingResultBuilder} instead.
   */
  @Deprecated
  public static class Builder extends RangingResultBuilder {
    public Builder(int status, MacAddress mac, long timestampMillis, int distanceMm) {
      super(status, mac, timestampMillis, distanceMm);
    }
  }
}
