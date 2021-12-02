package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.hardware.HardwareBuffer;
import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for org.robolectric.shadows.ShadowHardwareBuffer. */
@RunWith(AndroidJUnit4.class)
public class ShadowHardwareBufferTest {
  private static final int INVALID_WIDTH = 0;
  private static final int INVALID_HEIGHT = 0;
  private static final int INVALID_LAYERS = 0;
  private static final int INVALID_FORMAT = -1;
  private static final long INVALID_USAGE_FLAGS = -1;
  private static final int VALID_WIDTH = 16;
  private static final int VALID_HEIGHT = 32;
  private static final int VALID_LAYERS = 2;

  private static final ImmutableList<Integer> VALID_FORMATS_O =
      ImmutableList.of(
          HardwareBuffer.RGBA_8888,
          HardwareBuffer.RGBA_FP16,
          HardwareBuffer.RGBA_1010102,
          HardwareBuffer.RGBX_8888,
          HardwareBuffer.RGB_888,
          HardwareBuffer.RGB_565,
          HardwareBuffer.BLOB);

  private static final ImmutableList<Integer> VALID_FORMATS_P =
      ImmutableList.of(
          HardwareBuffer.D_16,
          HardwareBuffer.D_24,
          HardwareBuffer.D_FP32,
          HardwareBuffer.DS_24UI8,
          HardwareBuffer.DS_FP32UI8,
          HardwareBuffer.S_UI8);

  private static final long VALID_USAGE_FLAGS_O =
      HardwareBuffer.USAGE_CPU_READ_RARELY
          | HardwareBuffer.USAGE_CPU_READ_OFTEN
          | HardwareBuffer.USAGE_CPU_WRITE_RARELY
          | HardwareBuffer.USAGE_CPU_WRITE_OFTEN
          | HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE
          | HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
          | HardwareBuffer.USAGE_PROTECTED_CONTENT
          | HardwareBuffer.USAGE_VIDEO_ENCODE
          | HardwareBuffer.USAGE_GPU_DATA_BUFFER
          | HardwareBuffer.USAGE_SENSOR_DIRECT_DATA;

  private static final long VALID_USAGE_FLAGS_P =
      HardwareBuffer.USAGE_GPU_CUBE_MAP | HardwareBuffer.USAGE_GPU_MIPMAP_COMPLETE;

  @Test
  @Config(minSdk = O)
  public void createInvalidWidthThrows() {
    try {
      HardwareBuffer.create(
          INVALID_WIDTH, VALID_HEIGHT, HardwareBuffer.RGBA_8888, VALID_LAYERS, VALID_USAGE_FLAGS_O);
      fail("IllegalArgumentException should be thrown with invalid width.");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = O)
  public void createInvalidHeightThrows() {
    try {
      HardwareBuffer.create(
          VALID_WIDTH, INVALID_HEIGHT, HardwareBuffer.RGBA_8888, VALID_LAYERS, VALID_USAGE_FLAGS_O);
      fail("IllegalArgumentException should be thrown with invalid height.");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = O)
  public void createInvalidFormatThrows() {
    try {
      HardwareBuffer.create(
          VALID_WIDTH, VALID_HEIGHT, INVALID_FORMAT, VALID_LAYERS, VALID_USAGE_FLAGS_O);
      fail("IllegalArgumentException should be thrown with invalid format.");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = O)
  public void createInvalidLayersThrows() {
    try {
      HardwareBuffer.create(
          VALID_WIDTH, VALID_HEIGHT, HardwareBuffer.RGBA_8888, INVALID_LAYERS, VALID_USAGE_FLAGS_O);
      fail("IllegalArgumentException should be thrown with invalid layer count.");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = O)
  public void createInvalidUsageFlagsThrows() {
    try {
      HardwareBuffer.create(
          VALID_WIDTH, VALID_HEIGHT, HardwareBuffer.RGBA_8888, VALID_LAYERS, INVALID_USAGE_FLAGS);
      fail("IllegalArgumentException should be thrown with invalid usage flags.");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = O, maxSdk = O)
  public void createWithPFormatsFailsOnO() {
    for (int format : VALID_FORMATS_P) {
      try {
        HardwareBuffer.create(
            VALID_WIDTH,
            format == HardwareBuffer.BLOB ? 1 : VALID_HEIGHT,
            format,
            VALID_LAYERS,
            VALID_USAGE_FLAGS_O);
        fail(
            "IllegalArgumentException should be thrown when using Android P formats on Android O.");
      } catch (IllegalArgumentException e) {
        // pass
      }
    }
  }

  @Test
  @Config(minSdk = O, maxSdk = O)
  public void createWithPFlagsThrowsOnO() {
    try {
      HardwareBuffer.create(
          VALID_WIDTH, VALID_HEIGHT, HardwareBuffer.RGBA_8888, VALID_LAYERS, VALID_USAGE_FLAGS_P);
      fail("IllegalArgumentException should be thrown when using Android P flags on Android O.");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = O)
  public void createWithBlobFormatInvalidHeightThrows() {
    try {
      HardwareBuffer.create(VALID_WIDTH, 0, HardwareBuffer.BLOB, VALID_LAYERS, VALID_USAGE_FLAGS_O);
      fail("IllegalArgumentException should be thrown when creating a BLOB buffer with height 0.");
    } catch (IllegalArgumentException e) {
      // pass
    }

    try {
      HardwareBuffer.create(VALID_WIDTH, 2, HardwareBuffer.BLOB, VALID_LAYERS, VALID_USAGE_FLAGS_O);
      fail("IllegalArgumentException should be thrown when creating a BLOB buffer with height 2.");
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = O)
  public void createWithOFormatsAndFlagsSucceedsOnOAndLater() {
    for (int format : VALID_FORMATS_O) {
      int height = format == HardwareBuffer.BLOB ? 1 : VALID_HEIGHT;
      try (HardwareBuffer buffer =
          HardwareBuffer.create(VALID_WIDTH, height, format, VALID_LAYERS, VALID_USAGE_FLAGS_O)) {
        assertNotNull(buffer);
      }
    }
  }

  @Test
  @Config(minSdk = P)
  public void createWithPFormatsAndFlagsSucceedsOnPAndLater() {
    for (int format : VALID_FORMATS_P) {
      try (HardwareBuffer buffer =
          HardwareBuffer.create(
              VALID_WIDTH, VALID_HEIGHT, format, VALID_LAYERS, VALID_USAGE_FLAGS_P)) {
        assertNotNull(buffer);
      }
    }
  }

  @Test
  @Config(minSdk = P)
  public void createWithPFlagsSucceedsOnPAndLater() {
    try (HardwareBuffer buffer =
        HardwareBuffer.create(
            VALID_WIDTH,
            VALID_HEIGHT,
            HardwareBuffer.RGBA_8888,
            VALID_LAYERS,
            VALID_USAGE_FLAGS_P)) {
      assertNotNull(buffer);
    }
  }

  @Test
  @Config(minSdk = O)
  public void gettersOnHardwareBufferAreCorrect() {
    HardwareBuffer buffer =
        HardwareBuffer.create(
            VALID_WIDTH, VALID_HEIGHT, HardwareBuffer.RGBA_8888, VALID_LAYERS, VALID_USAGE_FLAGS_O);
    assertNotNull(buffer);
    assertEquals(VALID_WIDTH, buffer.getWidth());
    assertEquals(VALID_HEIGHT, buffer.getHeight());
    assertEquals(HardwareBuffer.RGBA_8888, buffer.getFormat());
    assertEquals(VALID_LAYERS, buffer.getLayers());
    assertEquals(VALID_USAGE_FLAGS_O, buffer.getUsage());
    buffer.close();
  }

  @Test
  @Config(minSdk = O)
  public void gettersOnClosedHardwareBufferThrows() {
    HardwareBuffer buffer =
        HardwareBuffer.create(
            VALID_WIDTH, VALID_HEIGHT, HardwareBuffer.RGBA_8888, VALID_LAYERS, VALID_USAGE_FLAGS_O);
    assertNotNull(buffer);
    buffer.close();
    assertTrue(buffer.isClosed());
    try {
      buffer.getWidth();
      fail("IllegalStateException should be thrown when accessing getWidth on a closed buffer.");
    } catch (IllegalStateException e) {
      // pass
    }
    try {
      buffer.getHeight();
      fail("IllegalStateException should be thrown when accessing getHeight on a closed buffer.");
    } catch (IllegalStateException e) {
      // pass
    }
    try {
      buffer.getFormat();
      fail("IllegalStateException should be thrown when accessing getWidth on a closed buffer.");
    } catch (IllegalStateException e) {
      // pass
    }
    try {
      buffer.getLayers();
      fail("IllegalStateException should be thrown when accessing getLayers on a closed buffer.");
    } catch (IllegalStateException e) {
      // pass
    }
    try {
      buffer.getLayers();
      fail("IllegalStateException should be thrown when accessing getLayers on a closed buffer.");
    } catch (IllegalStateException e) {
      // pass
    }
  }

  @Test
  @Config(minSdk = O)
  public void gettersOnParceledBufferAreCorrect() {
    HardwareBuffer buffer =
        HardwareBuffer.create(
            VALID_WIDTH, VALID_HEIGHT, HardwareBuffer.RGBA_8888, VALID_LAYERS, VALID_USAGE_FLAGS_O);
    assertNotNull(buffer);
    final Parcel parcel = Parcel.obtain();
    buffer.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);

    HardwareBuffer otherBuffer = HardwareBuffer.CREATOR.createFromParcel(parcel);
    assertEquals(VALID_WIDTH, otherBuffer.getWidth());
    assertEquals(VALID_HEIGHT, otherBuffer.getHeight());
    assertEquals(HardwareBuffer.RGBA_8888, otherBuffer.getFormat());
    assertEquals(VALID_LAYERS, otherBuffer.getLayers());
    assertEquals(VALID_USAGE_FLAGS_O, otherBuffer.getUsage());
    buffer.close();
    otherBuffer.close();
  }
}
