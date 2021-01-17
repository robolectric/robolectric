package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.location.GnssStatus;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.GnssStatusBuilder.GnssSatelliteInfo;

/** Tests for {@link GnssStatusBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = N, maxSdk = Q)
public class GnssStatusBuilderTest {

  private static final int SVID = 42;
  private static final float CN0 = 33.25f;
  private static final float ELEVATION = 45.0f;
  private static final float AZIMUTH = 90.0f;
  private static final boolean HAS_EPHEMERIS = false;
  private static final boolean HAS_ALMANAC = true;
  private static final boolean USED_IN_FIX = true;

  @Test
  public void emptyBuilder() {
    GnssStatus status = GnssStatusBuilder.create().build();
    assertThat(status.getSatelliteCount()).isEqualTo(0);
  }

  @Test
  public void builder_addSatellite() {
    GnssStatusBuilder builder = GnssStatusBuilder.create();
    builder.addSatellite(
        GnssSatelliteInfo.builder()
            .setConstellation(GnssStatus.CONSTELLATION_GPS)
            .setSvid(SVID)
            .setCn0DbHz(CN0)
            .setElevation(ELEVATION)
            .setAzimuth(AZIMUTH)
            .setHasEphemeris(HAS_EPHEMERIS)
            .setHasAlmanac(HAS_ALMANAC)
            .setUsedInFix(USED_IN_FIX)
            .build());
    GnssStatus status = builder.build();

    assertThat(status.getSatelliteCount()).isEqualTo(1);
    assertThat(status.getConstellationType(0)).isEqualTo(GnssStatus.CONSTELLATION_GPS);
    assertThat(status.getSvid(0)).isEqualTo(SVID);
    assertThat(status.getCn0DbHz(0)).isEqualTo(CN0);
    assertThat(status.getElevationDegrees(0)).isEqualTo(ELEVATION);
    assertThat(status.getAzimuthDegrees(0)).isEqualTo(AZIMUTH);
    assertThat(status.hasEphemerisData(0)).isEqualTo(HAS_EPHEMERIS);
    assertThat(status.hasAlmanacData(0)).isEqualTo(HAS_ALMANAC);
    assertThat(status.usedInFix(0)).isEqualTo(USED_IN_FIX);
  }

  @Test
  public void builder_addAll() {
    GnssSatelliteInfo.Builder infoBuilder =
        GnssSatelliteInfo.builder()
            .setConstellation(GnssStatus.CONSTELLATION_GPS)
            .setCn0DbHz(CN0)
            .setElevation(ELEVATION)
            .setAzimuth(AZIMUTH)
            .setHasEphemeris(HAS_EPHEMERIS)
            .setHasAlmanac(HAS_ALMANAC)
            .setUsedInFix(USED_IN_FIX);


    List<GnssSatelliteInfo> satelliteInfos = new ArrayList<>();
    satelliteInfos.add(infoBuilder.setSvid(SVID).build());
    satelliteInfos.add(infoBuilder.setSvid(SVID + 1).build());
    satelliteInfos.add(infoBuilder.setSvid(SVID - 1).build());

    GnssStatus status = GnssStatusBuilder.create().addAllSatellites(satelliteInfos).build();
    assertThat(status.getSatelliteCount()).isEqualTo(3);
    assertThat(status.getSvid(0)).isEqualTo(SVID);
    assertThat(status.getSvid(1)).isEqualTo(SVID + 1);
    assertThat(status.getSvid(2)).isEqualTo(SVID - 1);
  }

  @Test
  public void builder_buildFrom() {
    GnssSatelliteInfo.Builder infoBuilder =
        GnssSatelliteInfo.builder()
            .setConstellation(GnssStatus.CONSTELLATION_GPS)
            .setCn0DbHz(CN0)
            .setElevation(ELEVATION)
            .setAzimuth(AZIMUTH)
            .setHasEphemeris(HAS_EPHEMERIS)
            .setHasAlmanac(HAS_ALMANAC)
            .setUsedInFix(USED_IN_FIX);

    GnssSatelliteInfo info1 = infoBuilder.setSvid(SVID).build();
    GnssSatelliteInfo info2 = infoBuilder.setSvid(SVID * 2).build();

    GnssStatus status = GnssStatusBuilder.buildFrom(info1, info2);

    assertThat(status.getSatelliteCount()).isEqualTo(2);
    assertThat(status.getSvid(0)).isEqualTo(SVID);
    assertThat(status.getSvid(1)).isEqualTo(SVID * 2);
  }

  @Test
  @Config(minSdk = O)
  public void addSatellite_carrierFrequency() {
    GnssSatelliteInfo.Builder infoBuilder =
    GnssSatelliteInfo.builder()
        .setConstellation(GnssStatus.CONSTELLATION_GPS)
        .setCn0DbHz(CN0)
        .setElevation(ELEVATION)
        .setAzimuth(AZIMUTH)
        .setHasEphemeris(HAS_EPHEMERIS)
        .setHasAlmanac(HAS_ALMANAC)
        .setUsedInFix(USED_IN_FIX);

    GnssStatus status = GnssStatusBuilder.create()
        .addSatellite(infoBuilder.setSvid(SVID).build())
        .addSatellite(infoBuilder.setSvid(SVID + 1).setCarrierFrequencyHz(null).build())
        .addSatellite(infoBuilder.setSvid(SVID - 1).setCarrierFrequencyHz(1575.42f).build())
        .build();

    assertThat(status.getSatelliteCount()).isEqualTo(3);
    assertThat(status.hasCarrierFrequencyHz(0)).isFalse();
    assertThat(status.getCarrierFrequencyHz(0)).isEqualTo(0.0f);
    assertThat(status.hasCarrierFrequencyHz(1)).isFalse();
    assertThat(status.getCarrierFrequencyHz(1)).isEqualTo(0.0f);
    assertThat(status.hasCarrierFrequencyHz(2)).isTrue();
    assertThat(status.getCarrierFrequencyHz(2)).isEqualTo(1575.42f);
  }
}
