package org.robolectric.shadows;

import android.graphics.Typeface;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowTypeface.Picker;

/** Base class for {@link ShadowTypeface} classes. */
@Implements(value = Typeface.class, shadowPicker = Picker.class)
public abstract class ShadowTypeface {

  /**
   * Returns the font description.
   *
   * @return Font description.
   */
  public abstract FontDesc getFontDescription();

  /** Contains data about a font. */
  public static class FontDesc {
    public final String familyName;
    public final int style;

    public FontDesc(String familyName, int style) {
      this.familyName = familyName;
      this.style = style;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof FontDesc)) {
        return false;
      }

      FontDesc fontDesc = (FontDesc) o;

      if (style != fontDesc.style) {
        return false;
      }
      if (familyName != null
          ? !familyName.equals(fontDesc.familyName)
          : fontDesc.familyName != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = familyName != null ? familyName.hashCode() : 0;
      result = 31 * result + style;
      return result;
    }

    public String getFamilyName() {
      return familyName;
    }

    public int getStyle() {
      return style;
    }
  }

  /** Shadow picker for {@link Typeface}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowLegacyTypeface.class, ShadowNativeTypeface.class);
    }
  }
}
