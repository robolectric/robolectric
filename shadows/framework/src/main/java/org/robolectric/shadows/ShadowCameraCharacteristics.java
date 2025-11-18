package org.robolectric.shadows;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.impl.CameraMetadataNative;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(CameraCharacteristics.class)
public class ShadowCameraCharacteristics {

  private final Map<Key<?>, Object> charactersKeyToValue = new HashMap<>();

  /** Convenience method which returns a new instance of {@link CameraCharacteristics}. */
  public static CameraCharacteristics newCameraCharacteristics() {
    CameraMetadataNative cm = new CameraMetadataNative();
    return new CameraCharacteristics(cm);
  }

  @Implementation
  @Nullable
  protected <T> T get(Key<T> key) {
    return (T) charactersKeyToValue.get(key);
  }

  /**
   * Sets the value for a given key.
   *
   * @throws IllegalArgumentException if there's an existing value for the key.
   */
  public <T> void set(Key<T> key, Object value) {
    Preconditions.checkArgument(!charactersKeyToValue.containsKey(key));
    charactersKeyToValue.put(key, value);
  }
}
