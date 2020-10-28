package org.robolectric.shadows;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraCaptureSessionImpl;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/** Shadow class for {@link CameraCaptureSessionImpl} */
@Implements(
    value = CameraCaptureSessionImpl.class,
    minSdk = VERSION_CODES.LOLLIPOP,
    isInAndroidSdk = false)
public class ShadowCameraCaptureSessionImpl {
  @RealObject private CameraCaptureSessionImpl realObject;

  @Implementation(minSdk = VERSION_CODES.P)
  protected int setRepeatingRequest(
      CaptureRequest request, CaptureCallback callback, Handler handler)
      throws CameraAccessException {
    return 1;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected int setSingleRepeatingRequest(
      CaptureRequest request, Executor executor, CaptureCallback callback)
      throws CameraAccessException {
    return 1;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected int capture(CaptureRequest request, CaptureCallback callback, Handler handler)
      throws CameraAccessException {
    return 1;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected int captureSingleRequest(
      CaptureRequest request, Executor executor, CaptureCallback callback)
      throws CameraAccessException {
    return 1;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected void close() {
    CameraCaptureSession.StateCallback callback =
        ReflectionHelpers.getField(realObject, "mStateCallback");
    if (callback == null) {
      throw new IllegalArgumentException("blah");
    }
    callback.onClosed(realObject);
  }
}
