package org.robolectric.android;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import org.robolectric.res.Qualifiers;
import org.robolectric.shadows.ShadowDateUtils;
import org.robolectric.shadows.ShadowDisplayManager;
import org.robolectric.shadows.ShadowDisplayManagerGlobal;

public class Bootstrap {

  private static Configuration configuration = new Configuration();
  private static DisplayMetrics displayMetrics = new DisplayMetrics();
  private static Resources displayResources;

  /** internal only */
  public static boolean displaySet = false;

  public static Configuration getConfiguration() {
    if (displayResources != null) {
      return displayResources.getConfiguration();
    }
    return Bootstrap.configuration;
  }

  public static DisplayMetrics getDisplayMetrics() {
    if (displayResources != null) {
      return displayResources.getDisplayMetrics();
    }
    return Bootstrap.displayMetrics;
  }

  /** internal only */
  public static void setDisplayConfiguration(
      Configuration configuration, DisplayMetrics displayMetrics) {
    Bootstrap.configuration = configuration;
    Bootstrap.displayMetrics = displayMetrics;
  }

  /** internal only */
  public static void resetDisplayConfiguration() {
    // This is called to avoid the configureDefaultDisplay should only be called once exception that
    // occurs if ShadowDisplayManagerGlobal is not properly reset during resetter.
    ShadowDisplayManagerGlobal.reset();

    configuration = new Configuration();
    displayMetrics = new DisplayMetrics();
    displayResources = null;
    displaySet = false;
  }

  /** internal only */
  public static void updateDisplayResources(
      Configuration configuration, DisplayMetrics displayMetrics) {
    if (displayResources == null) {
      displayResources =
          new Resources(
              AssetManager.getSystem(), Bootstrap.displayMetrics, Bootstrap.configuration);
    }
    displayResources.updateConfiguration(configuration, displayMetrics);
  }

  /** internal only */
  public static void setUpDisplay() {
    if (!displaySet) {
      displaySet = true;
      ShadowDisplayManager.configureDefaultDisplay(configuration, displayMetrics);
    }
  }

  public static void applyQualifiers(
      String qualifiersStrs,
      int apiLevel,
      Configuration configuration,
      DisplayMetrics displayMetrics) {

    String[] qualifiersParts = qualifiersStrs.split(" ", 0);
    int i = qualifiersParts.length - 1;
    // find the index of the left-most qualifier string that doesn't start with '+'
    for (; i >= 0; i--) {
      String qualifiersStr = qualifiersParts[i];
      if (qualifiersStr.startsWith("+")) {
        qualifiersParts[i] = qualifiersStr.substring(1);
      } else {
        break;
      }
    }

    for (i = (i < 0) ? 0 : i; i < qualifiersParts.length; i++) {
      String qualifiersStr = qualifiersParts[i];
      int platformVersion = Qualifiers.getPlatformVersion(qualifiersStr);
      if (platformVersion != -1 && platformVersion != apiLevel) {
        throw new IllegalArgumentException(
            "Cannot specify conflicting platform version in qualifiers: \"" + qualifiersStr + "\"");
      }

      Qualifiers qualifiers = Qualifiers.parse(qualifiersStr);

      DeviceConfig.applyToConfiguration(qualifiers, apiLevel, configuration, displayMetrics);
    }

    DeviceConfig.applyRules(configuration, displayMetrics, apiLevel);

    // DateUtils has a static cache of the last Configuration, so it may need to be reset.
    ShadowDateUtils.resetLastConfig();
  }
}
