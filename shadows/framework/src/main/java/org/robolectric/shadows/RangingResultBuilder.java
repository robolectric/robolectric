package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.MacAddress;
import android.net.wifi.WifiAnnotations.ChannelWidth;
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.ResponderLocation;
import android.os.Build;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;
import org.robolectric.versioning.AndroidVersions.V;

/**
 * A builder for creating ShadowRangingResults. Status, macAddress, distance [mm] and timestamp are
 * all mandatory fields. Additional fields can be specified by setters. Use build() to return the
 * ShadowRangingResult object.
 *
 * <p>TODO:org/robolectric/shadows/ShadowLegacyTypeface Remove after V is generally available.
 */
public class RangingResultBuilder {

  // Required Values
  private final int status;
  private final MacAddress mac;
  private final int distanceMm;
  private final long timestampMillis;

  // Optional Values
  private int distanceStdDevMm = 0;
  private int rssi = 0;
  private int numAttemptedMeasurements = 0;
  private int numSuccessfulMeasurements = 0;
  private byte[] lci = new byte[0];
  private byte[] lcr = new byte[0];
  private ResponderLocation unverifiedResponderLocation = null;
  private boolean is80211mcMeasurement = true;

  public RangingResultBuilder(int status, MacAddress mac, long timestampMillis, int distanceMm) {
    this.status = status;
    this.mac = mac;
    this.timestampMillis = timestampMillis;
    this.distanceMm = distanceMm;
  }

  public RangingResultBuilder setDistanceStandardDeviation(int stddev) {
    this.distanceStdDevMm = stddev;
    return this;
  }

  public RangingResultBuilder setRssi(int rssi) {
    this.rssi = rssi;
    return this;
  }

  public RangingResultBuilder setNumAttemptedMeasurements(int num) {
    this.numAttemptedMeasurements = num;
    return this;
  }

  public RangingResultBuilder setNumSuccessfulMeasurements(int num) {
    this.numSuccessfulMeasurements = num;
    return this;
  }

  public RangingResultBuilder setLci(byte[] lci) {
    this.lci = lci;
    return this;
  }

  public RangingResultBuilder setLcr(byte[] lcr) {
    this.lcr = lcr;
    return this;
  }

  public RangingResultBuilder setUnverifiedResponderLocation(
      ResponderLocation unverifiedResponderLocation) {
    this.unverifiedResponderLocation = unverifiedResponderLocation;
    return this;
  }

  public RangingResultBuilder setIs80211mcMeasurement(boolean is80211mcMeasurement) {
    this.is80211mcMeasurement = is80211mcMeasurement;
    return this;
  }

  public RangingResult build() {
    if (RuntimeEnvironment.getApiLevel() >= V.SDK_INT) {
      Object builder;
      try {
        builder =
            ReflectionHelpers.newInstance(
                Class.forName("android.net.wifi.rtt.RangingResult$Builder"));
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      reflector(RangingResultBuilderReflector.class, builder).setStatus(status);
      reflector(RangingResultBuilderReflector.class, builder).setMacAddress(mac);
      reflector(RangingResultBuilderReflector.class, builder).setDistanceMm(distanceMm);
      reflector(RangingResultBuilderReflector.class, builder).setDistanceStdDevMm(distanceStdDevMm);
      reflector(RangingResultBuilderReflector.class, builder).setRssi(rssi);
      reflector(RangingResultBuilderReflector.class, builder)
          .setNumAttemptedMeasurements(numAttemptedMeasurements);
      reflector(RangingResultBuilderReflector.class, builder)
          .setNumSuccessfulMeasurements(numSuccessfulMeasurements);
      reflector(RangingResultBuilderReflector.class, builder).setLci(lci);
      reflector(RangingResultBuilderReflector.class, builder).setLcr(lcr);
      reflector(RangingResultBuilderReflector.class, builder)
          .setUnverifiedResponderLocation(unverifiedResponderLocation);
      reflector(RangingResultBuilderReflector.class, builder)
          .setRangingTimestampMillis(timestampMillis);
      reflector(RangingResultBuilderReflector.class, builder)
          .set80211mcMeasurement(is80211mcMeasurement);
      return reflector(RangingResultBuilderReflector.class, builder).build();
    }
    if (RuntimeEnvironment.getApiLevel() > Build.VERSION_CODES.R) {
      return asRangingResultS(
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
          timestampMillis,
          is80211mcMeasurement);
    }

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.Q) {
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

  private static RangingResult asRangingResultS(
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
      long timestamp,
      boolean is80211mcMeasurement) {
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
        ReflectionHelpers.ClassParameter.from(long.class, timestamp),
        ReflectionHelpers.ClassParameter.from(boolean.class, is80211mcMeasurement));
  }

  @ForType(className = "android.net.wifi.rtt.RangingResult$Builder")
  interface RangingResultBuilderReflector {
    @Accessor("mStatus")
    void setStatus(int status);

    @Accessor("mMac")
    void setMacAddress(MacAddress macAddress);

    @Accessor("mPeerHandle")
    void setPeerHandle(PeerHandle peerHandle);

    @Accessor("mDistanceMm")
    void setDistanceMm(int distanceMm);

    @Accessor("mDistanceStdDevMm")
    void setDistanceStdDevMm(int distanceStdDevMm);

    @Accessor("mRssi")
    void setRssi(int rssi);

    @Accessor("mNumAttemptedMeasurements")
    void setNumAttemptedMeasurements(int numAttemptedMeasurements);

    @Accessor("mNumSuccessfulMeasurements")
    void setNumSuccessfulMeasurements(int numSuccessfulMeasurements);

    @Accessor("mLci")
    void setLci(byte[] lci);

    @Accessor("mLcr")
    void setLcr(byte[] lcr);

    @Accessor("mResponderLocation")
    void setUnverifiedResponderLocation(ResponderLocation responderLocation);

    @Accessor("mTimestamp")
    void setRangingTimestampMillis(long timestamp);

    @Accessor("mIs80211mcMeasurement")
    void set80211mcMeasurement(boolean is80211mcMeasurement);

    @Accessor("mFrequencyMHz")
    void setMeasurementChannelFrequencyMHz(int frequencyMHz);

    @Accessor("mPacketBw")
    void setMeasurementBandwidth(@ChannelWidth int measurementBandwidth);

    @Accessor("mIs80211azNtbMeasurement")
    void set80211azNtbMeasurement(boolean is80211azNtbMeasurement);

    @Accessor("mNtbMinMeasurementTime")
    void setMinTimeBetweenNtbMeasurementsMicros(long ntbMinMeasurementTime);

    @Accessor("mNtbMaxMeasurementTime")
    void setMaxTimeBetweenNtbMeasurementsMicros(long ntbMaxMeasurementTime);

    @Accessor("mI2rTxLtfRepetitions")
    void set80211azInitiatorTxLtfRepetitionsCount(int i2rTxLtfRepetitions);

    @Accessor("mR2iTxLtfRepetitions")
    void set80211azResponderTxLtfRepetitionsCount(int r2iTxLtfRepetitions);

    @Accessor("mNumTxSpatialStreams")
    void set80211azNumberOfTxSpatialStreams(int numTxSpatialStreams);

    @Accessor("mNumRxSpatialStreams")
    void set80211azNumberOfRxSpatialStreams(int numRxSpatialStreams);

    @Accessor("mVendorData")
    void setVendorData(
        @WithType("java.util.List<android.net.wifi.OuiKeyedData>") Object vendorData);

    RangingResult build();
  }
}
