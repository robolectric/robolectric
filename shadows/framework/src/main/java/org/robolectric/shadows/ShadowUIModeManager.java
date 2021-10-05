package org.robolectric.shadows;

import android.annotation.SystemApi;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** */
@Implements(UiModeManager.class)
public class ShadowUIModeManager {
  public int currentModeType = Configuration.UI_MODE_TYPE_UNDEFINED;
  public int currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
  public int lastFlags;
  public int lastCarModePriority;
  private int currentApplicationNightMode = 0;
  private final Set<Integer> activeProjectionTypes = new HashSet<>();
  private boolean failOnProjectionToggle;

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

  public int getApplicationNightMode() {
    return currentApplicationNightMode;
  }

  public Set<Integer> getActiveProjectionTypes() {
    return new HashSet<>(activeProjectionTypes);
  }

  public void setFailOnProjectionToggle(boolean failOnProjectionToggle) {
    this.failOnProjectionToggle = failOnProjectionToggle;
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @HiddenApi
  protected void setApplicationNightMode(int mode) {
    currentApplicationNightMode = mode;
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @SystemApi
  protected boolean requestProjection(int projectionType) {
    if (projectionType == UiModeManager.PROJECTION_TYPE_AUTOMOTIVE) {
      assertHasPermission(android.Manifest.permission.TOGGLE_AUTOMOTIVE_PROJECTION);
    }
    if (failOnProjectionToggle) {
      return false;
    }
    activeProjectionTypes.add(projectionType);
    return true;
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @SystemApi
  protected boolean releaseProjection(int projectionType) {
    if (projectionType == UiModeManager.PROJECTION_TYPE_AUTOMOTIVE) {
      assertHasPermission(android.Manifest.permission.TOGGLE_AUTOMOTIVE_PROJECTION);
    }
    if (failOnProjectionToggle) {
      return false;
    }
    return activeProjectionTypes.remove(projectionType);
  }

  private void assertHasPermission(String... permissions) {
    Context context = RuntimeEnvironment.getApplication();
    for (String permission : permissions) {
      if (context.getPackageManager().checkPermission(permission, context.getPackageName())
          != PackageManager.PERMISSION_GRANTED) {
        throw new SecurityException("Missing required permission: " + permission);
      }
    }
  }
}
