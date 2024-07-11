package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;

import android.net.MacAddress;
import android.net.wifi.rtt.RangingResult;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests ShadowRangeringResult's builder. */
@RunWith(AndroidJUnit4.class)
public class ShadowRangingResultTest {

  @Test
  @Config(minSdk = P, maxSdk = R)
  public void testBuilder_pThroughR() {
    RangingResult result =
        new ShadowRangingResult.Builder(
                RangingResult.STATUS_SUCCESS,
                MacAddress.fromString("00:0a:95:9d:68:16"),
                1548290142, // Timestamp
                10000) // DistanceMm
            .setRssi(100)
            .setDistanceStandardDeviation(200)
            .setNumAttemptedMeasurements(3)
            .setNumSuccessfulMeasurements(5)
            .setUnverifiedResponderLocation(null)
            .build();
    assertThat(result.getMacAddress()).isEqualTo(MacAddress.fromString("00:0a:95:9d:68:16"));
    assertThat(result.getDistanceMm()).isEqualTo(10000);
    assertThat(result.getRssi()).isEqualTo(100);
    assertThat(result.getDistanceStdDevMm()).isEqualTo(200);
    assertThat(result.getNumAttemptedMeasurements()).isEqualTo(3);
    assertThat(result.getNumSuccessfulMeasurements()).isEqualTo(5);
    assertThat(result.getPeerHandle()).isNull();
  }

  @Test
  @Config(minSdk = S)
  public void testBuilder_sPlus() {
    RangingResult result =
        new ShadowRangingResult.Builder(
                RangingResult.STATUS_SUCCESS,
                MacAddress.fromString("00:0a:95:9d:68:16"),
                1548290142, // Timestamp
                10000) // DistanceMm
            .setRssi(100)
            .setDistanceStandardDeviation(200)
            .setNumAttemptedMeasurements(3)
            .setNumSuccessfulMeasurements(5)
            .setUnverifiedResponderLocation(null)
            .setIs80211mcMeasurement(true)
            .build();
    assertThat(result.getMacAddress()).isEqualTo(MacAddress.fromString("00:0a:95:9d:68:16"));
    assertThat(result.getDistanceMm()).isEqualTo(10000);
    assertThat(result.getRssi()).isEqualTo(100);
    assertThat(result.getDistanceStdDevMm()).isEqualTo(200);
    assertThat(result.getNumAttemptedMeasurements()).isEqualTo(3);
    assertThat(result.getNumSuccessfulMeasurements()).isEqualTo(5);
    assertThat(result.getPeerHandle()).isNull();
    assertThat(result.getUnverifiedResponderLocation()).isNull();
    assertThat(result.is80211mcMeasurement()).isTrue();
  }
}
