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
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYBOARD_12KEY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYBOARD_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYBOARD_NOKEYS;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_KEYBOARD_QWERTY;
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
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_NAVIGATION_DPAD;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_NAVIGATION_NONAV;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_NAVIGATION_TRACKBALL;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_NAVIGATION_WHEEL;
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
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_TOUCHSCREEN_FINGER;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_TOUCHSCREEN_NOTOUCH;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_TOUCHSCREEN_STYLUS;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_NIGHT_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_NIGHT_NO;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_NIGHT_YES;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_ANY;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_APPLIANCE;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_CAR;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_DESK;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_NORMAL;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_TELEVISION;
import static org.robolectric.res.android.AConfiguration.ACONFIGURATION_UI_MODE_TYPE_WATCH;
import static org.robolectric.res.android.LocaleData.localeDataComputeScript;
import static org.robolectric.res.android.ResTable.kDebugTableSuperNoisy;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.isTruthy;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import java.nio.ByteBuffer;
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

  // Codes for specially handled languages and regions
  static final byte[] kEnglish = new byte[] {'e', 'n'};  // packed version of "en"
  static final byte[] kUnitedStates = new byte[] {'U', 'S'};  // packed version of "US"
  static final byte[] kFilipino = new byte[] {(byte)0xAD, 0x05};  // packed version of "fil" ported from C {'\xAD', '\x05'}
  static final byte[] kTagalog = new byte[] {'t', 'l'};  // packed version of "tl"

  static ResTableConfig createConfig(ByteBuffer buffer) {
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

    return new ResTableConfig(size, mcc, mnc, language, region, orientation,
        touchscreen, density, keyboard, navigation, inputFlags, screenWidth, screenHeight,
        sdkVersion, minorVersion, screenLayout, uiMode, smallestScreenWidthDp, screenWidthDp,
        screenHeightDp, localeScript, localeVariant, screenLayout2, screenConfigPad1, screenConfigPad2, unknown);
  }

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
//  public static final int SCREENSIZE_NORMAL = ACONFIGURATION_SCREENSIZE_NORMAL;
  public static final int SCREENSIZE_LARGE = ACONFIGURATION_SCREENSIZE_LARGE;
  public static final int SCREENSIZE_XLARGE = ACONFIGURATION_SCREENSIZE_XLARGE;

  // uiMode bits for the mode type.
  public static final int MASK_UI_MODE_TYPE = 0x0f;
  public static final int UI_MODE_TYPE_ANY = ACONFIGURATION_UI_MODE_TYPE_ANY;
  public static final int UI_MODE_TYPE_NORMAL = ACONFIGURATION_UI_MODE_TYPE_NORMAL;
  public static final int UI_MODE_TYPE_DESK = ACONFIGURATION_UI_MODE_TYPE_DESK;
  public static final int UI_MODE_TYPE_CAR = ACONFIGURATION_UI_MODE_TYPE_CAR;
  public static final int UI_MODE_TYPE_TELEVISION = ACONFIGURATION_UI_MODE_TYPE_TELEVISION;
  public static final int UI_MODE_TYPE_APPLIANCE = ACONFIGURATION_UI_MODE_TYPE_APPLIANCE;
  public static final int UI_MODE_TYPE_WATCH = ACONFIGURATION_UI_MODE_TYPE_WATCH;

  // uiMode bits for the night switch;
  public static final int MASK_UI_MODE_NIGHT = 0x30;
  public static final int SHIFT_UI_MODE_NIGHT = 4;
  public static final int UI_MODE_NIGHT_ANY = ACONFIGURATION_UI_MODE_NIGHT_ANY << SHIFT_UI_MODE_NIGHT;
  public static final int UI_MODE_NIGHT_NO = ACONFIGURATION_UI_MODE_NIGHT_NO << SHIFT_UI_MODE_NIGHT;
  public static final int UI_MODE_NIGHT_YES = ACONFIGURATION_UI_MODE_NIGHT_YES << SHIFT_UI_MODE_NIGHT;

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
  public static final int TOUCHSCREEN_NOTOUCH  = ACONFIGURATION_TOUCHSCREEN_NOTOUCH;
  public static final int TOUCHSCREEN_STYLUS  = ACONFIGURATION_TOUCHSCREEN_STYLUS;
  public static final int TOUCHSCREEN_FINGER  = ACONFIGURATION_TOUCHSCREEN_FINGER;

  public static final int MASK_KEYSHIDDEN = 0x0003;
  public static final byte KEYSHIDDEN_ANY = ACONFIGURATION_KEYSHIDDEN_ANY;
  public static final byte KEYSHIDDEN_NO = ACONFIGURATION_KEYSHIDDEN_NO;
  public static final byte KEYSHIDDEN_YES = ACONFIGURATION_KEYSHIDDEN_YES;
  public static final byte KEYSHIDDEN_SOFT = ACONFIGURATION_KEYSHIDDEN_SOFT;

  public static final int KEYBOARD_ANY  = ACONFIGURATION_KEYBOARD_ANY;
  public static final int KEYBOARD_NOKEYS  = ACONFIGURATION_KEYBOARD_NOKEYS;
  public static final int KEYBOARD_QWERTY  = ACONFIGURATION_KEYBOARD_QWERTY;
  public static final int KEYBOARD_12KEY  = ACONFIGURATION_KEYBOARD_12KEY;

  public static final int MASK_NAVHIDDEN = 0x000c;
  public static final int SHIFT_NAVHIDDEN = 2;
  public static final byte NAVHIDDEN_ANY = ACONFIGURATION_NAVHIDDEN_ANY << SHIFT_NAVHIDDEN;
  public static final byte NAVHIDDEN_NO = ACONFIGURATION_NAVHIDDEN_NO << SHIFT_NAVHIDDEN;
  public static final byte NAVHIDDEN_YES = ACONFIGURATION_NAVHIDDEN_YES << SHIFT_NAVHIDDEN;

  public static final int NAVIGATION_ANY  = ACONFIGURATION_NAVIGATION_ANY;
  public static final int NAVIGATION_NONAV  = ACONFIGURATION_NAVIGATION_NONAV;
  public static final int NAVIGATION_DPAD  = ACONFIGURATION_NAVIGATION_DPAD;
  public static final int NAVIGATION_TRACKBALL  = ACONFIGURATION_NAVIGATION_TRACKBALL;
  public static final int NAVIGATION_WHEEL  = ACONFIGURATION_NAVIGATION_WHEEL;

  public static final int SCREENHEIGHT_ANY = 0;

  public static final int SDKVERSION_ANY = 0;
  public static final int MINORVERSION_ANY = 0;

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

  private static final Map<Integer, String> TOUCHSCREEN_VALUES = ImmutableMap.of(
      TOUCHSCREEN_NOTOUCH, "notouch",
      TOUCHSCREEN_FINGER, "finger");

  private static final int UI_MODE_NIGHT_MASK = 0x30;
  private static final Map<Integer, String> UI_MODE_NIGHT_VALUES = ImmutableMap.of(
      UI_MODE_NIGHT_NO, "notnight",
      UI_MODE_NIGHT_YES, "night");

  private static final int UI_MODE_TYPE_MASK       = 0x0F;
  private static final Map<Integer, String> UI_MODE_TYPE_VALUES = ImmutableMap.of(
      UI_MODE_TYPE_DESK, "desk",
      UI_MODE_TYPE_CAR, "car",
      UI_MODE_TYPE_TELEVISION, "television",
      UI_MODE_TYPE_APPLIANCE, "appliance",
      UI_MODE_TYPE_WATCH, "watch");

  // screenLayout bits for wide/long screen variation.
  public static final int MASK_SCREENLONG = 0x30;
  public static final int SHIFT_SCREENLONG = 4;
  public static final int SCREENLONG_ANY = ACONFIGURATION_SCREENLONG_ANY << SHIFT_SCREENLONG;
  public static final int SCREENLONG_NO = ACONFIGURATION_SCREENLONG_NO << SHIFT_SCREENLONG;
  public static final int SCREENLONG_YES = ACONFIGURATION_SCREENLONG_YES << SHIFT_SCREENLONG;

  // screenLayout2 bits for round/notround.
  public static final int MASK_SCREENROUND = 0x03;
  public static final int SCREENROUND_ANY = ACONFIGURATION_SCREENROUND_ANY;
  public static final int SCREENROUND_NO = ACONFIGURATION_SCREENROUND_NO;
  public static final int SCREENROUND_YES = ACONFIGURATION_SCREENROUND_YES;

  public static final int ORIENTATION_ANY  = ACONFIGURATION_ORIENTATION_ANY;
  public static final int ORIENTATION_PORT = ACONFIGURATION_ORIENTATION_PORT;
  public static final int ORIENTATION_LAND = ACONFIGURATION_ORIENTATION_LAND;
  public static final int ORIENTATION_SQUARE = ACONFIGURATION_ORIENTATION_SQUARE;

  /** The number of bytes that this resource configuration takes up. */
  int size;

  public int mcc;
  public int mnc;

  /** Returns a packed 2-byte language code. */
  @SuppressWarnings("mutable")
  public final byte[] language;

  /** Returns {@link #language} as an unpacked string representation. */
  public final String languageString() {
    return unpackLanguage();
  }

  /** Returns a packed 2-byte country code. */
  @SuppressWarnings("mutable")
  public final byte[] country;

  /** Returns {@link #country} as an unpacked string representation. */
  public final String regionString() {
    return unpackRegion();
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

  public final int navigationHidden() {
    return inputFlags & NAVIGATIONHIDDEN_MASK;
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
  public final ResTableConfig withSdkVersion(int sdkVersion) {
    if (sdkVersion == this.sdkVersion) {
      return this;
    }
    return new ResTableConfig(size, mcc, mnc, language, country,
        orientation, touchscreen, density, keyboard, navigation, inputFlags,
        screenWidth, screenHeight, sdkVersion, minorVersion, screenLayout, uiMode,
        smallestScreenWidthDp, screenWidthDp, screenHeightDp, localeScript, localeVariant,
        screenLayout2, screenConfigPad1, screenConfigPad2, unknown);
  }

  public ResTableConfig(int size, int mcc, int mnc, byte[] language, byte[] country,
      int orientation, int touchscreen, int density, int keyboard, int navigation, int inputFlags,
      int screenWidth, int screenHeight, int sdkVersion, int minorVersion, int screenLayout,
      int uiMode, int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      byte[] localeScript, byte[] localeVariant, byte screenLayout2, byte screenConfigPad1,
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
    this.screenConfigPad1 = screenConfigPad1;
    this.screenConfigPad2 = screenConfigPad2;
    this.unknown = unknown;
  }

  public ResTableConfig() {
    this.language = new byte[2];
    this.country = new byte[2];
    this.localeScript = new byte[LocaleData.SCRIPT_LENGTH];
    this.localeVariant = new byte[2];
  }

  public int minorVersion;
  public int screenLayout;

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

  public int colorMode;

  public int uiMode;

  public final int uiModeType() {
    return uiMode & UI_MODE_TYPE_MASK;
  }

  public final int uiModeNight() {
    return uiMode & UI_MODE_NIGHT_MASK;
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
  public byte screenLayout2;
  public byte screenConfigPad1;
  public short screenConfigPad2;

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
    return (screenLayout2 & 0xff << 24) | (screenConfigPad1 * 0xff << 16) | screenConfigPad2 & 0xffff;
  }

  // If false and localeScript is set, it means that the script of the locale
  // was explicitly provided.
  //
  // If true, it means that localeScript was automatically computed.
  // localeScript may still not be set in this case, which means that we
  // tried but could not compute a script.
  boolean localeScriptWasComputed;

// --------------------------------------------------------------------
// --------------------------------------------------------------------
// --------------------------------------------------------------------

//  void copyFromDeviceNoSwap(final ResTableConfig o) {
//    final int size = dtohl(o.size);
//    if (size >= sizeof(ResTable_config)) {
//        *this = o;
//    } else {
//      memcpy(this, &o, size);
//      memset(((uint8_t*)this)+size, 0, sizeof(ResTable_config)-size);
//    }
//  }

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

  /* static */ void packLanguageOrRegion(final byte[] in, final byte base,
      final byte out[]) {
    if (in.length < 3 || in[2] == 0 || in[2] == '-') {
      out[0] = in[0];
      out[1] = in[1];
    } else {
      byte first = (byte) ((in[0] - base) & 0x007f);
      byte second = (byte) ((in[1] - base) & 0x007f);
      byte third = (byte) ((in[2] - base) & 0x007f);

      out[0] = (byte) (0x80 | (third << 2) | (second >> 3));
      out[1] = (byte) ((second << 5) | first);
    }
  }

  void packLanguage(final byte[] language) {
    packLanguageOrRegion(language, (byte) 'a', this.language);
  }

  void packLanguage(final String language) {
    byte[] bytes = language == null ? new byte[2] : language.getBytes();
    packLanguageOrRegion(bytes, (byte) 'a', this.language);
  }

  void packRegion(final byte[] region) {
    packLanguageOrRegion(region, (byte) '0', this.country);
  }

  void packRegion(final String region) {
    byte[] bytes = region == null ? new byte[2] : region.getBytes();
    packLanguageOrRegion(bytes, (byte) '0', this.country);
  }

  private String unpackLanguage() {
    return unpackLanguageOrRegion(language, 0x61);
  }

  private String unpackRegion() {
    return unpackLanguageOrRegion(country, 0x30);
  }

//  void copyFromDtoH(final ResTableConfig o) {
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
  static ResTableConfig fromDtoH(final ResTableConfig o) {
    return new ResTableConfig(
        0 /*sizeof(ResTable_config)*/,
        dtohs((short) o.mcc),
        dtohs((short) o.mnc),
        o.language,
        o.country,
        o.orientation,
        o.touchscreen,
        dtohs((short) o.density),
        o.keyboard,
        o.navigation,
        o.inputFlags,
        dtohs((short) o.screenWidth),
        dtohs((short) o.screenHeight),
        dtohs((short) o.sdkVersion),
        dtohs((short) o.minorVersion),
        o.screenLayout,
        o.uiMode,
        dtohs((short) o.smallestScreenWidthDp),
        dtohs((short) o.screenWidthDp),
        dtohs((short) o.screenHeightDp),
        o.localeScript,
        o.localeVariant,
        o.screenLayout2,
        o.screenConfigPad1,
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

  static final int compareLocales(final ResTableConfig l, final ResTableConfig r) {
    if (l.locale() != r.locale()) {
      // NOTE: This is the old behaviour with respect to comparison orders.
      // The diff value here doesn't make much sense (given our bit packing scheme)
      // but it's stable, and that's all we need.
      return l.locale() - r.locale();
    }

    // The language & region are equal, so compare the scripts and variants.
    final byte emptyScript[] = {'\0', '\0', '\0', '\0'};
    final byte[] lScript = l.localeScriptWasComputed ? emptyScript : l.localeScript;
    final byte[] rScript = r.localeScriptWasComputed ? emptyScript : r.localeScript;
//    int script = memcmp(lScript, rScript);
//    if (script) {
//      return script;
//    }
    int d = arrayCompare(lScript, rScript);
    if (d != 0) return d;

    // The language, region and script are equal, so compare variants.
    //
    // This should happen very infrequently (if at all.)
    return arrayCompare(l.localeVariant, r.localeVariant);
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

  int compare(final ResTableConfig o) {
    int diff = imsi() - o.imsi();
    if (diff != 0) return diff;
    diff = compareLocales(this, o);
    if (diff != 0) return diff;
    diff = (screenType() - o.screenType());
    if (diff != 0) return diff;
    diff = (input() - o.input());
    if (diff != 0) return diff;
    diff = (screenSize() - o.screenSize());
    if (diff != 0) return diff;
    diff = (version() - o.version());
    if (diff != 0) return diff;
    diff = (screenLayout - o.screenLayout);
    if (diff != 0) return diff;
    diff = (screenLayout2 - o.screenLayout2);
    if (diff != 0) return diff;
    diff = (uiMode - o.uiMode);
    if (diff != 0) return diff;
    diff = (smallestScreenWidthDp - o.smallestScreenWidthDp);
    if (diff != 0) return diff;
    diff = (screenSizeDp() - o.screenSizeDp());
    return diff;
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
        && screenLayout2 == 0;
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
   * <p>If a configuration part is not defined for this {@link ResTableConfig}, its value
   * will be the empty string.
   */
  public final Map<Type, String> toStringParts() {
    Map<Type, String> result = new LinkedHashMap<>();  // Preserve order for #toString().
    result.put(Type.MCC, isTruthy(mcc) ? "mcc" + mcc : "");
    result.put(Type.MNC, isTruthy(mnc) ? "mnc" + mnc : "");
    result.put(Type.LANGUAGE_STRING, !languageString().isEmpty() ? "" + languageString() : "");
    result.put(Type.REGION_STRING, !regionString().isEmpty() ? "r" + regionString() : "");
    result.put(Type.SCREEN_LAYOUT_DIRECTION,
        getOrDefault(SCREENLAYOUT_LAYOUTDIR_VALUES, screenLayoutDirection(), ""));
    result.put(Type.SMALLEST_SCREEN_WIDTH_DP,
        isTruthy(smallestScreenWidthDp) ? "sw" + smallestScreenWidthDp + "dp" : "");
    result.put(Type.SCREEN_WIDTH_DP, isTruthy(screenWidthDp) ? "w" + screenWidthDp + "dp" : "");
    result.put(Type.SCREEN_HEIGHT_DP, isTruthy(screenHeightDp) ? "h" + screenHeightDp + "dp" : "");
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
    result.put(Type.SDK_VERSION, isTruthy(sdkVersion) ? "v" + sdkVersion : "");
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
  static final int SCREENSIZE_NORMAL = ACONFIGURATION_SCREENSIZE_NORMAL;



  // transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp

/*
  */
/**
   * Is {@code requested} a better match to this {@link ResTableConfig} object than {@code o}
   *//*

  public boolean isBetterThan(ResTableConfig o, ResTableConfig requested) {
    boolean result = isBetterThan_(o, requested);
    System.out.println(this);
    System.out.println("  .isBetterThan(");
    System.out.println("    o: " + o);
    System.out.println("    requested: " + o);
    System.out.println("      -> " + result);
    return result;
  }
*/

  public boolean isBetterThan(ResTableConfig o, ResTableConfig requested) {
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
  boolean match(final ResTableConfig settings) {
    System.out.println(this + ".match(" + settings + ")");
    boolean result = match_(settings);
    System.out.println("    -> " + result);
    return result;
  }
*/

  boolean match(final ResTableConfig settings) {
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
      if (language[0] != settings.language[0] || language[1] != settings.language[1]) {
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
        if (country[0] != '\0'
            && (country[0] != settings.country[0]
            || country[1] != settings.country[1])) {
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
//    if (!scriptWasProvided && !localeVariant[0]) {
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
//  }

  String getBcp47Locale() {
    StringBuilder str = new StringBuilder();

    // This represents the "any" locale value, which has traditionally been
    // represented by the empty string.
    if (!isTruthy(language[0]) && !isTruthy(country[0])) {
      return "";
    }

    if (isTruthy(language[0])) {
      String languageStr = unpackLanguage();
      str.append(languageStr);
    }

    if (isTruthy(localeScript[0]) && !localeScriptWasComputed) {
      if (str.length() > 0) {
        str.append('-');
      }
      for (byte aLocaleScript : localeScript) {
        str.append((char) aLocaleScript);
      }
    }

    if (isTruthy(country[0])) {
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

    return str.toString();
  }

   static boolean assignLocaleComponent(ResTableConfig config,
        final String start, int size) {

    switch (size) {
      case 0:
        return false;
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
        return false;
    }

    return true;
  }

  void setBcp47Locale(final String in) {
//    locale = 0;
    clear(language);
    clear(country);

    clear(localeScript);
    clear(localeVariant);

    int separator;
    int start = 0;
    while ((separator = in.indexOf('-', start)) > 0) {
      final int size = separator - start;
      if (!assignLocaleComponent(this, in.substring(start), size)) {
        System.err.println(String.format("Invalid BCP-47 locale string: %s", in));
      }

      start = (separator + 1);
    }

    final int size = in.length() - start;
    assignLocaleComponent(this, in.substring(start), size);
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
    return (screenLayout & 0xff << 24) | (uiMode * 0xff << 16) | smallestScreenWidthDp & 0xffff;
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

