package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.admin.DevicePolicyResourcesManager;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link DevicePolicyResourcesManager}. */
@Implements(
    value = DevicePolicyResourcesManager.class,
    minSdk = VERSION_CODES.TIRAMISU,
    // turn off shadowOf generation (new API)
    isInAndroidSdk = false)
public class ShadowDevicePolicyResourcesManager {

  @RealObject DevicePolicyResourcesManager realDevicePolicyResourcesManager;
  private final Map<String, String> stringMappings = new HashMap<>();

  /**
   * Override string returned by the resource identified by {@code stringId}. Reset the override by
   * providing null as the {@code vaNlue}.
   */
  public void setString(@NonNull String stringId, String value) {
    stringMappings.put(stringId, value);
  }

  @Implementation
  @Nullable
  protected String getString(
      @NonNull String stringId, @NonNull Supplier<String> defaultStringLoader) {
    String value = stringMappings.get(stringId);
    if (value != null) {
      return value;
    }

    return reflector(DevicePolicyResourcesManagerReflector.class, realDevicePolicyResourcesManager)
        .getString(stringId, defaultStringLoader);
  }

  @ForType(DevicePolicyResourcesManager.class)
  interface DevicePolicyResourcesManagerReflector {
    @Direct
    String getString(@NonNull String stringId, @NonNull Supplier<String> defaultStringLoader);
  }
}
