package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowCaptureRequestBuilder}. */
@Config(minSdk = VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class ShadowCaptureRequestBuilderTest {

  private static final String CAMERA_ID_0 = "cameraId0";

  private final CameraCharacteristics characteristics =
      ShadowCameraCharacteristics.newCameraCharacteristics();
  private final CameraManager cameraManager =
      (CameraManager)
          ApplicationProvider.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
  private CameraDevice cameraDevice;
  private CaptureRequest.Builder builder;

  @Before
  public void setUp() throws CameraAccessException {
    shadowOf(cameraManager).addCamera(CAMERA_ID_0, characteristics);
    cameraManager.openCamera(CAMERA_ID_0, new CameraStateCallback(), new Handler());
    shadowOf(Looper.getMainLooper()).idle();

    builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW /* ignored */);
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void testGetAndSet() {
    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
    builder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_FAST);
    assertThat(builder.get(CaptureRequest.CONTROL_AF_MODE))
        .isEqualTo(CaptureRequest.CONTROL_AF_MODE_OFF);
    assertThat(builder.get(CaptureRequest.COLOR_CORRECTION_MODE))
        .isEqualTo(CaptureRequest.COLOR_CORRECTION_MODE_FAST);
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
