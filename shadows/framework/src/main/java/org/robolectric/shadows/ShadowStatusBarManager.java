package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.app.StatusBarManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Robolectric implementation of {@link android.app.StatusBarManager}. */
@Implements(value = StatusBarManager.class, isInAndroidSdk = false)
public class ShadowStatusBarManager {

  public static final int DEFAULT_DISABLE_MASK = StatusBarManager.DISABLE_MASK;
  public static final int DEFAULT_DISABLE2_MASK = StatusBarManager.DISABLE2_MASK;
  private int disabled = StatusBarManager.DISABLE_NONE;
  private int disabled2 = StatusBarManager.DISABLE2_NONE;

  @Implementation
  protected void disable(int what) {
    disabled = what;
  }

  @Implementation(minSdk = M)
  protected void disable2(int what) {
    disabled2 = what;
  }

  /** Returns the disable flags previously set in {@link #disable}. */
  public int getDisableFlags() {
    return disabled;
  }

  /** Returns the disable flags previously set in {@link #disable2}. */
  public int getDisable2Flags() {
    return disabled2;
  }
}
