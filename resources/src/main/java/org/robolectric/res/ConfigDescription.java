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
    return false;
  }

  private boolean parseScreenHeightDp(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseScreenLayoutSize(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseScreenLayoutLong(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseScreenRound(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseWideColorGamut(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseHdr(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseOrientation(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseUiModeType(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseUiModeNight(String name, ResTableConfig out) {
    return false;
  }

  private boolean parseDensity(String name, ResTableConfig out) {
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
