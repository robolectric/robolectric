package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.ComposeShader;
import android.graphics.Shader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.ComposeShaderNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeComposeShader.Picker;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link ComposeShader} that is backed by native code */
@Implements(
    value = ComposeShader.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeComposeShader {

  @RealObject private ComposeShader realComposeShader;

  @Implementation(minSdk = O, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeCreate(
      long nativeMatrix, long nativeShaderA, long nativeShaderB, int porterDuffMode) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return ComposeShaderNatives.nativeCreate(
        nativeMatrix, nativeShaderA, nativeShaderB, porterDuffMode);
  }

  /**
   * A {@link ComposeShader} notices that a child has changed by the address of the native shader of
   * the child. It remembers the native shaders that the children have for the filtering of the
   * paint, but is created from the ones that they have without filtering. A {@link
   * android.graphics.BitmapShader} frees the first to create the second, so the remembered address
   * is one of a freed native shader. If the native shader of a changed child gets that address, the
   * child counts as unchanged, and the old native shader is drawn.
   *
   * <p>This makes it remember the native shaders that it is created from, which it keeps alive, so
   * that no other native shader can get their addresses.
   */
  @Implementation(minSdk = S)
  protected long createNativeInstance(long nativeMatrix, boolean filterFromPaint) {
    ComposeShaderReflector composeShader =
        reflector(ComposeShaderReflector.class, realComposeShader);
    long nativeInstance = composeShader.createNativeInstance(nativeMatrix, filterFromPaint);
    composeShader.setNativeInstanceShaderA(composeShader.getShaderA().getNativeInstance());
    composeShader.setNativeInstanceShaderB(composeShader.getShaderB().getNativeInstance());
    return nativeInstance;
  }

  @ForType(ComposeShader.class)
  interface ComposeShaderReflector {
    @Direct
    long createNativeInstance(long nativeMatrix, boolean filterFromPaint);

    @Accessor("mShaderA")
    Shader getShaderA();

    @Accessor("mShaderB")
    Shader getShaderB();

    @Accessor("mNativeInstanceShaderA")
    void setNativeInstanceShaderA(long nativeInstance);

    @Accessor("mNativeInstanceShaderB")
    void setNativeInstanceShaderB(long nativeInstance);
  }

  /** Shadow picker for {@link ComposeShader}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeComposeShader.class);
    }
  }
}
