package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.impl.CameraMetadataNative;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowCameraCharacteristics}. */
@RunWith(AndroidJUnit4.class)
public class ShadowCameraCharacteristicsTest {

  private final CameraCharacteristics.Key<Integer> key0 =
      new CameraCharacteristics.Key<>("key0", Integer.class);
  private final CameraCharacteristics cameraCharacteristics =
      ShadowCameraCharacteristics.newCameraCharacteristics();

  @Test
  public void testSetExistingKey() {
    shadowOf(cameraCharacteristics).set(key0, 1);
    shadowOf(cameraCharacteristics).set(key0, 2);
    assertThat(cameraCharacteristics.get(key0)).isEqualTo(2);
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

  @Test
  public void getNativeCopy_doesNotNPE() {
    CameraMetadataNative nativeCopy = cameraCharacteristics.getNativeCopy();
    assertThat(nativeCopy).isNotNull();
  }

  @Test
  public void setKey_intArray() {
    int[] capabilities = new int[] {1, 2, 3};
    shadowOf(cameraCharacteristics)
        .set(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, capabilities);
    assertThat(cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES))
        .isEqualTo(capabilities);
  }
}
