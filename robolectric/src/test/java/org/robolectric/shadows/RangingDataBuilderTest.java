package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.ranging.RangingManager.BLE_CS;
import static android.ranging.RangingManager.UWB;
import static com.google.common.truth.Truth.assertThat;

import android.ranging.RangingData;
import android.ranging.RangingMeasurement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public final class RangingDataBuilderTest {

  @Test
  public void constructUwbRangingData_returnsUwbRangingData() {
    RangingData rangingData =
        new RangingDataBuilder()
            .setRangingTechnology(UWB)
            .setTimestampMillis(222)
            .setDistance(2.0)
            .build();

    assertThat(rangingData).isNotNull();
    assertThat(rangingData.getRangingTechnology()).isEqualTo(UWB);
    assertThat(rangingData.getTimestampMillis()).isEqualTo(222);
    assertThat(rangingData.getDistance().getMeasurement()).isEqualTo(2.0);
  }

  @Test
  public void constructCsRangingData_returnsCsRangingData() {
    RangingData rangingData =
        new RangingDataBuilder()
            .setRangingTechnology(BLE_CS)
            .setTimestampMillis(111)
            .setDistance(3.0)
            .build();

    assertThat(rangingData).isNotNull();
    assertThat(rangingData.getRangingTechnology()).isEqualTo(BLE_CS);
    assertThat(rangingData.getTimestampMillis()).isEqualTo(111);
    assertThat(rangingData.getDistance().getMeasurement()).isEqualTo(3.0);
  }

  @Test
  public void constructCsRangingDataWithNoConfidence_returnsDefaultConfidenceOfHigh() {
    RangingData rangingData =
        new RangingDataBuilder()
            .setRangingTechnology(BLE_CS)
            .setTimestampMillis(111)
            .setDistance(3.0)
            .build();

    assertThat(rangingData).isNotNull();
    assertThat(rangingData.getDistance().getConfidence())
        .isEqualTo(RangingMeasurement.CONFIDENCE_HIGH);
  }

  @Test
  public void constructCsRangingDataWithLowConfidence_returnsLowConfidence() {
    RangingData rangingData =
        new RangingDataBuilder()
            .setRangingTechnology(BLE_CS)
            .setTimestampMillis(111)
            .setDistance(3.0)
            .setConfidence(RangingMeasurement.CONFIDENCE_LOW)
            .build();

    assertThat(rangingData).isNotNull();
    assertThat(rangingData.getDistance().getConfidence())
        .isEqualTo(RangingMeasurement.CONFIDENCE_LOW);
  }
}
