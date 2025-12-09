package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.hardware.camera2.CaptureResult;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowCaptureResult}. */
@RunWith(AndroidJUnit4.class)
public class ShadowCaptureResultTest {

  private final CaptureResult.Key<Long> timestampKey = CaptureResult.SENSOR_TIMESTAMP;
  private final CaptureResult captureResult = ShadowCaptureResult.newCaptureResult();

  @Test
  public void testSetExistingKey_updatesValue() {
    shadowOf(captureResult).set(timestampKey, 1L);
    shadowOf(captureResult).set(timestampKey, 2L);
    assertThat(captureResult.get(timestampKey)).isEqualTo(2L);
  }

  @Test
  public void testGetUnrecognizedKey() {
    assertThat(captureResult.get(timestampKey)).isNull();
  }

  @Test
  public void testGetRecognizedKey() {
    shadowOf(captureResult).set(timestampKey, 1L);
    assertThat(captureResult.get(timestampKey)).isEqualTo(1L);
  }
}
