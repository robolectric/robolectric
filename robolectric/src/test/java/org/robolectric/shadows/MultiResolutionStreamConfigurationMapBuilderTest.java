package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.graphics.ImageFormat;
import android.hardware.camera2.params.MultiResolutionStreamConfigurationMap;
import android.hardware.camera2.params.MultiResolutionStreamInfo;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link MultiResolutionStreamConfigurationMapBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.S)
public final class MultiResolutionStreamConfigurationMapBuilderTest {

  @Test
  public void build_withOutputAndInputStreamConfigs_returnsConfiguredStreams() {
    MultiResolutionStreamInfo rawOutputCam2 =
        new MultiResolutionStreamInfo(
            /* streamWidth= */ 4080, /* streamHeight= */ 3072, /* physicalCameraId= */ "2");
    MultiResolutionStreamInfo rawOutputCam3 =
        new MultiResolutionStreamInfo(
            /* streamWidth= */ 4032, /* streamHeight= */ 3024, /* physicalCameraId= */ "3");
    MultiResolutionStreamInfo jpegOutputCam2 =
        new MultiResolutionStreamInfo(
            /* streamWidth= */ 8160, /* streamHeight= */ 6144, /* physicalCameraId= */ "2");
    MultiResolutionStreamInfo yuvInputCam2 =
        new MultiResolutionStreamInfo(
            /* streamWidth= */ 1920, /* streamHeight= */ 1080, /* physicalCameraId= */ "2");

    MultiResolutionStreamConfigurationMap map =
        MultiResolutionStreamConfigurationMapBuilder.newBuilder()
            .addOutputStreamConfig(ImageFormat.RAW10, rawOutputCam2)
            .addOutputStreamConfig(ImageFormat.RAW10, rawOutputCam3)
            .addOutputStreamConfig(ImageFormat.JPEG, jpegOutputCam2)
            .addInputStreamConfig(ImageFormat.YUV_420_888, yuvInputCam2)
            .build();

    assertThat(map.getOutputFormats())
        .asList()
        .containsExactly(ImageFormat.RAW10, ImageFormat.JPEG);
    assertThat(map.getOutputInfo(ImageFormat.RAW10)).containsExactly(rawOutputCam2, rawOutputCam3);
    assertThat(map.getOutputInfo(ImageFormat.JPEG)).containsExactly(jpegOutputCam2);
    assertThat(map.getInputFormats()).asList().containsExactly(ImageFormat.YUV_420_888);
    assertThat(map.getInputInfo(ImageFormat.YUV_420_888)).containsExactly(yuvInputCam2);
  }

  @Test
  public void build_whenEmpty_throwsIllegalArgumentException() {
    MultiResolutionStreamConfigurationMapBuilder builder =
        MultiResolutionStreamConfigurationMapBuilder.newBuilder();

    assertThrows(IllegalArgumentException.class, builder::build);
  }
}
