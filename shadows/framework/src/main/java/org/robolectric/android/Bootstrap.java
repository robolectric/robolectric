package org.robolectric.android;

import static com.google.common.base.Strings.isNullOrEmpty;

import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import org.robolectric.res.Qualifiers;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.ResTable_config;

public class Bootstrap {

  @VisibleForTesting
  public static void applyQualifiers(String qualifiers, int apiLevel,
      Configuration configuration, DisplayMetrics displayMetrics) {
    ConfigDescription configDescription = new ConfigDescription();
    ResTable_config resTab = new ResTable_config();

    int platformVersion = Qualifiers.getPlatformVersion(qualifiers);
    if (platformVersion != -1 && platformVersion != apiLevel) {
      throw new IllegalArgumentException(
          "Cannot specify conflicting platform version in qualifiers: \"" + qualifiers + "\"");
    }

    if (!qualifiers.isEmpty() && !configDescription.parse(qualifiers, resTab)) {
      throw new IllegalArgumentException("Invalid qualifiers \"" + qualifiers + "\"");
    }

    if (resTab.languageString().isEmpty()) {
      resTab.packLanguage("en");

      if (resTab.regionString().isEmpty()) {
        resTab.packRegion("us");
      }
    }

    if (apiLevel <= VERSION_CODES.JELLY_BEAN
        && resTab.screenLayoutDirection() == ResTable_config.LAYOUTDIR_ANY) {
      resTab.screenLayoutDirection(ResTable_config.LAYOUTDIR_LTR);
    }

    if (resTab.smallestScreenWidthDp == 0) {
      resTab.smallestScreenWidthDp = 320;
    }

    if (resTab.screenWidthDp == 0) {
      resTab.screenWidthDp = 320;
    }

    if (resTab.screenLayoutSize() == ResTable_config.SCREENSIZE_ANY) {
      resTab.screenLayoutSize(ResTable_config.SCREENSIZE_NORMAL);
    }

    if (resTab.screenLayoutLong() == ResTable_config.SCREENLONG_ANY) {
      resTab.screenLayoutLong(ResTable_config.SCREENLONG_NO);
    }

    if (resTab.screenLayoutRound() == ResTable_config.SCREENROUND_ANY) {
      resTab.screenLayoutRound(ResTable_config.SCREENROUND_NO);
    }

    if (resTab.orientation == ResTable_config.ORIENTATION_ANY) {
      resTab.orientation = ResTable_config.ORIENTATION_PORT;
    }

    if (resTab.uiModeNight() == ResTable_config.UI_MODE_NIGHT_ANY) {
      resTab.uiModeNight(ResTable_config.UI_MODE_NIGHT_NO);
    }

    switch (resTab.density) {
      case Configuration.DENSITY_DPI_ANY:
        throw new IllegalArgumentException("'anydpi' isn't actually a dpi");
      case Configuration.DENSITY_DPI_NONE:
        throw new IllegalArgumentException("'nodpi' isn't actually a dpi");
      case Configuration.DENSITY_DPI_UNDEFINED:
        resTab.density = DisplayMetrics.DENSITY_DEFAULT;
    }

    if (resTab.touchscreen == ResTable_config.TOUCHSCREEN_ANY) {
      resTab.touchscreen = ResTable_config.TOUCHSCREEN_FINGER;
    }

    configuration.smallestScreenWidthDp = resTab.smallestScreenWidthDp;
    configuration.screenWidthDp = resTab.screenWidthDp;
    configuration.orientation = resTab.orientation;

    // begin new stuff
    configuration.mcc = resTab.mcc;
    configuration.mnc = resTab.mnc;
    configuration.screenLayout = resTab.screenLayout | (resTab.screenLayout2 << 8);
    configuration.touchscreen = resTab.touchscreen;
    configuration.keyboard = resTab.keyboard;
    configuration.keyboardHidden = resTab.keyboardHidden();
    configuration.navigation = resTab.navigation;
    configuration.navigationHidden = resTab.navigationHidden();
    configuration.orientation = resTab.orientation;
    configuration.uiMode = resTab.uiMode;
    configuration.screenHeightDp = resTab.screenHeightDp;
    if (apiLevel >= VERSION_CODES.JELLY_BEAN_MR1) {
      configuration.densityDpi = resTab.density;
    }
    displayMetrics.densityDpi = resTab.density;
    displayMetrics.density = displayMetrics.densityDpi * DisplayMetrics.DENSITY_DEFAULT_SCALE;

    Locale locale;
    String lang = resTab.languageString();
    String region = resTab.regionString();
    String script = resTab.scriptString();
    if (isNullOrEmpty(lang) && isNullOrEmpty(region) && isNullOrEmpty(script)) {
      locale = null;
    } else {
      locale = new Locale.Builder()
          .setLanguage(lang == null ? "" : lang)
          .setRegion(region == null ? "" : region)
          .setScript(script == null ? "" : script)
          .build();
    }
    if (locale != null) {
      if (apiLevel >= VERSION_CODES.JELLY_BEAN_MR1) {
        configuration.setLocale(locale);
      } else {
        configuration.locale = locale;
      }
    }
  }
}
