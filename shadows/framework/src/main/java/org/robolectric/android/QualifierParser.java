package org.robolectric.android;

import android.content.res.Configuration;
import android.util.DisplayMetrics;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/** Parser that converts a qualifier string to a Configuration and DisplayMetrics. */
class QualifierParser {

  private QualifierParser() {}

  /**
   * Constant used to to represent MNC (Mobile Network Code) zero. 0 cannot be used, since it is
   * used to represent an undefined MNC.
   */
  private static final int ACONFIGURATION_MNC_ZERO = 0xffff;

  private static final String WILDCARD_NAME = "any";

  private static final Pattern MCC_PATTERN = Pattern.compile("mcc(\\d+)");
  private static final Pattern MNC_PATTERN = Pattern.compile("mnc(\\d+)");
  private static final Pattern SMALLEST_SCREEN_WIDTH_PATTERN = Pattern.compile("^sw([0-9]+)dp");
  private static final Pattern SCREEN_WIDTH_PATTERN = Pattern.compile("^w([0-9]+)dp");
  private static final Pattern SCREEN_HEIGHT_PATTERN = Pattern.compile("^h([0-9]+)dp");
  private static final Pattern DENSITY_PATTERN = Pattern.compile("^([0-9]+)dpi");
  private static final Pattern HEIGHT_WIDTH_PATTERN = Pattern.compile("^([0-9]+)x([0-9]+)");
  private static final Pattern VERSION_QUALIFIER_PATTERN = Pattern.compile("v([0-9]+)$");

  public static class LocaleValue {

    String language;
    String region;
    String script;
    String variant;

    void set_language(String language_chars) {
      language = language_chars.trim().toLowerCase();
    }

    void set_region(String region_chars) {
      region = region_chars.trim().toUpperCase();
    }

    void set_script(String script_chars) {
      script =
          Character.toUpperCase(script_chars.charAt(0)) + script_chars.substring(1).toLowerCase();
    }

    void set_variant(String variant_chars) {
      variant = variant_chars.trim();
    }

    static boolean is_alpha(final String str) {
      for (int i = 0; i < str.length(); i++) {
        if (!Character.isAlphabetic(str.charAt(i))) {
          return false;
        }
      }

      return true;
    }

    int initFromParts(PeekingIterator<String> iter) {

      String part = iter.peek();
      if (part.startsWith("b+")) {
        // This is a "modified" BCP 47 language tag. Same semantics as BCP 47 tags,
        // except that the separator is "+" and not "-".
        String[] subtags = part.substring(2).toLowerCase().split("\\+", 0);
        if (subtags.length == 1) {
          set_language(subtags[0]);
        } else if (subtags.length == 2) {
          set_language(subtags[0]);

          // The second tag can either be a region, a variant or a script.
          switch (subtags[1].length()) {
            case 2:
            case 3:
              set_region(subtags[1]);
              break;
            case 4:
              if ('0' <= subtags[1].charAt(0) && subtags[1].charAt(0) <= '9') {
                // This is a variant: fall through
              } else {
                set_script(subtags[1]);
                break;
              }
            // fall through
            case 5:
            case 6:
            case 7:
            case 8:
              set_variant(subtags[1]);
              break;
            default:
              return -1;
          }
        } else if (subtags.length == 3) {
          // The language is always the first subtag.
          set_language(subtags[0]);

          // The second subtag can either be a script or a region code.
          // If its size is 4, it's a script code, else it's a region code.
          if (subtags[1].length() == 4) {
            set_script(subtags[1]);
          } else if (subtags[1].length() == 2 || subtags[1].length() == 3) {
            set_region(subtags[1]);
          } else {
            return -1;
          }

          // The third tag can either be a region code (if the second tag was
          // a script), else a variant code.
          if (subtags[2].length() >= 4) {
            set_variant(subtags[2]);
          } else {
            set_region(subtags[2]);
          }
        } else if (subtags.length == 4) {
          set_language(subtags[0]);
          set_script(subtags[1]);
          set_region(subtags[2]);
          set_variant(subtags[3]);
        } else {
          return -1;
        }

        iter.next();

      } else {
        if ((part.length() == 2 || part.length() == 3)
            && is_alpha(part)
            && !Objects.equals(part, "car")) {
          set_language(part);
          iter.next();

          if (iter.hasNext()) {
            final String region_part = iter.peek();
            if (region_part.charAt(0) == 'r' && region_part.length() == 3) {
              set_region(region_part.substring(1));
              iter.next();
            }
          }
        }
      }

      return 0;
    }

    @Nullable
    public Locale buildLocale() {
      if (Strings.isNullOrEmpty(language)
          && Strings.isNullOrEmpty(region)
          && Strings.isNullOrEmpty(script)) {
        return null;
      } else {
        return new Locale.Builder()
            .setLanguage(language)
            .setRegion(region)
            .setScript(script == null ? "" : script)
            .build();
      }
    }
  }

  public static boolean parse(
      final String qualifierString, Configuration configOut, DisplayMetrics displayMetricsOut) {
    if (qualifierString.isEmpty()) {
      return true;
    }
    PeekingIterator<String> partIter =
        Iterators.peekingIterator(
            Arrays.asList(qualifierString.toLowerCase().split("-")).iterator());

    Locale locale = null;

    if (partIter.hasNext() && parseMcc(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseMnc(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext()) {
      // Locale spans a few '-' separators, so we let it
      // control the index.
      LocaleValue val = new LocaleValue();
      int partsConsumed = val.initFromParts(partIter);
      if (partsConsumed < 0) {
        return false;
      } else {
        locale = val.buildLocale();
      }
    }

    if (partIter.hasNext() && parseLayoutDirection(partIter.peek(), configOut)) {
      partIter.next();
    }

    // for backwards compatibility, actually set the locale after layout direction, as
    // locale can force a different layout direction
    if (locale != null) {
      configOut.setLocale(locale);
    }

    if (partIter.hasNext() && parseSmallestScreenWidthDp(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseScreenWidthDp(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseScreenHeightDp(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseScreenLayoutSize(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseScreenLayoutLong(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseScreenRound(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseWideColorGamut(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseHdr(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseOrientation(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseUiModeType(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseUiModeNight(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseDensity(partIter.peek(), configOut)) {
      setDensity(configOut.densityDpi, displayMetricsOut);
      partIter.next();
    }

    if (partIter.hasNext() && parseTouchscreen(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseKeysHidden(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseKeyboard(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseNavHidden(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseNavigation(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseScreenSize(partIter.peek(), displayMetricsOut)) {
      partIter.next();
    }

    if (partIter.hasNext() && parseVersion(partIter.peek(), configOut)) {
      partIter.next();
    }

    if (partIter.hasNext()) {
      StringBuilder sb = new StringBuilder();
      partIter.forEachRemaining(
          s -> {
            sb.append(s);
            sb.append(", ");
          });
      throwParseError(qualifierString, "Found unrecognized trailing qualifier segments " + sb);
    }

    return true;
  }

  private static void throwParseError(String qualifierString, String detail) {
    throw new IllegalArgumentException(
        String.format(
            "Failed to parse qualifier string '%s'. %s. See"
                + " https://developer.android.com/guide/topics/resources/providing-resources.html#QualifierRules"
                + " for expected format.",
            qualifierString, detail));
  }

  /** Populate the given densityDpi value in DisplayMetrics */
  public static void setDensity(int densityDpi, DisplayMetrics displayMetrics) {
    displayMetrics.densityDpi = densityDpi;
    displayMetrics.density = displayMetrics.densityDpi * DisplayMetrics.DENSITY_DEFAULT_SCALE;

    displayMetrics.xdpi = displayMetrics.noncompatXdpi = displayMetrics.densityDpi;
    displayMetrics.ydpi = displayMetrics.noncompatYdpi = displayMetrics.densityDpi;
  }

  private static boolean parseLayoutDirection(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_LAYOUTDIR_MASK)
              | Configuration.SCREENLAYOUT_LAYOUTDIR_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "ldltr")) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_LAYOUTDIR_MASK)
              | Configuration.SCREENLAYOUT_LAYOUTDIR_LTR;
      return true;
    } else if (Objects.equals(name, "ldrtl")) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_LAYOUTDIR_MASK)
              | Configuration.SCREENLAYOUT_LAYOUTDIR_RTL;
      return true;
    }

    return false;
  }

  private static boolean parseSmallestScreenWidthDp(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.smallestScreenWidthDp = Configuration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED;
      return true;
    }

    Matcher matcher = SMALLEST_SCREEN_WIDTH_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.smallestScreenWidthDp = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private static boolean parseScreenWidthDp(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.screenWidthDp = Configuration.SCREEN_WIDTH_DP_UNDEFINED;
      return true;
    }

    Matcher matcher = SCREEN_WIDTH_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.screenWidthDp = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private static boolean parseScreenHeightDp(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.screenHeightDp = Configuration.SCREEN_HEIGHT_DP_UNDEFINED;
      return true;
    }

    Matcher matcher = SCREEN_HEIGHT_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.screenHeightDp = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private static boolean parseScreenLayoutSize(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK)
              | Configuration.SCREENLAYOUT_SIZE_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "small")) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK)
              | Configuration.SCREENLAYOUT_SIZE_SMALL;
      return true;
    } else if (Objects.equals(name, "normal")) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK)
              | Configuration.SCREENLAYOUT_SIZE_NORMAL;
      return true;
    } else if (Objects.equals(name, "large")) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK)
              | Configuration.SCREENLAYOUT_SIZE_LARGE;
      return true;
    } else if (Objects.equals(name, "xlarge")) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK)
              | Configuration.SCREENLAYOUT_SIZE_XLARGE;
      return true;
    }

    return false;
  }

  static boolean parseScreenLayoutLong(final String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_LONG_MASK)
              | Configuration.SCREENLAYOUT_LONG_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "long")) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_LONG_MASK)
              | Configuration.SCREENLAYOUT_LONG_YES;
      return true;
    } else if (Objects.equals(name, "notlong")) {
      out.screenLayout =
          (out.screenLayout & ~Configuration.SCREENLAYOUT_LONG_MASK)
              | Configuration.SCREENLAYOUT_LONG_NO;
      return true;
    }
    return false;
  }

  private static boolean parseScreenRound(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.screenLayout =
          ((out.screenLayout & ~Configuration.SCREENLAYOUT_ROUND_MASK)
              | Configuration.SCREENLAYOUT_ROUND_UNDEFINED);
      return true;
    } else if (Objects.equals(name, "round")) {
      out.screenLayout =
          ((out.screenLayout & ~Configuration.SCREENLAYOUT_ROUND_MASK)
              | Configuration.SCREENLAYOUT_ROUND_YES);
      return true;
    } else if (Objects.equals(name, "notround")) {
      out.screenLayout =
          ((out.screenLayout & ~Configuration.SCREENLAYOUT_ROUND_MASK)
              | Configuration.SCREENLAYOUT_ROUND_NO);
      return true;
    }
    return false;
  }

  private static boolean parseWideColorGamut(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.colorMode =
          (byte)
              ((out.colorMode & ~Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK)
                  | Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_UNDEFINED);
      return true;
    } else if (Objects.equals(name, "widecg")) {
      out.colorMode =
          (byte)
              ((out.colorMode & ~Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK)
                  | Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_YES);
      return true;
    } else if (Objects.equals(name, "nowidecg")) {
      out.colorMode =
          (byte)
              ((out.colorMode & ~Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK)
                  | Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_NO);
      return true;
    }
    return false;
  }

  private static boolean parseHdr(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.colorMode =
          (byte)
              ((out.colorMode & ~Configuration.COLOR_MODE_HDR_MASK)
                  | Configuration.COLOR_MODE_HDR_UNDEFINED);
      return true;
    } else if (Objects.equals(name, "highdr")) {
      out.colorMode =
          (byte)
              ((out.colorMode & ~Configuration.COLOR_MODE_HDR_MASK)
                  | Configuration.COLOR_MODE_HDR_YES);
      return true;
    } else if (Objects.equals(name, "lowdr")) {
      out.colorMode =
          (byte)
              ((out.colorMode & ~Configuration.COLOR_MODE_HDR_MASK)
                  | Configuration.COLOR_MODE_HDR_NO);
      return true;
    }
    return false;
  }

  private static boolean parseOrientation(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.orientation = Configuration.ORIENTATION_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "port")) {
      out.orientation = Configuration.ORIENTATION_PORTRAIT;
      return true;
    } else if (Objects.equals(name, "land")) {
      out.orientation = Configuration.ORIENTATION_LANDSCAPE;
      return true;
    } else if (Objects.equals(name, "square")) {
      out.orientation = Configuration.ORIENTATION_SQUARE;
      return true;
    }

    return false;
  }

  private static boolean parseUiModeType(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | Configuration.UI_MODE_TYPE_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "desk")) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | Configuration.UI_MODE_TYPE_DESK;
      return true;
    } else if (Objects.equals(name, "car")) {
      out.uiMode = (out.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | Configuration.UI_MODE_TYPE_CAR;
      return true;
    } else if (Objects.equals(name, "television")) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | Configuration.UI_MODE_TYPE_TELEVISION;
      return true;
    } else if (Objects.equals(name, "appliance")) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | Configuration.UI_MODE_TYPE_APPLIANCE;
      return true;
    } else if (Objects.equals(name, "watch")) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | Configuration.UI_MODE_TYPE_WATCH;
      return true;
    } else if (Objects.equals(name, "vrheadset")) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | Configuration.UI_MODE_TYPE_VR_HEADSET;
      return true;
    }

    return false;
  }

  private static boolean parseUiModeNight(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | Configuration.UI_MODE_NIGHT_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "night")) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | Configuration.UI_MODE_NIGHT_YES;
      return true;
    } else if (Objects.equals(name, "notnight")) {
      out.uiMode =
          (out.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | Configuration.UI_MODE_NIGHT_NO;
      return true;
    }

    return false;
  }

  private static boolean parseDensity(String name, Configuration configOut) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      configOut.densityDpi = Configuration.DENSITY_DPI_UNDEFINED;
      return true;
    }

    if (Objects.equals(name, "anydpi")) {
      configOut.densityDpi = Configuration.DENSITY_DPI_ANY;
      return true;
    }

    if (Objects.equals(name, "nodpi")) {
      configOut.densityDpi = Configuration.DENSITY_DPI_NONE;
      return true;
    }

    if (Objects.equals(name, "ldpi")) {
      configOut.densityDpi = DisplayMetrics.DENSITY_LOW;
      return true;
    }

    if (Objects.equals(name, "mdpi")) {
      configOut.densityDpi = DisplayMetrics.DENSITY_MEDIUM;
      return true;
    }

    if (Objects.equals(name, "tvdpi")) {
      configOut.densityDpi = DisplayMetrics.DENSITY_TV;
      return true;
    }

    if (Objects.equals(name, "hdpi")) {
      configOut.densityDpi = DisplayMetrics.DENSITY_HIGH;
      return true;
    }

    if (Objects.equals(name, "xhdpi")) {
      configOut.densityDpi = DisplayMetrics.DENSITY_XHIGH;
      return true;
    }

    if (Objects.equals(name, "xxhdpi")) {
      configOut.densityDpi = DisplayMetrics.DENSITY_XXHIGH;
      return true;
    }

    if (Objects.equals(name, "xxxhdpi")) {
      configOut.densityDpi = DisplayMetrics.DENSITY_XXXHIGH;
      return true;
    }

    // check that we have 'dpi' after the last digit.
    Matcher matcher = DENSITY_PATTERN.matcher(name);
    if (matcher.matches()) {
      configOut.densityDpi = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private static boolean parseTouchscreen(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.touchscreen = Configuration.TOUCHSCREEN_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "notouch")) {
      out.touchscreen = Configuration.TOUCHSCREEN_NOTOUCH;
      return true;
    } else if (Objects.equals(name, "stylus")) {
      out.touchscreen = Configuration.TOUCHSCREEN_STYLUS;
      return true;
    } else if (Objects.equals(name, "finger")) {
      out.touchscreen = Configuration.TOUCHSCREEN_FINGER;
      return true;
    }

    return false;
  }

  private static boolean parseKeysHidden(String name, Configuration out) {

    if (Objects.equals(name, WILDCARD_NAME)) {
      out.keyboardHidden = Configuration.KEYBOARDHIDDEN_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "keysexposed")) {
      out.keyboardHidden = Configuration.KEYBOARDHIDDEN_NO;
      return true;
    } else if (Objects.equals(name, "keyshidden")) {
      out.keyboardHidden = Configuration.KEYBOARDHIDDEN_YES;
      return true;
    } else if (Objects.equals(name, "keyssoft")) {
      out.keyboardHidden = Configuration.KEYBOARDHIDDEN_SOFT;
      return true;
    }
    return false;
  }

  private static boolean parseKeyboard(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.keyboard = Configuration.KEYBOARD_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "nokeys")) {
      out.keyboard = Configuration.KEYBOARD_NOKEYS;
      return true;
    } else if (Objects.equals(name, "qwerty")) {
      out.keyboard = Configuration.KEYBOARD_QWERTY;
      return true;
    } else if (Objects.equals(name, "12key")) {
      out.keyboard = Configuration.KEYBOARD_12KEY;
      return true;
    }

    return false;
  }

  private static boolean parseNavHidden(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.navigationHidden = Configuration.NAVIGATIONHIDDEN_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "navexposed")) {
      out.navigationHidden = Configuration.NAVIGATIONHIDDEN_NO;
      return true;
    } else if (Objects.equals(name, "navhidden")) {
      out.navigationHidden = Configuration.NAVIGATIONHIDDEN_YES;
      return true;
    }
    return false;
  }

  private static boolean parseNavigation(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.navigation = Configuration.NAVIGATION_UNDEFINED;
      return true;
    } else if (Objects.equals(name, "nonav")) {
      out.navigation = Configuration.NAVIGATION_NONAV;
      return true;
    } else if (Objects.equals(name, "dpad")) {
      out.navigation = Configuration.NAVIGATION_DPAD;
      return true;
    } else if (Objects.equals(name, "trackball")) {
      out.navigation = Configuration.NAVIGATION_TRACKBALL;
      return true;
    } else if (Objects.equals(name, "wheel")) {
      out.navigation = Configuration.NAVIGATION_WHEEL;
      return true;
    }

    return false;
  }

  private static boolean parseScreenSize(String name, DisplayMetrics out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.widthPixels = 0;
      out.heightPixels = 0;
      return true;
    }

    Matcher matcher = HEIGHT_WIDTH_PATTERN.matcher(name);
    if (matcher.matches()) {
      int w = Integer.parseInt(matcher.group(1));
      int h = Integer.parseInt(matcher.group(2));
      out.widthPixels = w;
      out.heightPixels = h;
      return true;
    }
    return false;
  }

  private static boolean parseMnc(String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.mnc = 0;
      return true;
    }

    Matcher matcher = MNC_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.mnc = Integer.parseInt(matcher.group(1));
      if (out.mnc == 0) {
        out.mnc = ACONFIGURATION_MNC_ZERO;
      }
      return true;
    }
    return false;
  }

  private static boolean parseMcc(final String name, Configuration out) {
    if (Objects.equals(name, WILDCARD_NAME)) {
      out.mcc = 0;
      return true;
    }

    Matcher matcher = MCC_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.mcc = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private static boolean parseVersion(String name, Configuration out) {
    // version is unused in Configuration, just advance to next token
    if (Objects.equals(name, WILDCARD_NAME)) {
      return true;
    }

    Matcher matcher = VERSION_QUALIFIER_PATTERN.matcher(name);
    if (matcher.matches()) {
      return true;
    }
    return false;
  }
}
