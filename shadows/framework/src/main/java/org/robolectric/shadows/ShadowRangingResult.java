package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.net.MacAddress;
import android.net.wifi.rtt.RangingResult;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link android.net.wifi.rtt.RangingResult}. */
@Config(minSdk = P)
@Implements(RangingResult.class)
public class ShadowRangingResult {

  /**
   * A builder for creating ShadowRangingResults. Status, macaddress, distance [mm] and timestamp
   * are all mandatory fields. Additional fields can be specified by setters. Use build() to return
   * the ShadowRangingResult object.
   */
  public static final class Builder {
    // Required Values
    private final int status;
    private final MacAddress mac;
    private final int distanceMm;
    private final long timestamp;

    // Optional Values
    private int distanceStdDevMm = 0;
    private int rssi = 0;
    private int numAttemptedMeasurements = 0;
    private int numSuccessfulMeasurements = 0;
    private byte[] lci = new byte[0];
    private byte[] lcr = new byte[0];

    public Builder(int status, MacAddress mac, long timestamp, int distanceMm) {
      this.status = status;
      this.mac = mac;
      this.timestamp = timestamp;
      this.distanceMm = distanceMm;
    }

    public Builder setDistanceStandardDeviation(int stddev) {
      this.distanceStdDevMm = stddev;
      return this;
    }

    public Builder setRssi(int rssi) {
      this.rssi = rssi;
      return this;
    }

    public Builder setNumAttemptedMeasurements(int num) {
      this.numAttemptedMeasurements = num;
      return this;
    }

    public Builder setNumSuccessfulMeasurements(int num) {
      this.numSuccessfulMeasurements = num;
      return this;
    }

    public Builder setLci(byte[] lci) {
      this.lci = lci;
      return this;
    }

    public Builder setLcr(byte[] lcr) {
      this.lcr = lcr;
      return this;
    }

    public RangingResult build() {
      return asRangingResult(
          status,
          mac,
          distanceMm,
          distanceStdDevMm,
          rssi,
          numAttemptedMeasurements,
          numSuccessfulMeasurements,
          lci,
          lcr,
          timestamp);
    }
  }

  private static RangingResult asRangingResult(
      int status,
      MacAddress mac,
      int distanceMm,
      int distanceStdDevMm,
      int rssi,
      int numAttemptedMeasurements,
      int numSuccessfulMeasurements,
      byte[] lci,
      byte[] lcr,
      long timestamp) {
    return ReflectionHelpers.callConstructor(
        RangingResult.class,
        ReflectionHelpers.ClassParameter.from(int.class, status),
        ReflectionHelpers.ClassParameter.from(MacAddress.class, mac),
        ReflectionHelpers.ClassParameter.from(int.class, distanceMm),
        ReflectionHelpers.ClassParameter.from(int.class, distanceStdDevMm),
        ReflectionHelpers.ClassParameter.from(int.class, rssi),
        ReflectionHelpers.ClassParameter.from(int.class, numAttemptedMeasurements),
        ReflectionHelpers.ClassParameter.from(int.class, numSuccessfulMeasurements),
        ReflectionHelpers.ClassParameter.from(byte[].class, lci),
        ReflectionHelpers.ClassParameter.from(byte[].class, lcr),
        ReflectionHelpers.ClassParameter.from(long.class, timestamp));
  }
}
