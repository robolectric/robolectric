package org.robolectric.shadows;

import android.app.UiModeManager;
import android.content.res.Configuration;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** */
@Implements(UiModeManager.class)
public class ShadowUIModeManager {
  public int currentModeType = Configuration.UI_MODE_TYPE_UNDEFINED;

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
}
