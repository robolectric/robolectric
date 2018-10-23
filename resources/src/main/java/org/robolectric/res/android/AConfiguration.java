package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/native/+/android-9.0.0_r12/include/android/configuration.h
public class AConfiguration {
/** Orientation: not specified. */
  public static final int ACONFIGURATION_ORIENTATION_ANY  = 0x0000;
  /**
   * Orientation: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#OrientationQualifier">port</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_ORIENTATION_PORT = 0x0001;
  /**
   * Orientation: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#OrientationQualifier">land</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_ORIENTATION_LAND = 0x0002;
  /** @deprecated Not currently supported or used. */
  @Deprecated
  public static final int ACONFIGURATION_ORIENTATION_SQUARE = 0x0003;
  /** Touchscreen: not specified. */
  public static final int ACONFIGURATION_TOUCHSCREEN_ANY  = 0x0000;
  /**
   * Touchscreen: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#TouchscreenQualifier">notouch</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_TOUCHSCREEN_NOTOUCH  = 0x0001;
  /** @deprecated Not currently supported or used. */
  @Deprecated
  public static final int ACONFIGURATION_TOUCHSCREEN_STYLUS  = 0x0002;
  /**
   * Touchscreen: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#TouchscreenQualifier">finger</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_TOUCHSCREEN_FINGER  = 0x0003;
  /** Density: default density. */
  public static final int ACONFIGURATION_DENSITY_DEFAULT = 0;
  /**
   * Density: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#DensityQualifier">ldpi</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_DENSITY_LOW = 120;
  /**
   * Density: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#DensityQualifier">mdpi</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_DENSITY_MEDIUM = 160;
  /**
   * Density: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#DensityQualifier">tvdpi</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_DENSITY_TV = 213;
  /**
   * Density: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#DensityQualifier">hdpi</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_DENSITY_HIGH = 240;
  /**
   * Density: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#DensityQualifier">xhdpi</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_DENSITY_XHIGH = 320;
  /**
   * Density: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#DensityQualifier">xxhdpi</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_DENSITY_XXHIGH = 480;
  /**
   * Density: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#DensityQualifier">xxxhdpi</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_DENSITY_XXXHIGH = 640;
  /** Density: any density. */
  public static final int ACONFIGURATION_DENSITY_ANY = 0xfffe;
  /** Density: no density specified. */
  public static final int ACONFIGURATION_DENSITY_NONE = 0xffff;
  /** Keyboard: not specified. */
  public static final int ACONFIGURATION_KEYBOARD_ANY  = 0x0000;
  /**
   * Keyboard: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ImeQualifier">nokeys</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_KEYBOARD_NOKEYS  = 0x0001;
  /**
   * Keyboard: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ImeQualifier">qwerty</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_KEYBOARD_QWERTY  = 0x0002;
  /**
   * Keyboard: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ImeQualifier">12key</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_KEYBOARD_12KEY  = 0x0003;
  /** Navigation: not specified. */
  public static final int ACONFIGURATION_NAVIGATION_ANY  = 0x0000;
  /**
   * Navigation: value corresponding to the
   * <a href="@@dacRoot/guide/topics/resources/providing-resources.html#NavigationQualifier">nonav</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_NAVIGATION_NONAV  = 0x0001;
  /**
   * Navigation: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#NavigationQualifier">dpad</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_NAVIGATION_DPAD  = 0x0002;
  /**
   * Navigation: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#NavigationQualifier">trackball</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_NAVIGATION_TRACKBALL  = 0x0003;
  /**
   * Navigation: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#NavigationQualifier">wheel</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_NAVIGATION_WHEEL  = 0x0004;
  /** Keyboard availability: not specified. */
  public static final int ACONFIGURATION_KEYSHIDDEN_ANY = 0x0000;
  /**
   * Keyboard availability: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#KeyboardAvailQualifier">keysexposed</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_KEYSHIDDEN_NO = 0x0001;
  /**
   * Keyboard availability: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#KeyboardAvailQualifier">keyshidden</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_KEYSHIDDEN_YES = 0x0002;
  /**
   * Keyboard availability: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#KeyboardAvailQualifier">keyssoft</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_KEYSHIDDEN_SOFT = 0x0003;
  /** Navigation availability: not specified. */
  public static final int ACONFIGURATION_NAVHIDDEN_ANY = 0x0000;
  /**
   * Navigation availability: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#NavAvailQualifier">navexposed</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_NAVHIDDEN_NO = 0x0001;
  /**
   * Navigation availability: value corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#NavAvailQualifier">navhidden</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_NAVHIDDEN_YES = 0x0002;
  /** Screen size: not specified. */
  public static final int ACONFIGURATION_SCREENSIZE_ANY  = 0x00;
  /**
   * Screen size: value indicating the screen is at least
   * approximately 320x426 dp units, corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">small</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_SCREENSIZE_SMALL = 0x01;
  /**
   * Screen size: value indicating the screen is at least
   * approximately 320x470 dp units, corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">normal</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_SCREENSIZE_NORMAL = 0x02;
  /**
   * Screen size: value indicating the screen is at least
   * approximately 480x640 dp units, corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">large</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_SCREENSIZE_LARGE = 0x03;
  /**
   * Screen size: value indicating the screen is at least
   * approximately 720x960 dp units, corresponding to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">xlarge</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_SCREENSIZE_XLARGE = 0x04;
  /** Screen layout: not specified. */
  public static final int ACONFIGURATION_SCREENLONG_ANY = 0x00;
  /**
   * Screen layout: value that corresponds to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenAspectQualifier">notlong</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_SCREENLONG_NO = 0x1;
  /**
   * Screen layout: value that corresponds to the
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenAspectQualifier">long</a>
   * resource qualifier.
   */
  public static final int ACONFIGURATION_SCREENLONG_YES = 0x2;
  public static final int ACONFIGURATION_SCREENROUND_ANY = 0x00;
  public static final int ACONFIGURATION_SCREENROUND_NO = 0x1;
  public static final int ACONFIGURATION_SCREENROUND_YES = 0x2;

  /** Wide color gamut: not specified. */
  public static final int ACONFIGURATION_WIDE_COLOR_GAMUT_ANY = 0x00;
  /**
   * Wide color gamut: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#WideColorGamutQualifier">no
   * nowidecg</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_WIDE_COLOR_GAMUT_NO = 0x1;
  /**
   * Wide color gamut: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#WideColorGamutQualifier">
   * widecg</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_WIDE_COLOR_GAMUT_YES = 0x2;

  /** HDR: not specified. */
  public static final int ACONFIGURATION_HDR_ANY = 0x00;
  /**
   * HDR: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#HDRQualifier">
   * lowdr</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_HDR_NO = 0x1;
  /**
   * HDR: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#HDRQualifier">
   * highdr</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_HDR_YES = 0x2;

  /** UI mode: not specified. */
  public static final int ACONFIGURATION_UI_MODE_TYPE_ANY = 0x00;
  /**
   * UI mode: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#UiModeQualifier">no
   * UI mode type</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_UI_MODE_TYPE_NORMAL = 0x01;
  /**
   * UI mode: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#UiModeQualifier">desk</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_UI_MODE_TYPE_DESK = 0x02;
  /**
   * UI mode: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#UiModeQualifier">car</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_UI_MODE_TYPE_CAR = 0x03;
  /**
   * UI mode: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#UiModeQualifier">television</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_UI_MODE_TYPE_TELEVISION = 0x04;
  /**
   * UI mode: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#UiModeQualifier">appliance</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_UI_MODE_TYPE_APPLIANCE = 0x05;
  /**
   * UI mode: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#UiModeQualifier">watch</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_UI_MODE_TYPE_WATCH = 0x06;
   /**
  * UI mode: value that corresponds to
  * <a href="@dacRoot/guide/topics/resources/providing-resources.html#UiModeQualifier">vr</a> resource qualifier specified.
  */
  public static final int ACONFIGURATION_UI_MODE_TYPE_VR_HEADSET = 0x07;
  /** UI night mode: not specified.*/
  public static final int ACONFIGURATION_UI_MODE_NIGHT_ANY = 0x00;
  /**
   * UI night mode: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#NightQualifier">notnight</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_UI_MODE_NIGHT_NO = 0x1;
  /**
   * UI night mode: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#NightQualifier">night</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_UI_MODE_NIGHT_YES = 0x2;
  /** Screen width DPI: not specified. */
  public static final int ACONFIGURATION_SCREEN_WIDTH_DP_ANY = 0x0000;
  /** Screen height DPI: not specified. */
  public static final int ACONFIGURATION_SCREEN_HEIGHT_DP_ANY = 0x0000;
  /** Smallest screen width DPI: not specified.*/
  public static final int ACONFIGURATION_SMALLEST_SCREEN_WIDTH_DP_ANY = 0x0000;
  /** Layout direction: not specified. */
  public static final int ACONFIGURATION_LAYOUTDIR_ANY  = 0x00;
  /**
   * Layout direction: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#LayoutDirectionQualifier">ldltr</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_LAYOUTDIR_LTR  = 0x01;
  /**
   * Layout direction: value that corresponds to
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#LayoutDirectionQualifier">ldrtl</a> resource qualifier specified.
   */
  public static final int ACONFIGURATION_LAYOUTDIR_RTL  = 0x02;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#MccQualifier">mcc</a>
   * configuration.
   */
  public static final int ACONFIGURATION_MCC = 0x0001;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#MccQualifier">mnc</a>
   * configuration.
   */
  public static final int ACONFIGURATION_MNC = 0x0002;
  /**
   * Bit mask for
   * <a href="{@docRoot}guide/topics/resources/providing-resources.html#LocaleQualifier">locale</a>
   * configuration.
   */
  public static final int ACONFIGURATION_LOCALE = 0x0004;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#TouchscreenQualifier">touchscreen</a>
   * configuration.
   */
  public static final int ACONFIGURATION_TOUCHSCREEN = 0x0008;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ImeQualifier">keyboard</a>
   * configuration.
   */
  public static final int ACONFIGURATION_KEYBOARD = 0x0010;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#KeyboardAvailQualifier">keyboardHidden</a>
   * configuration.
   */
  public static final int ACONFIGURATION_KEYBOARD_HIDDEN = 0x0020;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#NavigationQualifier">navigation</a>
   * configuration.
   */
  public static final int ACONFIGURATION_NAVIGATION = 0x0040;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#OrientationQualifier">orientation</a>
   * configuration.
   */
  public static final int ACONFIGURATION_ORIENTATION = 0x0080;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#DensityQualifier">density</a>
   * configuration.
   */
  public static final int ACONFIGURATION_DENSITY = 0x0100;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">screen size</a>
   * configuration.
   */
  public static final int ACONFIGURATION_SCREEN_SIZE = 0x0200;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#VersionQualifier">platform version</a>
   * configuration.
   */
  public static final int ACONFIGURATION_VERSION = 0x0400;
  /**
   * Bit mask for screen layout configuration.
   */
  public static final int ACONFIGURATION_SCREEN_LAYOUT = 0x0800;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#UiModeQualifier">ui mode</a>
   * configuration.
   */
  public static final int ACONFIGURATION_UI_MODE = 0x1000;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#SmallestScreenWidthQualifier">smallest screen width</a>
   * configuration.
   */
  public static final int ACONFIGURATION_SMALLEST_SCREEN_SIZE = 0x2000;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#LayoutDirectionQualifier">layout direction</a>
   * configuration.
   */
  public static final int ACONFIGURATION_LAYOUTDIR = 0x4000;
  public static final int ACONFIGURATION_SCREEN_ROUND = 0x8000;
  /**
   * Bit mask for
   * <a href="@dacRoot/guide/topics/resources/providing-resources.html#WideColorGamutQualifier">wide color gamut</a>
   * and <a href="@dacRoot/guide/topics/resources/providing-resources.html#HDRQualifier">HDR</a> configurations.
   */
  public static final int ACONFIGURATION_COLOR_MODE = 0x10000;
  /**
   * Constant used to to represent MNC (Mobile Network Code) zero.
   * 0 cannot be used, since it is used to represent an undefined MNC.
   */
  public static final int ACONFIGURATION_MNC_ZERO = 0xffff;
}
