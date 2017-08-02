package org.robolectric.res.android;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes a particular resource configuration.
 *
 * <p>frameworks/base/include/androidfw/ResourceTypes.h (struct ResTable_config)
 */
public class ResTableConfig {

  // Codes for specially handled languages and regions
  static final byte[] kEnglish = new byte[] {'e', 'n'};  // packed version of "en"
  static final byte[] kUnitedStates = new byte[] {'U', 'S'};  // packed version of "US"
  static final byte[] kFilipino = new byte[] {(byte)0xAD, 0x05};  // packed version of "fil" ported from C {'\xAD', '\x05'}
  static final byte[] kTagalog = new byte[] {'t', 'l'};  // packed version of "tl"

  /** The different types of configs that can be present in a {@link ResTableConfig}. */
  public enum Type {
    MCC,
    MNC,
    LANGUAGE_STRING,
    REGION_STRING,
    SCREEN_LAYOUT_DIRECTION,
    SMALLEST_SCREEN_WIDTH_DP,
    SCREEN_WIDTH_DP,
    SCREEN_HEIGHT_DP,
    SCREEN_LAYOUT_SIZE,
    SCREEN_LAYOUT_LONG,
    SCREEN_LAYOUT_ROUND,
    ORIENTATION,
    UI_MODE_TYPE,
    UI_MODE_NIGHT,
    DENSITY_DPI,
    TOUCHSCREEN,
    KEYBOARD_HIDDEN,
    KEYBOARD,
    NAVIGATION_HIDDEN,
    NAVIGATION,
    SDK_VERSION
  }

  /** The below constants are from android.content.res.Configuration. */
  private static final int DENSITY_DPI_UNDEFINED = 0;
  private static final int DENSITY_DPI_LDPI = 120;
  private static final int DENSITY_DPI_MDPI = 160;
  private static final int DENSITY_DPI_TVDPI = 213;
  private static final int DENSITY_DPI_HDPI = 240;
  private static final int DENSITY_DPI_XHDPI = 320;
  private static final int DENSITY_DPI_XXHDPI = 480;
  private static final int DENSITY_DPI_XXXHDPI = 640;
  private static final int DENSITY_DPI_ANY  = 0xFFFE;
  private static final int DENSITY_DPI_NONE = 0xFFFF;
  private static final Map<Integer, String> DENSITY_DPI_VALUES =
      ImmutableMap.<Integer, String>builder()
          .put(DENSITY_DPI_UNDEFINED, "")
          .put(DENSITY_DPI_LDPI, "ldpi")
          .put(DENSITY_DPI_MDPI, "mdpi")
          .put(DENSITY_DPI_TVDPI, "tvdpi")
          .put(DENSITY_DPI_HDPI, "hdpi")
          .put(DENSITY_DPI_XHDPI, "xhdpi")
          .put(DENSITY_DPI_XXHDPI, "xxhdpi")
          .put(DENSITY_DPI_XXXHDPI, "xxxhdpi")
          .put(DENSITY_DPI_ANY, "anydpi")
          .put(DENSITY_DPI_NONE, "nodpi")
          .build();

  private static final int KEYBOARD_NOKEYS = 1;
  private static final int KEYBOARD_QWERTY = 2;
  private static final int KEYBOARD_12KEY  = 3;
  private static final Map<Integer, String> KEYBOARD_VALUES = ImmutableMap.of(
      KEYBOARD_NOKEYS, "nokeys",
      KEYBOARD_QWERTY, "qwerty",
      KEYBOARD_12KEY, "12key");

  private static final int KEYBOARDHIDDEN_MASK = 0x03;
  private static final int KEYBOARDHIDDEN_NO   = 1;
  private static final int KEYBOARDHIDDEN_YES  = 2;
  private static final int KEYBOARDHIDDEN_SOFT = 3;
  private static final Map<Integer, String> KEYBOARDHIDDEN_VALUES = ImmutableMap.of(
      KEYBOARDHIDDEN_NO, "keysexposed",
      KEYBOARDHIDDEN_YES, "keyshidden",
      KEYBOARDHIDDEN_SOFT, "keyssoft");

  private static final int NAVIGATION_NONAV     = 1;
  private static final int NAVIGATION_DPAD      = 2;
  private static final int NAVIGATION_TRACKBALL = 3;
  private static final int NAVIGATION_WHEEL     = 4;
  private static final Map<Integer, String> NAVIGATION_VALUES = ImmutableMap.of(
      NAVIGATION_NONAV, "nonav",
      NAVIGATION_DPAD, "dpad",
      NAVIGATION_TRACKBALL, "trackball",
      NAVIGATION_WHEEL, "wheel");

  private static final int NAVIGATIONHIDDEN_MASK  = 0x0C;
  private static final int NAVIGATIONHIDDEN_NO    = 0x04;
  private static final int NAVIGATIONHIDDEN_YES   = 0x08;
  private static final Map<Integer, String> NAVIGATIONHIDDEN_VALUES = ImmutableMap.of(
      NAVIGATIONHIDDEN_NO, "navexposed",
      NAVIGATIONHIDDEN_YES, "navhidden");

  private static final int ORIENTATION_PORTRAIT  = 0x01;
  private static final int ORIENTATION_LANDSCAPE = 0x02;
  private static final Map<Integer, String> ORIENTATION_VALUES = ImmutableMap.of(
      ORIENTATION_PORTRAIT, "port",
      ORIENTATION_LANDSCAPE, "land");

  private static final int SCREENLAYOUT_LAYOUTDIR_MASK = 0xC0;
  static final int SCREENLAYOUT_LAYOUTDIR_LTR  = 0x40;
  static final int SCREENLAYOUT_LAYOUTDIR_RTL  = 0x80;
  private static final Map<Integer, String> SCREENLAYOUT_LAYOUTDIR_VALUES = ImmutableMap.of(
      SCREENLAYOUT_LAYOUTDIR_LTR, "ldltr",
      SCREENLAYOUT_LAYOUTDIR_RTL, "ldrtl");

  private static final int SCREENLAYOUT_LONG_MASK = 0x30;
  private static final int SCREENLAYOUT_LONG_NO   = 0x10;
  private static final int SCREENLAYOUT_LONG_YES  = 0x20;
  private static final Map<Integer, String> SCREENLAYOUT_LONG_VALUES = ImmutableMap.of(
      SCREENLAYOUT_LONG_NO, "notlong",
      SCREENLAYOUT_LONG_YES, "long");

  private static final int SCREENLAYOUT_ROUND_MASK = 0x0300;
  private static final int SCREENLAYOUT_ROUND_NO   = 0x0100;
  private static final int SCREENLAYOUT_ROUND_YES  = 0x0200;
  private static final Map<Integer, String> SCREENLAYOUT_ROUND_VALUES = ImmutableMap.of(
      SCREENLAYOUT_ROUND_NO, "notround",
      SCREENLAYOUT_ROUND_YES, "round");

  private static final int SCREENLAYOUT_SIZE_MASK   = 0x0F;
  private static final int SCREENLAYOUT_SIZE_SMALL  = 0x01;
  private static final int SCREENLAYOUT_SIZE_NORMAL = 0x02;
  private static final int SCREENLAYOUT_SIZE_LARGE  = 0x03;
  private static final int SCREENLAYOUT_SIZE_XLARGE = 0x04;
  private static final Map<Integer, String> SCREENLAYOUT_SIZE_VALUES = ImmutableMap.of(
      SCREENLAYOUT_SIZE_SMALL, "small",
      SCREENLAYOUT_SIZE_NORMAL, "normal",
      SCREENLAYOUT_SIZE_LARGE, "large",
      SCREENLAYOUT_SIZE_XLARGE, "xlarge");

  private static final int TOUCHSCREEN_NOTOUCH = 1;
  private static final int TOUCHSCREEN_FINGER  = 3;
  private static final Map<Integer, String> TOUCHSCREEN_VALUES = ImmutableMap.of(
      TOUCHSCREEN_NOTOUCH, "notouch",
      TOUCHSCREEN_FINGER, "finger");

  private static final int UI_MODE_NIGHT_MASK = 0x30;
  private static final int UI_MODE_NIGHT_NO   = 0x10;
  private static final int UI_MODE_NIGHT_YES  = 0x20;
  private static final Map<Integer, String> UI_MODE_NIGHT_VALUES = ImmutableMap.of(
      UI_MODE_NIGHT_NO, "notnight",
      UI_MODE_NIGHT_YES, "night");

  private static final int UI_MODE_TYPE_MASK       = 0x0F;
  private static final int UI_MODE_TYPE_DESK       = 0x02;
  private static final int UI_MODE_TYPE_CAR        = 0x03;
  private static final int UI_MODE_TYPE_TELEVISION = 0x04;
  private static final int UI_MODE_TYPE_APPLIANCE  = 0x05;
  private static final int UI_MODE_TYPE_WATCH      = 0x06;
  private static final Map<Integer, String> UI_MODE_TYPE_VALUES = ImmutableMap.of(
      UI_MODE_TYPE_DESK, "desk",
      UI_MODE_TYPE_CAR, "car",
      UI_MODE_TYPE_TELEVISION, "television",
      UI_MODE_TYPE_APPLIANCE, "appliance",
      UI_MODE_TYPE_WATCH, "watch");

  /** The number of bytes that this resource configuration takes up. */
  private final int size;

  private final int mcc;
  private final int mnc;

  /** Returns a packed 2-byte language code. */
  @SuppressWarnings("mutable")
  private final byte[] language;

  /** Returns {@link #language} as an unpacked string representation. */
  public final String languageString() {
    return unpackLanguage();
  }

  /** Returns a packed 2-byte country code. */
  @SuppressWarnings("mutable")
  private final byte[] country;

  /** Returns {@link #country} as an unpacked string representation. */
  public final String regionString() {
    return unpackRegion();
  }

  private final int orientation;
  private final int touchscreen;
  private final int density;
  private final int keyboard;
  private final int navigation;
  private final int inputFlags;

  public final int keyboardHidden() {
    return inputFlags & KEYBOARDHIDDEN_MASK;
  }

  public final int navigationHidden() {
    return inputFlags & NAVIGATIONHIDDEN_MASK;
  }

  private final int screenWidth;
  private final int screenHeight;
  private final int sdkVersion;

  /**
   * Returns a copy of this resource configuration with a different {@link #sdkVersion}, or this
   * configuration if the {@code sdkVersion} is the same.
   *
   * @param sdkVersion The SDK version of the returned configuration.
   * @return A copy of this configuration with the only difference being #sdkVersion.
   */
  public final ResTableConfig withSdkVersion(int sdkVersion) {
    if (sdkVersion == this.sdkVersion) {
      return this;
    }
    return new ResTableConfig(size, mcc, mnc, language, country,
        orientation, touchscreen, density, keyboard, navigation, inputFlags,
        screenWidth, screenHeight, sdkVersion, minorVersion, screenLayout, uiMode,
        smallestScreenWidthDp, screenWidthDp, screenHeightDp, localeScript, localeVariant,
        screenLayout2, unknown);
  }

  public ResTableConfig(int size, int mcc, int mnc, byte[] language, byte[] country,
      int orientation, int touchscreen, int density, int keyboard, int navigation, int inputFlags,
      int screenWidth, int screenHeight, int sdkVersion, int minorVersion, int screenLayout,
      int uiMode, int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      byte[] localeScript, byte[] localeVariant, int screenLayout2, byte[] unknown) {
    this.size = size;
    this.mcc = mcc;
    this.mnc = mnc;
    this.language = language;
    this.country = country;
    this.orientation = orientation;
    this.touchscreen = touchscreen;
    this.density = density;
    this.keyboard = keyboard;
    this.navigation = navigation;
    this.inputFlags = inputFlags;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.sdkVersion = sdkVersion;
    this.minorVersion = minorVersion;
    this.screenLayout = screenLayout;
    this.uiMode = uiMode;
    this.smallestScreenWidthDp = smallestScreenWidthDp;
    this.screenWidthDp = screenWidthDp;
    this.screenHeightDp = screenHeightDp;
    this.localeScript = localeScript;
    this.localeVariant = localeVariant;
    this.screenLayout2 = screenLayout2;
    this.unknown = unknown;
  }

  private final int minorVersion;
  private final int screenLayout;

  public final int screenLayoutDirection() {
    return screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK;
  }

  public final int screenLayoutSize() {
    return screenLayout & SCREENLAYOUT_SIZE_MASK;
  }

  public final int screenLayoutLong() {
    return screenLayout & SCREENLAYOUT_LONG_MASK;
  }

  public final int screenLayoutRound() {
    return screenLayout & SCREENLAYOUT_ROUND_MASK;
  }

  private final int uiMode;

  public final int uiModeType() {
    return uiMode & UI_MODE_TYPE_MASK;
  }

  public final int uiModeNight() {
    return uiMode & UI_MODE_NIGHT_MASK;
  }

  private final int smallestScreenWidthDp;
  private final int screenWidthDp;
  private final int screenHeightDp;

  /** The ISO-15924 short name for the script corresponding to this configuration. */
  @SuppressWarnings("mutable")
  private final byte[] localeScript;

  /** A single BCP-47 variant subtag. */
  @SuppressWarnings("mutable")
  private final byte[] localeVariant;

  /** An extension to {@link #screenLayout}. Contains round/notround qualifier. */
  private final int screenLayout2;

  /** Any remaining bytes in this resource configuration that are unaccounted for. */
  @SuppressWarnings("mutable")
  private final byte[] unknown;

  private String unpackLanguage() {
    return unpackLanguageOrRegion(language, 0x61);
  }

  private String unpackRegion() {
    return unpackLanguageOrRegion(country, 0x30);
  }

  private String unpackLanguageOrRegion(byte[] value, int base) {
    Preconditions.checkState(value.length == 2, "Language or country value must be 2 bytes.");
    if (value[0] == 0 && value[1] == 0) {
      return "";
    }
    if ((UnsignedBytes.toInt(value[0]) & 0x80) != 0) {
      byte[] result = new byte[3];
      result[0] = (byte) (base + (value[1] & 0x1F));
      result[1] = (byte) (base + ((value[1] & 0xE0) >>> 5) + ((value[0] & 0x03) << 3));
      result[2] = (byte) (base + ((value[0] & 0x7C) >>> 2));
      return new String(result, US_ASCII);
    }
    return new String(value, US_ASCII);
  }

  /** Returns true if this is the default "any" configuration. */
  public final boolean isDefault() {
    return mcc == 0
        && mnc == 0
        && Arrays.equals(language, new byte[2])
        && Arrays.equals(country, new byte[2])
        && orientation == 0
        && touchscreen == 0
        && density == 0
        && keyboard == 0
        && navigation == 0
        && inputFlags == 0
        && screenWidth == 0
        && screenHeight == 0
        && sdkVersion == 0
        && minorVersion == 0
        && screenLayout == 0
        && uiMode == 0
        && smallestScreenWidthDp == 0
        && screenWidthDp == 0
        && screenHeightDp == 0
        && Arrays.equals(localeScript, new byte[4])
        && Arrays.equals(localeVariant, new byte[8])
        && screenLayout2 == 0;
  }
  
  @Override
  public final String toString() {
    if (isDefault()) {  // Prevent the default configuration from returning the empty string
      return "default";
    }
    Collection<String> parts = toStringParts().values();
    parts.removeAll(Collections.singleton(""));
    return Joiner.on('-').join(parts);
  }

  /**
   * Returns a map of the configuration parts for {@link #toString}.
   *
   * <p>If a configuration part is not defined for this {@link ResTableConfig}, its value
   * will be the empty string.
   */
  public final Map<Type, String> toStringParts() {
    Map<Type, String> result = new LinkedHashMap<>();  // Preserve order for #toString().
    result.put(Type.MCC, mcc != 0 ? "mcc" + mcc : "");
    result.put(Type.MNC, mnc != 0 ? "mnc" + mnc : "");
    result.put(Type.LANGUAGE_STRING, !languageString().isEmpty() ? "" + languageString() : "");
    result.put(Type.REGION_STRING, !regionString().isEmpty() ? "r" + regionString() : "");
    result.put(Type.SCREEN_LAYOUT_DIRECTION,
        getOrDefault(SCREENLAYOUT_LAYOUTDIR_VALUES, screenLayoutDirection(), ""));
    result.put(Type.SMALLEST_SCREEN_WIDTH_DP,
        smallestScreenWidthDp != 0 ? "sw" + smallestScreenWidthDp + "dp" : "");
    result.put(Type.SCREEN_WIDTH_DP, screenWidthDp != 0 ? "w" + screenWidthDp + "dp" : "");
    result.put(Type.SCREEN_HEIGHT_DP, screenHeightDp != 0 ? "h" + screenHeightDp + "dp" : "");
    result.put(Type.SCREEN_LAYOUT_SIZE,
        getOrDefault(SCREENLAYOUT_SIZE_VALUES, screenLayoutSize(), ""));
    result.put(Type.SCREEN_LAYOUT_LONG,
        getOrDefault(SCREENLAYOUT_LONG_VALUES, screenLayoutLong(), ""));
    result.put(Type.SCREEN_LAYOUT_ROUND,
        getOrDefault(SCREENLAYOUT_ROUND_VALUES, screenLayoutRound(), ""));
    result.put(Type.ORIENTATION, getOrDefault(ORIENTATION_VALUES, orientation, ""));
    result.put(Type.UI_MODE_TYPE, getOrDefault(UI_MODE_TYPE_VALUES, uiModeType(), ""));
    result.put(Type.UI_MODE_NIGHT, getOrDefault(UI_MODE_NIGHT_VALUES, uiModeNight(), ""));
    result.put(Type.DENSITY_DPI, getOrDefault(DENSITY_DPI_VALUES, density, density + "dpi"));
    result.put(Type.TOUCHSCREEN, getOrDefault(TOUCHSCREEN_VALUES, touchscreen, ""));
    result.put(Type.KEYBOARD_HIDDEN, getOrDefault(KEYBOARDHIDDEN_VALUES, keyboardHidden(), ""));
    result.put(Type.KEYBOARD, getOrDefault(KEYBOARD_VALUES, keyboard, ""));
    result.put(Type.NAVIGATION_HIDDEN,
        getOrDefault(NAVIGATIONHIDDEN_VALUES, navigationHidden(), ""));
    result.put(Type.NAVIGATION, getOrDefault(NAVIGATION_VALUES, navigation, ""));
    result.put(Type.SDK_VERSION, sdkVersion != 0 ? "v" + sdkVersion : "");
    return result;
  }

  private <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
    // TODO(acornwall): Remove this when Java 8's Map#getOrDefault is available.
    // Null is not returned, even if the map contains a key whose value is null. This is intended.
    V value = map.get(key);
    return value != null ? value : defaultValue;
  }


  // constants for isBetterThan...
  private static final int MASK_LAYOUTDIR = SCREENLAYOUT_LAYOUTDIR_MASK;
  private static final int MASK_SCREENSIZE = SCREENLAYOUT_SIZE_MASK;
  private static final int ACONFIGURATION_SCREENSIZE_NORMAL = SCREENLAYOUT_SIZE_NORMAL;
  private static final int SCREENSIZE_NORMAL = ACONFIGURATION_SCREENSIZE_NORMAL;
  private static final int MASK_SCREENLONG = SCREENLAYOUT_LONG_MASK;
  private static final int MASK_SCREENROUND = SCREENLAYOUT_ROUND_MASK;
  private static final int MASK_UI_MODE_TYPE = UI_MODE_TYPE_MASK;
  private static final int MASK_UI_MODE_NIGHT = UI_MODE_NIGHT_MASK;
  private static final int ACONFIGURATION_DENSITY_MEDIUM = DENSITY_DPI_MDPI;
  private static final int DENSITY_MEDIUM = ACONFIGURATION_DENSITY_MEDIUM;
  private static final int ACONFIGURATION_DENSITY_ANY = DENSITY_DPI_ANY;
  private static final int DENSITY_ANY = ACONFIGURATION_DENSITY_ANY;
  private static final int MASK_KEYSHIDDEN = 0x0003;
  public static final int MASK_NAVHIDDEN = 0x000c;


  /**
   * Is {@code requested} a better match to this {@link ResTableConfig} object than {@code o}
   */
  boolean isBetterThan(ResTableConfig o, ResTableConfig requested) {
    if (requested != null) {
      if (imsi() != 0 || o.imsi() != 0) {
        if ((mcc != o.mcc) && requested.mcc != 0) {
          return (mcc != 0);
        }

        if ((mnc != o.mnc) && requested.mnc != 0) {
          return (mnc != 0);
        }
      }

      if (isLocaleBetterThan(o, requested)) {
        return true;
      }

      if (screenLayout != 0 || o.screenLayout != 0) {
        if (((screenLayout^o.screenLayout) & MASK_LAYOUTDIR) != 0
            && (requested.screenLayout & MASK_LAYOUTDIR) != 0) {
          int myLayoutDir = screenLayout & MASK_LAYOUTDIR;
          int oLayoutDir = o.screenLayout & MASK_LAYOUTDIR;
          return (myLayoutDir > oLayoutDir);
        }
      }

      if (smallestScreenWidthDp != 0 || o.smallestScreenWidthDp != 0) {
        // The configuration closest to the actual size is best.
        // We assume that larger configs have already been filtered
        // out at this point.  That means we just want the largest one.
        if (smallestScreenWidthDp != o.smallestScreenWidthDp) {
          return smallestScreenWidthDp > o.smallestScreenWidthDp;
        }
      }

      if (screenSizeDp() != 0 || o.screenSizeDp() != 0) {
        // "Better" is based on the sum of the difference between both
        // width and height from the requested dimensions.  We are
        // assuming the invalid configs (with smaller dimens) have
        // already been filtered.  Note that if a particular dimension
        // is unspecified, we will end up with a large value (the
        // difference between 0 and the requested dimension), which is
        // good since we will prefer a config that has specified a
        // dimension value.
        int myDelta = 0, otherDelta = 0;
        if (requested.screenWidthDp != 0) {
          myDelta += requested.screenWidthDp - screenWidthDp;
          otherDelta += requested.screenWidthDp - o.screenWidthDp;
        }
        if (requested.screenHeightDp != 0) {
          myDelta += requested.screenHeightDp - screenHeightDp;
          otherDelta += requested.screenHeightDp - o.screenHeightDp;
        }

        if (myDelta != otherDelta) {
          return myDelta < otherDelta;
        }
      }

      if (screenLayout != 0 || o.screenLayout != 0) {
        if (((screenLayout^o.screenLayout) & MASK_SCREENSIZE) != 0
            && (requested.screenLayout & MASK_SCREENSIZE) != 0) {
          // A little backwards compatibility here: undefined is
          // considered equivalent to normal.  But only if the
          // requested size is at least normal; otherwise, small
          // is better than the default.
          int mySL = (screenLayout & MASK_SCREENSIZE);
          int oSL = (o.screenLayout & MASK_SCREENSIZE);
          int fixedMySL = mySL;
          int fixedOSL = oSL;
          if ((requested.screenLayout & MASK_SCREENSIZE) >= SCREENSIZE_NORMAL) {
            if (fixedMySL == 0) fixedMySL = SCREENSIZE_NORMAL;
            if (fixedOSL == 0) fixedOSL = SCREENSIZE_NORMAL;
          }
          // For screen size, the best match is the one that is
          // closest to the requested screen size, but not over
          // (the not over part is dealt with in match() below).
          if (fixedMySL == fixedOSL) {
            // If the two are the same, but 'this' is actually
            // undefined, then the other is really a better match.
            if (mySL == 0) return false;
            return true;
          }
          if (fixedMySL != fixedOSL) {
            return fixedMySL > fixedOSL;
          }
        }
        if (((screenLayout^o.screenLayout) & MASK_SCREENLONG) != 0
            && (requested.screenLayout & MASK_SCREENLONG) != 0) {
          return (screenLayout & MASK_SCREENLONG) != 0;
        }
      }

      if (screenLayout2 != 0 || o.screenLayout2 != 0) {
        if (((screenLayout2^o.screenLayout2) & MASK_SCREENROUND) != 0 &&
            (requested.screenLayout2 & MASK_SCREENROUND) != 0) {
          return (screenLayout2 & MASK_SCREENROUND) != 0;
        }
      }

      if ((orientation != o.orientation) && requested.orientation != 0) {
        return (orientation) != 0;
      }

      if (uiMode != 0 || o.uiMode != 0) {
        if (((uiMode^o.uiMode) & MASK_UI_MODE_TYPE) != 0
            && (requested.uiMode & MASK_UI_MODE_TYPE) != 0) {
          return (uiMode & MASK_UI_MODE_TYPE) != 0;
        }
        if (((uiMode^o.uiMode) & MASK_UI_MODE_NIGHT) != 0
            && (requested.uiMode & MASK_UI_MODE_NIGHT) != 0) {
          return (uiMode & MASK_UI_MODE_NIGHT) != 0;
        }
      }

      if (screenType() != 0 || o.screenType() != 0) {
        if (density != o.density) {
          // Use the system default density (DENSITY_MEDIUM, 160dpi) if none specified.
                final int thisDensity = density != 0 ? density : DENSITY_MEDIUM;
                final int otherDensity = o.density != 0 ? o.density : DENSITY_MEDIUM;

          // We always prefer DENSITY_ANY over scaling a density bucket.
          if (thisDensity == DENSITY_ANY) {
            return true;
          } else if (otherDensity == DENSITY_ANY) {
            return false;
          }

          int requestedDensity = requested.density;
          if (requested.density == 0 ||
              requested.density == DENSITY_ANY) {
            requestedDensity = DENSITY_MEDIUM;
          }

          // DENSITY_ANY is now dealt with. We should look to
          // pick a density bucket and potentially scale it.
          // Any density is potentially useful
          // because the system will scale it.  Scaling down
          // is generally better than scaling up.
          int h = thisDensity;
          int l = otherDensity;
          boolean bImBigger = true;
          if (l > h) {
            int t = h;
            h = l;
            l = t;
            bImBigger = false;
          }

          if (requestedDensity >= h) {
            // requested value higher than both l and h, give h
            return bImBigger;
          }
          if (l >= requestedDensity) {
            // requested value lower than both l and h, give l
            return !bImBigger;
          }
          // saying that scaling down is 2x better than up
          if (((2 * l) - requestedDensity) * h > requestedDensity * requestedDensity) {
            return !bImBigger;
          } else {
            return bImBigger;
          }
        }

        if ((touchscreen != o.touchscreen) && requested.touchscreen != 0) {
          return (touchscreen) != 0;
        }
      }

      if (input() != 0 || o.input() != 0) {
            final int keysHidden = inputFlags & MASK_KEYSHIDDEN;
            final int oKeysHidden = o.inputFlags & MASK_KEYSHIDDEN;
        if (keysHidden != oKeysHidden) {
                final int reqKeysHidden =
              requested.inputFlags & MASK_KEYSHIDDEN;
          if (reqKeysHidden != 0) {

            if (keysHidden == 0) return false;
            if (oKeysHidden == 0) return true;
            // For compatibility, we count KEYSHIDDEN_NO as being
            // the same as KEYSHIDDEN_SOFT.  Here we disambiguate
            // these by making an exact match more specific.
            if (reqKeysHidden == keysHidden) return true;
            if (reqKeysHidden == oKeysHidden) return false;
          }
        }

            final int navHidden = inputFlags & MASK_NAVHIDDEN;
            final int oNavHidden = o.inputFlags & MASK_NAVHIDDEN;
        if (navHidden != oNavHidden) {
                final int reqNavHidden =
              requested.inputFlags & MASK_NAVHIDDEN;
          if (reqNavHidden != 0) {

            if (navHidden == 0) return false;
            if (oNavHidden == 0) return true;
          }
        }

        if ((keyboard != o.keyboard) && requested.keyboard != 0) {
          return (keyboard) != 0;
        }

        if ((navigation != o.navigation) && requested.navigation != 0) {
          return (navigation) != 0;
        }
      }

      if (screenSize() != 0 || o.screenSize() != 0) {
        // "Better" is based on the sum of the difference between both
        // width and height from the requested dimensions.  We are
        // assuming the invalid configs (with smaller sizes) have
        // already been filtered.  Note that if a particular dimension
        // is unspecified, we will end up with a large value (the
        // difference between 0 and the requested dimension), which is
        // good since we will prefer a config that has specified a
        // size value.
        int myDelta = 0, otherDelta = 0;
        if (requested.screenWidth != 0) {
          myDelta += requested.screenWidth - screenWidth;
          otherDelta += requested.screenWidth - o.screenWidth;
        }
        if (requested.screenHeight != 0) {
          myDelta += requested.screenHeight - screenHeight;
          otherDelta += requested.screenHeight - o.screenHeight;
        }
        if (myDelta != otherDelta) {
          return myDelta < otherDelta;
        }
      }

      if (version() != 0 || o.version() != 0) {
        if ((sdkVersion != o.sdkVersion) && requested.sdkVersion != 0) {
          return (sdkVersion > o.sdkVersion);
        }

        if ((minorVersion != o.minorVersion) &&
            requested.minorVersion != 0) {
          return (minorVersion) != 0;
        }
      }

      return false;
    }
    return isMoreSpecificThan(o);
  }

  /**
   *     union {
   struct {
   // Mobile country code (from SIM).  0 means "any".
   uint16_t mcc;
   // Mobile network code (from SIM).  0 means "any".
   uint16_t mnc;
   };
   uint32_t imsi;
   };
   */
  private int imsi() {
    return (mcc & 0xffff) << 16 | (mnc & 0xffff);
  }

  /**
   *     union {
   struct {
   uint16_t screenWidth;
   uint16_t screenHeight;
   };
   uint32_t screenSize;
   };
   */
  private int screenSize() {
    return (screenWidth & 0xffff) << 16 | (screenHeight & 0xffff);
  }


  /**
   *     union {
   struct {
   uint16_t screenWidthDp;
   uint16_t screenHeightDp;
   };
   uint32_t screenSizeDp;
   };
   */
  private int screenSizeDp() {
    // screenWidthDp and screenHeightDp are really shorts...
    return (screenWidthDp & 0xffff) << 16 | (screenHeightDp & 0xffff);
  }

  /**
     union {
     struct {
     uint8_t orientation;
     uint8_t touchscreen;
     uint16_t density;
     };
     uint32_t screenType;
     };
   */
  private int screenType() {
    return (orientation & 0xff << 24) | (touchscreen * 0xff << 16) | density & 0xffff;
  }

  /**
   *
   union {
   struct {
   uint8_t keyboard;
   uint8_t navigation;
   uint8_t inputFlags;
   uint8_t inputPad0;
   };
   uint32_t input;
   };
   */
  private int input() {
    // TODO is Pad Zeros?
    return (keyboard & 0xff << 24) | (navigation & 0xff << 16) | (inputFlags & 0xff << 8);
  }

  /**
   *     union {
   struct {
   uint16_t sdkVersion;
   // For now minorVersion must always be 0!!!  Its meaning
   // is currently undefined.
   uint16_t minorVersion;
   };
   uint32_t version;
   };
   */
  private int version() {
    return (sdkVersion & 0xffff) << 16 | (minorVersion & 0xffff);
  }

  /**
   union {
   struct {
   // This field can take three different forms:
   // - \0\0 means "any".
   //
   // - Two 7 bit ascii values interpreted as ISO-639-1 language
   //   codes ('fr', 'en' etc. etc.). The high bit for both bytes is
   //   zero.
   //
   // - A single 16 bit little endian packed value representing an
   //   ISO-639-2 3 letter language code. This will be of the form:
   //
   //   {1, t, t, t, t, t, s, s, s, s, s, f, f, f, f, f}
   //
   //   bit[0, 4] = first letter of the language code
   //   bit[5, 9] = second letter of the language code
   //   bit[10, 14] = third letter of the language code.
   //   bit[15] = 1 always
   //
   // For backwards compatibility, languages that have unambiguous
   // two letter codes are represented in that format.
   //
   // The layout is always bigendian irrespective of the runtime
   // architecture.
   char language[2];

   // This field can take three different forms:
   // - \0\0 means "any".
   //
   // - Two 7 bit ascii values interpreted as 2 letter country
   //   codes ('US', 'GB' etc.). The high bit for both bytes is zero.
   //
   // - An UN M.49 3 digit country code. For simplicity, these are packed
   //   in the same manner as the language codes, though we should need
   //   only 10 bits to represent them, instead of the 15.
   //
   // The layout is always bigendian irrespective of the runtime
   // architecture.
   char country[2];
   };
   uint32_t locale;
   };
   */
  int locale() {
    return (language[0] & 0xff << 24) | (language[1] * 0xff << 16) | (country[0] & 0xffff << 8) | (country[1] & 0xffff);
  }

  private boolean isLocaleBetterThan(ResTableConfig o, ResTableConfig requested) {
    if (requested.locale() == 0) {
      // The request doesn't have a locale, so no resource is better
      // than the other.
      return false;
    }

    if (locale() == 0 && o.locale() == 0) {
      // The locale part of both resources is empty, so none is better
      // than the other.
      return false;
    }

    // Non-matching locales have been filtered out, so both resources
    // match the requested locale.
    //
    // Because of the locale-related checks in match() and the checks, we know
    // that:
    // 1) The resource languages are either empty or match the request;
    // and
    // 2) If the request's script is known, the resource scripts are either
    //    unknown or match the request.

    if (!langsAreEquivalent(language, o.language)) {
      // The languages of the two resources are not equivalent. If we are
      // here, we can only assume that the two resources matched the request
      // because one doesn't have a language and the other has a matching
      // language.
      //
      // We consider the one that has the language specified a better match.
      //
      // The exception is that we consider no-language resources a better match
      // for US English and similar locales than locales that are a descendant
      // of Internatinal English (en-001), since no-language resources are
      // where the US English resource have traditionally lived for most apps.
      if (areIdentical(requested.language, kEnglish)) {
        if (areIdentical(requested.country, kUnitedStates)) {
          // For US English itself, we consider a no-locale resource a
          // better match if the other resource has a country other than
          // US specified.
          if (language[0] != '\0') {
            return country[0] == '\0' || areIdentical(country, kUnitedStates);
          } else {
            return !(o.country[0] == '\0' || areIdentical(o.country, kUnitedStates));
          }
        } else if (LocaleData.localeDataIsCloseToUsEnglish(requested.country)) {
          if (language[0] != '\0') {
            return LocaleData.localeDataIsCloseToUsEnglish(country);
          } else {
            return !LocaleData.localeDataIsCloseToUsEnglish(o.country);
          }
        }
      }
      return (language[0] != '\0');
    }

    // If we are here, both the resources have an equivalent non-empty language
    // to the request.
    //
    // Because the languages are equivalent, computeScript() always returns a
    // non-empty script for languages it knows about, and we have passed the
    // script checks in match(), the scripts are either all unknown or are all
    // the same. So we can't gain anything by checking the scripts. We need to
    // check the country and variant.

    // See if any of the regions is better than the other.
    final int region_comparison = LocaleData.localeDataCompareRegions(
        country, o.country,
        requested.language, str(requested.localeScript), requested.country);
    if (region_comparison != 0) {
      return (region_comparison > 0);
    }

    // The regions are the same. Try the variant.
    final boolean localeMatches = Arrays.equals(localeVariant, requested.localeVariant);
    final boolean otherMatches = Arrays.equals(o.localeVariant, requested.localeVariant);
    if (localeMatches != otherMatches) {
      return localeMatches;
    }

    // Finally, the languages, although equivalent, may still be different
    // (like for Tagalog and Filipino). Identical is better than just
    // equivalent.
    if (areIdentical(language, requested.language)
        && !areIdentical(o.language, requested.language)) {
      return true;
    }

    return false;
  }

  private String str(byte[] country) {
    return new String(country, UTF_8);
  }

  private boolean langsAreEquivalent(final byte[] lang1, final byte[] lang2) {
    return areIdentical(lang1, lang2) ||
        (areIdentical(lang1, kTagalog) && areIdentical(lang2, kFilipino)) ||
        (areIdentical(lang1, kFilipino) && areIdentical(lang2, kTagalog));
  }

  // Checks if two language or country codes are identical
  private boolean  areIdentical(final byte[] code1, final byte[] code2) {
    return code1[0] == code2[0] && code1[1] == code2[1];
  }

  // TODO Convert from C
  private boolean isMoreSpecificThan(ResTableConfig o) {
    return false;
  }

}

