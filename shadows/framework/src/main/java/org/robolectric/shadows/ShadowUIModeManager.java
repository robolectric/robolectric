package org.robolectric.shadows;

import android.app.UiModeManager;
import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import android.util.ArraySet;
import com.google.common.collect.ImmutableSet;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** */
@Implements(UiModeManager.class)
public class ShadowUIModeManager {
  @RealObject UiModeManager realUiModeManager;
  public int currentModeType = Configuration.UI_MODE_TYPE_UNDEFINED;
  public int currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
  public int lastFlags;
  public int lastCarModePriority;
  public final ArraySet<Integer> activeProjectionTypes = new ArraySet<>();

  private static final ImmutableSet<Integer> VALID_NIGHT_MODES =
      ImmutableSet.of(
          UiModeManager.MODE_NIGHT_AUTO, UiModeManager.MODE_NIGHT_NO, UiModeManager.MODE_NIGHT_YES);

  private static final int DEFAULT_PRIORITY = 0;

  @Implementation
  protected int getCurrentModeType() {
    return currentModeType;
  }

  @Implementation(maxSdk = VERSION_CODES.Q)
  protected void enableCarMode(int flags) {
    enableCarMode(DEFAULT_PRIORITY, flags);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected void enableCarMode(int priority, int flags) {
    currentModeType = Configuration.UI_MODE_TYPE_CAR;
    lastCarModePriority = priority;
    lastFlags = flags;
  }

  @Implementation
  protected void disableCarMode(int flags) {
    currentModeType = Configuration.UI_MODE_TYPE_NORMAL;
    lastFlags = flags;
  }

  @Implementation
  protected int getNightMode() {
    return currentNightMode;
  }

  @Implementation
  protected void setNightMode(int mode) {
    if (VALID_NIGHT_MODES.contains(mode)) {
      currentNightMode = mode;
    } else {
      currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
    }
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected boolean requestProjection(int projectionType) {
    boolean success =
        Shadow.directlyOn(
            realUiModeManager,
            UiModeManager.class,
            "requestProjection",
            ReflectionHelpers.ClassParameter.from(int.class, projectionType));
    if (success) {
      activeProjectionTypes.add(projectionType);
    }
    return success;
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected boolean releaseProjection(int projectionType) {
    boolean success =
        Shadow.directlyOn(
            realUiModeManager,
            UiModeManager.class,
            "releaseProjection",
            ReflectionHelpers.ClassParameter.from(int.class, projectionType));
    if (success) {
      activeProjectionTypes.remove(projectionType);
    }
    return success;
  }
}
