package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.StatusBarManager;
import androidx.annotation.VisibleForTesting;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Robolectric implementation of {@link android.app.StatusBarManager}. */
@Implements(value = StatusBarManager.class, isInAndroidSdk = false)
public class ShadowStatusBarManager {

  public static final int DEFAULT_DISABLE_MASK = StatusBarManager.DISABLE_MASK;
  public static final int DEFAULT_DISABLE2_MASK = StatusBarManager.DISABLE2_MASK;
  public static final int DISABLE_NOTIFICATION_ALERTS = 0x00040000;
  public static final int DISABLE_EXPAND = 0x00010000;
  public static final int DISABLE_HOME = 0x00200000;
  public static final int DISABLE_CLOCK = 0x00800000;
  public static final int DISABLE_RECENT = 0x01000000;
  public static final int DISABLE_SEARCH = 0x02000000;
  public static final int DISABLE_NONE = 0x00000000;
  public static final int DISABLE2_ROTATE_SUGGESTIONS = 1 << 4;
  public static final int DISABLE2_NONE = 0x00000000;

  private int disabled = StatusBarManager.DISABLE_NONE;
  private int disabled2 = StatusBarManager.DISABLE2_NONE;

  private int navBarMode = StatusBarManager.NAV_BAR_MODE_DEFAULT;

  @Implementation
  protected void disable(int what) {
    disabled = what;
  }

  @Implementation(minSdk = M)
  protected void disable2(int what) {
    disabled2 = what;
  }

  @Implementation(minSdk = Q)
  protected void setDisabledForSetup(boolean disabled) {
    disable(disabled ? getDefaultSetupDisableFlags() : StatusBarManager.DISABLE_NONE);
    disable2(disabled ? getDefaultSetupDisable2Flags() : StatusBarManager.DISABLE2_NONE);
  }

  @VisibleForTesting
  static int getDefaultSetupDisableFlags() {
    return reflector(StatusBarManagerReflector.class).getDefaultSetupDisableFlags();
  }

  @VisibleForTesting
  static int getDefaultSetupDisable2Flags() {
    return reflector(StatusBarManagerReflector.class).getDefaultSetupDisable2Flags();
  }

  /** Returns the disable flags previously set in {@link #disable}. */
  public int getDisableFlags() {
    return disabled;
  }

  /** Returns the disable flags previously set in {@link #disable2}. */
  public int getDisable2Flags() {
    return disabled2;
  }

  @Implementation(minSdk = TIRAMISU)
  protected void setNavBarMode(int mode) {
    navBarMode = mode;
  }

  @Implementation(minSdk = TIRAMISU)
  protected int getNavBarMode() {
    return navBarMode;
  }

  @ForType(StatusBarManager.class)
  interface StatusBarManagerReflector {
    @Static
    @Accessor("DEFAULT_SETUP_DISABLE_FLAGS")
    int getDefaultSetupDisableFlags();

    @Static
    @Accessor("DEFAULT_SETUP_DISABLE2_FLAGS")
    int getDefaultSetupDisable2Flags();
  }
}
