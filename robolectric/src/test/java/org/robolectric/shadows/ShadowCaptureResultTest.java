package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.hardware.camera2.CaptureResult;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowCaptureResult}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class ShadowCaptureResultTest {

  private final CaptureResult.Key<Long> timestampKey = CaptureResult.SENSOR_TIMESTAMP;
  private final CaptureResult captureResult = ShadowCaptureResult.newCaptureResult();

  @Test
  public void testSetExistingKey() {
    shadowOf(captureResult).set(timestampKey, 1L);
    try {
      shadowOf(captureResult).set(timestampKey, 2L);
      fail();
    } catch (IllegalArgumentException exception) {
      // Pass.
    }
  }

  @Test
  public void testGetUnrecongizedKey() {
    assertThat(captureResult.get(timestampKey)).isNull();
  }

  @Test
  public void testGetRecognizedKey() {
    shadowOf(captureResult).set(timestampKey, 1L);
    assertThat(captureResult.get(timestampKey)).isEqualTo(1L);
  }
}
