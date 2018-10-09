package org.robolectric.android;

import static com.google.common.base.Strings.isNullOrEmpty;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import java.util.Locale;
import org.robolectric.res.Qualifiers;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.ResTable_config;

/**
 * Supports device configuration for Robolectric tests.
 *
 * @see [Device Configuration](http://robolectric.org/device-configuration/)
 */
@SuppressWarnings("NewApi")
public class DeviceConfig {
  public static final int DEFAULT_DENSITY = ResTable_config.DENSITY_DPI_MDPI;
  public static final ScreenSize DEFAULT_SCREEN_SIZE = ScreenSize.normal;

  /**
   * Standard sizes for the
   * [screen size qualifier](https://developer.android.com/guide/topics/resources/providing-resources.html#ScreenSizeQualifier).
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

  private DeviceConfig() {
  }

  static void applyToConfiguration(Qualifiers qualifiers, int apiLevel,
      Configuration configuration, DisplayMetrics displayMetrics) {
    ResTable_config resTab = qualifiers.getConfig();

    if (resTab.mcc != 0) {
      configuration.mcc = resTab.mcc;
    }

    if (resTab.mnc != 0) {
      configuration.mnc = resTab.mnc;
    }

    // screenLayout includes size, long, layoutdir, and round.
    // layoutdir may be overridden by setLocale(), so do this first:
    int screenLayoutSize = getScreenLayoutSize(configuration);
    int resTabSize = resTab.screenLayoutSize();
    if (resTabSize != ResTable_config.SCREENSIZE_ANY) {
      screenLayoutSize = resTabSize;

      if (resTab.screenWidthDp == 0) {
        configuration.screenWidthDp = 0;
      }

      if (resTab.screenHeightDp == 0) {
        configuration.screenHeightDp = 0;
      }
    }

    int screenLayoutLong = getScreenLayoutLong(configuration);
    int resTabLong = resTab.screenLayoutLong();
    if (resTabLong != ResTable_config.SCREENLONG_ANY) {
      screenLayoutLong = resTabLong;
    }

    int screenLayoutLayoutDir = getScreenLayoutLayoutDir(configuration);
    int resTabLayoutDir = resTab.screenLayoutDirection();
    if (resTabLayoutDir != ResTable_config.LAYOUTDIR_ANY) {
      screenLayoutLayoutDir = resTabLayoutDir;
    }

    int screenLayoutRound = getScreenLayoutRound(configuration);
    int resTabRound = resTab.screenLayoutRound();
    if (resTabRound != ResTable_config.SCREENROUND_ANY) {
      screenLayoutRound = resTabRound << 8;
    }

    configuration.screenLayout =
        screenLayoutSize | screenLayoutLong | screenLayoutLayoutDir | screenLayoutRound;

    // locale...
    String lang = resTab.languageString();
    String region = resTab.regionString();
    String script = resTab.scriptString();

    Locale locale;
    if (isNullOrEmpty(lang) && isNullOrEmpty(region) && isNullOrEmpty(script)) {
      locale = null;
    } else {
      locale = new Locale.Builder()
          .setLanguage(lang)
          .setRegion(region)
          .setScript(script == null ? "" : script)
          .build();
    }
    if (locale != null) {
      setLocale(apiLevel, configuration, locale);
    }

    if (resTab.smallestScreenWidthDp != 0) {
      configuration.smallestScreenWidthDp = resTab.smallestScreenWidthDp;
    }

    if (resTab.screenWidthDp != 0) {
      configuration.screenWidthDp = resTab.screenWidthDp;
    }

    if (resTab.screenHeightDp != 0) {
      configuration.screenHeightDp = resTab.screenHeightDp;
    }

    if (resTab.orientation != ResTable_config.ORIENTATION_ANY) {
      configuration.orientation = resTab.orientation;
    }

    // uiMode includes type and night...
    int uiModeType = getUiModeType(configuration);
    int resTabType = resTab.uiModeType();
    if (resTabType != ResTable_config.UI_MODE_TYPE_ANY) {
      uiModeType = resTabType;
    }

    int uiModeNight = getUiModeNight(configuration);
    int resTabNight = resTab.uiModeNight();
    if (resTabNight != ResTable_config.UI_MODE_NIGHT_ANY) {
      uiModeNight = resTabNight;
    }
    configuration.uiMode = uiModeType | uiModeNight;

    if (resTab.density != ResTable_config.DENSITY_DEFAULT) {
      setDensity(resTab.density, apiLevel, configuration, displayMetrics);
    }

    if (resTab.touchscreen != ResTable_config.TOUCHSCREEN_ANY) {
      configuration.touchscreen = resTab.touchscreen;
    }

    if (resTab.keyboard != ResTable_config.KEYBOARD_ANY) {
      configuration.keyboard = resTab.keyboard;
    }

    if (resTab.keyboardHidden() != ResTable_config.KEYSHIDDEN_ANY) {
      configuration.keyboardHidden = resTab.keyboardHidden();
    }

    if (resTab.navigation != ResTable_config.NAVIGATION_ANY) {
      configuration.navigation = resTab.navigation;
    }

    if (resTab.navigationHidden() != ResTable_config.NAVHIDDEN_ANY) {
      configuration.navigationHidden = resTab.navigationHidden();
    }

    if (apiLevel >= VERSION_CODES.O) {
      if (resTab.colorModeWideColorGamut() != ResTable_config.WIDE_COLOR_GAMUT_ANY) {
        setColorModeGamut(configuration, resTab.colorMode & ResTable_config.MASK_WIDE_COLOR_GAMUT);
      }

      if (resTab.colorModeHdr() != ResTable_config.HDR_ANY) {
        setColorModeHdr(configuration, resTab.colorMode & ResTable_config.MASK_HDR);
      }
    }
  }

  private static void setDensity(int densityDpi, int apiLevel, Configuration configuration,
      DisplayMetrics displayMetrics) {
    if (apiLevel >= VERSION_CODES.JELLY_BEAN_MR1) {
      configuration.densityDpi = densityDpi;
    }
    displayMetrics.densityDpi = densityDpi;
    displayMetrics.density = displayMetrics.densityDpi * DisplayMetrics.DENSITY_DEFAULT_SCALE;
  }

  /**
   * Makes a given configuration, which may have undefined values, conform to the rules declared
   * [here](http://robolectric.org/device-configuration/).
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
      setLocale(apiLevel, configuration, locale);
    }

    if (apiLevel <= ConfigDescription.SDK_JELLY_BEAN &&
        getScreenLayoutLayoutDir(configuration) == Configuration.SCREENLAYOUT_LAYOUTDIR_UNDEFINED) {
      setScreenLayoutLayoutDir(configuration, Configuration.SCREENLAYOUT_LAYOUTDIR_LTR);
    }

    ScreenSize requestedScreenSize = getScreenSize(configuration);
    if (requestedScreenSize == null) {
      requestedScreenSize = DEFAULT_SCREEN_SIZE;
    }

    if (configuration.orientation == Configuration.ORIENTATION_UNDEFINED
        && configuration.screenWidthDp != 0 && configuration.screenHeightDp != 0) {
      configuration.orientation = (configuration.screenWidthDp > configuration.screenHeightDp)
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

    int lesserDimenPx = Math.min(configuration.screenWidthDp, configuration.screenHeightDp);
    int greaterDimenPx = Math.max(configuration.screenWidthDp, configuration.screenHeightDp);

    if (configuration.smallestScreenWidthDp == 0) {
      configuration.smallestScreenWidthDp = lesserDimenPx;
    }

    if (getScreenLayoutSize(configuration) == Configuration.SCREENLAYOUT_SIZE_UNDEFINED) {
      ScreenSize screenSize =
          ScreenSize.match(configuration.screenWidthDp, configuration.screenHeightDp);
      setScreenLayoutSize(configuration, screenSize.configValue);
    }

    if (getScreenLayoutLong(configuration) == Configuration.SCREENLAYOUT_LONG_UNDEFINED) {
      setScreenLayoutLong(configuration,
          ((float) greaterDimenPx) / lesserDimenPx >= 1.75
              ? Configuration.SCREENLAYOUT_LONG_YES
              : Configuration.SCREENLAYOUT_LONG_NO);
    }

    if (getScreenLayoutRound(configuration) == Configuration.SCREENLAYOUT_ROUND_UNDEFINED) {
      setScreenLayoutRound(configuration, Configuration.SCREENLAYOUT_ROUND_NO);
    }

    if (configuration.orientation == Configuration.ORIENTATION_UNDEFINED) {
      configuration.orientation = configuration.screenWidthDp > configuration.screenHeightDp
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

    switch (displayMetrics.densityDpi) {
      case ResTable_config.DENSITY_DPI_ANY:
        throw new IllegalArgumentException("'anydpi' isn't actually a dpi");
      case ResTable_config.DENSITY_DPI_NONE:
        throw new IllegalArgumentException("'nodpi' isn't actually a dpi");
      case ResTable_config.DENSITY_DPI_UNDEFINED:
        // DisplayMetrics.DENSITY_DEFAULT is mdpi
        setDensity(DEFAULT_DENSITY, apiLevel, configuration, displayMetrics);
    }

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

  private static void setLocale(int apiLevel, Configuration configuration, Locale locale) {
    if (apiLevel >= VERSION_CODES.JELLY_BEAN_MR1) {
      configuration.setLocale(locale);
    } else {
      configuration.locale = locale;
    }
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
        (configuration.screenLayout & ~Configuration.SCREENLAYOUT_SIZE_MASK)
            | value;
  }

  private static int getScreenLayoutLong(Configuration configuration) {
    return configuration.screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;
  }

  private static void setScreenLayoutLong(Configuration configuration, int value) {
    configuration.screenLayout =
        (configuration.screenLayout & ~Configuration.SCREENLAYOUT_LONG_MASK)
            | value;
  }

  private static int getScreenLayoutLayoutDir(Configuration configuration) {
    return configuration.screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;
  }

  private static void setScreenLayoutLayoutDir(Configuration configuration, int value) {
    configuration.screenLayout =
        (configuration.screenLayout & ~Configuration.SCREENLAYOUT_LAYOUTDIR_MASK)
            | value;
  }

  private static int getScreenLayoutRound(Configuration configuration) {
    return configuration.screenLayout & Configuration.SCREENLAYOUT_ROUND_MASK;
  }

  private static void setScreenLayoutRound(Configuration configuration, int value) {
    configuration.screenLayout =
        (configuration.screenLayout & ~Configuration.SCREENLAYOUT_ROUND_MASK)
            | value;
  }

  private static int getUiModeType(Configuration configuration) {
    return configuration.uiMode & Configuration.UI_MODE_TYPE_MASK;
  }

  private static void setUiModeType(Configuration configuration, int value) {
    configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_TYPE_MASK) | value;
  }

  private static int getUiModeNight(Configuration configuration) {
    return configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
  }

  private static void setUiModeNight(Configuration configuration, int value) {
    configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | value;
  }

  private static int getColorModeGamut(Configuration configuration) {
    return configuration.colorMode & Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK;
  }

  private static void setColorModeGamut(Configuration configuration, int value) {
    configuration.colorMode = (configuration.colorMode & ~Configuration.COLOR_MODE_WIDE_COLOR_GAMUT_MASK) | value;
  }

  private static int getColorModeHdr(Configuration configuration) {
    return configuration.colorMode & Configuration.COLOR_MODE_HDR_MASK;
  }

  private static void setColorModeHdr(Configuration configuration, int value) {
    configuration.colorMode = (configuration.colorMode & ~Configuration.COLOR_MODE_HDR_MASK) | value;
  }
}
