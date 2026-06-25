package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice.CameraDeviceSetup;
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
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow class for {@link CameraDeviceImpl} */
@Implements(value = CameraDeviceImpl.class, isInAndroidSdk = false)
public class ShadowCameraDeviceImpl {
  @RealObject private CameraDeviceImpl realObject;
  private boolean closed = false;

  @Filter(minSdk = VANILLA_ICE_CREAM, maxSdk = VANILLA_ICE_CREAM, order = Filter.Order.AFTER)
  protected void __constructor__(
      String cameraId,
      StateCallback callback,
      Executor executor,
      CameraCharacteristics characteristics,
      CameraManager cameraManager,
      int appTargetSdkVersion,
      Context ctx,
      CameraDeviceSetup cameraDeviceSetup) {
    // When singleThreadedDeviceExecutor flag is set, this gets put on a background thread.
    // This isn't necessary for Robolectric as there is no real camera, so we default back to the
    // given executor.
    reflector(CameraDeviceImplReflector.class, realObject)
        .setDeviceExecutor(MoreExecutors.directExecutor());
  }

  @Filter(minSdk = BAKLAVA, order = Filter.Order.AFTER)
  protected void __constructor__(
      String cameraId,
      StateCallback callback,
      Executor executor,
      CameraCharacteristics characteristics,
      CameraManager cameraManager,
      int appTargetSdkVersion,
      Context ctx,
      CameraDeviceSetup cameraDeviceSetup,
      boolean unused) {
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
    markClosed();
    closed = true;
  }

  void markClosed() {
    reflector(CameraDeviceImplReflector.class, realObject).getClosing().set(true);
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

    @Accessor("mClosing")
    AtomicBoolean getClosing();

    @Accessor("mDeviceExecutor")
    void setDeviceExecutor(Executor executor);
  }
}
