package org.robolectric.shadows;


import android.net.MacAddress;
import android.net.wifi.rtt.RangingResult;
import java.util.Arrays;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link android.net.wifi.rtt.RangingResult}. */
@Implements(RangingResult.class)
public class ShadowRangingResult {

  @RealObject RangingResult realObject;

  public static RangingResult newInstance(
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

  @Override
  @Implementation
  public String toString() {
    return new StringBuilder()
        .append("status: ").append(realObject.getStatus())
        .append(", mac: ").append(realObject.getMacAddress())
        .append(", distanceMm: ").append(realObject.getDistanceMm())
        .append(", distanceStdDevMm: ").append(realObject.getDistanceStdDevMm())
        .append(", rssi: ").append(realObject.getRssi())
        .append(", numAttemptedMeasurements: ").append(realObject.getNumAttemptedMeasurements())
        .append(", numSuccessfulMeasurements: ").append(realObject.getNumSuccessfulMeasurements())
        .append(", lci: ").append(Arrays.toString(realObject.getLci()))
        .append(", lcr: ").append(Arrays.toString(realObject.getLcr()))
        .append(", timestamp: ").append(realObject.getRangingTimestampMillis())
        .toString();
  }
}
