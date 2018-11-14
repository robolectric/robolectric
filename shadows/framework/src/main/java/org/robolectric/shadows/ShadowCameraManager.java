package org.robolectric.shadows;

import android.annotation.NonNull;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION_CODES;
import com.google.common.base.Preconditions;
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
}
