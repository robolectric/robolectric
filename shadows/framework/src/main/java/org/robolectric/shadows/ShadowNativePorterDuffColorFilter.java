package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.graphics.PorterDuffColorFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PorterDuffColorFilterNatives;
import org.robolectric.shadows.ShadowNativePorterDuffColorFilter.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link PorterDuffColorFilter} that is backed by native code */
@Implements(
    value = PorterDuffColorFilter.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativePorterDuffColorFilter extends ShadowPorterDuffColorFilter {

  @Implementation(minSdk = Q, maxSdk = U.SDK_INT)
  protected static long native_CreateBlendModeFilter(int srcColor, int blendmode) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PorterDuffColorFilterNatives.native_CreateBlendModeFilter(srcColor, blendmode);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static long native_CreatePorterDuffFilter(int srcColor, int porterDuffMode) {
    return native_CreateBlendModeFilter(srcColor, porterDuffMode);
  }

  /** Shadow picker for {@link PorterDuffColorFilter}. */
  public static final class Picker extends GraphicsShadowPicker<ShadowPorterDuffColorFilter> {
    public Picker() {
      super(ShadowPorterDuffColorFilter.class, ShadowNativePorterDuffColorFilter.class);
    }
  }
}
