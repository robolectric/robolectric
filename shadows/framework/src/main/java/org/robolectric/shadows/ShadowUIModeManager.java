package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SystemApi;
import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import com.android.internal.annotations.GuardedBy;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link UiModeManager}. */
@Implements(UiModeManager.class)
public class ShadowUIModeManager {
  public int currentModeType = Configuration.UI_MODE_TYPE_UNDEFINED;
  public int currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
  public int lastFlags;
  public int lastCarModePriority;
  private int currentApplicationNightMode = 0;
  private final Map<Integer, Set<String>> activeProjectionTypes = new HashMap<>();
  private boolean failOnProjectionToggle;

  private static final ImmutableSet<Integer> VALID_NIGHT_MODES =
      ImmutableSet.of(
          UiModeManager.MODE_NIGHT_AUTO, UiModeManager.MODE_NIGHT_NO, UiModeManager.MODE_NIGHT_YES);

  private static final int DEFAULT_PRIORITY = 0;

  private final Object lock = new Object();

  @GuardedBy("lock")
  private int nightModeCustomType = UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN;

  @GuardedBy("lock")
  private boolean isNightModeOn = false;

  @RealObject UiModeManager realUiModeManager;

  private static final ImmutableSet<Integer> VALID_NIGHT_MODE_CUSTOM_TYPES =
      ImmutableSet.of(
          UiModeManager.MODE_NIGHT_CUSTOM_TYPE_SCHEDULE,
          UiModeManager.MODE_NIGHT_CUSTOM_TYPE_BEDTIME);

  @Implementation
  protected int getCurrentModeType() {
    return currentModeType;
  }

  public void setCurrentModeType(int modeType) {
    this.currentModeType = modeType;
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
    synchronized (lock) {
      ContentResolver resolver = getContentResolver();
      switch (mode) {
        case UiModeManager.MODE_NIGHT_NO:
        case UiModeManager.MODE_NIGHT_YES:
        case UiModeManager.MODE_NIGHT_AUTO:
          currentNightMode = mode;
          nightModeCustomType = UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN;
          if (resolver != null) {
            Settings.Secure.putInt(resolver, Settings.Secure.UI_NIGHT_MODE, mode);
            Settings.Secure.putInt(
                resolver,
                Settings.Secure.UI_NIGHT_MODE_CUSTOM_TYPE,
                UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN);
          }
          break;
        default:
          currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
          if (resolver != null) {
            Settings.Secure.putInt(
                resolver, Settings.Secure.UI_NIGHT_MODE, UiModeManager.MODE_NIGHT_AUTO);
          }
      }
    }
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected Set<String> getProjectingPackages(int projectionType) {
    if (projectionType == UiModeManager.PROJECTION_TYPE_ALL) {
      Set<String> projections = new HashSet<>();
      activeProjectionTypes.values().forEach(projections::addAll);
      return projections;
    }
    return activeProjectionTypes.getOrDefault(projectionType, new HashSet<>());
  }

  public int getApplicationNightMode() {
    return currentApplicationNightMode;
  }

  public Set<Integer> getActiveProjectionTypes() {
    return new HashSet<>(activeProjectionTypes.keySet());
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
    Set<String> projections = activeProjectionTypes.getOrDefault(projectionType, new HashSet<>());
    projections.add(RuntimeEnvironment.getApplication().getPackageName());
    activeProjectionTypes.put(projectionType, projections);

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
    String packageName = RuntimeEnvironment.getApplication().getPackageName();
    Set<String> projections = activeProjectionTypes.getOrDefault(projectionType, new HashSet<>());
    if (projections.contains(packageName)) {
      projections.remove(packageName);
      if (projections.isEmpty()) {
        activeProjectionTypes.remove(projectionType);
      } else {
        activeProjectionTypes.put(projectionType, projections);
      }
      return true;
    }

    return false;
  }

  @Implementation(minSdk = TIRAMISU)
  protected int getNightModeCustomType() {
    synchronized (lock) {
      return nightModeCustomType;
    }
  }

  /** Returns whether night mode is currently on when a custom night mode type is selected. */
  public boolean isNightModeOn() {
    synchronized (lock) {
      return isNightModeOn;
    }
  }

  @Implementation(minSdk = TIRAMISU)
  protected void setNightModeCustomType(int mode) {
    synchronized (lock) {
      ContentResolver resolver = getContentResolver();
      if (VALID_NIGHT_MODE_CUSTOM_TYPES.contains(mode)) {
        nightModeCustomType = mode;
        currentNightMode = UiModeManager.MODE_NIGHT_CUSTOM;
        if (resolver != null) {
          Settings.Secure.putInt(resolver, Settings.Secure.UI_NIGHT_MODE_CUSTOM_TYPE, mode);
        }
      } else {
        nightModeCustomType = UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN;
        if (resolver != null) {
          Settings.Secure.putInt(
              resolver,
              Settings.Secure.UI_NIGHT_MODE_CUSTOM_TYPE,
              UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN);
        }
      }
    }
  }

  private ContentResolver getContentResolver() {
    Context context = getContext();
    return context == null ? null : context.getContentResolver();
  }

  // Note: UiModeManager stores the context only starting from Android R.
  private Context getContext() {
    if (VERSION.SDK_INT < VERSION_CODES.R) {
      return null;
    }
    return reflector(UiModeManagerReflector.class, realUiModeManager).getContext();
  }

  @Implementation(minSdk = TIRAMISU)
  protected boolean setNightModeActivatedForCustomMode(int mode, boolean active) {
    synchronized (lock) {
      if (VALID_NIGHT_MODE_CUSTOM_TYPES.contains(mode) && nightModeCustomType == mode) {
        isNightModeOn = active;
        return true;
      }
      return false;
    }
  }

  @ForType(UiModeManager.class)
  interface UiModeManagerReflector {
    @Accessor("mContext")
    Context getContext();
  }

  private void assertHasPermission(String... permissions) {
    Context context = RuntimeEnvironment.getApplication();
    for (String permission : permissions) {
      // Check both the Runtime based and Manifest based permissions
      if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
          && context.getPackageManager().checkPermission(permission, context.getPackageName())
              != PackageManager.PERMISSION_GRANTED) {
        throw new SecurityException("Missing required permission: " + permission);
      }
    }
  }
}
