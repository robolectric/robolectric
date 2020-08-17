package org.robolectric.shadows;

import android.app.UiModeManager;
import android.content.res.Configuration;
import com.google.common.collect.ImmutableSet;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** */
@Implements(UiModeManager.class)
public class ShadowUIModeManager {
  public int currentModeType = Configuration.UI_MODE_TYPE_UNDEFINED;
  public int currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
  public int lastFlags;

  private static final ImmutableSet<Integer> VALID_NIGHT_MODES =
      ImmutableSet.of(
          UiModeManager.MODE_NIGHT_AUTO, UiModeManager.MODE_NIGHT_NO, UiModeManager.MODE_NIGHT_YES);

  @Implementation
  public int getCurrentModeType() {
    return currentModeType;
  }

  @Implementation
  public void enableCarMode(int flags) {
    currentModeType = Configuration.UI_MODE_TYPE_CAR;
    lastFlags = flags;
  }

  @Implementation
  public void disableCarMode(int flags) {
    currentModeType = Configuration.UI_MODE_TYPE_NORMAL;
    lastFlags = flags;
  }

  @Implementation
  public int getNightMode() {
    return currentNightMode;
  }

  @Implementation
  public void setNightMode(int mode) {
    if (VALID_NIGHT_MODES.contains(mode)) {
      currentNightMode = mode;
    } else {
      currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
    }
  }
}
