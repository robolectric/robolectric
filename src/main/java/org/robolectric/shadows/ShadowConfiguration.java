package org.robolectric.shadows;

import android.content.res.Configuration;
import java.util.Locale;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(Configuration.class)
public class ShadowConfiguration {

  @RealObject
  private Configuration realConfiguration;

  public int screenLayout;
  public int touchscreen;
  public int orientation;
  private String qualifiers = "";

  public void __constructor__(Configuration other) {
    realConfiguration.setTo(other);
  }

  @Implementation
  public void setTo(Configuration o) {
    // commented out lines are coming in a newer version of Android SDK

    realConfiguration.fontScale = o.fontScale;
    realConfiguration.mcc = o.mcc;
    realConfiguration.mnc = o.mnc;
    if (o.locale != null) {
      realConfiguration.locale = (Locale) o.locale.clone();
      // realConfiguration.textLayoutDirection = o.textLayoutDirection;
    }
    // realConfiguration.userSetLocale = o.userSetLocale;
    realConfiguration.touchscreen = o.touchscreen;
    realConfiguration.keyboard = o.keyboard;
    realConfiguration.keyboardHidden = o.keyboardHidden;
    realConfiguration.hardKeyboardHidden = o.hardKeyboardHidden;
    realConfiguration.navigation = o.navigation;
    realConfiguration.navigationHidden = o.navigationHidden;
    realConfiguration.orientation = o.orientation;
    realConfiguration.screenLayout = o.screenLayout;
    realConfiguration.uiMode = o.uiMode;
    realConfiguration.screenWidthDp = o.screenWidthDp;
    realConfiguration.screenHeightDp = o.screenHeightDp;
    realConfiguration.smallestScreenWidthDp = o.smallestScreenWidthDp;
    // realConfiguration.compatScreenWidthDp = o.compatScreenWidthDp;
    // realConfiguration.compatScreenHeightDp = o.compatScreenHeightDp;
    // realConfiguration.compatSmallestScreenWidthDp = o.compatSmallestScreenWidthDp;
    // realConfiguration.seq = o.seq;
  }

  @Implementation
  public int compareTo(Configuration that) {
    int n;
    float a = realConfiguration.fontScale;
    float b = that.fontScale;
    if (a < b) return -1;
    if (a > b) return 1;
    n = realConfiguration.mcc - that.mcc;
    if (n != 0) return n;
    n = realConfiguration.mnc - that.mnc;
    if (n != 0) return n;
    if (realConfiguration.locale == null) {
      if (that.locale != null) return 1;
    } else if (that.locale == null) {
      return -1;
    } else {
      n = realConfiguration.locale.getLanguage().compareTo(that.locale.getLanguage());
      if (n != 0) return n;
      n = realConfiguration.locale.getCountry().compareTo(that.locale.getCountry());
      if (n != 0) return n;
      n = realConfiguration.locale.getVariant().compareTo(that.locale.getVariant());
      if (n != 0) return n;
    }
    n = realConfiguration.touchscreen - that.touchscreen;
    if (n != 0) return n;
    n = realConfiguration.keyboard - that.keyboard;
    if (n != 0) return n;
    n = realConfiguration.keyboardHidden - that.keyboardHidden;
    if (n != 0) return n;
    n = realConfiguration.hardKeyboardHidden - that.hardKeyboardHidden;
    if (n != 0) return n;
    n = realConfiguration.navigation - that.navigation;
    if (n != 0) return n;
    n = realConfiguration.navigationHidden - that.navigationHidden;
    if (n != 0) return n;
    n = realConfiguration.orientation - that.orientation;
    if (n != 0) return n;
    n = realConfiguration.screenLayout - that.screenLayout;
    if (n != 0) return n;
    n = realConfiguration.uiMode - that.uiMode;
    if (n != 0) return n;
    n = realConfiguration.screenWidthDp - that.screenWidthDp;
    if (n != 0) return n;
    n = realConfiguration.screenHeightDp - that.screenHeightDp;
    if (n != 0) return n;
    n = realConfiguration.smallestScreenWidthDp - that.smallestScreenWidthDp;
    //if (n != 0) return n;
    return n;
  }

  @Implementation
  public boolean equals(Configuration that) {
    if (that == null) return false;
    if (that == realConfiguration) return true;
    return realConfiguration.compareTo(that) == 0;
  }

  @Implementation
  public boolean equals(Object that) {
    try {
      return equals((Configuration)that);
    } catch (ClassCastException e) {
    }
    return false;
  }

  @Implementation
  public int hashCode() {
    int result = 17;
    result = 31 * result + Float.floatToIntBits(realConfiguration.fontScale);
    result = 31 * result + realConfiguration.mcc;
    result = 31 * result + realConfiguration.mnc;
    result = 31 * result + (realConfiguration.locale != null ? realConfiguration.locale.hashCode() : 0);
    result = 31 * result + touchscreen;
    result = 31 * result + realConfiguration.keyboard;
    result = 31 * result + realConfiguration.keyboardHidden;
    result = 31 * result + realConfiguration.hardKeyboardHidden;
    result = 31 * result + realConfiguration.navigation;
    result = 31 * result + realConfiguration.navigationHidden;
    result = 31 * result + orientation;
    result = 31 * result + screenLayout;
    result = 31 * result + realConfiguration.uiMode;
    result = 31 * result + realConfiguration.screenWidthDp;
    result = 31 * result + realConfiguration.screenHeightDp;
    result = 31 * result + realConfiguration.smallestScreenWidthDp;
    return result;
  }

  @Implementation
  public void setToDefaults() {
    realConfiguration.screenLayout = Configuration.SCREENLAYOUT_LONG_NO |
        Configuration.SCREENLAYOUT_SIZE_NORMAL;
  }

  @Implementation
  public void setLocale( Locale l ) {
    realConfiguration.locale = l;
  }

  public void overrideQualifiers(String qualifiers) {
    this.qualifiers = qualifiers;
  }

  public String getQualifiers() {
    return qualifiers;
  }
}
