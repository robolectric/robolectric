package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.BitmapRegionDecoder;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowNativeBitmapRegionDecoder.Picker;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link BitmapRegionDecoder} that is backed by native code */
@Implements(
    value = BitmapRegionDecoder.class,
    isInAndroidSdk = false,
    minSdk = VANILLA_ICE_CREAM,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeBitmapRegionDecoder {

  /**
   * The real implementation checks to see if the InputStream is an AssetInputStream. However,
   * Robolectric does not support native assets for all SDK levels.
   */
  @Implementation
  protected static BitmapRegionDecoder newInstance(InputStream is) throws IOException {
    byte[] tempStorage = new byte[16 * 1024];
    return reflector(BitmapRegionDecoderReflector.class).nativeNewInstance(is, tempStorage);
  }

  @ForType(BitmapRegionDecoder.class)
  interface BitmapRegionDecoderReflector {
    @Static
    BitmapRegionDecoder nativeNewInstance(InputStream is, byte[] storage);
  }

  /** Shadow picker for {@link BitmapRegionDecoder}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowBitmapRegionDecoder.class, ShadowNativeBitmapRegionDecoder.class);
    }

    @Override
    protected int getMinApiLevel() {
      return VANILLA_ICE_CREAM;
    }
  }
}
