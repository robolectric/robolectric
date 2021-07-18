package org.robolectric.shadows;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.os.Build;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.ShadowPicker;

/** Base class for shadow implementations of {@link CameraMetadataNative} */
public class ShadowCameraMetadataNativePicker {
  /** Picker to choose the correct shadow based on API version. */
  private static class PickerInternal implements ShadowPicker<Object> {
    public PickerInternal() {}

    @Override
    public Class<? extends Object> pickShadowClass() {
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.R) {
        return ShadowCameraMetadataNativeR.class;
      } else {
        return ShadowCameraMetadataNative.class;
      }
    }
  }

  /** This subclass is required to avoid an internal error when loading the picker. */
  public static class Picker extends PickerInternal {}
}
