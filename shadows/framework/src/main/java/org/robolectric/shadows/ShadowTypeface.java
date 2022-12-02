package org.robolectric.shadows;

import android.graphics.Typeface;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.ShadowPicker;
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

  /** A {@link ShadowPicker} that always selects the legacy ShadowTypeface. */
  public static class Picker implements ShadowPicker<ShadowTypeface> {
    @Override
    public Class<? extends ShadowTypeface> pickShadowClass() {
      return ShadowLegacyTypeface.class;
    }
  }
}
