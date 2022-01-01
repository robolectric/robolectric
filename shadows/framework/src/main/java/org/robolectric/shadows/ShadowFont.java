package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;

import android.graphics.fonts.Font;
import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link Font} for Android S */
@Implements(value = Font.class, minSdk = S)
public class ShadowFont {
  @Implementation
  protected static int nGetPackedStyle(long fontPtr) {
    // This value represents FontStyle.FONT_WEIGHT_NORMAL (first four bits)
    // combined with FONT_SLANT_UPRIGHT (0 in the 5th bit).
    return ShadowFontBuilder.getPackedStyle(fontPtr);
  }

  @Implementation
  protected static ByteBuffer nNewByteBuffer(long font) {
    return ShadowFontBuilder.getBuffer(font);
  }
}
