package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.annotation.RequiresApi;
import android.companion.virtual.VirtualDeviceParams;

/**
 * Builder class to create instance of {@link VirtualDeviceParams}.
 *
 * <p>VirtualDeviceParams is marked as @SystemApi starting from Android U.
 */
@RequiresApi(UPSIDE_DOWN_CAKE)
public class VirtualDeviceParamsBuilder {

  private final VirtualDeviceParams.Builder builder = new VirtualDeviceParams.Builder();

  public VirtualDeviceParamsBuilder setName(String name) {
    builder.setName(name);
    return this;
  }

  public VirtualDeviceParams build() {
    return builder.build();
  }
}
