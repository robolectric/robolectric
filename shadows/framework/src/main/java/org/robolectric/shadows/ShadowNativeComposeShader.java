package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.ComposeShader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.ComposeShaderNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeComposeShader.Picker;

/** Shadow for {@link ComposeShader} that is backed by native code */
@Implements(value = ComposeShader.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeComposeShader {

  @Implementation(minSdk = O)
  protected static long nativeCreate(
      long nativeMatrix, long nativeShaderA, long nativeShaderB, int porterDuffMode) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return ComposeShaderNatives.nativeCreate(
        nativeMatrix, nativeShaderA, nativeShaderB, porterDuffMode);
  }

  /** Shadow picker for {@link ComposeShader}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeComposeShader.class);
    }
  }
}
