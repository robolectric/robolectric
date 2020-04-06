package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow class for {@link CameraManager} */
@Implements(value = CameraManager.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowCameraManager {
  @RealObject private CameraManager realObject;

  // LinkedHashMap used to ensure getCameraIdList returns ids in the order in which they were added
  private final Map<String, CameraCharacteristics> cameraIdToCharacteristics =
      new LinkedHashMap<>();
  private final Map<String, Boolean> cameraTorches = new HashMap<>();

  @Implementation
  @NonNull
  protected String[] getCameraIdList() throws CameraAccessException {
    Set<String> cameraIds = cameraIdToCharacteristics.keySet();
    return cameraIds.toArray(new String[0]);
  }

  @Implementation
  @NonNull
  protected CameraCharacteristics getCameraCharacteristics(@NonNull String cameraId) {
    Preconditions.checkNotNull(cameraId);
    CameraCharacteristics characteristics = cameraIdToCharacteristics.get(cameraId);
    Preconditions.checkArgument(characteristics != null);
    return characteristics;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected void setTorchMode(@NonNull String cameraId, boolean enabled) {
    Preconditions.checkNotNull(cameraId);
    Preconditions.checkArgument(cameraIdToCharacteristics.keySet().contains(cameraId));
    cameraTorches.put(cameraId, enabled);
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId, CameraDevice.StateCallback callback, Executor executor, final int uid)
      throws CameraAccessException {
    CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
    Context context = reflector(ReflectorCameraManager.class, realObject).getContext();

    android.hardware.camera2.impl.CameraDeviceImpl deviceImpl =
        new android.hardware.camera2.impl.CameraDeviceImpl(
            cameraId,
            callback,
            executor,
            characteristics,
            context.getApplicationInfo().targetSdkVersion);

    executor.execute(() -> callback.onOpened(deviceImpl));
    return deviceImpl;
  }

  @Implementation(minSdk = VERSION_CODES.N_MR1, maxSdk = VERSION_CODES.O_MR1)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId, CameraDevice.StateCallback callback, Handler handler, final int uid)
      throws CameraAccessException {
    CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
    Context context = reflector(ReflectorCameraManager.class, realObject).getContext();

    android.hardware.camera2.impl.CameraDeviceImpl deviceImpl;
    if (Build.VERSION.SDK_INT == VERSION_CODES.N_MR1) {
      deviceImpl =
          ReflectionHelpers.callConstructor(
              android.hardware.camera2.impl.CameraDeviceImpl.class,
              ClassParameter.from(String.class, cameraId),
              ClassParameter.from(CameraDevice.StateCallback.class, callback),
              ClassParameter.from(Handler.class, handler),
              ClassParameter.from(CameraCharacteristics.class, characteristics));
    } else {
      deviceImpl =
          ReflectionHelpers.callConstructor(
              android.hardware.camera2.impl.CameraDeviceImpl.class,
              ClassParameter.from(String.class, cameraId),
              ClassParameter.from(CameraDevice.StateCallback.class, callback),
              ClassParameter.from(Handler.class, handler),
              ClassParameter.from(CameraCharacteristics.class, characteristics),
              ClassParameter.from(int.class, context.getApplicationInfo().targetSdkVersion));
    }

    handler.post(() -> callback.onOpened(deviceImpl));
    return deviceImpl;
  }

  /**
   * Enables {@link CameraManager#openCamera(String, StateCallback, Handler)} to open a
   * {@link CameraDevice}.
   *
   * <p>If the provided cameraId exists, this will always post
   * {@link CameraDevice.StateCallback#onOpened(CameraDevice) to the provided {@link Handler}.
   * Unlike on real Android, this will not check if the camera has been disabled by device policy
   * and does not attempt to connect to the camera service, so
   * {@link CameraDevice.StateCallback#onError(CameraDevice, int)} and
   * {@link CameraDevice.StateCallback#onDisconnected(CameraDevice)} will not be triggered by
   * {@link CameraManager#openCamera(String, StateCallback, Handler)}.
   */
  @Implementation(minSdk = VERSION_CODES.LOLLIPOP, maxSdk = VERSION_CODES.N)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId, CameraDevice.StateCallback callback, Handler handler)
      throws CameraAccessException {
    CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);

    android.hardware.camera2.impl.CameraDeviceImpl deviceImpl =
        ReflectionHelpers.callConstructor(
            android.hardware.camera2.impl.CameraDeviceImpl.class,
            ClassParameter.from(String.class, cameraId),
            ClassParameter.from(CameraDevice.StateCallback.class, callback),
            ClassParameter.from(Handler.class, handler),
            ClassParameter.from(CameraCharacteristics.class, characteristics));

    handler.post(() -> callback.onOpened(deviceImpl));
    return deviceImpl;
  }

  /**
   * Adds the given cameraId and characteristics to this shadow.
   *
   * <p>The result from {@link #getCameraIdList()} will be in the order in which cameras were added.
   *
   * @throws IllegalArgumentException if there's already an existing camera with the given id.
   */
  public void addCamera(@NonNull String cameraId, @NonNull CameraCharacteristics characteristics) {
    Preconditions.checkNotNull(cameraId);
    Preconditions.checkNotNull(characteristics);
    Preconditions.checkArgument(!cameraIdToCharacteristics.containsKey(cameraId));

    cameraIdToCharacteristics.put(cameraId, characteristics);
  }

  /** Returns what the supplied camera's torch is set to. */
  public boolean getTorchMode(@NonNull String cameraId) {
    Preconditions.checkNotNull(cameraId);
    Preconditions.checkArgument(cameraIdToCharacteristics.keySet().contains(cameraId));
    Boolean torchState = cameraTorches.get(cameraId);
    return torchState;
  }

  /** Accessor interface for {@link CameraManager}'s internals. */
  @ForType(CameraManager.class)
  private interface ReflectorCameraManager {

    @Accessor("mContext")
    Context getContext();
  }

  /** Shadow class for internal class CameraManager$CameraManagerGlobal */
  @Implements(
      className = "android.hardware.camera2.CameraManager$CameraManagerGlobal",
      minSdk = VERSION_CODES.LOLLIPOP)
  public static class ShadowCameraManagerGlobal {
    /**
     * Cannot create a CameraService connection within Robolectric. Avoid endless reconnect loop.
     */
    @Implementation(minSdk = VERSION_CODES.N)
    protected void scheduleCameraServiceReconnectionLocked() {}
  }
}
