package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Rect;
import android.graphics.RegionIterator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RegionIteratorNatives;
import org.robolectric.shadows.ShadowNativeRegionIterator.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link RegionIterator} that is backed by native code */
@Implements(
    value = RegionIterator.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeRegionIterator {

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nativeConstructor(long nativeRegion) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RegionIteratorNatives.nativeConstructor(nativeRegion);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static void nativeDestructor(long nativeIter) {
    RegionIteratorNatives.nativeDestructor(nativeIter);
  }

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static boolean nativeNext(long nativeIter, Rect r) {
    return RegionIteratorNatives.nativeNext(nativeIter, r);
  }

  /** Shadow picker for {@link RegionIterator}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeRegionIterator.class);
    }
  }
}
