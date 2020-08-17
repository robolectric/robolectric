package org.robolectric.shadows;

import android.location.GnssStatus;
import android.os.Build;
import androidx.annotation.Nullable;
import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link GnssStatus} objects, since they have a hidden constructor. */
public final class GnssStatusBuilder {
  /** Information about a single satellite in a {@link GnssStatus}. */
  @AutoValue
  public abstract static class GnssSatelliteInfo {
    /**
     * Gets the {@link GnssStatus#getConstellationType(int) GNSS constellation} of the satellite.
     */
    public abstract int getConstellation();

    /** Gets the {@link GnssStatus#getSvid(int) identification number} of the satellite. */
    public abstract int getSvid();

    /** Gets the {@link GnssStatus#getCn0DbHz(int) carrier-to-noise density} of the satellite. */
    public abstract float getCn0DbHz();

    /**
     * Gets the {@link GnssStatus#getElevationDegrees(int) elevation} of the satellite, in degrees.
     */
    public abstract float getElevation();

    /** Gets the {@link GnssStatus#getAzimuthDegrees(int) azimuth} of the satellite, in degrees. */
    public abstract float getAzimuth();

    /** Gets whether the satellite {@link GnssStatus#hasEphemerisData(int) has ephemeris data}. */
    public abstract boolean getHasEphemeris();

    /** Gets whether the satellite {@link GnssStatus#hasAlmanacData(int) has almanac data}. */
    public abstract boolean getHasAlmanac();

    /**
     * Gets whether the satellite {@link GnssStatus#usedInFix(int) was used in the most recent
     * position fix}.
     */
    public abstract boolean isUsedInFix();

    /**
     * Gets the {@link GnssStatus#getCarrierFrequencyHz(int) carrier frequency} of the satellite, in
     * Hz, if present; if {@code null}, indicates that the carrier frequency {@link
     * GnssStatus#hasCarrierFrequencyHz(int) is not available}.
     */
    @Nullable
    public abstract Float getCarrierFrequencyHz();

    public static Builder builder() {
      return new AutoValue_GnssStatusBuilder_GnssSatelliteInfo.Builder();
    }

    /** Builder for {@link GnssSatelliteInfo}. */
    @AutoValue.Builder
    public abstract static class Builder {
      /**
       * Sets the {@link GnssStatus#getConstellationType(int) GNSS constellation} of the satellite.
       */
      public abstract Builder setConstellation(int constellation);

      /** Sets the {@link GnssStatus#getSvid(int) identification number} of the satellite. */
      public abstract Builder setSvid(int svid);

      /** Gets the {@link GnssStatus#getCn0DbHz(int) carrier-to-noise density} of the satellite. */
      public abstract Builder setCn0DbHz(float cn0DbHz);

      /**
       * Sets the {@link GnssStatus#getElevationDegrees(int) elevation} of the satellite, in
       * degrees.
       */
      public abstract Builder setElevation(float elevation);

      /**
       * Sets the {@link GnssStatus#getAzimuthDegrees(int) azimuth} of the satellite, in degrees.
       */
      public abstract Builder setAzimuth(float azimuth);

      /** Sets whether the satellite {@link GnssStatus#hasEphemerisData(int) has ephemeris data}. */
      public abstract Builder setHasEphemeris(boolean hasEphemeris);

      /** Sets whether the satellite {@link GnssStatus#hasAlmanacData(int) has almanac data}. */
      public abstract Builder setHasAlmanac(boolean hasAlmanac);

      /**
       * Sets whether the satellite {@link GnssStatus#usedInFix(int) was used in the most recent
       * position fix}.
       */
      public abstract Builder setUsedInFix(boolean usedInFix);

      /**
       * Sets the {@link GnssStatus#getCarrierFrequencyHz(int) carrier frequency} of the satellite,
       * in Hz, if present; if {@code null}, indicates that the carrier frequency {@link
       * GnssStatus#hasCarrierFrequencyHz(int) is not available}.
       */
      public abstract Builder setCarrierFrequencyHz(@Nullable Float carrierFrequencyHz);

      /** Builds the {@link GnssSatelliteInfo}. */
      public abstract GnssSatelliteInfo build();
    }
  }

  private GnssStatusBuilder() {}

  /** Creates a new {@link GnssStatusBuilder}. */
  public static GnssStatusBuilder create() {
    return new GnssStatusBuilder();
  }

  private final List<GnssSatelliteInfo> satelliteInfos = new ArrayList<>();

  /** Adds a satellite to the {@link GnssStatus} being built. */
  public GnssStatusBuilder addSatellite(GnssSatelliteInfo satelliteInfo) {
    satelliteInfos.add(satelliteInfo);
    return this;
  }

  /** Adds a collection of satellites to the {@link GnssStatus} being built. */
  public GnssStatusBuilder addAllSatellites(Collection<GnssSatelliteInfo> satelliteInfos) {
    this.satelliteInfos.addAll(satelliteInfos);
    return this;
  }

  /** Builds the {@link GnssStatus} from the satellites previously added. */
  public GnssStatus build() {
    return createFrom(satelliteInfos);
  }

  /** Convenience method to create a {@link GnssStatus} directly from known satellite info. */
  public static GnssStatus buildFrom(GnssSatelliteInfo... satelliteInfos) {
    return createFrom(Arrays.asList(satelliteInfos));
  }

  private static final int GNSS_SV_FLAGS_HAS_EPHEMERIS_DATA =
      (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
          ? ReflectionHelpers.getStaticField(GnssStatus.class, "GNSS_SV_FLAGS_HAS_EPHEMERIS_DATA")
          : 0;
  private static final int GNSS_SV_FLAGS_HAS_ALMANAC_DATA =
      (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
          ? ReflectionHelpers.getStaticField(GnssStatus.class, "GNSS_SV_FLAGS_HAS_ALMANAC_DATA")
          : 0;
  private static final int GNSS_SV_FLAGS_USED_IN_FIX =
      (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
          ? ReflectionHelpers.getStaticField(GnssStatus.class, "GNSS_SV_FLAGS_USED_IN_FIX")
          : 0;
  private static final int GNSS_SV_FLAGS_HAS_CARRIER_FREQUENCY =
      (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
              && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
          ? ReflectionHelpers.getStaticField(
              GnssStatus.class, "GNSS_SV_FLAGS_HAS_CARRIER_FREQUENCY")
          : 0;
  private static final boolean SUPPORTS_CARRIER_FREQUENCY =
      (GNSS_SV_FLAGS_HAS_CARRIER_FREQUENCY != 0);

  private static final int SVID_SHIFT_WIDTH =
      ReflectionHelpers.getStaticField(GnssStatus.class, "SVID_SHIFT_WIDTH");
  private static final int CONSTELLATION_TYPE_SHIFT_WIDTH =
      ReflectionHelpers.getStaticField(GnssStatus.class, "CONSTELLATION_TYPE_SHIFT_WIDTH");
  private static final int CONSTELLATION_TYPE_MASK =
      ReflectionHelpers.getStaticField(GnssStatus.class, "CONSTELLATION_TYPE_MASK");

  private static GnssStatus createFrom(List<GnssSatelliteInfo> satelliteInfos) {
    int svCount = satelliteInfos.size();
    int[] svidWithFlags = new int[svCount];
    float[] cn0DbHz = new float[svCount];
    float[] elevations = new float[svCount];
    float[] azimuths = new float[svCount];
    float[] carrierFrequencies = new float[svCount];

    for (int i = 0; i < svCount; i++) {
      GnssSatelliteInfo info = satelliteInfos.get(i);

      int packedSvid =
          (info.getSvid() << SVID_SHIFT_WIDTH)
              | (info.getConstellation() & CONSTELLATION_TYPE_MASK)
                  << CONSTELLATION_TYPE_SHIFT_WIDTH;

      if (info.getHasEphemeris()) {
        packedSvid |= GNSS_SV_FLAGS_HAS_EPHEMERIS_DATA;
      }
      if (info.getHasAlmanac()) {
        packedSvid |= GNSS_SV_FLAGS_HAS_ALMANAC_DATA;
      }
      if (info.isUsedInFix()) {
        packedSvid |= GNSS_SV_FLAGS_USED_IN_FIX;
      }
      if (SUPPORTS_CARRIER_FREQUENCY && info.getCarrierFrequencyHz() != null) {
        packedSvid |= GNSS_SV_FLAGS_HAS_CARRIER_FREQUENCY;
        carrierFrequencies[i] = info.getCarrierFrequencyHz();
      }
      svidWithFlags[i] = packedSvid;

      cn0DbHz[i] = info.getCn0DbHz();
      elevations[i] = info.getElevation();
      azimuths[i] = info.getAzimuth();
    }

    List<ClassParameter<?>> classParameters = new ArrayList<>();
    classParameters.add(ClassParameter.from(int.class, svCount));
    classParameters.add(ClassParameter.from(int[].class, svidWithFlags));
    classParameters.add(ClassParameter.from(float[].class, cn0DbHz));
    classParameters.add(ClassParameter.from(float[].class, elevations));
    classParameters.add(ClassParameter.from(float[].class, azimuths));

    if (SUPPORTS_CARRIER_FREQUENCY) {
      classParameters.add(ClassParameter.from(float[].class, carrierFrequencies));
    }

    return ReflectionHelpers.callConstructor(
        GnssStatus.class, classParameters.toArray(new ClassParameter<?>[0]));
  }
}
