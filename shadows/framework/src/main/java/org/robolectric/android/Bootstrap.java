package org.robolectric.android;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.Qualifiers;
import org.robolectric.shadows.ShadowDisplayManager;
import org.robolectric.shadows.ShadowWindowManagerImpl;

public class Bootstrap {

  public static void setUpDisplay(Configuration configuration, DisplayMetrics displayMetrics) {
    if (Build.VERSION.SDK_INT == VERSION_CODES.JELLY_BEAN) {
      ShadowWindowManagerImpl.configureDefaultDisplayForJBOnly(configuration, displayMetrics);
    } else {
      ShadowDisplayManager.configureDefaultDisplay(configuration, displayMetrics);
    }
  }

  public static void applyQualifiers(String qualifiersStrs, int apiLevel,
      Configuration configuration, DisplayMetrics displayMetrics) {

    String[] qualifiersParts = qualifiersStrs.split(" ", 0);
    int i = qualifiersParts.length - 1;
    // find the index of the left-most qualifier string that doesn't start with '+'
    for (; i >= 0 ; i--) {
      String qualifiersStr = qualifiersParts[i];
      if (qualifiersStr.startsWith("+")) {
        qualifiersParts[i] = qualifiersStr.substring(1);
      } else {
        break;
      }
    }

    for (i = (i < 0) ? 0 : i; i < qualifiersParts.length ; i++) {
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

    fixJellyBean(configuration, displayMetrics);
  }

  private static void fixJellyBean(Configuration configuration, DisplayMetrics displayMetrics) {
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.KITKAT) {
      int widthPx = (int) (configuration.screenWidthDp * displayMetrics.density);
      int heightPx = (int) (configuration.screenHeightDp * displayMetrics.density);
      displayMetrics.widthPixels = displayMetrics.noncompatWidthPixels = widthPx;
      displayMetrics.heightPixels = displayMetrics.noncompatHeightPixels = heightPx;
      displayMetrics.xdpi = displayMetrics.noncompatXdpi = displayMetrics.densityDpi;
      displayMetrics.ydpi = displayMetrics.noncompatYdpi = displayMetrics.densityDpi;
    }
  }

}
