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
  public static String applyQualifiers(String qualifiers, int apiLevel,
      Configuration configuration,
      DisplayMetrics displayMetrics) {
    ConfigDescription configDescription = new ConfigDescription();
    ResTable_config resTab = new ResTable_config();

    if (Qualifiers.getPlatformVersion(qualifiers) != -1) {
      throw new IllegalArgumentException("Cannot specify platform version in qualifiers: \"" + qualifiers + "\"");
    }

    if (!qualifiers.isEmpty() && !configDescription.parse(qualifiers, resTab)) {
      throw new IllegalArgumentException("Invalid qualifiers \"" + qualifiers + "\"");
    }

    if (resTab.smallestScreenWidthDp == 0) {
      resTab.smallestScreenWidthDp = 320;
    }

    if (resTab.screenWidthDp == 0) {
      resTab.screenWidthDp = 320;
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
    } else {
      if (resTab.density != Configuration.DENSITY_DPI_UNDEFINED) {
        displayMetrics.densityDpi = resTab.density;
        displayMetrics.density =
            displayMetrics.densityDpi * DisplayMetrics.DENSITY_DEFAULT_SCALE;
      }
    }

    //configuration.
    // end new stuff

    Locale locale = null;
    if (!isNullOrEmpty(resTab.languageString()) || !isNullOrEmpty(resTab.regionString())) {
      locale = new Locale(resTab.languageString(), resTab.regionString());
    } else if (!isNullOrEmpty(resTab.languageString())) {
      locale = new Locale(resTab.languageString());
    }
    if (locale != null) {
      if (apiLevel >= VERSION_CODES.JELLY_BEAN_MR1) {
        configuration.setLocale(locale);
      } else {
        configuration.locale = locale;
      }
    }

    return ConfigurationV25.resourceQualifierString(configuration);
  }
}
