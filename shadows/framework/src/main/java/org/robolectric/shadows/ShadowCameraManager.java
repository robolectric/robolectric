package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION_CODES;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
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
