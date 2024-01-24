package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.fonts.FontFileUtil;
import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.FontFileUtilNatives;
import org.robolectric.shadows.ShadowNativeFontFileUtil.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link FontFileUtil} that is backed by native code */
@Implements(
    value = FontFileUtil.class,
    isInAndroidSdk = false,
    minSdk = Q,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeFontFileUtil {
  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static long nGetFontRevision(ByteBuffer buffer, int index) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return FontFileUtilNatives.nGetFontRevision(buffer, index);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static String nGetFontPostScriptName(ByteBuffer buffer, int index) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return FontFileUtilNatives.nGetFontPostScriptName(buffer, index);
  }

  @Implementation(minSdk = S, maxSdk = U.SDK_INT)
  protected static int nIsPostScriptType1Font(ByteBuffer buffer, int index) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return FontFileUtilNatives.nIsPostScriptType1Font(buffer, index);
  }

  /** Shadow picker for {@link FontFileUtil}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeFontFileUtil.class);
    }
  }
}
