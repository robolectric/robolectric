package org.robolectric.res;

public class ResTableConfig {

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

    public static final int ACONFIGURATION_ORIENTATION_ANY  = 0x0000;
    public static final int ACONFIGURATION_ORIENTATION_PORT = 0x0001;
    public static final int ACONFIGURATION_ORIENTATION_LAND = 0x0002;
    public static final int ACONFIGURATION_ORIENTATION_SQUARE = 0x0003;

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

    /** Touchscreen: not specified. */
    public static final int ACONFIGURATION_TOUCHSCREEN_ANY  = 0x0000;
    /**
     * Touchscreen: value corresponding to the
     * <a href="@dacRoot/guide/topics/resources/providing-resources.html#TouchscreenQualifier">notouch</a>
     * resource qualifier.
     */
    public static final int ACONFIGURATION_TOUCHSCREEN_NOTOUCH  = 0x0001;
    /** @deprecated Not currently supported or used. */
    public static final int ACONFIGURATION_TOUCHSCREEN_STYLUS  = 0x0002;
    /**
     * Touchscreen: value corresponding to the
     * <a href="@dacRoot/guide/topics/resources/providing-resources.html#TouchscreenQualifier">finger</a>
     * resource qualifier.
     */
    public static final int ACONFIGURATION_TOUCHSCREEN_FINGER  = 0x0003;

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

    // screenLayout bits for layout direction.
    public static final int MASK_LAYOUTDIR = 0xC0;
    public static final int SHIFT_LAYOUTDIR = 6;
    public static final int LAYOUTDIR_ANY = ACONFIGURATION_LAYOUTDIR_ANY << SHIFT_LAYOUTDIR;
    public static final int LAYOUTDIR_LTR = ACONFIGURATION_LAYOUTDIR_LTR << SHIFT_LAYOUTDIR;
    public static final int LAYOUTDIR_RTL = ACONFIGURATION_LAYOUTDIR_RTL << SHIFT_LAYOUTDIR;

    public static final int SCREENWIDTH_ANY = 0;
    public static final int MASK_SCREENSIZE = 0x0f;
    public static final int SCREENSIZE_ANY = ACONFIGURATION_SCREENSIZE_ANY;
    public static final int SCREENSIZE_SMALL = ACONFIGURATION_SCREENSIZE_SMALL;
    public static final int SCREENSIZE_NORMAL = ACONFIGURATION_SCREENSIZE_NORMAL;
    public static final int SCREENSIZE_LARGE = ACONFIGURATION_SCREENSIZE_LARGE;
    public static final int SCREENSIZE_XLARGE = ACONFIGURATION_SCREENSIZE_XLARGE;

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

    public static final int MASK_WIDE_COLOR_GAMUT = 0x03;
    public static final int WIDE_COLOR_GAMUT_ANY = ACONFIGURATION_WIDE_COLOR_GAMUT_ANY;
    public static final int WIDE_COLOR_GAMUT_NO = ACONFIGURATION_WIDE_COLOR_GAMUT_NO;
    public static final int WIDE_COLOR_GAMUT_YES = ACONFIGURATION_WIDE_COLOR_GAMUT_YES;

    // colorMode bits for HDR/LDR.
    public static final int MASK_HDR = 0x0c;
    public static final int SHIFT_COLOR_MODE_HDR = 2;
    public static final int HDR_ANY = ACONFIGURATION_HDR_ANY << SHIFT_COLOR_MODE_HDR;
    public static final int HDR_NO = ACONFIGURATION_HDR_NO << SHIFT_COLOR_MODE_HDR;
    public static final int HDR_YES = ACONFIGURATION_HDR_YES << SHIFT_COLOR_MODE_HDR;

    public static final int ORIENTATION_ANY  = ACONFIGURATION_ORIENTATION_ANY;
    public static final int ORIENTATION_PORT = ACONFIGURATION_ORIENTATION_PORT;
    public static final int ORIENTATION_LAND = ACONFIGURATION_ORIENTATION_LAND;
    public static final int ORIENTATION_SQUARE = ACONFIGURATION_ORIENTATION_SQUARE;

    // uiMode bits for the mode type.
    public static final int MASK_UI_MODE_TYPE = 0x0f;
    public static final int UI_MODE_TYPE_ANY = ACONFIGURATION_UI_MODE_TYPE_ANY;
    public static final int UI_MODE_TYPE_NORMAL = ACONFIGURATION_UI_MODE_TYPE_NORMAL;
    public static final int UI_MODE_TYPE_DESK = ACONFIGURATION_UI_MODE_TYPE_DESK;
    public static final int UI_MODE_TYPE_CAR = ACONFIGURATION_UI_MODE_TYPE_CAR;
    public static final int UI_MODE_TYPE_TELEVISION = ACONFIGURATION_UI_MODE_TYPE_TELEVISION;
    public static final int UI_MODE_TYPE_APPLIANCE = ACONFIGURATION_UI_MODE_TYPE_APPLIANCE;
    public static final int UI_MODE_TYPE_WATCH = ACONFIGURATION_UI_MODE_TYPE_WATCH;
    public static final int UI_MODE_TYPE_VR_HEADSET = ACONFIGURATION_UI_MODE_TYPE_VR_HEADSET;

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

    public int mcc;
    public int mnc;
    public int screenLayout;
    public int smallestScreenWidthDp;
    public int screenWidthDp;
    public int screenHeightDp;
    public int screenLayout2;
    public int colorMode;
    public int orientation;
    public int uiMode;
    public int density;
    public int touchscreen;
    public int inputFlags;
    public int keyboard;
    public int navigation;
    public int screenWidth;
    public int screenHeight;
    public int sdkVersion;
    public int minorVersion;
}
