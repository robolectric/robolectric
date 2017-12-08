package org.robolectric.android;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import com.google.common.annotations.VisibleForTesting;
import org.robolectric.res.Qualifiers;
import org.robolectric.shadows.ShadowDisplayManager;
import org.robolectric.shadows.ShadowWindowManagerImpl;

public class Bootstrap {

  public static void setUpDisplay(Configuration configuration, DisplayMetrics displayMetrics) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      int widthPx = (int) (configuration.screenWidthDp * displayMetrics.density);
      int heightPx = (int) (configuration.screenHeightDp * displayMetrics.density);
      displayMetrics.widthPixels = displayMetrics.noncompatWidthPixels = widthPx;
      displayMetrics.heightPixels = displayMetrics.noncompatHeightPixels = heightPx;
      displayMetrics.xdpi = displayMetrics.noncompatXdpi = displayMetrics.densityDpi;
      displayMetrics.ydpi = displayMetrics.noncompatYdpi = displayMetrics.densityDpi;
    }

    if (Build.VERSION.SDK_INT == VERSION_CODES.JELLY_BEAN) {
      ShadowWindowManagerImpl.configureDefaultDisplayForJBOnly(configuration, displayMetrics);
    } else {
      DisplayInfo displayInfo = createDisplayInfo(configuration, displayMetrics);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        displayInfo.getAppMetrics(displayMetrics);
      }
      ShadowDisplayManager.addDisplay(displayInfo);
    }
  }

  private static DisplayInfo createDisplayInfo(Configuration configuration, DisplayMetrics displayMetrics) {
    int widthPx = (int) (configuration.screenWidthDp * displayMetrics.density);
    int heightPx = (int) (configuration.screenHeightDp * displayMetrics.density);

    DisplayInfo displayInfo = new DisplayInfo();
    displayInfo.name = "Built-in screen";
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      displayInfo.uniqueId = "screen0";
    }
    displayInfo.appWidth = widthPx;
    displayInfo.appHeight = heightPx;
    displayInfo.smallestNominalAppWidth = Math.min(widthPx, heightPx);
    displayInfo.smallestNominalAppHeight = Math.min(widthPx, heightPx);
    displayInfo.largestNominalAppWidth = Math.max(widthPx, heightPx);
    displayInfo.largestNominalAppHeight = Math.max(widthPx, heightPx);
    displayInfo.logicalWidth = widthPx;
    displayInfo.logicalHeight = heightPx;
    displayInfo.rotation = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ? Surface.ROTATION_0
        : Surface.ROTATION_90;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      displayInfo.modeId = 0;
      displayInfo.defaultModeId = 0;
      displayInfo.supportedModes = new Display.Mode[] {
          new Display.Mode(0, widthPx, heightPx, 60)
      };
    }
    displayInfo.logicalDensityDpi = displayMetrics.densityDpi;
    displayInfo.physicalXDpi = displayMetrics.densityDpi;
    displayInfo.physicalYDpi = displayMetrics.densityDpi;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      displayInfo.state = Display.STATE_ON;
    }
    return displayInfo;
  }

  @VisibleForTesting
  public static void applyQualifiers(String qualifiersStrs, int apiLevel,
      Configuration configuration, DisplayMetrics displayMetrics) {

    String[] qualifiersParts = qualifiersStrs.split(" ");
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
  }

}
