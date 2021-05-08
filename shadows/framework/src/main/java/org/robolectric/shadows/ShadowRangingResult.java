package org.robolectric.shadows;

import android.net.MacAddress;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.ResponderLocation;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link android.net.wifi.rtt.RangingResult}. */
@Implements(value = RangingResult.class, minSdk = VERSION_CODES.P)
public class ShadowRangingResult {

  /**
   * A builder for creating ShadowRangingResults. Status, macaddress, distance [mm] and timestamp
   * are all mandatory fields. Additional fields can be specified by setters. Use build() to return
   * the ShadowRangingResult object.
   */
  public static class Builder {
    // Required Values
    protected final int status;
    protected final MacAddress mac;
    protected final int distanceMm;
    protected final long timestampMillis;

    // Optional Values
    protected int distanceStdDevMm = 0;
    protected int rssi = 0;
    protected int numAttemptedMeasurements = 0;
    protected int numSuccessfulMeasurements = 0;
    protected byte[] lci = new byte[0];
    protected byte[] lcr = new byte[0];
    protected ResponderLocation unverifiedResponderLocation = null;

    public Builder(int status, MacAddress mac, long timestampMillis, int distanceMm) {
      this.status = status;
      this.mac = mac;
      this.timestampMillis = timestampMillis;
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

    public Builder setUnverifiedResponderLocation(ResponderLocation unverifiedResponderLocation) {
      this.unverifiedResponderLocation = unverifiedResponderLocation;
      return this;
    }

    public RangingResult build() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return asRangingResultQ(
            status,
            mac,
            distanceMm,
            distanceStdDevMm,
            rssi,
            numAttemptedMeasurements,
            numSuccessfulMeasurements,
            lci,
            lcr,
            unverifiedResponderLocation,
            timestampMillis);
      }

      return asRangingResultP(
          status,
          mac,
          distanceMm,
          distanceStdDevMm,
          rssi,
          numAttemptedMeasurements,
          numSuccessfulMeasurements,
          lci,
          lcr,
          timestampMillis);
    }
  }

  private static RangingResult asRangingResultP(
      int status,
      MacAddress mac,
      int distanceMm,
      int distanceStdDevMm,
      int rssi,
      int numAttemptedMeasurements,
      int numSuccessfulMeasurements,
      byte[] lci,
      byte[] lcr,
      long timestampMillis) {
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
        ReflectionHelpers.ClassParameter.from(long.class, timestampMillis));
  }

  private static RangingResult asRangingResultQ(
      int status,
      MacAddress mac,
      int distanceMm,
      int distanceStdDevMm,
      int rssi,
      int numAttemptedMeasurements,
      int numSuccessfulMeasurements,
      byte[] lci,
      byte[] lcr,
      ResponderLocation unverifiedResponderLocation,
      long timestampMillis) {
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
        ReflectionHelpers.ClassParameter.from(ResponderLocation.class, unverifiedResponderLocation),
        ReflectionHelpers.ClassParameter.from(long.class, timestampMillis));
  }
}
