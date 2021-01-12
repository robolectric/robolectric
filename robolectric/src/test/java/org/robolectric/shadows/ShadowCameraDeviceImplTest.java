package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
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
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collections;
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
  @Config(minSdk = VERSION_CODES.LOLLIPOP, maxSdk = VERSION_CODES.Q)
  public void createCaptureRequest() throws CameraAccessException {
    builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
    CaptureRequest request = builder.build();
    assertThat(request).isNotNull();
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureRequest_throwsIllegalStateExceptionAfterClose()
      throws CameraAccessException {
    cameraDevice.close();

    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD));
    assertThat(thrown).hasMessageThat().contains("CameraDevice was already closed");
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureSession() throws CameraAccessException {
    builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
    cameraDevice.createCaptureSession(
        new ArrayList<>(), new CaptureSessionCallback(/*useExecutor=*/ false), new Handler());
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureSession_configuration() throws CameraAccessException {
    Surface mockSurface = mock(Surface.class);
    builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
    builder.addTarget(mockSurface);
    SessionConfiguration configuration =
        new SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            Collections.singletonList(new OutputConfiguration(mockSurface)),
            MoreExecutors.directExecutor(),
            new CaptureSessionCallback(/*useExecutor=*/ true));
    cameraDevice.createCaptureSession(configuration);
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureSession_throwsIllegalStateExceptionAfterClose()
      throws CameraAccessException {
    cameraDevice.close();

    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                cameraDevice.createCaptureSession(
                    new ArrayList<>(),
                    new CaptureSessionCallback(/*useExecutor=*/ false),
                    new Handler()));
    assertThat(thrown).hasMessageThat().contains("CameraDevice was already closed");
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void createCaptureSession_configuration_throwsIllegalStateExceptionAfterClose()
      throws CameraAccessException {
    cameraDevice.close();

    SessionConfiguration configuration =
        new SessionConfiguration(
            SessionConfiguration.SESSION_REGULAR,
            Collections.singletonList(new OutputConfiguration(mock(Surface.class))),
            MoreExecutors.directExecutor(),
            new CaptureSessionCallback(/*useExecutor=*/ true));
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class, () -> cameraDevice.createCaptureSession(configuration));
    assertThat(thrown).hasMessageThat().contains("CameraDevice was already closed");
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
    private final boolean useExecutor;

    /**
     * Creates a capture session callback that tests capture methods.
     *
     * @param useExecutor if true will test the Executor flavor of capture methods, otherwise will
     *     test the Handler flavor.
     */
    public CaptureSessionCallback(boolean useExecutor) {
      this.useExecutor = useExecutor;
    }

    @Override
    public void onConfigured(CameraCaptureSession cameraCaptureSession) {
      captureSession = cameraCaptureSession;
      assertThat(captureSession.getDevice().getId()).isEqualTo(CAMERA_ID_0);

      CaptureCallback captureCallback =
          new CaptureCallback() {
            @Override
            public void onCaptureCompleted(
                CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {}
          };

      try {
        final int repeatingResponse;
        if (useExecutor) {
          repeatingResponse =
              captureSession.setSingleRepeatingRequest(
                  builder.build(), MoreExecutors.directExecutor(), captureCallback);
        } else {
          repeatingResponse =
              captureSession.setRepeatingRequest(builder.build(), captureCallback, new Handler());
        }
        assertThat(repeatingResponse).isEqualTo(1);

        final int captureResponse;
        if (useExecutor) {
          captureResponse =
              captureSession.captureSingleRequest(
                  builder.build(), MoreExecutors.directExecutor(), captureCallback);
        } else {
          captureResponse = captureSession.capture(builder.build(), captureCallback, new Handler());
        }
        assertThat(captureResponse).isEqualTo(1);
      } catch (CameraAccessException e) {
        throw new AssertionError("Got CameraAccessException when testing onConfigured", e);
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
