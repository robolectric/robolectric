package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowCameraDeviceImpl}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public final class ShadowCameraDeviceImplTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private static final String CAMERA_ID_0 = "cameraId0";
  private final CameraManager cameraManager =
      (CameraManager)
          ApplicationProvider.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);

  private final CameraCharacteristics characteristics =
      ShadowCameraCharacteristics.newCameraCharacteristics();
  private CameraDevice cameraDevice;
  private CameraCaptureSession captureSession;
  private CaptureRequest.Builder builder;
  private CameraDevice.StateCallback stateCallback;

  @Before
  public void setUp() throws CameraAccessException {
    stateCallback = createMockCameraDeviceCallback();
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    cameraManager.openCamera(CAMERA_ID_0, stateCallback, new Handler());
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
  public void createCaptureRequest_throwsIllegalStateExceptionAfterClose()
      throws CameraAccessException {
    cameraDevice.close();

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("CameraDevice was already closed");
    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureSession() throws CameraAccessException {
    builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
    cameraDevice.createCaptureSession(
        new ArrayList<Surface>(), new CaptureSessionCallback(), new Handler());
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureSession_throwsIllegalStateExceptionAfterClose()
      throws CameraAccessException {
    cameraDevice.close();

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("CameraDevice was already closed");
    cameraDevice.createCaptureSession(
        new ArrayList<Surface>(), new CaptureSessionCallback(), new Handler());
  }

  @Test
  public void close() {
    cameraDevice.close();
    shadowOf(Looper.getMainLooper()).idle();
    verify(stateCallback).onClosed(eq(cameraDevice));
  }

  private CameraDevice.StateCallback createMockCameraDeviceCallback() {
    CameraDevice.StateCallback mockCallback = mock(CameraDevice.StateCallback.class);
    doAnswer(
            args -> {
              cameraDevice = args.getArgument(0);
              return null;
            })
        .when(mockCallback)
        .onOpened(any(CameraDevice.class));
    doAnswer(
            args -> {
              fail();
              return null;
            })
        .when(mockCallback)
        .onDisconnected(any(CameraDevice.class));
    doAnswer(
            args -> {
              fail();
              return null;
            })
        .when(mockCallback)
        .onError(any(CameraDevice.class), anyInt());

    return mockCallback;
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
}
