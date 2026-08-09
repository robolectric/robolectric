package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.util.ReflectionHelpers;

/** Tests for {@link ShadowCameraManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowCameraManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private static final String CAMERA_ID_0 = "cameraId0";
  private static final String CAMERA_ID_1 = "cameraId1";

  private static final boolean ENABLE = true;

  private static final int MAXIMUM_STRENGTH_LEVEL = 5;
  private static final int DEFAULT_STRENGTH_LEVEL = 2;

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
  public void openCamera() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    CameraDevice.StateCallback mockCallback = mock(CameraDevice.StateCallback.class);
    cameraManager.openCamera(CAMERA_ID_0, mockCallback, new Handler());
    shadowOf(Looper.myLooper()).idle();
    verify(mockCallback).onOpened(any(CameraDevice.class));
  }

  @Test
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
  public void triggerDisconnect_noCameraOpen() {
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
    cameraManager.openCamera(CAMERA_ID_0, mockCallback, new Handler(Looper.myLooper()));
    shadowOf(Looper.myLooper()).idle();
    ArgumentCaptor<CameraDevice> cameraDeviceCaptor = ArgumentCaptor.forClass(CameraDevice.class);
    verify(mockCallback).onOpened(cameraDeviceCaptor.capture());
    ShadowCameraManager.reset();
    boolean isClosed =
        ReflectionHelpers.callInstanceMethod(cameraDeviceCaptor.getValue(), "isClosed");
    assertThat(isClosed).isTrue();
    shadowOf(Looper.myLooper()).idle();
    // Verify that the closed callback is not called by the resetter, in case the executor is
    // already closed.
    verify(mockCallback, never()).onClosed(cameraDeviceCaptor.getValue());
  }

  @Test
  public void registerCallbackAvailable() {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);
    // Verify adding the camera triggers the callback
    cameraManager.registerAvailabilityCallback(mockCallback, /* handler= */ null);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    verify(mockCallback).onCameraAvailable(CAMERA_ID_0);
  }

  @Test
  public void unregisterCallbackAvailable() {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);
    cameraManager.registerAvailabilityCallback(mockCallback, /* handler= */ null);
    cameraManager.unregisterAvailabilityCallback(mockCallback);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    verify(mockCallback, never()).onCameraAvailable(CAMERA_ID_0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void registerCallbackAvailable_withExecutor() {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);
    Executor executor = Runnable::run;

    cameraManager.registerAvailabilityCallback(executor, mockCallback);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    verify(mockCallback).onCameraAvailable(CAMERA_ID_0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void unregisterCallbackAvailable_withExecutor() {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);
    Executor executor = Runnable::run;

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);

    cameraManager.registerAvailabilityCallback(executor, mockCallback);
    cameraManager.unregisterAvailabilityCallback(mockCallback);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    verify(mockCallback, never()).onCameraAvailable(CAMERA_ID_0);
  }

  @Test
  public void registerCallbackUnavailable() {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);

    // Verify that the camera unavailable callback is called when the camera is removed
    cameraManager.registerAvailabilityCallback(mockCallback, /* handler= */ null);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);

    verify(mockCallback).onCameraUnavailable(CAMERA_ID_0);
  }

  @Test
  public void unregisterCallbackUnavailable() {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);

    cameraManager.registerAvailabilityCallback(mockCallback, /* handler= */ null);
    cameraManager.unregisterAvailabilityCallback(mockCallback);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);

    verify(mockCallback, never()).onCameraUnavailable(CAMERA_ID_0);
  }

  @Test
  public void registerCallbackUnavailableInvalidCameraId() {
    CameraManager.AvailabilityCallback mockCallback =
        mock(CameraManager.AvailabilityCallback.class);

    // Verify that the callback is not triggered for a camera that was never added
    cameraManager.registerAvailabilityCallback(mockCallback, /* handler= */ null);
    try {
      shadowOf(cameraManager).removeCamera(CAMERA_ID_0);
    } catch (IllegalArgumentException e) {
      // Expected path for a bad cameraId
    }

    verify(mockCallback, never()).onCameraUnavailable(CAMERA_ID_0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void registerTorchCallbackEnabled() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);

    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    boolean torchEnabled = true;
    cameraManager.setTorchMode(CAMERA_ID_0, torchEnabled);

    verify(mockCallback).onTorchModeChanged(CAMERA_ID_0, torchEnabled);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void unregisterTorchCallbackEnabled() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);
    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    cameraManager.unregisterTorchCallback(mockCallback);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    boolean torchEnabled = true;
    cameraManager.setTorchMode(CAMERA_ID_0, torchEnabled);

    verify(mockCallback, never()).onTorchModeChanged(CAMERA_ID_0, torchEnabled);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void registerTorchCallbackDisabled() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);

    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    boolean torchEnabled = false;
    cameraManager.setTorchMode(CAMERA_ID_0, torchEnabled);

    verify(mockCallback).onTorchModeChanged(CAMERA_ID_0, torchEnabled);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void unregisterTorchCallbackDisabled() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);

    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    cameraManager.unregisterTorchCallback(mockCallback);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    boolean torchEnabled = false;
    cameraManager.setTorchMode(CAMERA_ID_0, torchEnabled);

    verify(mockCallback, never()).onTorchModeChanged(CAMERA_ID_0, torchEnabled);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void registerTorchCallbackInvalidCameraId() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);

    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);

    boolean torchEnabled = true;
    try {
      cameraManager.setTorchMode(CAMERA_ID_0, torchEnabled);
    } catch (IllegalArgumentException e) {
      // Expected path for a bad cameraId
    }

    verify(mockCallback, never()).onTorchModeChanged(CAMERA_ID_0, torchEnabled);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void registerTorchCallbackWithExecutor() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);
    Executor executor = Runnable::run;

    cameraManager.registerTorchCallback(executor, mockCallback);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    cameraManager.setTorchMode(CAMERA_ID_0, ENABLE);

    verify(mockCallback).onTorchModeChanged(CAMERA_ID_0, ENABLE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void unregisterTorchCallbackRegisteredWithExecutor() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);
    Executor executor = Runnable::run;

    cameraManager.registerTorchCallback(executor, mockCallback);
    cameraManager.unregisterTorchCallback(mockCallback);

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    cameraManager.setTorchMode(CAMERA_ID_0, ENABLE);

    verify(mockCallback, never()).onTorchModeChanged(CAMERA_ID_0, ENABLE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void getTorchStrengthLevelReturnsDefaultLevel() throws CameraAccessException {
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL, MAXIMUM_STRENGTH_LEVEL);
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL, DEFAULT_STRENGTH_LEVEL);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    assertThat(cameraManager.getTorchStrengthLevel(CAMERA_ID_0)).isEqualTo(DEFAULT_STRENGTH_LEVEL);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void getTorchStrengthLevelWithoutDefaultLevelCharacteristic()
      throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    assertThat(cameraManager.getTorchStrengthLevel(CAMERA_ID_0)).isEqualTo(1);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void turnOnTorchWithStrengthLevelTurnsTorchOn() throws CameraAccessException {
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL, MAXIMUM_STRENGTH_LEVEL);
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL, DEFAULT_STRENGTH_LEVEL);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 3);

    assertThat(shadowOf(cameraManager).getTorchMode(CAMERA_ID_0)).isTrue();
    assertThat(cameraManager.getTorchStrengthLevel(CAMERA_ID_0)).isEqualTo(3);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void turnOnTorchWithStrengthLevelNotifiesTorchModeChanged() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL, MAXIMUM_STRENGTH_LEVEL);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 3);

    verify(mockCallback).onTorchModeChanged(CAMERA_ID_0, true);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void turnOnTorchWithStrengthLevelWhileTorchOnNotifiesTorchStrengthLevelChanged()
      throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL, MAXIMUM_STRENGTH_LEVEL);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 3);
    cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 4);

    verify(mockCallback).onTorchStrengthLevelChanged(CAMERA_ID_0, 4);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void setTorchModeResetsTorchStrengthLevelToDefault() throws CameraAccessException {
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL, MAXIMUM_STRENGTH_LEVEL);
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL, DEFAULT_STRENGTH_LEVEL);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, MAXIMUM_STRENGTH_LEVEL);
    cameraManager.setTorchMode(CAMERA_ID_0, false);

    assertThat(cameraManager.getTorchStrengthLevel(CAMERA_ID_0)).isEqualTo(DEFAULT_STRENGTH_LEVEL);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void turnOnTorchWithStrengthLevelStrengthOutOfRange() throws CameraAccessException {
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL, MAXIMUM_STRENGTH_LEVEL);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    try {
      cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }

    try {
      cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, MAXIMUM_STRENGTH_LEVEL + 1);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void turnOnTorchWithStrengthLevelInvalidCameraId() throws CameraAccessException {
    try {
      cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 1);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void registerTorchCallbackWithNullExecutor() {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);

    try {
      cameraManager.registerTorchCallback((Executor) null, mockCallback);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void turnOnTorchWithStrengthLevelWithoutFlashUnit() throws CameraAccessException {
    shadowOf(characteristics).set(CameraCharacteristics.FLASH_INFO_AVAILABLE, false);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    try {
      cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 1);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void getTorchStrengthLevelWithoutFlashUnit() throws CameraAccessException {
    shadowOf(characteristics).set(CameraCharacteristics.FLASH_INFO_AVAILABLE, false);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    try {
      cameraManager.getTorchStrengthLevel(CAMERA_ID_0);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void removeCameraResetsTorchStrengthLevel() throws CameraAccessException {
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL, MAXIMUM_STRENGTH_LEVEL);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, MAXIMUM_STRENGTH_LEVEL);

    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);
    shadowOf(cameraManager)
        .addCamera(CAMERA_ID_0, ShadowCameraCharacteristics.newCameraCharacteristics());

    assertThat(cameraManager.getTorchStrengthLevel(CAMERA_ID_0)).isEqualTo(1);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void removeCameraResetsTorchMode() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);
    shadowOf(characteristics)
        .set(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL, MAXIMUM_STRENGTH_LEVEL);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 3);

    shadowOf(cameraManager).removeCamera(CAMERA_ID_0);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    // The torch is no longer considered on, so turning it on again reports a mode change rather
    // than a strength level change.
    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    cameraManager.turnOnTorchWithStrengthLevel(CAMERA_ID_0, 2);

    verify(mockCallback).onTorchModeChanged(CAMERA_ID_0, true);
    verify(mockCallback, never()).onTorchStrengthLevelChanged(CAMERA_ID_0, 2);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void setTorchModeWhileCameraInUse() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    cameraManager.openCamera(CAMERA_ID_0, mock(CameraDevice.StateCallback.class), new Handler());
    shadowOf(Looper.myLooper()).idle();

    try {
      cameraManager.setTorchMode(CAMERA_ID_0, ENABLE);
      fail();
    } catch (CameraAccessException e) {
      assertThat(e.getReason()).isEqualTo(CameraAccessException.CAMERA_IN_USE);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void closeCameraDeviceNotifiesTorchModeChangedOff() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);

    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    CameraDevice.StateCallback stateCallback = mock(CameraDevice.StateCallback.class);
    cameraManager.openCamera(CAMERA_ID_0, stateCallback, new Handler());
    shadowOf(Looper.myLooper()).idle();
    ArgumentCaptor<CameraDevice> deviceCaptor = ArgumentCaptor.forClass(CameraDevice.class);
    verify(stateCallback).onOpened(deviceCaptor.capture());

    deviceCaptor.getValue().close();
    shadowOf(Looper.myLooper()).idle();

    verify(mockCallback).onTorchModeChanged(CAMERA_ID_0, false);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void setTorchModeAfterCameraDeviceClosed() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    CameraDevice.StateCallback stateCallback = mock(CameraDevice.StateCallback.class);
    cameraManager.openCamera(CAMERA_ID_0, stateCallback, new Handler());
    shadowOf(Looper.myLooper()).idle();
    ArgumentCaptor<CameraDevice> deviceCaptor = ArgumentCaptor.forClass(CameraDevice.class);
    verify(stateCallback).onOpened(deviceCaptor.capture());

    deviceCaptor.getValue().close();
    shadowOf(Looper.myLooper()).idle();

    cameraManager.setTorchMode(CAMERA_ID_0, ENABLE);

    assertThat(shadowOf(cameraManager).getTorchMode(CAMERA_ID_0)).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void openCameraNotifiesTorchModeUnavailable() throws CameraAccessException {
    CameraManager.TorchCallback mockCallback = mock(CameraManager.TorchCallback.class);

    cameraManager.registerTorchCallback(mockCallback, /* handler= */ null);
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    cameraManager.openCamera(CAMERA_ID_0, mock(CameraDevice.StateCallback.class), new Handler());
    shadowOf(Looper.myLooper()).idle();

    verify(mockCallback).onTorchModeUnavailable(CAMERA_ID_0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void cameraManager_activityContextEnabled_differentInstancesRetrieveCameraIdList()
      throws Exception {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      CameraManager applicationCameraManager =
          (CameraManager)
              ApplicationProvider.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
      Activity activity = controller.get();
      CameraManager activityCameraManager =
          (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

      assertThat(applicationCameraManager).isNotSameInstanceAs(activityCameraManager);

      CameraCharacteristics characteristics =
          ShadowCameraCharacteristics.newCameraCharacteristics();
      shadowOf(applicationCameraManager).addCamera(CAMERA_ID_0, characteristics);
      shadowOf(activityCameraManager).addCamera(CAMERA_ID_1, characteristics);

      String[] applicationCameraIdList = applicationCameraManager.getCameraIdList();
      String[] activityCameraIdList = activityCameraManager.getCameraIdList();

      assertThat(activityCameraIdList.length).isEqualTo(2);
      assertThat(activityCameraIdList[0]).isEqualTo(CAMERA_ID_0);
      assertThat(activityCameraIdList[1]).isEqualTo(CAMERA_ID_1);

      assertThat(activityCameraIdList).isEqualTo(applicationCameraIdList);
    }
  }

  @Config(minSdk = VERSION_CODES.P)
  @Test
  public void reset_withClosedCallbackExecutor_doesNotThrow() throws Exception {

    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);

    ExecutorService singletonExecutor = Executors.newSingleThreadExecutor();
    shadowOf(Looper.myLooper()).idle();
    CameraDevice.StateCallback mockCallback = mock(CameraDevice.StateCallback.class);

    cameraManager.openCamera(CAMERA_ID_0, singletonExecutor, mockCallback);

    singletonExecutor.shutdown();
    singletonExecutor.awaitTermination(1, SECONDS);

    ShadowCameraManager.reset();
  }
}
