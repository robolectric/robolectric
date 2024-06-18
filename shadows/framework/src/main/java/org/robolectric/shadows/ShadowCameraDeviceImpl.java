package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.impl.CameraCaptureSessionImpl;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.view.Surface;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.InDevelopment;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow class for {@link CameraDeviceImpl} */
@Implements(value = CameraDeviceImpl.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowCameraDeviceImpl {
  @RealObject private CameraDeviceImpl realObject;
  private boolean closed = false;

  @Implementation(minSdk = V.SDK_INT)
  @InDevelopment
  protected void __constructor__(
      Object cameraId,
      Object callback,
      Object executor,
      Object characteristics,
      Object cameraManager,
      Object appTargetSdkVersion,
      Object ctx,
      Object cameraDeviceSetup) {
    try {
      reflector(CameraDeviceImplReflector.class, realObject)
          .__constructor__(
              (String) cameraId,
              (StateCallback) callback,
              (Executor) executor,
              (CameraCharacteristics) characteristics,
              (CameraManager) cameraManager,
              (int) appTargetSdkVersion,
              (Context) ctx,
              // TODO(juliansull) Remove once Robolectric compiles against Android V
              Class.forName("android.hardware.camera2.CameraDevice$CameraDeviceSetup")
                  .cast(cameraDeviceSetup));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    // When singleThreadedDeviceExecutor flag is set, this gets put on a background thread.
    // This isn't necessary for Robolectric as there is no real camera, so we default back to the
    // given executor.
    reflector(CameraDeviceImplReflector.class, realObject)
        .setDeviceExecutor(MoreExecutors.directExecutor());
  }

  @Implementation
  protected CaptureRequest.Builder createCaptureRequest(int templateType) {
    checkIfCameraClosedOrInError();
    CameraMetadataNative templatedRequest = new CameraMetadataNative();
    String cameraId = ReflectionHelpers.getField(realObject, "mCameraId");
    final CaptureRequest.Builder builder;
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      builder =
          new CaptureRequest.Builder(
              templatedRequest, /*reprocess*/
              false,
              CameraCaptureSession.SESSION_ID_NONE,
              cameraId, /*physicalCameraIdSet*/
              null);
    } else if (VERSION.SDK_INT >= VERSION_CODES.M) {
      builder =
          ReflectionHelpers.callConstructor(
              CaptureRequest.Builder.class,
              ReflectionHelpers.ClassParameter.from(CameraMetadataNative.class, templatedRequest),
              ReflectionHelpers.ClassParameter.from(Boolean.TYPE, false),
              ReflectionHelpers.ClassParameter.from(
                  Integer.TYPE, CameraCaptureSession.SESSION_ID_NONE));
    } else {
      builder =
          ReflectionHelpers.callConstructor(
              CaptureRequest.Builder.class,
              ReflectionHelpers.ClassParameter.from(CameraMetadataNative.class, templatedRequest));
    }
    return builder;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected void createCaptureSession(
      List<Surface> outputs, CameraCaptureSession.StateCallback callback, Handler handler)
      throws CameraAccessException {
    checkIfCameraClosedOrInError();
    CameraCaptureSession session = createCameraCaptureSession(callback);
    handler.post(() -> callback.onConfigured(session));
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected void createCaptureSession(SessionConfiguration config) throws CameraAccessException {
    checkIfCameraClosedOrInError();
    CameraCaptureSession session = createCameraCaptureSession(config.getStateCallback());
    config.getExecutor().execute(() -> config.getStateCallback().onConfigured(session));
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

  private CameraCaptureSession createCameraCaptureSession(
      CameraCaptureSession.StateCallback callback) {
    CameraCaptureSession sess = Shadow.newInstanceOf(CameraCaptureSessionImpl.class);
    ReflectionHelpers.setField(CameraCaptureSessionImpl.class, sess, "mStateCallback", callback);
    ReflectionHelpers.setField(CameraCaptureSessionImpl.class, sess, "mDeviceImpl", realObject);
    return sess;
  }

  @ForType(CameraDeviceImpl.class)
  interface CameraDeviceImplReflector {
    @Direct
    void __constructor__(
        String cameraId,
        StateCallback callback,
        Executor executor,
        CameraCharacteristics characteristics,
        CameraManager cameraManager,
        int appTargetSdkVersion,
        Context ctx,
        @WithType("android.hardware.camera2.CameraDevice$CameraDeviceSetup")
            Object cameraDeviceSetup);

    @Accessor("mDeviceExecutor")
    void setDeviceExecutor(Executor executor);
  }
}
