package org.robolectric.res.android;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_DEFAULT;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_HIGH;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_LOW;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_MEDIUM;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_NONE;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_TV;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_XHIGH;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_XXHIGH;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_DENSITY_XXXHIGH;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_HDR_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_HDR_NO;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_HDR_YES;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYBOARD_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYSHIDDEN_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYSHIDDEN_NO;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYSHIDDEN_SOFT;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYSHIDDEN_YES;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_LAYOUTDIR_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_LAYOUTDIR_LTR;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_LAYOUTDIR_RTL;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_NAVHIDDEN_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_NAVHIDDEN_NO;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_NAVHIDDEN_YES;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_NAVIGATION_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_ORIENTATION_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_ORIENTATION_LAND;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_ORIENTATION_PORT;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_ORIENTATION_SQUARE;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENLONG_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENLONG_NO;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENLONG_YES;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENROUND_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENROUND_NO;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENROUND_YES;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENSIZE_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENSIZE_LARGE;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENSIZE_NORMAL;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENSIZE_SMALL;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_SCREENSIZE_XLARGE;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_TOUCHSCREEN_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_NIGHT_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_NORMAL;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_WIDE_COLOR_GAMUT_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_WIDE_COLOR_GAMUT_NO;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_WIDE_COLOR_GAMUT_YES;
import static org.robolectric.res.android.LocaleData.localeDataCompareRegions;
import static org.robolectric.res.android.LocaleData.localeDataComputeScript;
import static org.robolectric.res.android.LocaleData.localeDataIsCloseToUsEnglish;
import static org.robolectric.res.android.ResTable.kDebugTableSuperNoisy;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.isTruthy;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedBytes;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Describes a particular resource configuration.
 *
 * Transliterated from:
 * * https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/ResourceTypes.cpp
 * * https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/ResourceTypes.h (struct ResTable_config)
 *
 * Changes from 8.0.0_r4 partially applied.
 */
@SuppressWarnings("NewApi")
public class ResTable_config {

  // The most specific locale can consist of:
  //
  // - a 3 char language code
  // - a 3 char region code prefixed by a 'r'
  // - a 4 char script code prefixed by a 's'
  // - a 8 char variant code prefixed by a 'v'
  //
  // each separated by a single char separator, which sums up to a total of 24
  // chars, (25 include the string terminator) rounded up to 28 to be 4 byte
  // aligned.
  public static final int RESTABLE_MAX_LOCALE_LEN = 28;

  /** The minimum size in bytes that this configuration must be to contain screen config info. */
  private static final int SCREEN_CONFIG_MIN_SIZE = 32;

  /** The minimum size in bytes that this configuration must be to contain screen dp info. */
  private static final int SCREEN_DP_MIN_SIZE = 36;

  /** The minimum size in bytes that this configuration must be to contain locale info. */
  private static final int LOCALE_MIN_SIZE = 48;

  /** The minimum size in bytes that this config must be to contain the screenConfig extension. */
  private static final int SCREEN_CONFIG_EXTENSION_MIN_SIZE = 52;

  public static final int SIZEOF = SCREEN_CONFIG_EXTENSION_MIN_SIZE;

  // Codes for specially handled languages and regions
  static final byte[] kEnglish = new byte[] {'e', 'n'};  // packed version of "en"
  static final byte[] kUnitedStates = new byte[] {'U', 'S'};  // packed version of "US"
  static final byte[] kFilipino = new byte[] {(byte)0xAD, 0x05};  // packed version of "fil" ported from C {'\xAD', '\x05'}
  static final byte[] kTagalog = new byte[] {'t', 'l'};  // packed version of "tl"

  static ResTable_config createConfig(ByteBuffer buffer) {
    int startPosition = buffer.position();  // The starting buffer position to calculate bytes read.
    int size = buffer.getInt();
    int mcc = buffer.getShort() & 0xFFFF;
    int mnc = buffer.getShort() & 0xFFFF;
    byte[] language = new byte[2];
    buffer.get(language);
    byte[] region = new byte[2];
    buffer.get(region);
    int orientation = UnsignedBytes.toInt(buffer.get());
    int touchscreen = UnsignedBytes.toInt(buffer.get());
    int density = buffer.getShort() & 0xFFFF;
    int keyboard = UnsignedBytes.toInt(buffer.get());
    int navigation = UnsignedBytes.toInt(buffer.get());
    int inputFlags = UnsignedBytes.toInt(buffer.get());
    buffer.get();  // 1 byte of padding
    int screenWidth = buffer.getShort() & 0xFFFF;
    int screenHeight = buffer.getShort() & 0xFFFF;
    int sdkVersion = buffer.getShort() & 0xFFFF;
    int minorVersion = buffer.getShort() & 0xFFFF;

    // At this point, the configuration's size needs to be taken into account as not all
    // configurations have all values.
    int screenLayout = 0;
    int uiMode = 0;
    int smallestScreenWidthDp = 0;
    int screenWidthDp = 0;
    int screenHeightDp = 0;
    byte[] localeScript = new byte[4];
    byte[] localeVariant = new byte[8];
    byte screenLayout2 = 0;
    byte screenConfigPad1 = 0;
    short screenConfigPad2 = 0;

    if (size >= SCREEN_CONFIG_MIN_SIZE) {
      screenLayout = UnsignedBytes.toInt(buffer.get());
      uiMode = UnsignedBytes.toInt(buffer.get());
      smallestScreenWidthDp = buffer.getShort() & 0xFFFF;
    }

    if (size >= SCREEN_DP_MIN_SIZE) {
      screenWidthDp = buffer.getShort() & 0xFFFF;
      screenHeightDp = buffer.getShort() & 0xFFFF;
    }

    if (size >= LOCALE_MIN_SIZE) {
      buffer.get(localeScript);
      buffer.get(localeVariant);
    }

    if (size >= SCREEN_CONFIG_EXTENSION_MIN_SIZE) {
      screenLayout2 = (byte) UnsignedBytes.toInt(buffer.get());
      screenConfigPad1 = buffer.get();  // Reserved padding
      screenConfigPad2 = buffer.getShort();  // More reserved padding
    }

    // After parsing everything that's known, account for anything that's unknown.
    int bytesRead = buffer.position() - startPosition;
    byte[] unknown = new byte[size - bytesRead];
    buffer.get(unknown);

    return new ResTable_config(size, mcc, mnc, language, region, orientation,
        touchscreen, density, keyboard, navigation, inputFlags, screenWidth, screenHeight,
        sdkVersion, minorVersion, screenLayout, uiMode, smallestScreenWidthDp, screenWidthDp,
        screenHeightDp, localeScript, localeVariant, screenLayout2, screenConfigPad1, screenConfigPad2, unknown);
  }

  /**
   * The different types of configs that can be present in a {@link ResTable_config}.
   *
   * The ordering of these types is roughly the same as {@code #isBetterThan}, but is not
   * guaranteed to be the same.
   */
  public enum Type {
    MCC,
    MNC,
    LANGUAGE_STRING,
    LOCALE_SCRIPT_STRING,
    REGION_STRING,
    LOCALE_VARIANT_STRING,
    SCREEN_LAYOUT_DIRECTION,
    SMALLEST_SCREEN_WIDTH_DP,
    SCREEN_WIDTH_DP,
    SCREEN_HEIGHT_DP,
    SCREEN_LAYOUT_SIZE,
    SCREEN_LAYOUT_LONG,
    SCREEN_LAYOUT_ROUND,
    COLOR_MODE_WIDE_COLOR_GAMUT, // NB: COLOR_GAMUT takes priority over HDR in #isBetterThan.
    COLOR_MODE_HDR,
    ORIENTATION,
    UI_MODE_TYPE,
    UI_MODE_NIGHT,
    DENSITY_DPI,
    TOUCHSCREEN,
    KEYBOARD_HIDDEN,
    KEYBOARD,
    NAVIGATION_HIDDEN,
    NAVIGATION,
    SCREEN_SIZE,
    SDK_VERSION
  }

  // screenLayout bits for layout direction.
//  public static final int MASK_LAYOUTDIR = 0xC0;
  public static final int SHIFT_LAYOUTDIR = 6;
  public static final int LAYOUTDIR_ANY = ACONFIGURATION_LAYOUTDIR_ANY << SHIFT_LAYOUTDIR;
  public static final int LAYOUTDIR_LTR = ACONFIGURATION_LAYOUTDIR_LTR << SHIFT_LAYOUTDIR;
  public static final int LAYOUTDIR_RTL = ACONFIGURATION_LAYOUTDIR_RTL << SHIFT_LAYOUTDIR;

  public static final int SCREENWIDTH_ANY = 0;
//  public static final int MASK_SCREENSIZE = 0x0f;
  public static final int SCREENSIZE_ANY = ACONFIGURATION_SCREENSIZE_ANY;
  public static final int SCREENSIZE_SMALL = ACONFIGURATION_SCREENSIZE_SMALL;
  public static final int SCREENSIZE_NORMAL = ACONFIGURATION_SCREENSIZE_NORMAL;
  public static final int SCREENSIZE_LARGE = ACONFIGURATION_SCREENSIZE_LARGE;
  public static final int SCREENSIZE_XLARGE = ACONFIGURATION_SCREENSIZE_XLARGE;

  // uiMode bits for the mode type.
  public static final int MASK_UI_MODE_TYPE = 0x0f;
  public static final int UI_MODE_TYPE_ANY = ACONFIGURATION_UI_MODE_TYPE_ANY;
  public static final int UI_MODE_TYPE_NORMAL = ACONFIGURATION_UI_MODE_TYPE_NORMAL;

  // uiMode bits for the night switch;
  public static final int MASK_UI_MODE_NIGHT = 0x30;
  public static final int SHIFT_UI_MODE_NIGHT = 4;
  public static final int UI_MODE_NIGHT_ANY = ACONFIGURATION_UI_MODE_NIGHT_ANY << SHIFT_UI_MODE_NIGHT;

  public static final int DENSITY_DEFAULT = ACONFIGURATION_DENSITY_DEFAULT;
  public static final int DENSITY_LOW = ACONFIGURATION_DENSITY_LOW;
  public static final int DENSITY_MEDIUM = ACONFIGURATION_DENSITY_MEDIUM;
  public static final int DENSITY_TV = ACONFIGURATION_DENSITY_TV;
  public static final int DENSITY_HIGH = ACONFIGURATION_DENSITY_HIGH;
  public static final int DENSITY_XHIGH = ACONFIGURATION_DENSITY_XHIGH;
  public static final int DENSITY_XXHIGH = ACONFIGURATION_DENSITY_XXHIGH;
  public static final int DENSITY_XXXHIGH = ACONFIGURATION_DENSITY_XXXHIGH;
  public static final int DENSITY_ANY = ACONFIGURATION_DENSITY_ANY;
  public static final int DENSITY_NONE = ACONFIGURATION_DENSITY_NONE;

  public static final int TOUCHSCREEN_ANY  = ACONFIGURATION_TOUCHSCREEN_ANY;

  public static final int MASK_KEYSHIDDEN = 0x0003;
  public static final byte KEYSHIDDEN_ANY = ACONFIGURATION_KEYSHIDDEN_ANY;
  public static final byte KEYSHIDDEN_NO = ACONFIGURATION_KEYSHIDDEN_NO;
  public static final byte KEYSHIDDEN_YES = ACONFIGURATION_KEYSHIDDEN_YES;
  public static final byte KEYSHIDDEN_SOFT = ACONFIGURATION_KEYSHIDDEN_SOFT;

  public static final int KEYBOARD_ANY  = ACONFIGURATION_KEYBOARD_ANY;

  public static final int MASK_NAVHIDDEN = 0x000c;
  public static final int SHIFT_NAVHIDDEN = 2;
  public static final byte NAVHIDDEN_ANY = ACONFIGURATION_NAVHIDDEN_ANY << SHIFT_NAVHIDDEN;
  public static final byte NAVHIDDEN_NO = ACONFIGURATION_NAVHIDDEN_NO << SHIFT_NAVHIDDEN;
  public static final byte NAVHIDDEN_YES = ACONFIGURATION_NAVHIDDEN_YES << SHIFT_NAVHIDDEN;

  public static final int NAVIGATION_ANY  = ACONFIGURATION_NAVIGATION_ANY;

  public static final int SCREENHEIGHT_ANY = 0;

  public static final int SDKVERSION_ANY = 0;
  public static final int MINORVERSION_ANY = 0;

  // from https://github.com/google/android-arscblamer/blob/master/java/com/google/devrel/gmscore/tools/apk/arsc/ResourceConfiguration.java
  /** The below constants are from android.content.res.Configuration. */
  static final int COLOR_MODE_WIDE_COLOR_GAMUT_MASK = 0x03;

  public static final int WIDE_COLOR_GAMUT_ANY = ACONFIGURATION_WIDE_COLOR_GAMUT_ANY;
  public static final int WIDE_COLOR_GAMUT_NO = ACONFIGURATION_WIDE_COLOR_GAMUT_NO;
  public static final int WIDE_COLOR_GAMUT_YES = ACONFIGURATION_WIDE_COLOR_GAMUT_YES;
  public static final int MASK_WIDE_COLOR_GAMUT = 0x03;
  static final int COLOR_MODE_WIDE_COLOR_GAMUT_UNDEFINED = 0;
  static final int COLOR_MODE_WIDE_COLOR_GAMUT_NO = 0x01;
  static final int COLOR_MODE_WIDE_COLOR_GAMUT_YES = 0x02;

  private static final Map<Integer, String> COLOR_MODE_WIDE_COLOR_GAMUT_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(COLOR_MODE_WIDE_COLOR_GAMUT_UNDEFINED, "");
    map.put(COLOR_MODE_WIDE_COLOR_GAMUT_NO, "nowidecg");
    map.put(COLOR_MODE_WIDE_COLOR_GAMUT_YES, "widecg");
    COLOR_MODE_WIDE_COLOR_GAMUT_VALUES = Collections.unmodifiableMap(map);
  }

  public static final int HDR_ANY = ACONFIGURATION_HDR_ANY;
  public static final int HDR_NO = ACONFIGURATION_HDR_NO << 2;
  public static final int HDR_YES = ACONFIGURATION_HDR_YES << 2;
  public static final int MASK_HDR = 0x0c;
  static final int COLOR_MODE_HDR_MASK = 0x0C;
  static final int COLOR_MODE_HDR_UNDEFINED = 0;
  static final int COLOR_MODE_HDR_NO = 0x04;
  static final int COLOR_MODE_HDR_YES = 0x08;

  private static final Map<Integer, String> COLOR_MODE_HDR_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(COLOR_MODE_HDR_UNDEFINED, "");
    map.put(COLOR_MODE_HDR_NO, "lowdr");
    map.put(COLOR_MODE_HDR_YES, "highdr");
    COLOR_MODE_HDR_VALUES = Collections.unmodifiableMap(map);
  }

  public static final int DENSITY_DPI_UNDEFINED = 0;
  static final int DENSITY_DPI_LDPI = 120;
  public static final int DENSITY_DPI_MDPI = 160;
  static final int DENSITY_DPI_TVDPI = 213;
  static final int DENSITY_DPI_HDPI = 240;
  static final int DENSITY_DPI_XHDPI = 320;
  static final int DENSITY_DPI_XXHDPI = 480;
  static final int DENSITY_DPI_XXXHDPI = 640;
  public static final int DENSITY_DPI_ANY  = 0xFFFE;
  public static final int DENSITY_DPI_NONE = 0xFFFF;

  private static final Map<Integer, String> DENSITY_DPI_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(DENSITY_DPI_UNDEFINED, "");
    map.put(DENSITY_DPI_LDPI, "ldpi");
    map.put(DENSITY_DPI_MDPI, "mdpi");
    map.put(DENSITY_DPI_TVDPI, "tvdpi");
    map.put(DENSITY_DPI_HDPI, "hdpi");
    map.put(DENSITY_DPI_XHDPI, "xhdpi");
    map.put(DENSITY_DPI_XXHDPI, "xxhdpi");
    map.put(DENSITY_DPI_XXXHDPI, "xxxhdpi");
    map.put(DENSITY_DPI_ANY, "anydpi");
    map.put(DENSITY_DPI_NONE, "nodpi");
    DENSITY_DPI_VALUES = Collections.unmodifiableMap(map);
  }

  static final int KEYBOARD_NOKEYS = 1;
  static final int KEYBOARD_QWERTY = 2;
  static final int KEYBOARD_12KEY  = 3;

  private static final Map<Integer, String> KEYBOARD_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(KEYBOARD_NOKEYS, "nokeys");
    map.put(KEYBOARD_QWERTY, "qwerty");
    map.put(KEYBOARD_12KEY, "12key");
    KEYBOARD_VALUES = Collections.unmodifiableMap(map);
  }

  static final int KEYBOARDHIDDEN_MASK = 0x03;
  static final int KEYBOARDHIDDEN_NO   = 1;
  static final int KEYBOARDHIDDEN_YES  = 2;
  static final int KEYBOARDHIDDEN_SOFT = 3;

  private static final Map<Integer, String> KEYBOARDHIDDEN_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(KEYBOARDHIDDEN_NO, "keysexposed");
    map.put(KEYBOARDHIDDEN_YES, "keyshidden");
    map.put(KEYBOARDHIDDEN_SOFT, "keyssoft");
    KEYBOARDHIDDEN_VALUES = Collections.unmodifiableMap(map);
  }

  static final int NAVIGATION_NONAV     = 1;
  static final int NAVIGATION_DPAD      = 2;
  static final int NAVIGATION_TRACKBALL = 3;
  static final int NAVIGATION_WHEEL     = 4;

  private static final Map<Integer, String> NAVIGATION_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(NAVIGATION_NONAV, "nonav");
    map.put(NAVIGATION_DPAD, "dpad");
    map.put(NAVIGATION_TRACKBALL, "trackball");
    map.put(NAVIGATION_WHEEL, "wheel");
    NAVIGATION_VALUES = Collections.unmodifiableMap(map);
  }

  static final int NAVIGATIONHIDDEN_MASK  = 0x0C;
  static final int NAVIGATIONHIDDEN_NO    = 0x04;
  static final int NAVIGATIONHIDDEN_YES   = 0x08;

  private static final Map<Integer, String> NAVIGATIONHIDDEN_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(NAVIGATIONHIDDEN_NO, "navexposed");
    map.put(NAVIGATIONHIDDEN_YES, "navhidden");
    NAVIGATIONHIDDEN_VALUES = Collections.unmodifiableMap(map);
  }

  public static final int ORIENTATION_ANY  = ACONFIGURATION_ORIENTATION_ANY;
  public static final int ORIENTATION_PORT = ACONFIGURATION_ORIENTATION_PORT;
  public static final int ORIENTATION_LAND = ACONFIGURATION_ORIENTATION_LAND;
  public static final int ORIENTATION_SQUARE = ACONFIGURATION_ORIENTATION_SQUARE;
  static final int ORIENTATION_PORTRAIT  = 0x01;
  static final int ORIENTATION_LANDSCAPE = 0x02;

  private static final Map<Integer, String> ORIENTATION_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(ORIENTATION_PORTRAIT, "port");
    map.put(ORIENTATION_LANDSCAPE, "land");
    ORIENTATION_VALUES = Collections.unmodifiableMap(map);
  }

  static final int SCREENLAYOUT_LAYOUTDIR_MASK = 0xC0;
  static final int SCREENLAYOUT_LAYOUTDIR_LTR  = 0x40;
  static final int SCREENLAYOUT_LAYOUTDIR_RTL  = 0x80;

  private static final Map<Integer, String> SCREENLAYOUT_LAYOUTDIR_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(SCREENLAYOUT_LAYOUTDIR_LTR, "ldltr");
    map.put(SCREENLAYOUT_LAYOUTDIR_RTL, "ldrtl");
    SCREENLAYOUT_LAYOUTDIR_VALUES = Collections.unmodifiableMap(map);
  }

  // screenLayout bits for wide/long screen variation.
  public static final int MASK_SCREENLONG = 0x30;
  public static final int SHIFT_SCREENLONG = 4;
  public static final int SCREENLONG_ANY = ACONFIGURATION_SCREENLONG_ANY << SHIFT_SCREENLONG;
  public static final int SCREENLONG_NO = ACONFIGURATION_SCREENLONG_NO << SHIFT_SCREENLONG;
  public static final int SCREENLONG_YES = ACONFIGURATION_SCREENLONG_YES << SHIFT_SCREENLONG;
  static final int SCREENLAYOUT_LONG_MASK = 0x30;
  static final int SCREENLAYOUT_LONG_NO   = 0x10;
  static final int SCREENLAYOUT_LONG_YES  = 0x20;

  private static final Map<Integer, String> SCREENLAYOUT_LONG_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(SCREENLAYOUT_LONG_NO, "notlong");
    map.put(SCREENLAYOUT_LONG_YES, "long");
    SCREENLAYOUT_LONG_VALUES = Collections.unmodifiableMap(map);
  }

  // screenLayout2 bits for round/notround.
  static final int MASK_SCREENROUND = 0x03;
  public static final int SCREENROUND_ANY = ACONFIGURATION_SCREENROUND_ANY;
  public static final int SCREENROUND_NO = ACONFIGURATION_SCREENROUND_NO;
  public static final int SCREENROUND_YES = ACONFIGURATION_SCREENROUND_YES;

  static final int SCREENLAYOUT_ROUND_MASK = 0x03;
  static final int SCREENLAYOUT_ROUND_NO   = 0x01;
  static final int SCREENLAYOUT_ROUND_YES  = 0x02;

  private static final Map<Integer, String> SCREENLAYOUT_ROUND_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(SCREENLAYOUT_ROUND_NO, "notround");
    map.put(SCREENLAYOUT_ROUND_YES, "round");
    SCREENLAYOUT_ROUND_VALUES = Collections.unmodifiableMap(map);
  }

  static final int SCREENLAYOUT_SIZE_MASK   = 0x0F;
  static final int SCREENLAYOUT_SIZE_SMALL  = 0x01;
  static final int SCREENLAYOUT_SIZE_NORMAL = 0x02;
  static final int SCREENLAYOUT_SIZE_LARGE  = 0x03;
  static final int SCREENLAYOUT_SIZE_XLARGE = 0x04;

  private static final Map<Integer, String> SCREENLAYOUT_SIZE_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(SCREENLAYOUT_SIZE_SMALL, "small");
    map.put(SCREENLAYOUT_SIZE_NORMAL, "normal");
    map.put(SCREENLAYOUT_SIZE_LARGE, "large");
    map.put(SCREENLAYOUT_SIZE_XLARGE, "xlarge");
    SCREENLAYOUT_SIZE_VALUES = Collections.unmodifiableMap(map);
  }

  static final int TOUCHSCREEN_NOTOUCH = 1;
  @Deprecated static final int TOUCHSCREEN_STYLUS  = 2;
  public static final int TOUCHSCREEN_FINGER  = 3;

  private static final Map<Integer, String> TOUCHSCREEN_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(TOUCHSCREEN_NOTOUCH, "notouch");
    map.put(TOUCHSCREEN_FINGER, "finger");
    TOUCHSCREEN_VALUES = Collections.unmodifiableMap(map);
  }

  static final int UI_MODE_NIGHT_MASK = 0x30;
  public static final int UI_MODE_NIGHT_NO   = 0x10;
  static final int UI_MODE_NIGHT_YES  = 0x20;

  private static final Map<Integer, String> UI_MODE_NIGHT_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(UI_MODE_NIGHT_NO, "notnight");
    map.put(UI_MODE_NIGHT_YES, "night");
    UI_MODE_NIGHT_VALUES = Collections.unmodifiableMap(map);
  }

  static final int UI_MODE_TYPE_MASK       = 0x0F;
  static final int UI_MODE_TYPE_DESK       = 0x02;
  static final int UI_MODE_TYPE_CAR        = 0x03;
  static final int UI_MODE_TYPE_TELEVISION = 0x04;
  static final int UI_MODE_TYPE_APPLIANCE  = 0x05;
  static final int UI_MODE_TYPE_WATCH      = 0x06;
  static final int UI_MODE_TYPE_VR_HEADSET = 0x07;

  private static final Map<Integer, String> UI_MODE_TYPE_VALUES;

  static {
    Map<Integer, String> map = new HashMap<>();
    map.put(UI_MODE_TYPE_DESK, "desk");
    map.put(UI_MODE_TYPE_CAR, "car");
    map.put(UI_MODE_TYPE_TELEVISION, "television");
    map.put(UI_MODE_TYPE_APPLIANCE, "appliance");
    map.put(UI_MODE_TYPE_WATCH, "watch");
    map.put(UI_MODE_TYPE_VR_HEADSET, "vrheadset");
    UI_MODE_TYPE_VALUES = Collections.unmodifiableMap(map);
  }

  /** The number of bytes that this resource configuration takes up. */
  int size;

  public int mcc;
  public int mnc;

  /** Returns a packed 2-byte language code. */
  @SuppressWarnings("mutable")
  public final byte[] language;

  /** Returns {@link #language} as an unpacked string representation. */
  @Nonnull
  public final String languageString() {
    return unpackLanguage();
  }

  /** Returns the {@link #localeScript} as a string. */
  public final String localeScriptString() {
    return byteArrayToString(localeScript);
  }

  /** Returns the {@link #localeVariant} as a string. */
  public final String localeVariantString() {
    return byteArrayToString(localeVariant);
  }

  private String byteArrayToString(byte[] data) {
    int length = Bytes.indexOf(data, (byte) 0);
    return new String(data, 0, length >= 0 ? length : data.length, US_ASCII);
  }

  /** Returns the wide color gamut section of {@link #colorMode}. */
  public final int colorModeWideColorGamut() {
    return colorMode & COLOR_MODE_WIDE_COLOR_GAMUT_MASK;
  }

  /** Returns the HDR section of {@link #colorMode}. */
  public final int colorModeHdr() {
    return colorMode & COLOR_MODE_HDR_MASK;
  }

  /** Returns a packed 2-byte country code. */
  @SuppressWarnings("mutable")
  public final byte[] country;

  /** Returns {@link #country} as an unpacked string representation. */
  @Nonnull
  public final String regionString() {
    return unpackRegion();
  }

  public final String scriptString() {
    if (localeScript[0] != '\0') {
      return new String(localeScript, UTF_8);
    } else {
      return null;
    }
  }

  public int orientation;
  public int touchscreen;
  public int density;
  public int keyboard;
  public int navigation;
  public int inputFlags;

  public final int keyboardHidden() {
    return inputFlags & KEYBOARDHIDDEN_MASK;
  }

  public final void keyboardHidden(int value) {
    inputFlags = (inputFlags & ~KEYBOARDHIDDEN_MASK) | value;
  }

  public final int navigationHidden() {
    return (inputFlags & NAVIGATIONHIDDEN_MASK) >> 2;
  }

  public final void navigationHidden(int value) {
    inputFlags = (inputFlags & ~NAVIGATIONHIDDEN_MASK) | value;
  }

  public int screenWidth;
  public int screenHeight;
  public int sdkVersion;

  /**
   * Returns a copy of this resource configuration with a different {@link #sdkVersion}, or this
   * configuration if the {@code sdkVersion} is the same.
   *
   * @param sdkVersion The SDK version of the returned configuration.
   * @return A copy of this configuration with the only difference being #sdkVersion.
   */
  public final ResTable_config withSdkVersion(int sdkVersion) {
    if (sdkVersion == this.sdkVersion) {
      return this;
    }
    return new ResTable_config(size, mcc, mnc, language, country,
        orientation, touchscreen, density, keyboard, navigation, inputFlags,
        screenWidth, screenHeight, sdkVersion, minorVersion, screenLayout, uiMode,
        smallestScreenWidthDp, screenWidthDp, screenHeightDp, localeScript, localeVariant,
        screenLayout2, colorMode, screenConfigPad2, unknown);
  }

  public ResTable_config(ResTable_config other) {
    this.size = other.size;
    this.mcc = other.mcc;
    this.mnc = other.mnc;
    this.language = other.language;
    this.country = other.country;
    this.orientation = other.orientation;
    this.touchscreen = other.touchscreen;
    this.density = other.density;
    this.keyboard = other.keyboard;
    this.navigation = other.navigation;
    this.inputFlags = other.inputFlags;
    this.screenWidth = other.screenWidth;
    this.screenHeight = other.screenHeight;
    this.sdkVersion = other.sdkVersion;
    this.minorVersion = other.minorVersion;
    this.screenLayout = other.screenLayout;
    this.uiMode = other.uiMode;
    this.smallestScreenWidthDp = other.smallestScreenWidthDp;
    this.screenWidthDp = other.screenWidthDp;
    this.screenHeightDp = other.screenHeightDp;
    this.localeScript = other.localeScript;
    this.localeVariant = other.localeVariant;
    this.screenLayout2 = other.screenLayout2;
    this.colorMode = other.colorMode;
    this.screenConfigPad2 = other.screenConfigPad2;
    this.unknown = other.unknown;
  }


  public ResTable_config(int size, int mcc, int mnc, byte[] language, byte[] country,
      int orientation, int touchscreen, int density, int keyboard, int navigation, int inputFlags,
      int screenWidth, int screenHeight, int sdkVersion, int minorVersion, int screenLayout,
      int uiMode, int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      byte[] localeScript, byte[] localeVariant, byte screenLayout2, byte colorMode,
      short screenConfigPad2, byte[] unknown) {
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
    this.colorMode = colorMode;
    this.screenConfigPad2 = screenConfigPad2;
    this.unknown = unknown;
  }

  public ResTable_config() {
    this.language = new byte[2];
    this.country = new byte[2];
    this.localeScript = new byte[LocaleData.SCRIPT_LENGTH];
    this.localeVariant = new byte[8];
  }

  public int minorVersion;
  public int screenLayout;

  public final int screenLayoutDirection() {
    return screenLayout & SCREENLAYOUT_LAYOUTDIR_MASK;
  }

  public final void screenLayoutDirection(int value) {
    screenLayout = (screenLayout & ~SCREENLAYOUT_LAYOUTDIR_MASK) | value;
  }

  public final int screenLayoutSize() {
    return screenLayout & SCREENLAYOUT_SIZE_MASK;
  }

  public final void screenLayoutSize(int value) {
    screenLayout = (screenLayout & ~SCREENLAYOUT_SIZE_MASK) | value;
  }

  public final int screenLayoutLong() {
    return screenLayout & SCREENLAYOUT_LONG_MASK;
  }

  public final void screenLayoutLong(int value) {
    screenLayout = (screenLayout & ~SCREENLAYOUT_LONG_MASK) | value;
  }

  public final int screenLayoutRound() {
    return screenLayout2 & SCREENLAYOUT_ROUND_MASK;
  }

  public final void screenLayoutRound(int value) {
    screenLayout2 = (byte) ((screenLayout2 & ~SCREENLAYOUT_ROUND_MASK) | value);
  }

  public int uiMode;

  public final int uiModeType() {
    return uiMode & UI_MODE_TYPE_MASK;
  }

  public final void uiModeType(int value) {
    uiMode = (uiMode & ~UI_MODE_TYPE_MASK) | value;
  }

  public final int uiModeNight() {
    return uiMode & UI_MODE_NIGHT_MASK;
  }

  public final void uiModeNight(int value) {
    uiMode = (uiMode & ~UI_MODE_NIGHT_MASK) | value;
  }

  public int smallestScreenWidthDp;
  public int screenWidthDp;
  public int screenHeightDp;

  /** The ISO-15924 short name for the script corresponding to this configuration. */
  @SuppressWarnings("mutable")
  public final byte[] localeScript;

  /** A single BCP-47 variant subtag. */
  @SuppressWarnings("mutable")
  public final byte[] localeVariant;

  /** An extension to {@link #screenLayout}. Contains round/notround qualifier. */
  public byte screenLayout2;        // Contains round/notround qualifier.
  public byte colorMode;            // Wide-gamut, HDR, etc.
  public short screenConfigPad2;    // Reserved padding.

  /** Any remaining bytes in this resource configuration that are unaccounted for. */
  @SuppressWarnings("mutable")
  public byte[] unknown;


  /**
   *     // An extension of screenConfig.
   union {
   struct {
   uint8_t screenLayout2;      // Contains round/notround qualifier.
   uint8_t screenConfigPad1;   // Reserved padding.
   uint16_t screenConfigPad2;  // Reserved padding.
   };
   uint32_t screenConfig2;
   };
   */
  private int screenConfig2() {
    return ((screenLayout2 & 0xff) << 24) | ((colorMode * 0xff) << 16) | (screenConfigPad2 & 0xffff);
  }

  // If false and localeScript is set, it means that the script of the locale
  // was explicitly provided.
  //
  // If true, it means that localeScript was automatically computed.
  // localeScript may still not be set in this case, which means that we
  // tried but could not compute a script.
  boolean localeScriptWasComputed;

  // The value of BCP 47 Unicode extension for key 'nu' (numbering system).
  // Varies in length from 3 to 8 chars. Zero-filled value.
  byte[] localeNumberingSystem = new byte[8];

// --------------------------------------------------------------------
// --------------------------------------------------------------------
// --------------------------------------------------------------------

//  void copyFromDeviceNoSwap(final ResTable_config o) {
//    final int size = dtohl(o.size);
//    if (size >= sizeof(ResTable_config)) {
//        *this = o;
//    } else {
//      memcpy(this, &o, size);
//      memset(((uint8_t*)this)+size, 0, sizeof(ResTable_config)-size);
//    }
//  }

  @Nonnull
  private String unpackLanguageOrRegion(byte[] value, int base) {
    Preconditions.checkState(value.length == 2, "Language or country value must be 2 bytes.");
    if (value[0] == 0 && value[1] == 0) {
      return "";
    }
    if (isTruthy(UnsignedBytes.toInt(value[0]) & 0x80)) {
      byte[] result = new byte[3];
      result[0] = (byte) (base + (value[1] & 0x1F));
      result[1] = (byte) (base + ((value[1] & 0xE0) >>> 5) + ((value[0] & 0x03) << 3));
      result[2] = (byte) (base + ((value[0] & 0x7C) >>> 2));
      return new String(result, US_ASCII);
    }
    return new String(value, US_ASCII);
  }

  /* static */ void packLanguageOrRegion(final String in, final byte base,
      final byte[] out) {
    if (in == null) {
      out[0] = 0;
      out[1] = 0;
    } else if (in.length() < 3 || in.charAt(2) == 0 || in.charAt(2) == '-') {
      out[0] = (byte) in.charAt(0);
      out[1] = (byte) in.charAt(1);
    } else {
      byte first = (byte) ((in.charAt(0) - base) & 0x007f);
      byte second = (byte) ((in.charAt(1) - base) & 0x007f);
      byte third = (byte) ((in.charAt(2) - base) & 0x007f);

      out[0] = (byte) (0x80 | (third << 2) | (second >> 3));
      out[1] = (byte) ((second << 5) | first);
    }
  }

  public void packLanguage(final String language) {
    packLanguageOrRegion(language, (byte) 'a', this.language);
  }

  public void packRegion(final String region) {
    packLanguageOrRegion(region, (byte) '0', this.country);
  }

  @Nonnull
  private String unpackLanguage() {
    return unpackLanguageOrRegion(language, 0x61);
  }

  private String unpackRegion() {
    return unpackLanguageOrRegion(country, 0x30);
  }

//  void copyFromDtoH(final ResTable_config o) {
//    copyFromDeviceNoSwap(o);
//    size = sizeof(ResTable_config);
//    mcc = dtohs(mcc);
//    mnc = dtohs(mnc);
//    density = dtohs(density);
//    screenWidth = dtohs(screenWidth);
//    screenHeight = dtohs(screenHeight);
//    sdkVersion = dtohs(sdkVersion);
//    minorVersion = dtohs(minorVersion);
//    smallestScreenWidthDp = dtohs(smallestScreenWidthDp);
//    screenWidthDp = dtohs(screenWidthDp);
//    screenHeightDp = dtohs(screenHeightDp);
//  }

//  void ResTable_config::copyFromDtoH(const ResTable_config& o) {
  static ResTable_config fromDtoH(final ResTable_config o) {
    return new ResTable_config(
        0 /*sizeof(ResTable_config)*/,
        dtohs((short) o.mcc) & 0xFFFF,
        dtohs((short) o.mnc) & 0xFFFF,
        o.language,
        o.country,
        o.orientation,
        o.touchscreen,
        dtohl(o.density),
        o.keyboard,
        o.navigation,
        o.inputFlags,
        dtohs((short) o.screenWidth) & 0xFFFF,
        dtohs((short) o.screenHeight) & 0xFFFF,
        dtohs((short) o.sdkVersion) & 0xFFFF,
        dtohs((short) o.minorVersion) & 0xFFFF,
        o.screenLayout,
        o.uiMode,
        dtohs((short) o.smallestScreenWidthDp) & 0xFFFF,
        dtohs((short) o.screenWidthDp) & 0xFFFF,
        dtohs((short) o.screenHeightDp) & 0xFFFF,
        o.localeScript,
        o.localeVariant,
        o.screenLayout2,
        o.colorMode,
        o.screenConfigPad2,
        o.unknown
    );
  }

  void swapHtoD() {
//    size = htodl(size);
//    mcc = htods(mcc);
//    mnc = htods(mnc);
//    density = htods(density);
//    screenWidth = htods(screenWidth);
//    screenHeight = htods(screenHeight);
//    sdkVersion = htods(sdkVersion);
//    minorVersion = htods(minorVersion);
//    smallestScreenWidthDp = htods(smallestScreenWidthDp);
//    screenWidthDp = htods(screenWidthDp);
//    screenHeightDp = htods(screenHeightDp);
  }

  static final int compareLocales(final ResTable_config l, final ResTable_config r) {
    if (l.locale() != r.locale()) {
      // NOTE: This is the old behaviour with respect to comparison orders.
      // The diff value here doesn't make much sense (given our bit packing scheme)
      // but it's stable, and that's all we need.
      return (l.locale() > r.locale()) ? 1 : -1;
    }

    // The language & region are equal, so compare the scripts, variants and
    // numbering systms in this order. Comparison of variants and numbering
    // systems should happen very infrequently (if at all.)
    // The comparison code relies on memcmp low-level optimizations that make it
    // more efficient than strncmp.
    final byte emptyScript[] = {'\0', '\0', '\0', '\0'};
    final byte[] lScript = l.localeScriptWasComputed ? emptyScript : l.localeScript;
    final byte[] rScript = r.localeScriptWasComputed ? emptyScript : r.localeScript;
//    int script = memcmp(lScript, rScript);
//    if (script) {
//      return script;
//    }
    int d = arrayCompare(lScript, rScript);
    if (d != 0) return d;

    int variant = arrayCompare(l.localeVariant, r.localeVariant);
    if (isTruthy(variant)) {
      return variant;
    }

    return arrayCompare(l.localeNumberingSystem, r.localeNumberingSystem);
  }

  private static int arrayCompare(byte[] l, byte[] r) {
    for (int i = 0; i < l.length; i++) {
      byte l0 = l[i];
      byte r0 = r[i];
      int d = l0 - r0;
      if (d != 0) return d;
    }
    return 0;
  }

  // Flags indicating a set of config values.  These flag constants must
  // match the corresponding ones in android.content.pm.ActivityInfo and
  // attrs_manifest.xml.
  private static final int CONFIG_MCC = AConfiguration.ACONFIGURATION_MCC;
  private static final int CONFIG_MNC = AConfiguration.ACONFIGURATION_MNC;
  private static final int CONFIG_LOCALE = AConfiguration.ACONFIGURATION_LOCALE;
  private static final int CONFIG_TOUCHSCREEN = AConfiguration.ACONFIGURATION_TOUCHSCREEN;
  private static final int CONFIG_KEYBOARD = AConfiguration.ACONFIGURATION_KEYBOARD;
  private static final int CONFIG_KEYBOARD_HIDDEN = AConfiguration.ACONFIGURATION_KEYBOARD_HIDDEN;
  private static final int CONFIG_NAVIGATION = AConfiguration.ACONFIGURATION_NAVIGATION;
  private static final int CONFIG_ORIENTATION = AConfiguration.ACONFIGURATION_ORIENTATION;
  private static final int CONFIG_DENSITY = AConfiguration.ACONFIGURATION_DENSITY;
  private static final int CONFIG_SCREEN_SIZE = AConfiguration.ACONFIGURATION_SCREEN_SIZE;
  private static final int CONFIG_SMALLEST_SCREEN_SIZE = AConfiguration.ACONFIGURATION_SMALLEST_SCREEN_SIZE;
  private static final int CONFIG_VERSION = AConfiguration.ACONFIGURATION_VERSION;
  private static final int CONFIG_SCREEN_LAYOUT = AConfiguration.ACONFIGURATION_SCREEN_LAYOUT;
  private static final int CONFIG_UI_MODE = AConfiguration.ACONFIGURATION_UI_MODE;
  private static final int CONFIG_LAYOUTDIR = AConfiguration.ACONFIGURATION_LAYOUTDIR;
  private static final int CONFIG_SCREEN_ROUND = AConfiguration.ACONFIGURATION_SCREEN_ROUND;
  private static final int CONFIG_COLOR_MODE = AConfiguration.ACONFIGURATION_COLOR_MODE;

  // Compare two configuration, returning CONFIG_* flags set for each value
  // that is different.
  int diff(final ResTable_config o) {
    int diffs = 0;
    if (mcc != o.mcc) diffs |= CONFIG_MCC;
    if (mnc != o.mnc) diffs |= CONFIG_MNC;
    if (orientation != o.orientation) diffs |= CONFIG_ORIENTATION;
    if (density != o.density) diffs |= CONFIG_DENSITY;
    if (touchscreen != o.touchscreen) diffs |= CONFIG_TOUCHSCREEN;
    if (((inputFlags^o.inputFlags)&(MASK_KEYSHIDDEN|MASK_NAVHIDDEN)) != 0)
      diffs |= CONFIG_KEYBOARD_HIDDEN;
    if (keyboard != o.keyboard) diffs |= CONFIG_KEYBOARD;
    if (navigation != o.navigation) diffs |= CONFIG_NAVIGATION;
    if (screenSize() != o.screenSize()) diffs |= CONFIG_SCREEN_SIZE;
    if (version() != o.version()) diffs |= CONFIG_VERSION;
    if ((screenLayout & MASK_LAYOUTDIR) != (o.screenLayout & MASK_LAYOUTDIR)) diffs |= CONFIG_LAYOUTDIR;
    if ((screenLayout & ~MASK_LAYOUTDIR) != (o.screenLayout & ~MASK_LAYOUTDIR)) diffs |= CONFIG_SCREEN_LAYOUT;
    if ((screenLayout2 & MASK_SCREENROUND) != (o.screenLayout2 & MASK_SCREENROUND)) diffs |= CONFIG_SCREEN_ROUND;
    if ((colorMode & MASK_WIDE_COLOR_GAMUT) != (o.colorMode & MASK_WIDE_COLOR_GAMUT)) diffs |= CONFIG_COLOR_MODE;
    if ((colorMode & MASK_HDR) != (o.colorMode & MASK_HDR)) diffs |= CONFIG_COLOR_MODE;
    if (uiMode != o.uiMode) diffs |= CONFIG_UI_MODE;
    if (smallestScreenWidthDp != o.smallestScreenWidthDp) diffs |= CONFIG_SMALLEST_SCREEN_SIZE;
    if (screenSizeDp() != o.screenSizeDp()) diffs |= CONFIG_SCREEN_SIZE;

    int diff = compareLocales(this, o);
    if (isTruthy(diff)) diffs |= CONFIG_LOCALE;

    return diffs;
  }

  // There isn't a well specified "importance" order between variants and
  // scripts. We can't easily tell whether, say "en-Latn-US" is more or less
  // specific than "en-US-POSIX".
  //
  // We therefore arbitrarily decide to give priority to variants over
  // scripts since it seems more useful to do so. We will consider
  // "en-US-POSIX" to be more specific than "en-Latn-US".
  //
  // Unicode extension keywords are considered to be less important than
  // scripts and variants.
  int getImportanceScoreOfLocale() {
    return (isTruthy(localeVariant[0]) ? 4 : 0)
        + (isTruthy(localeScript[0]) && !localeScriptWasComputed ? 2: 0)
        + (isTruthy(localeNumberingSystem[0]) ? 1: 0);
  }

  int compare(final ResTable_config o) {
       if (imsi() != o.imsi()) {
       return (imsi() > o.imsi()) ? 1 : -1;
   }

   int diff = compareLocales(this, o);
   if (diff < 0) {
       return -1;
   }
   if (diff > 0) {
       return 1;
   }

   if (screenType() != o.screenType()) {
       return (screenType() > o.screenType()) ? 1 : -1;
   }
   if (input() != o.input()) {
       return (input() > o.input()) ? 1 : -1;
   }
   if (screenSize() != o.screenSize()) {
       return (screenSize() > o.screenSize()) ? 1 : -1;
   }
   if (version() != o.version()) {
       return (version() > o.version()) ? 1 : -1;
   }
   if (screenLayout != o.screenLayout) {
       return (screenLayout > o.screenLayout) ? 1 : -1;
   }
   if (screenLayout2 != o.screenLayout2) {
       return (screenLayout2 > o.screenLayout2) ? 1 : -1;
   }
   if (colorMode != o.colorMode) {
       return (colorMode > o.colorMode) ? 1 : -1;
   }
   if (uiMode != o.uiMode) {
       return (uiMode > o.uiMode) ? 1 : -1;
   }
   if (smallestScreenWidthDp != o.smallestScreenWidthDp) {
       return (smallestScreenWidthDp > o.smallestScreenWidthDp) ? 1 : -1;
   }
   if (screenSizeDp() != o.screenSizeDp()) {
       return (screenSizeDp() > o.screenSizeDp()) ? 1 : -1;
   }
   return 0;
  }


  /** Returns true if this is the default "any" configuration. */
  public final boolean isDefault() {
    return mcc == 0
        && mnc == 0
        && isZeroes(language)
        && isZeroes(country)
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
        && isZeroes(localeScript)
        && isZeroes(localeVariant)
        && screenLayout2 == 0
        && colorMode == 0
        ;
  }

  private boolean isZeroes(byte[] bytes1) {
    for (byte b : bytes1) {
      if (b != 0) {
        return false;
      }
    }
    return true;
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
   * If a configuration part is not defined for this {@link ResTable_config}, its value
   * will be the empty string.
   */
  public final Map<Type, String> toStringParts() {
    Map<Type, String> result = new LinkedHashMap<>();  // Preserve order for #toString().
    result.put(Type.MCC, mcc != 0 ? "mcc" + mcc : "");
    result.put(Type.MNC, mnc != 0 ? "mnc" + mnc : "");
    result.put(Type.LANGUAGE_STRING, languageString());
    result.put(Type.LOCALE_SCRIPT_STRING, localeScriptString());
    result.put(Type.REGION_STRING, !regionString().isEmpty() ? "r" + regionString() : "");
    result.put(Type.LOCALE_VARIANT_STRING, localeVariantString());
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
    result.put(Type.COLOR_MODE_HDR, getOrDefault(COLOR_MODE_HDR_VALUES, colorModeHdr(), ""));
    result.put(
        Type.COLOR_MODE_WIDE_COLOR_GAMUT,
        getOrDefault(COLOR_MODE_WIDE_COLOR_GAMUT_VALUES, colorModeWideColorGamut(), ""));
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
    result.put(Type.SCREEN_SIZE,
        screenWidth != 0 || screenHeight != 0 ? screenWidth + "x" + screenHeight : "");

    String sdkVersion = "";
    if (this.sdkVersion != 0) {
      sdkVersion = "v" + this.sdkVersion;
      if (minorVersion != 0) {
        sdkVersion += "." + minorVersion;
      }
    }
    result.put(Type.SDK_VERSION, sdkVersion);
    return result;
  }

  private <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
    // TODO(acornwall): Remove this when Java 8's Map#getOrDefault is available.
    // Null is not returned, even if the map contains a key whose value is null. This is intended.
    V value = map.get(key);
    return value != null ? value : defaultValue;
  }


  // constants for isBetterThan...
  public static final int MASK_LAYOUTDIR = SCREENLAYOUT_LAYOUTDIR_MASK;
  static final int MASK_SCREENSIZE = SCREENLAYOUT_SIZE_MASK;

  public boolean isBetterThan(
      ResTable_config o, ResTable_config requested) {
    if (isTruthy(requested)) {
      if (isTruthy(imsi()) || isTruthy(o.imsi())) {
        if ((mcc != o.mcc) && isTruthy(requested.mcc)) {
          return (isTruthy(mcc));
        }

        if ((mnc != o.mnc) && isTruthy(requested.mnc)) {
          return (isTruthy(mnc));
        }
      }

      if (isLocaleBetterThan(o, requested)) {
        return true;
      }

      if (isTruthy(screenLayout) || isTruthy(o.screenLayout)) {
        if (isTruthy((screenLayout^o.screenLayout) & MASK_LAYOUTDIR)
            && isTruthy(requested.screenLayout & MASK_LAYOUTDIR)) {
          int myLayoutDir = screenLayout & MASK_LAYOUTDIR;
          int oLayoutDir = o.screenLayout & MASK_LAYOUTDIR;
          return (myLayoutDir > oLayoutDir);
        }
      }

      if (isTruthy(smallestScreenWidthDp) || isTruthy(o.smallestScreenWidthDp)) {
        // The configuration closest to the actual size is best.
        // We assume that larger configs have already been filtered
        // out at this point.  That means we just want the largest one.
        if (smallestScreenWidthDp != o.smallestScreenWidthDp) {
          return smallestScreenWidthDp > o.smallestScreenWidthDp;
        }
      }

      if (isTruthy(screenSizeDp()) || isTruthy(o.screenSizeDp())) {
        // "Better" is based on the sum of the difference between both
        // width and height from the requested dimensions.  We are
        // assuming the invalid configs (with smaller dimens) have
        // already been filtered.  Note that if a particular dimension
        // is unspecified, we will end up with a large value (the
        // difference between 0 and the requested dimension), which is
        // good since we will prefer a config that has specified a
        // dimension value.
        int myDelta = 0, otherDelta = 0;
        if (isTruthy(requested.screenWidthDp)) {
          myDelta += requested.screenWidthDp - screenWidthDp;
          otherDelta += requested.screenWidthDp - o.screenWidthDp;
        }
        if (isTruthy(requested.screenHeightDp)) {
          myDelta += requested.screenHeightDp - screenHeightDp;
          otherDelta += requested.screenHeightDp - o.screenHeightDp;
        }

        if (myDelta != otherDelta) {
          return myDelta < otherDelta;
        }
      }

      if (isTruthy(screenLayout) || isTruthy(o.screenLayout)) {
        if (isTruthy((screenLayout^o.screenLayout) & MASK_SCREENSIZE)
            && isTruthy(requested.screenLayout & MASK_SCREENSIZE)) {
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
            && isTruthy(requested.screenLayout & MASK_SCREENLONG)) {
          return isTruthy(screenLayout & MASK_SCREENLONG);
        }
      }

      if (isTruthy(screenLayout2) || isTruthy(o.screenLayout2)) {
        if (((screenLayout2^o.screenLayout2) & MASK_SCREENROUND) != 0 &&
            isTruthy(requested.screenLayout2 & MASK_SCREENROUND)) {
          return isTruthy(screenLayout2 & MASK_SCREENROUND);
        }
      }

      if (isTruthy(colorMode) || isTruthy(o.colorMode)) {
        if (((colorMode^o.colorMode) & MASK_WIDE_COLOR_GAMUT) != 0 &&
            isTruthy((requested.colorMode & MASK_WIDE_COLOR_GAMUT))) {
          return isTruthy(colorMode & MASK_WIDE_COLOR_GAMUT);
        }
        if (((colorMode^o.colorMode) & MASK_HDR) != 0 &&
            isTruthy((requested.colorMode & MASK_HDR))) {
          return isTruthy(colorMode & MASK_HDR);
        }
      }

      if ((orientation != o.orientation) && isTruthy(requested.orientation)) {
        return isTruthy(orientation);
      }

      if (isTruthy(uiMode) || isTruthy(o.uiMode)) {
        if (((uiMode^o.uiMode) & MASK_UI_MODE_TYPE) != 0
            && isTruthy(requested.uiMode & MASK_UI_MODE_TYPE)) {
          return isTruthy(uiMode & MASK_UI_MODE_TYPE);
        }
        if (((uiMode^o.uiMode) & MASK_UI_MODE_NIGHT) != 0
            && isTruthy(requested.uiMode & MASK_UI_MODE_NIGHT)) {
          return isTruthy(uiMode & MASK_UI_MODE_NIGHT);
        }
      }

      if (isTruthy(screenType()) || isTruthy(o.screenType())) {
        if (density != o.density) {
          // Use the system default density (DENSITY_MEDIUM, 160dpi) if none specified.
          final int thisDensity = isTruthy(density) ? density : DENSITY_MEDIUM;
          final int otherDensity = isTruthy(o.density) ? o.density : DENSITY_MEDIUM;

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

        if ((touchscreen != o.touchscreen) && isTruthy(requested.touchscreen)) {
          return isTruthy(touchscreen);
        }
      }

      if (isTruthy(input()) || isTruthy(o.input())) {
            final int keysHidden = inputFlags & MASK_KEYSHIDDEN;
            final int oKeysHidden = o.inputFlags & MASK_KEYSHIDDEN;
        if (keysHidden != oKeysHidden) {
                final int reqKeysHidden =
              requested.inputFlags & MASK_KEYSHIDDEN;
          if (isTruthy(reqKeysHidden)) {

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
          if (isTruthy(reqNavHidden)) {

            if (navHidden == 0) return false;
            if (oNavHidden == 0) return true;
          }
        }

        if ((keyboard != o.keyboard) && isTruthy(requested.keyboard)) {
          return isTruthy(keyboard);
        }

        if ((navigation != o.navigation) && isTruthy(requested.navigation)) {
          return isTruthy(navigation);
        }
      }

      if (isTruthy(screenSize()) || isTruthy(o.screenSize())) {
        // "Better" is based on the sum of the difference between both
        // width and height from the requested dimensions.  We are
        // assuming the invalid configs (with smaller sizes) have
        // already been filtered.  Note that if a particular dimension
        // is unspecified, we will end up with a large value (the
        // difference between 0 and the requested dimension), which is
        // good since we will prefer a config that has specified a
        // size value.
        int myDelta = 0, otherDelta = 0;
        if (isTruthy(requested.screenWidth)) {
          myDelta += requested.screenWidth - screenWidth;
          otherDelta += requested.screenWidth - o.screenWidth;
        }
        if (isTruthy(requested.screenHeight)) {
          myDelta += requested.screenHeight - screenHeight;
          otherDelta += requested.screenHeight - o.screenHeight;
        }
        if (myDelta != otherDelta) {
          return myDelta < otherDelta;
        }
      }

      if (isTruthy(version()) || isTruthy(o.version())) {
        if ((sdkVersion != o.sdkVersion) && isTruthy(requested.sdkVersion)) {
          return (sdkVersion > o.sdkVersion);
        }

        if ((minorVersion != o.minorVersion) &&
            isTruthy(requested.minorVersion)) {
          return isTruthy(minorVersion);
        }
      }

      return false;
    }
    return isMoreSpecificThan(o);
  }

/*
  boolean match(final ResTable_config settings) {
    System.out.println(this + ".match(" + settings + ")");
    boolean result = match_(settings);
    System.out.println("    -> " + result);
    return result;
  }
*/

  public boolean match(final ResTable_config settings) {
    if (imsi() != 0) {
      if (mcc != 0 && mcc != settings.mcc) {
        return false;
      }
      if (mnc != 0 && mnc != settings.mnc) {
        return false;
      }
    }
    if (locale() != 0) {
      // Don't consider country and variants when deciding matches.
      // (Theoretically, the variant can also affect the script. For
      // example, "ar-alalc97" probably implies the Latin script, but since
      // CLDR doesn't support getting likely scripts for that, we'll assume
      // the variant doesn't change the script.)
      //
      // If two configs differ only in their country and variant,
      // they can be weeded out in the isMoreSpecificThan test.
      if (!langsAreEquivalent(language, settings.language)) {
        return false;
      }

      // For backward compatibility and supporting private-use locales, we
      // fall back to old behavior if we couldn't determine the script for
      // either of the desired locale or the provided locale. But if we could determine
      // the scripts, they should be the same for the locales to match.
      boolean countriesMustMatch = false;
      byte[] computed_script = new byte[4];
      byte[] script = null;
      if (settings.localeScript[0] == '\0') { // could not determine the request's script
        countriesMustMatch = true;
      } else {
        if (localeScript[0] == '\0' && !localeScriptWasComputed) {
          // script was not provided or computed, so we try to compute it
          localeDataComputeScript(computed_script, language, country);
          if (computed_script[0] == '\0') { // we could not compute the script
            countriesMustMatch = true;
          } else {
            script = computed_script;
          }
        } else { // script was provided, so just use it
          script = localeScript;
        }
      }

      if (countriesMustMatch) {
        if (country[0] != '\0' && !areIdentical(country, settings.country)) {
          return false;
        }
      } else {
        if (!Arrays.equals(script, settings.localeScript)) {
          return false;
        }
      }
    }

    if (screenConfig() != 0) {
        final int layoutDir = screenLayout&MASK_LAYOUTDIR;
        final int setLayoutDir = settings.screenLayout&MASK_LAYOUTDIR;
      if (layoutDir != 0 && layoutDir != setLayoutDir) {
        return false;
      }

        final int screenSize = screenLayout&MASK_SCREENSIZE;
        final int setScreenSize = settings.screenLayout&MASK_SCREENSIZE;
      // Any screen sizes for larger screens than the setting do not
      // match.
      if (screenSize != 0 && screenSize > setScreenSize) {
        return false;
      }

        final int screenLong = screenLayout&MASK_SCREENLONG;
        final int setScreenLong = settings.screenLayout&MASK_SCREENLONG;
      if (screenLong != 0 && screenLong != setScreenLong) {
        return false;
      }

        final int uiModeType = uiMode&MASK_UI_MODE_TYPE;
        final int setUiModeType = settings.uiMode&MASK_UI_MODE_TYPE;
      if (uiModeType != 0 && uiModeType != setUiModeType) {
        return false;
      }

        final int uiModeNight = uiMode&MASK_UI_MODE_NIGHT;
        final int setUiModeNight = settings.uiMode&MASK_UI_MODE_NIGHT;
      if (uiModeNight != 0 && uiModeNight != setUiModeNight) {
        return false;
      }

      if (smallestScreenWidthDp != 0
          && smallestScreenWidthDp > settings.smallestScreenWidthDp) {
        return false;
      }
    }

    if (screenConfig2() != 0) {
        final int screenRound = screenLayout2 & MASK_SCREENROUND;
        final int setScreenRound = settings.screenLayout2 & MASK_SCREENROUND;
      if (screenRound != 0 && screenRound != setScreenRound) {
        return false;
      }
    }

    final int hdr = colorMode & MASK_HDR;
    final int setHdr = settings.colorMode & MASK_HDR;
    if (hdr != 0 && hdr != setHdr) {
      return false;
    }

    final int wideColorGamut = colorMode & MASK_WIDE_COLOR_GAMUT;
    final int setWideColorGamut = settings.colorMode & MASK_WIDE_COLOR_GAMUT;
    if (wideColorGamut != 0 && wideColorGamut != setWideColorGamut) {
      return false;
    }

    if (screenSizeDp() != 0) {
      if (screenWidthDp != 0 && screenWidthDp > settings.screenWidthDp) {
        if (kDebugTableSuperNoisy) {
          ALOGI("Filtering out width %d in requested %d", screenWidthDp,
              settings.screenWidthDp);
        }
        return false;
      }
      if (screenHeightDp != 0 && screenHeightDp > settings.screenHeightDp) {
        if (kDebugTableSuperNoisy) {
          ALOGI("Filtering out height %d in requested %d", screenHeightDp,
              settings.screenHeightDp);
        }
        return false;
      }
    }
    if (screenType() != 0) {
      if (orientation != 0 && orientation != settings.orientation) {
        return false;
      }
      // density always matches - we can scale it.  See isBetterThan
      if (touchscreen != 0 && touchscreen != settings.touchscreen) {
        return false;
      }
    }
    if (input() != 0) {
        final int keysHidden = inputFlags&MASK_KEYSHIDDEN;
        final int setKeysHidden = settings.inputFlags&MASK_KEYSHIDDEN;
      if (keysHidden != 0 && keysHidden != setKeysHidden) {
        // For compatibility, we count a request for KEYSHIDDEN_NO as also
        // matching the more recent KEYSHIDDEN_SOFT.  Basically
        // KEYSHIDDEN_NO means there is some kind of keyboard available.
        if (kDebugTableSuperNoisy) {
          ALOGI("Matching keysHidden: have=%d, config=%d\n", keysHidden, setKeysHidden);
        }
        if (keysHidden != KEYSHIDDEN_NO || setKeysHidden != KEYSHIDDEN_SOFT) {
          if (kDebugTableSuperNoisy) {
            ALOGI("No match!");
          }
          return false;
        }
      }
        final int navHidden = inputFlags&MASK_NAVHIDDEN;
        final int setNavHidden = settings.inputFlags&MASK_NAVHIDDEN;
      if (navHidden != 0 && navHidden != setNavHidden) {
        return false;
      }
      if (keyboard != 0 && keyboard != settings.keyboard) {
        return false;
      }
      if (navigation != 0 && navigation != settings.navigation) {
        return false;
      }
    }
    if (screenSize() != 0) {
      if (screenWidth != 0 && screenWidth > settings.screenWidth) {
        return false;
      }
      if (screenHeight != 0 && screenHeight > settings.screenHeight) {
        return false;
      }
    }
    if (version() != 0) {
      if (sdkVersion != 0 && sdkVersion > settings.sdkVersion) {
        return false;
      }
      if (minorVersion != 0 && minorVersion != settings.minorVersion) {
        return false;
      }
    }
    return true;
  }

//  void appendDirLocale(String8& out) const {
//    if (!language[0]) {
//      return;
//    }
//    const bool scriptWasProvided = localeScript[0] != '\0' && !localeScriptWasComputed;
//    if (!scriptWasProvided && !localeVariant[0] && !localeNumberingSystem[0]) {
//      // Legacy format.
//      if (out.size() > 0) {
//        out.append("-");
//      }
//
//      char buf[4];
//      size_t len = unpackLanguage(buf);
//      out.append(buf, len);
//
//      if (country[0]) {
//        out.append("-r");
//        len = unpackRegion(buf);
//        out.append(buf, len);
//      }
//      return;
//    }
//
//    // We are writing the modified BCP 47 tag.
//    // It starts with 'b+' and uses '+' as a separator.
//
//    if (out.size() > 0) {
//      out.append("-");
//    }
//    out.append("b+");
//
//    char buf[4];
//    size_t len = unpackLanguage(buf);
//    out.append(buf, len);
//
//    if (scriptWasProvided) {
//      out.append("+");
//      out.append(localeScript, sizeof(localeScript));
//    }
//
//    if (country[0]) {
//      out.append("+");
//      len = unpackRegion(buf);
//      out.append(buf, len);
//    }
//
//    if (localeVariant[0]) {
//      out.append("+");
//      out.append(localeVariant, strnlen(localeVariant, sizeof(localeVariant)));
//    }
//
//    if (localeNumberingSystem[0]) {
//      out.append("+u+nu+");
//      out.append(localeNumberingSystem,
//                 strnlen(localeNumberingSystem, sizeof(localeNumberingSystem)));
//    }
//  }

  // returns string as return value instead of by mutating first arg
  // void ResTable_config::getBcp47Locale(char str[RESTABLE_MAX_LOCALE_LEN], bool canonicalize) const {
  String getBcp47Locale(boolean canonicalize) {
    StringBuilder str = new StringBuilder();

    // This represents the "any" locale value, which has traditionally been
    // represented by the empty string.
    if (language[0] == '\0' && country[0] == '\0') {
      return "";
    }

    if (language[0] != '\0') {
      if (canonicalize && areIdentical(language, kTagalog)) {
        // Replace Tagalog with Filipino if we are canonicalizing
        str.setLength(0);
        str.append("fil");// 3-letter code for Filipino
      } else {
        str.append(unpackLanguage());
      }
    }

    if (isTruthy(localeScript[0]) && !localeScriptWasComputed) {
      if (str.length() > 0) {
        str.append('-');
      }
      for (byte aLocaleScript : localeScript) {
        str.append((char) aLocaleScript);
      }
    }

    if (country[0] != '\0') {
      if (str.length() > 0) {
        str.append('-');
      }
      String regionStr = unpackRegion();
      str.append(regionStr);
    }

    if (isTruthy(localeVariant[0])) {
      if (str.length() > 0) {
        str.append('-');
      }

      for (byte aLocaleScript : localeVariant) {
        str.append((char) aLocaleScript);
      }
    }

    // Add Unicode extension only if at least one other locale component is present
    if (localeNumberingSystem[0] != '\0' && str.length() > 0) {
      String NU_PREFIX = "-u-nu-";
      str.append(NU_PREFIX);
      str.append(new String(localeNumberingSystem, UTF_8));
    }

    return str.toString();
  }

  enum State {
    BASE, UNICODE_EXTENSION, IGNORE_THE_REST
  }

  enum UnicodeState {
    /* Initial state after the Unicode singleton is detected. Either a keyword
     * or an attribute is expected. */
    NO_KEY,
    /* Unicode extension key (but not attribute) is expected. Next states:
     * NO_KEY, IGNORE_KEY or NUMBERING_SYSTEM. */
    EXPECT_KEY,
    /* A key is detected, however it is not supported for now. Ignore its
     * value. Next states: IGNORE_KEY or NUMBERING_SYSTEM. */
    IGNORE_KEY,
    /* Numbering system key was detected. Store its value in the configuration
     * localeNumberingSystem field. Next state: EXPECT_KEY */
    NUMBERING_SYSTEM
  }

  static class LocaleParserState {
    State parserState;
    UnicodeState unicodeState;

    // LocaleParserState(): parserState(BASE), unicodeState(NO_KEY) {}
    public LocaleParserState() {
      this.parserState = State.BASE;
      this.unicodeState = UnicodeState.NO_KEY;
    }
  }

  static LocaleParserState assignLocaleComponent(ResTable_config config,
      final String start, int size, LocaleParserState state) {

    /* It is assumed that this function is not invoked with state.parserState
     * set to IGNORE_THE_REST. The condition is checked by setBcp47Locale
     * function. */

    if (state.parserState == State.UNICODE_EXTENSION) {
      switch (size) {
        case 1:
          /* Other BCP 47 extensions are not supported at the moment */
          state.parserState = State.IGNORE_THE_REST;
          break;
        case 2:
          if (state.unicodeState == UnicodeState.NO_KEY ||
              state.unicodeState == UnicodeState.EXPECT_KEY) {
            /* Analyze Unicode extension key. Currently only 'nu'
             * (numbering system) is supported.*/
            if ((start.charAt(0) == 'n' || start.charAt(0) == 'N') &&
                (start.charAt(1) == 'u' || start.charAt(1) == 'U')) {
              state.unicodeState = UnicodeState.NUMBERING_SYSTEM;
            } else {
              state.unicodeState = UnicodeState.IGNORE_KEY;
            }
          } else {
            /* Keys are not allowed in other state allowed, ignore the rest. */
            state.parserState = State.IGNORE_THE_REST;
          }
          break;
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
          switch (state.unicodeState) {
            case NUMBERING_SYSTEM:
              /* Accept only the first occurrence of the numbering system. */
              if (config.localeNumberingSystem[0] == '\0') {
                for (int i = 0; i < size; ++i) {
                  config.localeNumberingSystem[i] = (byte) Character.toLowerCase(start.charAt(i));
                }
                state.unicodeState = UnicodeState.EXPECT_KEY;
              } else {
                state.parserState = State.IGNORE_THE_REST;
              }
              break;
            case IGNORE_KEY:
              /* Unsupported Unicode keyword. Ignore. */
              state.unicodeState = UnicodeState.EXPECT_KEY;
              break;
            case EXPECT_KEY:
              /* A keyword followed by an attribute is not allowed. */
              state.parserState = State.IGNORE_THE_REST;
              break;
            case NO_KEY:
              /* Extension attribute. Do nothing. */
              break;
          }
          break;
        default:
          /* Unexpected field length - ignore the rest and treat as an error */
          state.parserState = State.IGNORE_THE_REST;
      }
      return state;
    }

    switch (size) {
      case 0:
        state.parserState = State.IGNORE_THE_REST;
        break;
      case 1:
        state.parserState = (start.charAt(0) == 'u' || start.charAt(0) == 'U')
            ? State.UNICODE_EXTENSION
            : State.IGNORE_THE_REST;
        break;
      case 2:
      case 3:
        if (isTruthy(config.language[0])) {
          config.packRegion(start);
        } else {
          config.packLanguage(start);
        }
        break;
      case 4:
        char start0 = start.charAt(0);
        if ('0' <= start0 && start0 <= '9') {
          // this is a variant, so fall through
        } else {
          config.localeScript[0] = (byte) Character.toUpperCase(start0);
          for (int i = 1; i < 4; ++i) {
            config.localeScript[i] = (byte) Character.toLowerCase(start.charAt(i));
          }
          break;
        }
        // fall through
      case 5:
      case 6:
      case 7:
      case 8:
        for (int i = 0; i < size; ++i) {
          config.localeVariant[i] = (byte) Character.toLowerCase(start.charAt(i));
        }
        break;
      default:
        state.parserState = State.IGNORE_THE_REST;
    }

    return state;
  }

  public void setBcp47Locale(final String in) {
    clearLocale();

    int start = 0;
    LocaleParserState state = new LocaleParserState();
    int separator;
    while ((separator = in.indexOf('-', start)) > 0) {
      final int size = separator - start;
      state = assignLocaleComponent(this, in.substring(start), size, state);
      if (state.parserState == State.IGNORE_THE_REST) {

        System.err.println(String.format("Invalid BCP-47 locale string: %s", in));
        break;
      }

      start = (separator + 1);
    }

    if (state.parserState != State.IGNORE_THE_REST) {
      final int size = in.length() - start;
      assignLocaleComponent(this, in.substring(start), size, state);
    }

    localeScriptWasComputed = (localeScript[0] == '\0');
    if (localeScriptWasComputed) {
      computeScript();
    }
  }

  void clearLocale() {
//    locale = 0;
    clear(language);
    clear(country);

    localeScriptWasComputed = false;
    clear(localeScript);
    clear(localeVariant);
  }

  void computeScript() {
    localeDataComputeScript(localeScript, language, country);
  }

  private void clear(byte[] bytes) {
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = 0;
    }
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
    return ((mcc & 0xffff) << 16) | (mnc & 0xffff);
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
    return ((screenWidth & 0xffff) << 16) | (screenHeight & 0xffff);
  }


  /**
   union {
   struct {
   uint8_t screenLayout;
   uint8_t uiMode;
   uint16_t smallestScreenWidthDp;
   };
   uint32_t screenConfig;
   };
   */
  private int screenConfig() {
    return ((screenLayout & 0xff) << 24) | ((uiMode * 0xff) << 16) | (smallestScreenWidthDp & 0xffff);
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
    return ((orientation & 0xff) << 24) | ((touchscreen & 0xff) << 16) | (density & 0xffff);
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
    return ((keyboard & 0xff) << 24) | ((navigation & 0xff) << 16) | ((inputFlags & 0xff) << 8);
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
    return ((sdkVersion & 0xffff) << 16) | (minorVersion & 0xffff);
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
    return ((language[0] & 0xff) << 24) | ((language[1] & 0xff) << 16) | ((country[0] & 0xff) << 8) | (country[1] & 0xff);
  }

  private boolean isLocaleBetterThan(ResTable_config o, ResTable_config requested) {
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
        } else if (localeDataIsCloseToUsEnglish(requested.country)) {
          if (language[0] != '\0') {
            return localeDataIsCloseToUsEnglish(country);
          } else {
            return !localeDataIsCloseToUsEnglish(o.country);
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
    final int region_comparison = localeDataCompareRegions(
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

    // The variants are the same, try numbering system.
    boolean localeNumsysMatches = arrayCompare(localeNumberingSystem,
                                             requested.localeNumberingSystem
                                             ) == 0;
    boolean otherNumsysMatches = arrayCompare(o.localeNumberingSystem,
                                            requested.localeNumberingSystem
                                            ) == 0;

    if (localeNumsysMatches != otherNumsysMatches) {
        return localeNumsysMatches;
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

  int isLocaleMoreSpecificThan(ResTable_config o) {
    if (isTruthy(locale()) || isTruthy(o.locale())) {
      if (language[0] != o.language[0]) {
        if (!isTruthy(language[0])) return -1;
        if (!isTruthy(o.language[0])) return 1;
      }
      if (country[0] != o.country[0]) {
        if (!isTruthy(country[0])) return -1;
        if (!isTruthy(o.country[0])) return 1;
      }
    }
    return getImportanceScoreOfLocale() - o.getImportanceScoreOfLocale();
  }

  private boolean isMoreSpecificThan(ResTable_config o) {
    // The order of the following tests defines the importance of one
    // configuration parameter over another.  Those tests first are more
    // important, trumping any values in those following them.
    if (isTruthy(imsi()) || isTruthy(o.imsi())) {
      if (mcc != o.mcc) {
        if (!isTruthy(mcc)) return false;
        if (!isTruthy(o.mcc)) return true;
      }
      if (mnc != o.mnc) {
        if (!isTruthy(mnc)) return false;
        if (!isTruthy(o.mnc)) return true;
      }
    }
    if (isTruthy(locale()) || isTruthy(o.locale())) {
      int diff = isLocaleMoreSpecificThan(o);
      if (diff < 0) {
        return false;
      }
      if (diff > 0) {
        return true;
      }
    }
    if (isTruthy(screenLayout) || isTruthy(o.screenLayout)) {
      if (((screenLayout^o.screenLayout) & MASK_LAYOUTDIR) != 0) {
        if (!isTruthy((screenLayout & MASK_LAYOUTDIR))) return false;
        if (!isTruthy((o.screenLayout & MASK_LAYOUTDIR))) return true;
      }
    }
    if (isTruthy(smallestScreenWidthDp) || isTruthy(o.smallestScreenWidthDp)) {
      if (smallestScreenWidthDp != o.smallestScreenWidthDp) {
        if (!isTruthy(smallestScreenWidthDp)) return false;
        if (!isTruthy(o.smallestScreenWidthDp)) return true;
      }
    }
    if (isTruthy(screenSizeDp()) || isTruthy(o.screenSizeDp())) {
      if (screenWidthDp != o.screenWidthDp) {
        if (!isTruthy(screenWidthDp)) return false;
        if (!isTruthy(o.screenWidthDp)) return true;
      }
      if (screenHeightDp != o.screenHeightDp) {
        if (!isTruthy(screenHeightDp)) return false;
        if (!isTruthy(o.screenHeightDp)) return true;
      }
    }
    if (isTruthy(screenLayout) || isTruthy(o.screenLayout)) {
      if (((screenLayout^o.screenLayout) & MASK_SCREENSIZE) != 0) {
        if (!isTruthy((screenLayout & MASK_SCREENSIZE))) return false;
        if (!isTruthy((o.screenLayout & MASK_SCREENSIZE))) return true;
      }
      if (((screenLayout^o.screenLayout) & MASK_SCREENLONG) != 0) {
        if (!isTruthy((screenLayout & MASK_SCREENLONG))) return false;
        if (!isTruthy((o.screenLayout & MASK_SCREENLONG))) return true;
      }
    }
    if (isTruthy(screenLayout2) || isTruthy(o.screenLayout2)) {
      if (((screenLayout2^o.screenLayout2) & MASK_SCREENROUND) != 0) {
        if (!isTruthy((screenLayout2 & MASK_SCREENROUND))) return false;
        if (!isTruthy((o.screenLayout2 & MASK_SCREENROUND))) return true;
      }
    }

    if (isTruthy(colorMode) || isTruthy(o.colorMode)) {
      if (((colorMode^o.colorMode) & MASK_HDR) != 0) {
        if (!isTruthy((colorMode & MASK_HDR))) return false;
        if (!isTruthy((o.colorMode & MASK_HDR))) return true;
      }
      if (((colorMode^o.colorMode) & MASK_WIDE_COLOR_GAMUT) != 0) {
        if (!isTruthy((colorMode & MASK_WIDE_COLOR_GAMUT))) return false;
        if (!isTruthy((o.colorMode & MASK_WIDE_COLOR_GAMUT))) return true;
      }
    }

    if (orientation != o.orientation) {
      if (!isTruthy(orientation)) return false;
      if (!isTruthy(o.orientation)) return true;
    }
    if (isTruthy(uiMode) || isTruthy(o.uiMode)) {
      if (((uiMode^o.uiMode) & MASK_UI_MODE_TYPE) != 0) {
        if (!isTruthy((uiMode & MASK_UI_MODE_TYPE))) return false;
        if (!isTruthy((o.uiMode & MASK_UI_MODE_TYPE))) return true;
      }
      if (((uiMode^o.uiMode) & MASK_UI_MODE_NIGHT) != 0) {
        if (!isTruthy((uiMode & MASK_UI_MODE_NIGHT))) return false;
        if (!isTruthy((o.uiMode & MASK_UI_MODE_NIGHT))) return true;
      }
    }
    // density is never 'more specific'
    // as the default just equals 160
    if (touchscreen != o.touchscreen) {
      if (!isTruthy(touchscreen)) return false;
      if (!isTruthy(o.touchscreen)) return true;
    }
    if (isTruthy(input()) || isTruthy(o.input())) {
      if (((inputFlags^o.inputFlags) & MASK_KEYSHIDDEN) != 0) {
        if (!isTruthy((inputFlags & MASK_KEYSHIDDEN))) return false;
        if (!isTruthy((o.inputFlags & MASK_KEYSHIDDEN))) return true;
      }
      if (((inputFlags^o.inputFlags) & MASK_NAVHIDDEN) != 0) {
        if (!isTruthy((inputFlags & MASK_NAVHIDDEN))) return false;
        if (!isTruthy((o.inputFlags & MASK_NAVHIDDEN))) return true;
      }
      if (keyboard != o.keyboard) {
        if (!isTruthy(keyboard)) return false;
        if (!isTruthy(o.keyboard)) return true;
      }
      if (navigation != o.navigation) {
        if (!isTruthy(navigation)) return false;
        if (!isTruthy(o.navigation)) return true;
      }
    }
    if (isTruthy(screenSize()) || isTruthy(o.screenSize())) {
      if (screenWidth != o.screenWidth) {
        if (!isTruthy(screenWidth)) return false;
        if (!isTruthy(o.screenWidth)) return true;
      }
      if (screenHeight != o.screenHeight) {
        if (!isTruthy(screenHeight)) return false;
        if (!isTruthy(o.screenHeight)) return true;
      }
    }
    if (isTruthy(version()) || isTruthy(o.version())) {
      if (sdkVersion != o.sdkVersion) {
        if (!isTruthy(sdkVersion)) return false;
        if (!isTruthy(o.sdkVersion)) return true;
      }
      if (minorVersion != o.minorVersion) {
        if (!isTruthy(minorVersion)) return false;
        if (!isTruthy(o.minorVersion)) return true;
      }
    }
    return false;
  }
}
