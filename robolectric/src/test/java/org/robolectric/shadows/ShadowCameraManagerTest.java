package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowCameraManager}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class ShadowCameraManagerTest {

  private static final String CAMERA_ID_0 = "cameraId0";
  private static final String CAMERA_ID_1 = "cameraId1";

  private static final boolean ENABLE = true;

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

    assertThat(cameraManager.getCameraCharacteristics(CAMERA_ID_0))
        .isSameInstanceAs(characteristics);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testSetTorchModeInvalidCameraId() throws CameraAccessException {
    try {
      cameraManager.setTorchMode(CAMERA_ID_0, ENABLE);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testGetTorchModeNullCameraId() {
    try {
      shadowOf(cameraManager).getTorchMode(null);
      fail();
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testGetTorchModeInvalidCameraId() {
    try {
      shadowOf(cameraManager).getTorchMode(CAMERA_ID_0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testGetTorchModeCameraTorchModeNotSet() {
    try {
      shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
      shadowOf(cameraManager).getTorchMode(CAMERA_ID_0);
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void testGetTorchModeCameraTorchModeSet() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    cameraManager.setTorchMode(CAMERA_ID_0, ENABLE);
    assertThat(shadowOf(cameraManager).getTorchMode(CAMERA_ID_0)).isEqualTo(ENABLE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void openCamera() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    CameraDevice.StateCallback mockCallback = mock(CameraDevice.StateCallback.class);
    cameraManager.openCamera(CAMERA_ID_0, mockCallback, new Handler());
    shadowOf(Looper.myLooper()).idle();
    verify(mockCallback).onOpened(any(CameraDevice.class));
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void triggerDisconnect() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    CameraDevice.StateCallback mockCallback = mock(CameraDevice.StateCallback.class);
    cameraManager.openCamera(CAMERA_ID_0, mockCallback, new Handler());
    shadowOf(Looper.myLooper()).idle();
    ArgumentCaptor<CameraDevice> deviceCaptor = ArgumentCaptor.forClass(CameraDevice.class);
    verify(mockCallback).onOpened(deviceCaptor.capture());
    verify(mockCallback, never()).onDisconnected(any(CameraDevice.class));

    shadowOf(cameraManager).triggerDisconnect();
    shadowOf(Looper.myLooper()).idle();
    verify(mockCallback).onDisconnected(deviceCaptor.getValue());
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void triggerDisconnect_noCameraOpen() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).triggerDisconnect();
    // Nothing should happen - just make sure we don't crash.
  }

  @Test
  public void testRemoveCameraNullCameraId() {
    try {
      shadowOf(cameraManager).removeCamera(null);
      fail();
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testRemoveCameraNoExistingId() {
    try {
      shadowOf(cameraManager).removeCamera(CAMERA_ID_0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testRemoveCameraAddCameraSucceedsAfterwards() {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);

    // Repeated call to add CAMERA_ID_0 succeeds and does not throw IllegalArgumentException.
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
  }

  @Test
  public void testRemoveCameraRemovedCameraIsNotInCameraIdList() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).addCamera(CAMERA_ID_1, characteristics);

    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);

    assertThat(cameraManager.getCameraIdList()).hasLength(1);
    assertThat(cameraManager.getCameraIdList()[0]).isEqualTo(CAMERA_ID_1);
  }

  @Test
  public void resetter_closesCameras() throws Exception {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    CameraDevice.StateCallback mockCallback = mock(CameraDevice.StateCallback.class);
    cameraManager.openCamera(CAMERA_ID_0, mockCallback, new Handler());
    shadowOf(Looper.myLooper()).idle();
    ArgumentCaptor<CameraDevice> cameraDeviceCaptor = ArgumentCaptor.forClass(CameraDevice.class);
    verify(mockCallback).onOpened(cameraDeviceCaptor.capture());
    ShadowCameraManager.reset();
    shadowOf(Looper.myLooper()).idle();
    verify(mockCallback).onClosed(cameraDeviceCaptor.getValue());
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void registerCallbackAvailable() throws CameraAccessException {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);
    // Verify adding the camera triggers the callback
    cameraManager.registerAvailabilityCallback(mockCallback, /* handler = */ null);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    verify(mockCallback).onCameraAvailable(CAMERA_ID_0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void unregisterCallbackAvailable() throws CameraAccessException {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);
    cameraManager.registerAvailabilityCallback(mockCallback, /* handler = */ null);
    cameraManager.unregisterAvailabilityCallback(mockCallback);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    verify(mockCallback, never()).onCameraAvailable(CAMERA_ID_0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void registerCallbackUnavailable() throws CameraAccessException {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);

    // Verify that the camera unavailable callback is called when the camera is removed
    cameraManager.registerAvailabilityCallback(mockCallback, /* handler = */ null);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);

    verify(mockCallback).onCameraUnavailable(CAMERA_ID_0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void unregisterCallbackUnavailable() throws CameraAccessException {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);

    cameraManager.registerAvailabilityCallback(mockCallback, /* handler = */ null);
    cameraManager.unregisterAvailabilityCallback(mockCallback);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);

    verify(mockCallback, never()).onCameraUnavailable(CAMERA_ID_0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void registerCallbackUnavailableInvalidCameraId() throws CameraAccessException {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);

    // Verify that the callback is not triggered for a camera that was never added
    cameraManager.registerAvailabilityCallback(mockCallback, /* handler = */ null);
    try {
      shadowOf(cameraManager).removeCamera(CAMERA_ID_0);
    } catch (IllegalArgumentException e) {
      // Expected path for a bad cameraId
    }

    verify(mockCallback, never()).onCameraUnavailable(CAMERA_ID_0);
  }
}
