package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowCameraManager}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class ShadowCameraManagerTest {

  private static final String CAMERA_ID_0 = "cameraId0";
  private static final String CAMERA_ID_1 = "cameraId1";

  private final CameraManager cameraManager =
      (CameraManager)
          ApplicationProvider.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);

  private final CameraCharacteristics characteristics =
      ShadowCameraCharacteristics.newCameraCharacteristics();

  @Test
  public void testAddCameraNullCameraId() {
    try {
      shadowOf(cameraManager).addCamera(null, characteristics);
      fail();
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testAddCameraNullCharacteristics() {
    try {
      shadowOf(cameraManager).addCamera(CAMERA_ID_0, null);
      fail();
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testAddCameraExistingId() {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    try {
      shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testGetCameraIdListNoCameras() throws CameraAccessException {
    assertThat(cameraManager.getCameraIdList()).isEmpty();
  }

  @Test
  public void testGetCameraIdListSingleCamera() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    assertThat(cameraManager.getCameraIdList()).asList().containsExactly(CAMERA_ID_0);
  }

  @Test
  public void testGetCameraIdListInOrderOfAdd() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).addCamera(CAMERA_ID_1, characteristics);

    assertThat(cameraManager.getCameraIdList()[0]).isEqualTo(CAMERA_ID_0);
    assertThat(cameraManager.getCameraIdList()[1]).isEqualTo(CAMERA_ID_1);
  }

  @Test
  public void testGetCameraCharacteristicsNullCameraId() throws CameraAccessException {
    try {
      cameraManager.getCameraCharacteristics(null);
      fail();
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testGetCameraCharacteristicsUnrecognizedCameraId() throws CameraAccessException {
    try {
      cameraManager.getCameraCharacteristics(CAMERA_ID_0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testGetCameraCharacteristicsRecognizedCameraId() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    assertThat(cameraManager.getCameraCharacteristics(CAMERA_ID_0)).isSameAs(characteristics);
  }
}
