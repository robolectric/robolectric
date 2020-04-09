package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowCameraDeviceImpl}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public final class ShadowCameraDeviceImplTest {

  private static final String CAMERA_ID_0 = "cameraId0";
  private final CameraManager cameraManager =
      (CameraManager)
          ApplicationProvider.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);

  private final CameraCharacteristics characteristics =
      ShadowCameraCharacteristics.newCameraCharacteristics();
  private CameraDevice cameraDevice;
  private CameraCaptureSession captureSession;
  private CaptureRequest.Builder builder;

  @Before
  public void setUp() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    cameraManager.openCamera(CAMERA_ID_0, new CameraStateCallback(), new Handler());
    shadowOf(Looper.getMainLooper()).idle();
  }

  @After
  public void tearDown() throws CameraAccessException {
    cameraDevice.close();
    if (captureSession != null) {
      captureSession.close();
    }
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureRequest() throws CameraAccessException {
    builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
    CaptureRequest request = builder.build();
    assertThat(request.getLogicalCameraId()).isEqualTo(CAMERA_ID_0);
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureSession() throws CameraAccessException {
    builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
    cameraDevice.createCaptureSession(
        new ArrayList<Surface>(), new CaptureSessionCallback(), new Handler());
  }

  private class CaptureSessionCallback extends CameraCaptureSession.StateCallback {
    @Override
    public void onConfigured(CameraCaptureSession cameraCaptureSession) {

      captureSession = cameraCaptureSession;
      assertThat(captureSession.getDevice().getId()).isEqualTo(CAMERA_ID_0);

      try {
        int response =
            captureSession.setRepeatingRequest(
                builder.build(),
                new CaptureCallback() {
                  @Override
                  public void onCaptureCompleted(
                      CameraCaptureSession session,
                      CaptureRequest request,
                      TotalCaptureResult result) {}
                },
                new Handler());
        assertThat(response).isEqualTo(1);

        response =
            captureSession.capture(
                builder.build(),
                new CaptureCallback() {
                  @Override
                  public void onCaptureCompleted(
                      CameraCaptureSession session,
                      CaptureRequest request,
                      TotalCaptureResult result) {}
                },
                new Handler());
        assertThat(response).isEqualTo(1);
      } catch (CameraAccessException e) {
        fail();
      }
    }

    @Override
    public void onClosed(CameraCaptureSession session) {
      assertThat(session.getDevice().getId()).isEqualTo(CAMERA_ID_0);
    }

    @Override
    public void onConfigureFailed(final CameraCaptureSession cameraCaptureSession) {
      fail();
    }
  }

  private class CameraStateCallback extends CameraDevice.StateCallback {

    @Override
    public void onOpened(CameraDevice camera) {
      cameraDevice = camera;
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
      fail();
    }

    @Override
    public void onError(CameraDevice camera, int error) {
      fail();
    }
  }
}
