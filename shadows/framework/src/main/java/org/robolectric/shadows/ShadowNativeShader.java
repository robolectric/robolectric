package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.Shader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.ShaderNatives;
import org.robolectric.shadows.ShadowNativeShader.Picker;

/** Shadow for {@link Shader} that is backed by native code */
@Implements(
    value = Shader.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeShader {

  @Implementation(minSdk = O, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeGetFinalizer() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return ShaderNatives.nativeGetFinalizer();
  }

  /** Shadow picker for {@link Shader}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeShader.class);
    }
  }
}
