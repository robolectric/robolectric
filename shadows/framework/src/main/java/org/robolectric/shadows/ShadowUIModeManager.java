package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.base.Preconditions.checkArgument;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SystemApi;
import android.app.IUiModeManager;
import android.app.UiModeManager;
import android.app.UiModeManager.ContrastChangeListener;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.provider.Settings;
import android.util.ArrayMap;
import com.android.internal.annotations.GuardedBy;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

/** Shadow for {@link UiModeManager}. */
@Implements(UiModeManager.class)
public class ShadowUIModeManager {
  private static final int DEFAULT_PRIORITY = 0;

  /**
   * @deprecated Use {@link #setCurrentModeType(int)} or {@link UiModeManager#getCurrentModeType()}
   *     instead.
   */
  @Deprecated public volatile int currentModeType = sharedCurrentModeType;

  private static int currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
  private static int lastFlags;
  private static int lastCarModePriority;

  // The public field `currentModeType` is deprecated. To maintain binary compatibility,
  // it remains an instance field. It is initialized with the shared static value upon
  // creation of a shadow instance, but it is not kept in sync afterwards. For up-to-date
  // values, use `getCurrentModeType()`. Direct access to `currentModeType` is discouraged
  // as it may yield stale data.
  private static int sharedCurrentModeType = Configuration.UI_MODE_TYPE_UNDEFINED;
  private static int currentApplicationNightMode = 0;
  private static final Map<Integer, Set<String>> activeProjectionTypes = new HashMap<>();
  private static boolean failOnProjectionToggle;
  private static final Object lock = new Object();

  @GuardedBy("lock")
  private static int nightModeCustomType = UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN;

  @GuardedBy("lock")
  private static boolean isNightModeOn = false;

  @RealObject UiModeManager realUiModeManager;

  private static final ImmutableSet<Integer> VALID_NIGHT_MODE_CUSTOM_TYPES =
      ImmutableSet.of(
          UiModeManager.MODE_NIGHT_CUSTOM_TYPE_SCHEDULE,
          UiModeManager.MODE_NIGHT_CUSTOM_TYPE_BEDTIME);

  @Implementation
  protected int getCurrentModeType() {
    synchronized (lock) {
      return sharedCurrentModeType;
    }
  }

  public void setCurrentModeType(int modeType) {
    synchronized (lock) {
      sharedCurrentModeType = modeType;
      currentModeType = sharedCurrentModeType;
    }
  }

  @Implementation(maxSdk = VERSION_CODES.Q)
  protected void enableCarMode(int flags) {
    enableCarMode(DEFAULT_PRIORITY, flags);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  protected void enableCarMode(int priority, int flags) {
    synchronized (lock) {
      sharedCurrentModeType = Configuration.UI_MODE_TYPE_CAR;
      currentModeType = sharedCurrentModeType;
      lastCarModePriority = priority;
      lastFlags = flags;
    }
  }

  @Implementation
  protected void disableCarMode(int flags) {
    synchronized (lock) {
      sharedCurrentModeType = Configuration.UI_MODE_TYPE_NORMAL;
      currentModeType = sharedCurrentModeType;
      lastFlags = flags;
    }
  }

  /**
   * Returns the last set car mode priority
   *
   * <p>It is changed by {@link UiModeManager#enableCarMode(int)} or {@link
   * UiModeManager#enableCarMode(int, int)}, and tracked by Robolectric for test purpose.
   *
   * @return The tracked last set car mode priority.
   */
  public int getLastCarModePriority() {
    synchronized (lock) {
      return lastCarModePriority;
    }
  }

  /**
   * Returns the last set flags.
   *
   * <p>It is changed by {@link UiModeManager#enableCarMode(int)}, {@link
   * UiModeManager#enableCarMode(int, int)} or {@link UiModeManager#disableCarMode(int)}, and
   * tracked by Robolectric for test purpose.
   *
   * @return The tracked last set flags.
   */
  public int getLastFlags() {
    synchronized (lock) {
      return lastFlags;
    }
  }

  @Implementation
  protected int getNightMode() {
    synchronized (lock) {
      return currentNightMode;
    }
  }

  @Implementation
  protected void setNightMode(int mode) {
    synchronized (lock) {
      switch (mode) {
        case UiModeManager.MODE_NIGHT_NO:
        case UiModeManager.MODE_NIGHT_YES:
        case UiModeManager.MODE_NIGHT_AUTO:
          currentNightMode = mode;
          setNightModeCustomType(UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN);
          break;
        case UiModeManager.MODE_NIGHT_CUSTOM:
          Preconditions.checkState(RuntimeEnvironment.getApiLevel() >= R);
          currentNightMode = mode;
          setNightModeCustomType(UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN);
          break;
        default:
          currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
      }
      ContentResolver resolver = getContentResolver();
      if (resolver != null) {
        Settings.Secure.putInt(resolver, Settings.Secure.UI_NIGHT_MODE, currentNightMode);
      }
    }
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected Set<String> getProjectingPackages(int projectionType) {
    synchronized (lock) {
      if (projectionType == UiModeManager.PROJECTION_TYPE_ALL) {
        Set<String> projections = new HashSet<>();
        activeProjectionTypes.values().forEach(projections::addAll);
        return projections;
      }
      return activeProjectionTypes.getOrDefault(projectionType, new HashSet<>());
    }
  }

  public int getApplicationNightMode() {
    synchronized (lock) {
      return currentApplicationNightMode;
    }
  }

  public Set<Integer> getActiveProjectionTypes() {
    synchronized (lock) {
      return new HashSet<>(activeProjectionTypes.keySet());
    }
  }

  public void setFailOnProjectionToggle(boolean failOnProjectionToggle) {
    synchronized (lock) {
      ShadowUIModeManager.failOnProjectionToggle = failOnProjectionToggle;
    }
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @HiddenApi
  protected void setApplicationNightMode(int mode) {
    synchronized (lock) {
      currentApplicationNightMode = mode;
    }
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @SystemApi
  protected boolean requestProjection(int projectionType) {
    synchronized (lock) {
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
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @SystemApi
  protected boolean releaseProjection(int projectionType) {
    synchronized (lock) {
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

  /**
   * Sets the contrast value.
   *
   * <p>The default value for contrast is 0.0f. The permitted values are between -1.0f and 1.0f
   * inclusive.
   */
  public void setContrast(float contrast) {
    checkArgument(
        contrast >= -1.0f && contrast <= 1.0f,
        "Contrast value must be between -1.0f and 1.0f inclusive. Provided value: %s",
        contrast);

    ArrayMap<ContrastChangeListener, Executor> listeners;
    if (RuntimeEnvironment.getApiLevel() == VERSION_CODES.UPSIDE_DOWN_CAKE) {
      reflector(UiModeManagerReflector.class, realUiModeManager).setContrast(contrast);
      listeners =
          reflector(UiModeManagerReflector.class, realUiModeManager).getContrastChangeListeners();
    } else if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.BAKLAVA) {
      Object globals = reflector(UiModeManagerReflector.class, realUiModeManager).getGlobals();
      int userId = reflector(UiModeManagerReflector.class, realUiModeManager).getUserId();
      Object userCallback =
          reflector(UiModeManagerGlobalsReflector.class, globals).getUserCallbackOrCreate(userId);
      reflector(UserCallbackReflector.class, userCallback).setContrast(contrast);
      listeners = reflector(UserCallbackReflector.class, userCallback).getContrastChangeListeners();
    } else {
      Object globals = reflector(UiModeManagerReflector.class, realUiModeManager).getGlobals();
      reflector(UiModeManagerGlobalsReflector.class, globals).setContrast(contrast);
      listeners =
          reflector(UiModeManagerGlobalsReflector.class, globals).getContrastChangeListeners();
    }

    for (Map.Entry<ContrastChangeListener, Executor> entry : listeners.entrySet()) {
      final ContrastChangeListener listener = entry.getKey();
      final Executor executor = entry.getValue();

      if (listener != null && executor != null) {
        executor.execute(
            () -> {
              listener.onContrastChanged(contrast);
            });
      }
    }
  }

  @Resetter
  public static void reset() {
    if (RuntimeEnvironment.getApiLevel() >= VANILLA_ICE_CREAM) {
      IUiModeManager service =
          IUiModeManager.Stub.asInterface(
              reflector(ServiceManagerReflector.class).getServiceOrThrow(Context.UI_MODE_SERVICE));
      reflector(UiModeManagerReflector.class)
          .setGlobals(reflector(UiModeManagerGlobalsReflector.class).newGlobals(service));
    }
    synchronized (lock) {
      // When the new instance is initialized, the currentModeType will use this
      // shared type to initialize it to keep the initial value same.
      sharedCurrentModeType = Configuration.UI_MODE_TYPE_UNDEFINED;
      currentNightMode = UiModeManager.MODE_NIGHT_AUTO;
      lastFlags = 0;
      lastCarModePriority = 0;
      currentApplicationNightMode = 0;
      activeProjectionTypes.clear();
      failOnProjectionToggle = false;
      nightModeCustomType = UiModeManager.MODE_NIGHT_CUSTOM_TYPE_UNKNOWN;
      isNightModeOn = false;
    }
  }

  @ForType(className = "android.os.ServiceManager")
  interface ServiceManagerReflector {
    @Static
    IBinder getServiceOrThrow(String name);
  }

  @ForType(UiModeManager.class)
  interface UiModeManagerReflector {
    @Accessor("mContext")
    Context getContext();

    @Accessor("mContrast")
    void setContrast(float value); // Stores the contrast value for Android U.

    @Accessor("mContrastChangeListeners")
    ArrayMap<ContrastChangeListener, Executor>
        getContrastChangeListeners(); // Stores the contrast listeners for Android U.

    @Accessor("sGlobals")
    @Static
    Object getGlobals(); // Stores the contrast value for Android V and above.

    @Accessor("sGlobals")
    @Static
    void setGlobals(@WithType("android.app.UiModeManager$Globals") Object value);

    int getUserId();
  }

  @ForType(className = "android.app.UiModeManager$Globals")
  interface UiModeManagerGlobalsReflector {
    @Accessor("mContrast")
    void setContrast(float contrast);

    @Accessor("mContrastChangeListeners")
    ArrayMap<ContrastChangeListener, Executor>
        getContrastChangeListeners(); // Stores the contrast listeners for Android V and above.

    @Constructor
    Object newGlobals(IUiModeManager iUiModeManager);

    /* UserCallback */ Object getUserCallbackOrCreate(int userId);
  }

  @ForType(className = "android.app.UiModeManager$UserCallback")
  private interface UserCallbackReflector {

    @Accessor("mContrastChangeListeners")
    ArrayMap<ContrastChangeListener, Executor>
        getContrastChangeListeners(); // Stores the contrast listeners for above Android B.

    @Accessor("mContrast")
    void setContrast(float contrast);
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
