package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build.VERSION_CODES;
import android.util.Size;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link StreamConfigurationMapBuilder}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class StreamConfigurationMapBuilderTest {
  @Test
  public void testGetOutputSizes() {
    Size size1 = new Size(1920, 1080);
    Size size2 = new Size(1280, 720);
    StreamConfigurationMap map =
        StreamConfigurationMapBuilder.newBuilder()
            .addOutputSize(size1)
            .addOutputSize(size2)
            .build();
    assertThat(Arrays.asList(map.getOutputSizes(MediaRecorder.class)))
        .containsExactly(size1, size2);
  }

  @Test
  public void testGetOutputSizesForTwoFormats() {
    Size size1 = new Size(1920, 1080);
    Size size2 = new Size(1280, 720);
    StreamConfigurationMap map =
        StreamConfigurationMapBuilder.newBuilder()
            .addOutputSize(size1)
            .addOutputSize(ImageFormat.YUV_420_888, size2)
            .build();
    assertThat(Arrays.asList(map.getOutputSizes(MediaRecorder.class))).containsExactly(size1);
    assertThat(Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888))).containsExactly(size2);
  }

  @Test
  public void testGetOutputSizesForImageFormatNV21() {
    Size size1 = new Size(1920, 1080);
    Size size2 = new Size(1280, 720);
    StreamConfigurationMap map =
        StreamConfigurationMapBuilder.newBuilder()
            .addOutputSize(ImageFormat.NV21, size1)
            .addOutputSize(ImageFormat.NV21, size2)
            .build();
    assertThat(Arrays.asList(map.getOutputSizes(ImageFormat.NV21))).containsExactly(size1, size2);
  }

  @Test
  public void testGetOutputSizesPixelFormatRgba8888() {
    Size size1 = new Size(1920, 1080);
    Size size2 = new Size(1280, 720);
    StreamConfigurationMap map =
        StreamConfigurationMapBuilder.newBuilder()
            .addOutputSize(PixelFormat.RGBA_8888, size1)
            .addOutputSize(PixelFormat.RGBA_8888, size2)
            .build();
    assertThat(Arrays.asList(map.getOutputSizes(PixelFormat.RGBA_8888)))
        .containsExactly(size1, size2);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testGetInputSizesIfNotAddInputSizes() {
    StreamConfigurationMap map = StreamConfigurationMapBuilder.newBuilder().build();
    assertThat(map.getInputSizes(ImageFormat.PRIVATE)).isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testGetInputSizesForTwoFormats() {
    Size size1 = new Size(1920, 1080);
    Size size2 = new Size(1280, 720);
    StreamConfigurationMap map =
        StreamConfigurationMapBuilder.newBuilder()
            .addInputSize(ImageFormat.PRIVATE, size1)
            .addInputSize(ImageFormat.YUV_420_888, size2)
            .build();
    assertThat(Arrays.asList(map.getInputSizes(ImageFormat.PRIVATE))).containsExactly(size1);
    assertThat(Arrays.asList(map.getInputSizes(ImageFormat.YUV_420_888))).containsExactly(size2);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testGetInputSizesForImageFormatNV21() {
    Size size1 = new Size(1920, 1080);
    Size size2 = new Size(1280, 720);
    StreamConfigurationMap map =
        StreamConfigurationMapBuilder.newBuilder()
            .addInputSize(ImageFormat.NV21, size1)
            .addInputSize(ImageFormat.NV21, size2)
            .build();
    assertThat(Arrays.asList(map.getInputSizes(ImageFormat.NV21))).containsExactly(size1, size2);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testGetInputSizesPixelFormatRgba8888() {
    Size size1 = new Size(1920, 1080);
    Size size2 = new Size(1280, 720);
    StreamConfigurationMap map =
        StreamConfigurationMapBuilder.newBuilder()
            .addInputSize(PixelFormat.RGBA_8888, size1)
            .addInputSize(PixelFormat.RGBA_8888, size2)
            .build();
    assertThat(Arrays.asList(map.getInputSizes(PixelFormat.RGBA_8888)))
        .containsExactly(size1, size2);
  }
}
