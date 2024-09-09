package org.robolectric.shadows;

import android.net.MacAddress;
import android.net.wifi.rtt.RangingResult;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.net.wifi.rtt.RangingResult}.
 *
 * @deprecated Use {link RangingResultBuilder} instead.
 */
@Deprecated
@Implements(value = RangingResult.class, minSdk = VERSION_CODES.P)
public class ShadowRangingResult {

  /**
   * A builder for creating ShadowRangingResults. Status, mac address, distance [mm] and timestamp
   * are all mandatory fields. Additional fields can be specified by setters. Use build() to return
   * the ShadowRangingResult object.
   *
   * @deprecated Use {link RangingResultBuilder} instead.
   */
  @Deprecated
  public static class Builder extends RangingResultBuilder {
    public Builder(int status, MacAddress mac, long timestampMillis, int distanceMm) {
      super(status, mac, timestampMillis, distanceMm);
    }
  }
}
