package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.ComposePathEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.ComposePathEffectNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeComposePathEffect.Picker;

/** Shadow for {@link ComposePathEffect} that is backed by native code */
@Implements(
    value = ComposePathEffect.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeComposePathEffect {

  @Implementation(minSdk = O, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeCreate(long nativeOuterpe, long nativeInnerpe) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return ComposePathEffectNatives.nativeCreate(nativeOuterpe, nativeInnerpe);
  }

  /** Shadow picker for {@link ComposePathEffect}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeComposePathEffect.class);
    }
  }
}
