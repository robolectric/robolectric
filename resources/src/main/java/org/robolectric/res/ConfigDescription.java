package org.robolectric.res;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * From android/frameworks/base/tools/aapt2/ConfigDescription.cpp
 */
public class ConfigDescription {

  /**
   * Constant used to to represent MNC (Mobile Network Code) zero.
   * 0 cannot be used, since it is used to represent an undefined MNC.
   */
  private static final int ACONFIGURATION_MNC_ZERO = 0xffff;

  private static final String kWildcardName = "any";

  private static final Pattern MCC_PATTERN = Pattern.compile("mcc([\\d]+)");
  private static final Pattern MNC_PATTERN = Pattern.compile("mnc([\\d]+)");
  private static final Pattern SMALLEST_SCREEN_WIDTH_PATTERN = Pattern.compile("^sw([0-9]+)dp");
  private static final Pattern SCREEN_WIDTH_PATTERN = Pattern.compile("^w([0-9]+)dp");
  private static final Pattern SCREEN_HEIGHT_PATTERN = Pattern.compile("^h([0-9]+)dp");
  private static final Pattern DENSITY_PATTERN = Pattern.compile("^([0-9]+)dpi");

  public class LocaleValue {

  }

  boolean parse(final String str, ResTableConfig out) {
    PeekingIterator<String> part_iter = Iterators
        .peekingIterator(Arrays.asList(str.toLowerCase().split("-")).iterator());

    LocaleValue locale;

    boolean success = !part_iter.hasNext();
    if (part_iter.hasNext() && parseMcc(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseMnc(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    // Locale spans a few '-' separators, so we let it
    // control the index.
//        parts_consumed = locale.InitFromParts(part_iter, parts_end);
//        if (parts_consumed < 0) {
//            return false;
//        } else {
//            locale.WriteTo(config);
//            part_iter += parts_consumed;
//            if (!part_iter.hasNext()) {
//      goto success;
//            }
//        }

    if (part_iter.hasNext() && parseLayoutDirection(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseSmallestScreenWidthDp(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseScreenWidthDp(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseScreenHeightDp(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseScreenLayoutSize(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseScreenLayoutLong(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseScreenRound(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseWideColorGamut(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseHdr(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseOrientation(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseUiModeType(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseUiModeNight(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseDensity(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseTouchscreen(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseKeysHidden(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseKeyboard(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseNavHidden(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseNavigation(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseScreenSize(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (part_iter.hasNext() && parseVersion(part_iter.peek(), out)) {
      part_iter.next();
      if (!part_iter.hasNext()) {
        success = !part_iter.hasNext();
      }
    }

    if (!success) {
      // Unrecognized.
      return false;
    }

    if (out != null) {
      ApplyVersionForCompatibility(out);
    }
    return true;
  }

  private boolean parseLayoutDirection(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null) {
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_LAYOUTDIR) |
                ResTableConfig.LAYOUTDIR_ANY;
      }
      return true;
    } else if (Objects.equals(name, "ldltr")) {
      if (out != null) {
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_LAYOUTDIR) |
                ResTableConfig.LAYOUTDIR_LTR;
      }
      return true;
    } else if (Objects.equals(name, "ldrtl")) {
      if (out != null) {
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_LAYOUTDIR) |
                ResTableConfig.LAYOUTDIR_RTL;
      }
      return true;
    }

    return false;
  }

  private boolean parseSmallestScreenWidthDp(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null) {
        out.smallestScreenWidthDp = ResTableConfig.SCREENWIDTH_ANY;
      }
      return true;
    }

    Matcher matcher = SMALLEST_SCREEN_WIDTH_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.smallestScreenWidthDp = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private boolean parseScreenWidthDp(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null) {
        out.screenWidthDp = ResTableConfig.SCREENWIDTH_ANY;
      }
      return true;
    }

    Matcher matcher = SCREEN_WIDTH_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.screenWidthDp = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private boolean parseScreenHeightDp(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null) {
        out.screenHeightDp = ResTableConfig.SCREENWIDTH_ANY;
      }
      return true;
    }

    Matcher matcher = SCREEN_HEIGHT_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.screenHeightDp = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private boolean parseScreenLayoutSize(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null) {
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_SCREENSIZE) |
                ResTableConfig.SCREENSIZE_ANY;
      }
      return true;
    } else if (Objects.equals(name, "small")) {
      if (out != null) {
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_SCREENSIZE) |
                ResTableConfig.SCREENSIZE_SMALL;
      }
      return true;
    } else if (Objects.equals(name, "normal")) {
      if (out != null) {
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_SCREENSIZE) |
                ResTableConfig.SCREENSIZE_NORMAL;
      }
      return true;
    } else if (Objects.equals(name, "large")) {
      if (out != null) {
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_SCREENSIZE) |
                ResTableConfig.SCREENSIZE_LARGE;
      }
      return true;
    } else if (Objects.equals(name, "xlarge")) {
      if (out != null) {
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_SCREENSIZE) |
                ResTableConfig.SCREENSIZE_XLARGE;
      }
      return true;
    }

    return false;
  }

  private boolean parseScreenLayoutLong(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null)
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_SCREENLONG) |
                ResTableConfig.SCREENLONG_ANY;
      return true;
    } else if (Objects.equals(name, "long")) {
      if (out != null)
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_SCREENLONG) |
                ResTableConfig.SCREENLONG_YES;
      return true;
    } else if (Objects.equals(name, "notlong")) {
      if (out != null)
        out.screenLayout =
            (out.screenLayout & ~ResTableConfig.MASK_SCREENLONG) |
                ResTableConfig.SCREENLONG_NO;
      return true;
    }

    return false;
  }

  private boolean parseScreenRound(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null)
        out.screenLayout2 =
            (out.screenLayout2 & ~ResTableConfig.MASK_SCREENROUND) |
                ResTableConfig.SCREENROUND_ANY;
      return true;
    } else if (Objects.equals(name, "round")) {
      if (out != null)
        out.screenLayout2 =
            (out.screenLayout2 & ~ResTableConfig.MASK_SCREENROUND) |
                ResTableConfig.SCREENROUND_YES;
      return true;
    } else if (Objects.equals(name, "notround")) {
      if (out != null)
        out.screenLayout2 =
            (out.screenLayout2 & ~ResTableConfig.MASK_SCREENROUND) |
                ResTableConfig.SCREENROUND_NO;
      return true;
    }
    return false;
  }

  private boolean parseWideColorGamut(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null)
        out.colorMode =
            (out.colorMode & ~ResTableConfig.MASK_WIDE_COLOR_GAMUT) |
                ResTableConfig.WIDE_COLOR_GAMUT_ANY;
      return true;
    } else if (Objects.equals(name, "widecg")) {
      if (out != null)
        out.colorMode =
            (out.colorMode & ~ResTableConfig.MASK_WIDE_COLOR_GAMUT) |
                ResTableConfig.WIDE_COLOR_GAMUT_YES;
      return true;
    } else if (Objects.equals(name, "nowidecg")) {
      if (out != null)
        out.colorMode =
            (out.colorMode & ~ResTableConfig.MASK_WIDE_COLOR_GAMUT) |
                ResTableConfig.WIDE_COLOR_GAMUT_NO;
      return true;
    }
    return false;
  }

  private boolean parseHdr(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null)
        out.colorMode =
            (out.colorMode & ~ResTableConfig.MASK_HDR) |
                ResTableConfig.HDR_ANY;
      return true;
    } else if (Objects.equals(name, "highdr")) {
      if (out != null)
        out.colorMode =
            (out.colorMode & ~ResTableConfig.MASK_HDR) |
                ResTableConfig.HDR_YES;
      return true;
    } else if (Objects.equals(name, "lowdr")) {
      if (out != null)
        out.colorMode =
            (out.colorMode & ~ResTableConfig.MASK_HDR) |
                ResTableConfig.HDR_NO;
      return true;
    }
    return false;
  }

  private boolean parseOrientation(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
        if (out != null) out.orientation = out.ORIENTATION_ANY;
      return true;
    } else if (Objects.equals(name, "port")) {
      if (out != null) out.orientation = out.ORIENTATION_PORT;
      return true;
    } else if (Objects.equals(name, "land")) {
      if (out != null) out.orientation = out.ORIENTATION_LAND;
      return true;
    } else if (Objects.equals(name, "square")) {
      if (out != null) out.orientation = out.ORIENTATION_SQUARE;
      return true;
    }

    return false;
  }

  private boolean parseUiModeType(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_TYPE) |
            ResTableConfig.UI_MODE_TYPE_ANY;
      return true;
    } else if (Objects.equals(name, "desk")) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_TYPE) |
            ResTableConfig.UI_MODE_TYPE_DESK;
      return true;
    } else if (Objects.equals(name, "car")) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_TYPE) |
            ResTableConfig.UI_MODE_TYPE_CAR;
      return true;
    } else if (Objects.equals(name, "television")) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_TYPE) |
            ResTableConfig.UI_MODE_TYPE_TELEVISION;
      return true;
    } else if (Objects.equals(name, "appliance")) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_TYPE) |
            ResTableConfig.UI_MODE_TYPE_APPLIANCE;
      return true;
    } else if (Objects.equals(name, "watch")) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_TYPE) |
            ResTableConfig.UI_MODE_TYPE_WATCH;
      return true;
    } else if (Objects.equals(name, "vrheadset")) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_TYPE) |
            ResTableConfig.UI_MODE_TYPE_VR_HEADSET;
      return true;
    }

    return false;
  }

  private boolean parseUiModeNight(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_NIGHT) |
            ResTableConfig.UI_MODE_NIGHT_ANY;
      return true;
    } else if (Objects.equals(name, "night")) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_NIGHT) |
            ResTableConfig.UI_MODE_NIGHT_YES;
      return true;
    } else if (Objects.equals(name, "notnight")) {
      if (out != null)
        out.uiMode = (out.uiMode & ~ResTableConfig.MASK_UI_MODE_NIGHT) |
            ResTableConfig.UI_MODE_NIGHT_NO;
      return true;
    }

    return false;
  }

  private boolean parseDensity(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null) out.density = ResTableConfig.DENSITY_DEFAULT;
      return true;
    }

    if (Objects.equals(name, "anydpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_ANY;
      return true;
    }

    if (Objects.equals(name, "nodpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_NONE;
      return true;
    }

    if (Objects.equals(name, "ldpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_LOW;
      return true;
    }

    if (Objects.equals(name, "mdpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_MEDIUM;
      return true;
    }

    if (Objects.equals(name, "tvdpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_TV;
      return true;
    }

    if (Objects.equals(name, "hdpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_HIGH;
      return true;
    }

    if (Objects.equals(name, "xhdpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_XHIGH;
      return true;
    }

    if (Objects.equals(name, "xxhdpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_XXHIGH;
      return true;
    }

    if (Objects.equals(name, "xxxhdpi")) {
      if (out != null) out.density = ResTableConfig.DENSITY_XXXHIGH;
      return true;
    }

    Matcher matcher = DENSITY_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.density = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private boolean parseTouchscreen(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseKeysHidden(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseKeyboard(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseNavHidden(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseNavigation(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseScreenSize(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseVersion(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseMnc(String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null) {
        out.mnc = 0;
      }
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

  private static boolean parseMcc(final String name, ResTableConfig out) {
    if (Objects.equals(name, kWildcardName)) {
      if (out != null) {
        out.mcc = 0;
      }
      return true;
    }

    Matcher matcher = MCC_PATTERN.matcher(name);
    if (matcher.matches()) {
      out.mcc = Integer.parseInt(matcher.group(1));
      return true;
    }
    return false;
  }

  private void ApplyVersionForCompatibility(ResTableConfig out) {

  }
}
