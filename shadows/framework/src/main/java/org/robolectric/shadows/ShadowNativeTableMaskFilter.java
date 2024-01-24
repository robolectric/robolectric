package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.TableMaskFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.TableMaskFilterNatives;
import org.robolectric.shadows.ShadowNativeTableMaskFilter.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link TableMaskFilter} that is backed by native code */
@Implements(
    value = TableMaskFilter.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeTableMaskFilter {

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nativeNewTable(byte[] table) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return TableMaskFilterNatives.nativeNewTable(table);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nativeNewClip(int min, int max) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return TableMaskFilterNatives.nativeNewClip(min, max);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nativeNewGamma(float gamma) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return TableMaskFilterNatives.nativeNewGamma(gamma);
  }

  /** Shadow picker for {@link TableMaskFilter}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeTableMaskFilter.class);
    }
  }
}
