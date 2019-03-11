package org.robolectric.shadows;

import android.annotation.NonNull;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION_CODES;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = CameraManager.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowCameraManager {

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
}

