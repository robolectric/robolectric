package org.robolectric.shadows;

import android.app.UiModeManager;
import android.content.res.Configuration;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** */
@Implements(UiModeManager.class)
public class ShadowUIModeManager {
  public int currentModeType = Configuration.UI_MODE_TYPE_UNDEFINED;
  public int currentNightMode = UiModeManager.MODE_NIGHT_AUTO;

  private static Set<Integer> validNightModes = new HashSet<>();

  static {
    validNightModes.add(UiModeManager.MODE_NIGHT_AUTO);
    validNightModes.add(UiModeManager.MODE_NIGHT_NO);
    validNightModes.add(UiModeManager.MODE_NIGHT_YES);
  }

  @Implementation
  public int getCurrentModeType() {
    return currentModeType;
  }

  @Implementation
  public void enableCarMode(int flags) {
    currentModeType = Configuration.UI_MODE_TYPE_CAR;
  }

  @Implementation
  public void disableCarMode(int flags) {
    currentModeType = Configuration.UI_MODE_TYPE_NORMAL;
  }

  @Implementation
  public int getNightMode() {
    return currentNightMode;
  }

  @Implementation
  public void setNightMode(int mode) {
    if (!validNightModes.contains(mode)) {
      currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
    } else {
      currentNightMode = mode;
    }
  }
}
