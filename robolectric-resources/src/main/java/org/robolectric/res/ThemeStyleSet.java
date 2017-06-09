package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the list of styles applied to a Theme.
 */
public class ThemeStyleSet implements Style {

  private List<OverlayedStyle> styles = new ArrayList<>();

  @Override public AttributeResource getAttrValue(ResName attrName) {
    AttributeResource attribute = null;

    for (OverlayedStyle overlayedStyle : styles) {
      AttributeResource overlayedAttribute = overlayedStyle.style.getAttrValue(attrName);
      if (overlayedAttribute != null && (attribute == null || overlayedStyle.force)) {
        attribute = overlayedAttribute;
      }
    }

    return attribute;
  }

  public void apply(Style style, boolean force) {
    OverlayedStyle styleToAdd = new OverlayedStyle(style, force);
    for (int i = 0; i < styles.size(); ++i) {
      if (styleToAdd.equals(styles.get(i))) {
        styles.remove(i);
        break;
      }
    }
    styles.add(styleToAdd);
  }

  public ThemeStyleSet copy() {
    ThemeStyleSet themeStyleSet = new ThemeStyleSet();
    themeStyleSet.styles.addAll(this.styles);
    return themeStyleSet;
  }

  @Override
  public String toString() {
    if (styles.isEmpty()) {
      return "theme with no applied styles";
    } else {
      return "theme with applied styles: " + styles + "";
    }
  }

  private static class OverlayedStyle {
    Style style;
    boolean force;

    OverlayedStyle(Style style, boolean force) {
      this.style = style;
      this.force = force;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof OverlayedStyle)) {
        return false;
      }
      OverlayedStyle overlayedStyle = (OverlayedStyle) obj;
      return style.equals(overlayedStyle.style);
    }

    @Override
    public int hashCode() {
      return style.hashCode();
    }

    @Override
    public String toString() {
      return style.toString() + (force ? " (forced)" : "");
    }
  }

}
