package org.robolectric.android;

import android.app.WindowConfiguration;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import java.util.Locale;
import org.robolectric.util.ReflectionHelpers;

/**
 * Supports device configuration for Robolectric tests.
 *
 * @see <a href="http://robolectric.org/device-configuration/">Device Configuration</a>
 */
@SuppressWarnings("NewApi")
public class DeviceConfig {
  public static final int DEFAULT_DENSITY = DisplayMetrics.DENSITY_MEDIUM;
  public static final ScreenSize DEFAULT_SCREEN_SIZE = ScreenSize.normal;

  /**
   * Standard sizes for the screen size qualifier.
   *
   * @see <a
   *     href="https://developer.android.com/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">Screen
   *     Size Qualifier</a>.
   */
  public enum ScreenSize {
    small(320, 426, Configuration.SCREENLAYOUT_SIZE_SMALL),
    normal(320, 470, Configuration.SCREENLAYOUT_SIZE_NORMAL),
    large(480, 640, Configuration.SCREENLAYOUT_SIZE_LARGE),
    xlarge(720, 960, Configuration.SCREENLAYOUT_SIZE_XLARGE);

    public final int width;
    public final int height;
    public final int landscapeWidth;
    public final int landscapeHeight;
    private final int configValue;

    ScreenSize(int width, int height, int configValue) {
      this.width = width;
      this.height = height;

      //noinspection SuspiciousNameCombination
      this.landscapeWidth = height;
      //noinspection SuspiciousNameCombination
      this.landscapeHeight = width;

      this.configValue = configValue;
    }

    private boolean isSmallerThanOrEqualTo(int x, int y) {
      if (y < x) {
        int oldY = y;
        //noinspection SuspiciousNameCombination
        y = x;
        //noinspection SuspiciousNameCombination
        x = oldY;
      }

      return width <= x && height <= y;
    }

    static ScreenSize find(int configValue) {
      switch (configValue) {
        case Configuration.SCREENLAYOUT_SIZE_SMALL:
          return small;
        case Configuration.SCREENLAYOUT_SIZE_NORMAL:
          return normal;
        case Configuration.SCREENLAYOUT_SIZE_LARGE:
          return large;
        case Configuration.SCREENLAYOUT_SIZE_XLARGE:
          return xlarge;
        case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
          return null;
        default:
          throw new IllegalArgumentException();
      }
    }

    static ScreenSize match(int x, int y) {
      ScreenSize bestMatch = small;

      for (ScreenSize screenSize : values()) {
        if (screenSize.isSmallerThanOrEqualTo(x, y)) {
          bestMatch = screenSize;
        }
      }

      return bestMatch;
    }
  }

  private DeviceConfig() {}

  static void applyToConfiguration(
      String qualifierString,
      Configuration existingConfiguration,
      DisplayMetrics existingDisplayMetrics) {

    Configuration configFromQualifiers = new Configuration();
    DisplayMetrics metricsFromQualifiers = new DisplayMetrics();

    QualifierParser.parse(qualifierString, configFromQualifiers, metricsFromQualifiers);

    existingConfiguration.updateFrom(configFromQualifiers);
    updateDisplayMetricsFrom(existingDisplayMetrics, metricsFromQualifiers);

    applyUpdateRules(existingConfiguration, configFromQualifiers);
  }

  private static void applyUpdateRules(
      Configuration configuration, Configuration configFromQualifiers) {
    // if screen layout size has been requested, with no screen dimensions, clear out screen
    // dimensions so they can be recalculated
    if (getScreenLayoutSize(configFromQualifiers) != Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
      if (configFromQualifiers.screenWidthDp == 0) {
        configuration.screenWidthDp = 0;
      }
      if (configFromQualifiers.screenHeightDp == 0) {
        configuration.screenHeightDp = 0;
      }
    }

    // reset orientation based on width and height if its unspecified in this qualifier update
    if (configFromQualifiers.orientation == Configuration.ORIENTATION_UNDEFINED
        && configuration.orientation != Configuration.ORIENTATION_UNDEFINED
        && (configFromQualifiers.screenWidthDp != 0 || configFromQualifiers.screenHeightDp != 0)) {
      configuration.orientation =
          configuration.screenWidthDp > configuration.screenHeightDp
              ? Configuration.ORIENTATION_LANDSCAPE
              : Configuration.ORIENTATION_PORTRAIT;
    }
  }

  private static void updateDisplayMetricsFrom(
      DisplayMetrics existingDisplayMetrics, DisplayMetrics metricsFromQualifiers) {
    if (metricsFromQualifiers.widthPixels > 0) {
      existingDisplayMetrics.widthPixels = metricsFromQualifiers.widthPixels;
    }
    if (metricsFromQualifiers.heightPixels > 0) {
      existingDisplayMetrics.heightPixels = metricsFromQualifiers.heightPixels;
    }
    if (metricsFromQualifiers.densityDpi > 0) {
      existingDisplayMetrics.densityDpi = metricsFromQualifiers.densityDpi;
    }
    if (metricsFromQualifiers.density > 0) {
      existingDisplayMetrics.density = metricsFromQualifiers.density;
    }
    if (metricsFromQualifiers.xdpi > 0) {
      existingDisplayMetrics.xdpi = metricsFromQualifiers.xdpi;
    }
    if (metricsFromQualifiers.ydpi > 0) {
      existingDisplayMetrics.ydpi = metricsFromQualifiers.ydpi;
    }
  }

  private static void setBounds(
      int apiLevel, Configuration configuration, DisplayMetrics displayMetrics) {

    if (apiLevel >= VERSION_CODES.P) {
      Rect bounds = new Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
      WindowConfiguration windowConfiguration =
          ReflectionHelpers.getField(configuration, "windowConfiguration");
      windowConfiguration.setBounds(bounds);
      windowConfiguration.setAppBounds(bounds);
      if (apiLevel >= VERSION_CODES.S && useMaxBounds()) {
        windowConfiguration.setMaxBounds(bounds);
      }
    }
  }

  private static boolean useMaxBounds() {
    return Boolean.parseBoolean(
        System.getProperty("robolectric.deviceconfig.useMaxBounds", "true"));
  }

  /**
   * Calculates and sets the DisplayMetrics width|height Pixels according to the screen width/height
   * dp from Configuration.
   *
   * <p>TODO: Historically in Robolectric a display's dimension was specified in density-independent
   * (dp) units. This is not consistent with how virtual and physical devices are described, which
   * is typically in terms of pixels. A qualifier String can specify width|height pixels as well.
   * Take that into account rather than overwriting display metrics.
   */
  private static void setPixels(Configuration configuration, DisplayMetrics displayMetrics) {
    int widthPx = (int) (configuration.screenWidthDp * displayMetrics.density);
    int heightPx = (int) (configuration.screenHeightDp * displayMetrics.density);
    displayMetrics.widthPixels = displayMetrics.noncompatWidthPixels = widthPx;
    displayMetrics.heightPixels = displayMetrics.noncompatHeightPixels = heightPx;
  }

  /**
   * Makes a given configuration, which may have undefined values, conform to the rules declared <a
   * href="http://robolectric.org/device-configuration/">here</a>.
   */
  static void applyRules(Configuration configuration, DisplayMetrics displayMetrics, int apiLevel) {
    Locale locale = getLocale(configuration, apiLevel);

    String language = locale == null ? "" : locale.getLanguage();
    if (language.isEmpty()) {
      language = "en";

      String country = locale == null ? "" : locale.getCountry();
      if (country.isEmpty()) {
        country = "us";
      }

      locale = new Locale(language, country);
      configuration.setLocale(locale);
    }

    ScreenSize requestedScreenSize = getScreenSize(configuration);
    if (requestedScreenSize == null) {
      requestedScreenSize = DEFAULT_SCREEN_SIZE;
    }

    if (configuration.orientation == Configuration.ORIENTATION_UNDEFINED
        && configuration.screenWidthDp != 0
        && configuration.screenHeightDp != 0) {
      configuration.orientation =
          (configuration.screenWidthDp > configuration.screenHeightDp)
              ? Configuration.ORIENTATION_LANDSCAPE
              : Configuration.ORIENTATION_PORTRAIT;
    }

    if (configuration.screenWidthDp == 0) {
      configuration.screenWidthDp = requestedScreenSize.width;
    }

    if (configuration.screenHeightDp == 0) {
      configuration.screenHeightDp = requestedScreenSize.height;

      if ((configuration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK)
          == Configuration.SCREENLAYOUT_LONG_YES) {
        configuration.screenHeightDp = (int) (configuration.screenHeightDp * 1.25f);
      }
    }

    int lesserDimenDp = Math.min(configuration.screenWidthDp, configuration.screenHeightDp);
    int greaterDimenDp = Math.max(configuration.screenWidthDp, configuration.screenHeightDp);

    if (configuration.smallestScreenWidthDp == 0) {
      configuration.smallestScreenWidthDp = lesserDimenDp;
    }

    if (getScreenLayoutSize(configuration) == Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
      ScreenSize screenSize =
          ScreenSize.match(configuration.screenWidthDp, configuration.screenHeightDp);
      setScreenLayoutSize(configuration, screenSize.configValue);
    }

    if (getScreenLayoutLong(configuration) == Configuration.SCREENLAYOUT_LONG_UNDEFINED) {
      setScreenLayoutLong(
          configuration,
          ((float) greaterDimenDp) / lesserDimenDp >= 1.75
              ? Configuration.SCREENLAYOUT_LONG_YES
              : Configuration.SCREENLAYOUT_LONG_NO);
    }

    if (getScreenLayoutRound(configuration) == Configuration.SCREENLAYOUT_ROUND_UNDEFINED) {
      setScreenLayoutRound(configuration, Configuration.SCREENLAYOUT_ROUND_NO);
    }

    if (configuration.orientation == Configuration.ORIENTATION_UNDEFINED) {
      configuration.orientation =
          configuration.screenWidthDp > configuration.screenHeightDp
              ? Configuration.ORIENTATION_LANDSCAPE
              : Configuration.ORIENTATION_PORTRAIT;
    } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        && configuration.screenWidthDp > configuration.screenHeightDp) {
      swapXY(configuration);
    } else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        && configuration.screenWidthDp < configuration.screenHeightDp) {
      swapXY(configuration);
    }

    if (getUiModeType(configuration) == Configuration.UI_MODE_TYPE_UNDEFINED) {
      setUiModeType(configuration, Configuration.UI_MODE_TYPE_NORMAL);
    }

    if (getUiModeNight(configuration) == Configuration.UI_MODE_NIGHT_UNDEFINED) {
      setUiModeNight(configuration, Configuration.UI_MODE_NIGHT_NO);
    }

    switch (configuration.densityDpi) {
      case Configuration.DENSITY_DPI_ANY:
        throw new IllegalArgumentException("'anydpi' isn't actually a dpi");
      case Configuration.DENSITY_DPI_NONE:
        throw new IllegalArgumentException("'nodpi' isn't actually a dpi");
      case Configuration.DENSITY_DPI_UNDEFINED:
        // DisplayMetrics.DENSITY_DEFAULT is mdpi
        configuration.densityDpi = DEFAULT_DENSITY;
        QualifierParser.setDensity(DEFAULT_DENSITY, displayMetrics);
    }
    setPixels(configuration, displayMetrics);
    setBounds(apiLevel, configuration, displayMetrics);

    if (configuration.touchscreen == Configuration.TOUCHSCREEN_UNDEFINED) {
      configuration.touchscreen = Configuration.TOUCHSCREEN_FINGER;
    }

    if (configuration.keyboardHidden == Configuration.KEYBOARDHIDDEN_UNDEFINED) {
      configuration.keyboardHidden = Configuration.KEYBOARDHIDDEN_SOFT;
    }

    if (configuration.keyboard == Configuration.KEYBOARD_UNDEFINED) {
      configuration.keyboard = Configuration.KEYBOARD_NOKEYS;
    }

    if (configuration.navigationHidden == Configuration.NAVIGATIONHIDDEN_UNDEFINED) {
      configuration.navigationHidden = Configuration.NAVIGATIONHIDDEN_YES;
    }

    if (configuration.navigation == Configuration.NAVIGATION_UNDEFINED) {
      configuration.navigation = Configuration.NAVIGATION_NONAV;
    }

    if (apiLevel >= VERSION_CODES.O) {
      if (getColorModeGamut(configuration) == Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_UNDEFINED) {
        setColorModeGamut(configuration, Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_NO);
      }

      if (getColorModeHdr(configuration) == Configuration.COLOR_MODE_HDR_UNDEFINED) {
        setColorModeHdr(configuration, Configuration.COLOR_MODE_HDR_NO);
      }
    }
  }

  public static ScreenSize getScreenSize(Configuration configuration) {
    return ScreenSize.find(getScreenLayoutSize(configuration));
  }

  private static void swapXY(Configuration configuration) {
    int oldWidth = configuration.screenWidthDp;
    //noinspection SuspiciousNameCombination
    configuration.screenWidthDp = configuration.screenHeightDp;
    //noinspection SuspiciousNameCombination
    configuration.screenHeightDp = oldWidth;
  }

  private static Locale getLocale(Configuration configuration, int apiLevel) {
    Locale locale;
    if (apiLevel > Build.VERSION_CODES.M) {
      locale = configuration.getLocales().get(0);
    } else {
      locale = configuration.locale;
    }
    return locale;
  }

  private static int getScreenLayoutSize(Configuration configuration) {
    return configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
  }

  private static void setScreenLayoutSize(Configuration configuration, int value) {
    configuration.screenLayout =
        (configuration.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK) | value;
  }

  private static int getScreenLayoutLong(Configuration configuration) {
    return configuration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;
  }

  private static void setScreenLayoutLong(Configuration configuration, int value) {
    configuration.screenLayout =
        (configuration.screenLayout & ~Configuration.SCREENLAYOUT_LONG_MASK) | value;
  }

  private static int getScreenLayoutRound(Configuration configuration) {
    return configuration.screenLayout & Configuration.SCREENLAYOUT_ROUND_MASK;
  }

  private static void setScreenLayoutRound(Configuration configuration, int value) {
    configuration.screenLayout =
        (configuration.screenLayout & ~Configuration.SCREENLAYOUT_ROUND_MASK) | value;
  }

  private static int getUiModeType(Configuration configuration) {
    return configuration.uiMode & Configuration.UI_MODE_TYPE_MASK;
  }

  private static void setUiModeType(Configuration configuration, int value) {
    configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | value;
  }

  static int getUiModeNight(Configuration configuration) {
    return configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
  }

  private static void setUiModeNight(Configuration configuration, int value) {
    configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | value;
  }

  private static int getColorModeGamut(Configuration configuration) {
    return configuration.colorMode & Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK;
  }

  private static void setColorModeGamut(Configuration configuration, int value) {
    configuration.colorMode =
        (configuration.colorMode & ~Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK) | value;
  }

  private static int getColorModeHdr(Configuration configuration) {
    return configuration.colorMode & Configuration.COLOR_MODE_HDR_MASK;
  }

  private static void setColorModeHdr(Configuration configuration, int value) {
    configuration.colorMode =
        (configuration.colorMode & ~Configuration.COLOR_MODE_HDR_MASK) | value;
  }
}
