package org.robolectric.shadows;

import android.graphics.ColorSpace;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowNativeColorSpace.Picker;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for {@link ColorSpace} that defers its static initializer. */
@Implements(
    value = ColorSpace.class,
    minSdk = V.SDK_INT,
    isInAndroidSdk = false,
    shadowPicker = Picker.class)
public class ShadowNativeColorSpace {

  /**
   * The {@link ColorSpace} static initializer invokes its own native methods in its constructor
   * when it initializes the named color spaces. This has to be deferred starting in Android V.
   */
  @Implementation(minSdk = V.SDK_INT)
  protected static void __staticInitializer__() {
    // deferred
  }

  /** Shadow picker for {@link ColorSpace}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeColorSpace.class);
    }
  }
}
