package org.robolectric.shadows;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraCaptureSessionImpl;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.view.Surface;
import java.util.List;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Shadow class for {@link CameraDeviceImpl} */
@Implements(value = CameraDeviceImpl.class, minSdk = VERSION_CODES.LOLLIPOP, isInAndroidSdk = false)
public class ShadowCameraDeviceImpl {
  @RealObject private CameraDeviceImpl realObject;
  private boolean closed = false;

  @Implementation
  protected CaptureRequest.Builder createCaptureRequest(int templateType) {
    checkIfCameraClosedOrInError();
    CameraMetadataNative templatedRequest = new CameraMetadataNative();
    String cameraId = ReflectionHelpers.getField(realObject, "mCameraId");

    CaptureRequest.Builder builder =
        new CaptureRequest.Builder(
            templatedRequest,
            /*reprocess*/ false,
            CameraCaptureSession.SESSION_ID_NONE,
            cameraId,
            /*physicalCameraIdSet*/ null);
    return builder;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected void createCaptureSession(
      List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler)
      throws CameraAccessException {
    checkIfCameraClosedOrInError();
    CameraCaptureSession sess = Shadow.newInstanceOf(CameraCaptureSessionImpl.class);
    ReflectionHelpers.setField(CameraCaptureSessionImpl.class, sess, "mStateCallback", callback);
    ReflectionHelpers.setField(CameraCaptureSessionImpl.class, sess, "mDeviceImpl", realObject);
    handler.post(() -> callback.onConfigured(sess));
  }

  @Implementation
  protected void close() {
    if (!closed) {
      Runnable callOnClosed = ReflectionHelpers.getField(realObject, "mCallOnClosed");
      if (VERSION.SDK_INT >= VERSION_CODES.P) {
        Executor deviceExecutor = ReflectionHelpers.getField(realObject, "mDeviceExecutor");
        deviceExecutor.execute(callOnClosed);
      } else {
        Handler deviceHandler = ReflectionHelpers.getField(realObject, "mDeviceHandler");
        deviceHandler.post(callOnClosed);
      }
    }

    closed = true;
  }

  @Implementation
  protected void checkIfCameraClosedOrInError() {
    if (closed) {
      throw new IllegalStateException("CameraDevice was already closed");
    }
  }
}
