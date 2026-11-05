package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.LOLLIPOP)
public final class TotalCaptureResultBuilderTest {

  @Test
  public void build_createsTotalCaptureResult() {
    TotalCaptureResult result = TotalCaptureResultBuilder.newBuilder().build();

    assertThat(result).isNotNull();
    assertThat(result.getSequenceId()).isEqualTo(0);
  }

  @Test
  public void build_withSequenceId_setsSequenceId() {
    TotalCaptureResult result = TotalCaptureResultBuilder.newBuilder().setSequenceId(42).build();

    assertThat(result.getSequenceId()).isEqualTo(42);
  }

  @Test
  public void build_withKey_setsValue() {
    TotalCaptureResult result =
        TotalCaptureResultBuilder.newBuilder()
            .set(CaptureResult.SENSOR_EXPOSURE_TIME, 1000L)
            .build();

    assertThat(result.get(CaptureResult.SENSOR_EXPOSURE_TIME)).isEqualTo(1000L);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void build_withPhysicalCameraResults_populatesPhysicalResults() {
    TotalCaptureResult physicalResult = TotalCaptureResultBuilder.newBuilder().build();
    TotalCaptureResult totalResult =
        TotalCaptureResultBuilder.newBuilder()
            .setPhysicalCameraResults(ImmutableMap.of("1", physicalResult))
            .build();

    Map<String, CaptureResult> physicalResults = totalResult.getPhysicalCameraResults();
    assertThat(physicalResults).containsEntry("1", physicalResult);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void build_addPhysicalCameraResult_populatesPhysicalResults() {
    TotalCaptureResult physicalResult = TotalCaptureResultBuilder.newBuilder().build();
    TotalCaptureResult totalResult =
        TotalCaptureResultBuilder.newBuilder().addPhysicalCameraResult("1", physicalResult).build();

    Map<String, CaptureResult> physicalResults = totalResult.getPhysicalCameraResults();
    assertThat(physicalResults).containsEntry("1", physicalResult);
  }
}
