package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowCameraCharacteristics}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class ShadowCameraCharacteristicsTest {

  private final Key key0 = new Key("key0", Integer.class);
  private final CameraCharacteristics cameraCharacteristics =
      ShadowCameraCharacteristics.newCameraCharacteristics();

  @Test
  public void testSetExistingKey() {
    shadowOf(cameraCharacteristics).set(key0, 1);

    try {
      shadowOf(cameraCharacteristics).set(key0, 1);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testGetUnrecognizedKey() {
    assertThat(cameraCharacteristics.get(key0)).isNull();
  }

  @Test
  public void testGetRecognizedKey() {
    shadowOf(cameraCharacteristics).set(key0, 1);

    assertThat(cameraCharacteristics.get(key0)).isEqualTo(1);
  }
}
