package org.robolectric.shadows;

import android.net.MacAddress;
import android.net.wifi.rtt.RangingResult;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implements;

/** Shadow for {@link android.net.wifi.rtt.RangingResult}. */
@Implements(value = RangingResult.class, minSdk = VERSION_CODES.P)
@Deprecated
public class ShadowRangingResult {

  /**
   * A builder for creating ShadowRangingResults. Status, macaddress, distance [mm] and timestamp
   * are all mandatory fields. Additional fields can be specified by setters. Use build() to return
   * the ShadowRangingResult object.
   */
  @Deprecated
  public static class Builder extends RangingResultBuilder {
    public Builder(int status, MacAddress mac, long timestampMillis, int distanceMm) {
      super(status, mac, timestampMillis, distanceMm);
    }
  }
}
