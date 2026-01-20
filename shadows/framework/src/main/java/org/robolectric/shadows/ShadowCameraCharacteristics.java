package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.impl.CameraMetadataNative;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(CameraCharacteristics.class)
public class ShadowCameraCharacteristics {

  @RealObject private CameraCharacteristics realObject;

  /** Convenience method which returns a new instance of {@link CameraCharacteristics}. */
  public static CameraCharacteristics newCameraCharacteristics() {
    CameraMetadataNative cm = new CameraMetadataNative();
    return new CameraCharacteristics(cm);
  }

  public <T> void set(CameraCharacteristics.Key<T> key, T value) {
    CameraMetadataNative cm =
        reflector(CameraCharacteristicsReflector.class, realObject).getProperties();
    cm.set(key, value);
  }

  @ForType(CameraCharacteristics.class)
  interface CameraCharacteristicsReflector {
    @Accessor("mProperties")
    CameraMetadataNative getProperties();
  }
}
