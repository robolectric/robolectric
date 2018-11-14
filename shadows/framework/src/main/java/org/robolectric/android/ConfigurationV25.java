package org.robolectric.android;

import static android.content.res.Configuration.DENSITY_DPI_ANY;
import static android.content.res.Configuration.DENSITY_DPI_NONE;
import static android.content.res.Configuration.DENSITY_DPI_UNDEFINED;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.robolectric.RuntimeEnvironment;

// adapted from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/java/android/content/res/Configuration.java
public class ConfigurationV25 {

  private static String localesToResourceQualifier(List<Locale> locs) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < locs.size(); i++) {
      final Locale loc = locs.get(i);
      final int l = loc.getLanguage().length();
      if (l == 0) {
        continue;
      }
      final int s = loc.getScript().length();
      final int c = loc.getCountry().length();
      final int v = loc.getVariant().length();
      // We ignore locale extensions, since they are not supported by AAPT

      if (sb.length() != 0) {
        sb.append(",");
      }
      if (l == 2 && s == 0 && (c == 0 || c == 2) && v == 0) {
        // Traditional locale format: xx or xx-rYY
        sb.append(loc.getLanguage());
        if (c == 2) {
          sb.append("-r").append(loc.getCountry());
        }
      } else {
        sb.append("b+");
        sb.append(loc.getLanguage());
        if (s != 0) {
          sb.append("+");
          sb.append(loc.getScript());
        }
        if (c != 0) {
          sb.append("+");
          sb.append(loc.getCountry());
        }
        if (v != 0) {
          sb.append("+");
          sb.append(loc.getVariant());
        }
      }
    }
    return sb.toString();
  }


  /**
   * Returns a string representation of the configuration that can be parsed
   * by build tools (like AAPT).
   *
   * @hide
   */
  public static String resourceQualifierString(Configuration config, DisplayMetrics displayMetrics) {
    return resourceQualifierString(config, displayMetrics, true);
  }

  public static String resourceQualifierString(Configuration config, DisplayMetrics displayMetrics, boolean includeSdk) {
    ArrayList<String> parts = new ArrayList<String>();

    if (config.mcc != 0) {
      parts.add("mcc" + config.mcc);
      if (config.mnc != 0) {
        parts.add("mnc" + config.mnc);
      }
    }

    List<Locale> locales = getLocales(config);
    if (!locales.isEmpty()) {
      final String resourceQualifier = localesToResourceQualifier(locales);
      if (!resourceQualifier.isEmpty()) {
        parts.add(resourceQualifier);
      }
    }

    switch (config.screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK) {
      case Configuration.SCREENLAYOUT_LAYOUTDIR_LTR:
        parts.add("ldltr");
        break;
      case Configuration.SCREENLAYOUT_LAYOUTDIR_RTL:
        parts.add("ldrtl");
        break;
      default:
        break;
    }

    if (config.smallestScreenWidthDp != 0) {
      parts.add("sw" + config.smallestScreenWidthDp + "dp");
    }

    if (config.screenWidthDp != 0) {
      parts.add("w" + config.screenWidthDp + "dp");
    }

    if (config.screenHeightDp != 0) {
      parts.add("h" + config.screenHeightDp + "dp");
    }

    switch (config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
      case Configuration.SCREENLAYOUT_SIZE_SMALL:
        parts.add("small");
        break;
      case Configuration.SCREENLAYOUT_SIZE_NORMAL:
        parts.add("normal");
        break;
      case Configuration.SCREENLAYOUT_SIZE_LARGE:
        parts.add("large");
        break;
      case Configuration.SCREENLAYOUT_SIZE_XLARGE:
        parts.add("xlarge");
        break;
      default:
        break;
    }

    switch (config.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK) {
      case Configuration.SCREENLAYOUT_LONG_YES:
        parts.add("long");
        break;
      case Configuration.SCREENLAYOUT_LONG_NO:
        parts.add("notlong");
        break;
      default:
        break;
    }

    switch (config.screenLayout & Configuration.SCREENLAYOUT_ROUND_MASK) {
      case Configuration.SCREENLAYOUT_ROUND_YES:
        parts.add("round");
        break;
      case Configuration.SCREENLAYOUT_ROUND_NO:
        parts.add("notround");
        break;
      default:
        break;
    }

    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.O) {
      switch (config.colorMode & Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK) {
        case Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_YES:
          parts.add("widecg");
          break;
        case Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_NO:
          parts.add("nowidecg");
          break;
        default:
          break;
      }

      switch (config.colorMode & Configuration.COLOR_MODE_HDR_MASK) {
        case Configuration.COLOR_MODE_HDR_YES:
          parts.add("highdr");
          break;
        case Configuration.COLOR_MODE_HDR_NO:
          parts.add("lowdr");
          break;
        default:
          break;
      }
    }

    switch (config.orientation) {
      case Configuration.ORIENTATION_LANDSCAPE:
        parts.add("land");
        break;
      case Configuration.ORIENTATION_PORTRAIT:
        parts.add("port");
        break;
      default:
        break;
    }

    switch (config.uiMode & Configuration.UI_MODE_TYPE_MASK) {
      case Configuration.UI_MODE_TYPE_APPLIANCE:
        parts.add("appliance");
        break;
      case Configuration.UI_MODE_TYPE_DESK:
        parts.add("desk");
        break;
      case Configuration.UI_MODE_TYPE_TELEVISION:
        parts.add("television");
        break;
      case Configuration.UI_MODE_TYPE_CAR:
        parts.add("car");
        break;
      case Configuration.UI_MODE_TYPE_WATCH:
        parts.add("watch");
        break;
      case Configuration.UI_MODE_TYPE_VR_HEADSET:
        parts.add("vrheadset");
        break;
      case Configuration.UI_MODE_TYPE_NORMAL:
      default:
        break;
    }

    switch (config.uiMode & Configuration.UI_MODE_NIGHT_MASK) {
      case Configuration.UI_MODE_NIGHT_YES:
        parts.add("night");
        break;
      case Configuration.UI_MODE_NIGHT_NO:
        parts.add("notnight");
        break;
      default:
        break;
    }

    int densityDpi;
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.JELLY_BEAN) {
      densityDpi = config.densityDpi;
    } else {
      densityDpi = displayMetrics.densityDpi;
    }

    switch (densityDpi) {
      case DENSITY_DPI_UNDEFINED:
        break;
      case 120:
        parts.add("ldpi");
        break;
      case 160:
        parts.add("mdpi");
        break;
      case 213:
        parts.add("tvdpi");
        break;
      case 240:
        parts.add("hdpi");
        break;
      case 320:
        parts.add("xhdpi");
        break;
      case 480:
        parts.add("xxhdpi");
        break;
      case 640:
        parts.add("xxxhdpi");
        break;
      case DENSITY_DPI_ANY:
        parts.add("anydpi");
        break;
      case DENSITY_DPI_NONE:
        parts.add("nodpi");
        break;
      default:
        parts.add(densityDpi + "dpi");
        break;
    }

    switch (config.touchscreen) {
      case Configuration.TOUCHSCREEN_NOTOUCH:
        parts.add("notouch");
        break;
      case Configuration.TOUCHSCREEN_FINGER:
        parts.add("finger");
        break;
      default:
        break;
    }

    switch (config.keyboardHidden) {
      case Configuration.KEYBOARDHIDDEN_NO:
        parts.add("keysexposed");
        break;
      case Configuration.KEYBOARDHIDDEN_YES:
        parts.add("keyshidden");
        break;
      case Configuration.KEYBOARDHIDDEN_SOFT:
        parts.add("keyssoft");
        break;
      default:
        break;
    }

    switch (config.keyboard) {
      case Configuration.KEYBOARD_NOKEYS:
        parts.add("nokeys");
        break;
      case Configuration.KEYBOARD_QWERTY:
        parts.add("qwerty");
        break;
      case Configuration.KEYBOARD_12KEY:
        parts.add("12key");
        break;
      default:
        break;
    }

    switch (config.navigationHidden) {
      case Configuration.NAVIGATIONHIDDEN_NO:
        parts.add("navexposed");
        break;
      case Configuration.NAVIGATIONHIDDEN_YES:
        parts.add("navhidden");
        break;
      default:
        break;
    }

    switch (config.navigation) {
      case Configuration.NAVIGATION_NONAV:
        parts.add("nonav");
        break;
      case Configuration.NAVIGATION_DPAD:
        parts.add("dpad");
        break;
      case Configuration.NAVIGATION_TRACKBALL:
        parts.add("trackball");
        break;
      case Configuration.NAVIGATION_WHEEL:
        parts.add("wheel");
        break;
      default:
        break;
    }

    if (includeSdk) {
      parts.add("v" + Build.VERSION.RESOURCES_SDK_INT);
    }

    return TextUtils.join("-", parts);
  }

  private static List<Locale> getLocales(Configuration config) {
    List<Locale> locales = new ArrayList<>();
    if (RuntimeEnvironment.getApiLevel() > Build.VERSION_CODES.M) {
      LocaleList localeList = config.getLocales();
      for (int i = 0; i < localeList.size(); i++) {
        locales.add(localeList.get(i));
      }
    } else if (config.locale != null) {
      locales.add(config.locale);
    }
    return locales;
  }
}
