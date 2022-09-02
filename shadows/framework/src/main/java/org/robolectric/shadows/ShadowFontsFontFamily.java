package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import java.util.ArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link FontFamily}. */
@Implements(value = FontFamily.class, minSdk = Q)
public class ShadowFontsFontFamily {

  private ArrayList<Font> fonts = new ArrayList<>();

  /**
   * The real {@link FontFamily#getFont} calls into native code, so it needs to be shadowed to
   * prevent an NPE.
   */
  @Implementation
  protected Font getFont(int index) {
    return fonts.get(index);
  }

  /** Shadow for {@link FontFamily.Builder}. */
  @Implements(value = FontFamily.Builder.class, minSdk = Q)
  public static class ShadowFontsFontFamilyBuilder {

    @RealObject FontFamily.Builder realFontFamilyBuilder;

    @Implementation
    protected FontFamily build() {
      FontFamily result =
          reflector(FontFamilyBuilderReflector.class, realFontFamilyBuilder).build();
      ShadowFontsFontFamily shadowFontFamily = Shadow.extract(result);
      shadowFontFamily.fonts =
          reflector(FontFamilyBuilderReflector.class, realFontFamilyBuilder).getFonts();
      return result;
    }

    @ForType(FontFamily.Builder.class)
    interface FontFamilyBuilderReflector {
      @Direct
      FontFamily build();

      @Accessor("mFonts")
      ArrayList<Font> getFonts();
    }
  }
}
