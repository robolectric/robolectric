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

  /**
   * Deprecated constructor.
   *
   * @deprecated Use {@link #newBuilder()} instead.
   */
  @Deprecated
  public VirtualDeviceParamsBuilder() {}

  public static VirtualDeviceParamsBuilder newBuilder() {
    return new VirtualDeviceParamsBuilder();
  }

  public VirtualDeviceParamsBuilder setName(String name) {
    builder.setName(name);
    return this;
  }

  public /* VirtualDeviceParams */ Object build() {
    return builder.build();
  }
}
