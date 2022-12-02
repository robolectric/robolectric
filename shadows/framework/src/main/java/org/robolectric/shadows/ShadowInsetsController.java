package org.robolectric.shadows;

import android.os.Build;
import android.view.InsetsController;
import android.view.WindowInsets;
import androidx.annotation.RequiresApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Intercepts calls to [InsetsController] to monitor system bars functionality (hide/show). */
@Implements(value = InsetsController.class, minSdk = Build.VERSION_CODES.R, isInAndroidSdk = false)
@RequiresApi(Build.VERSION_CODES.R)
public class ShadowInsetsController {
  @ReflectorObject private InsetsControllerReflector insetsControllerReflector;

  /**
   * Intercepts calls to [InsetsController.show] to detect requested changes to the system
   * status/nav bar visibility.
   */
  @Implementation
  protected void show(int types) {
    if (hasStatusBarType(types)) {
      ShadowViewRootImpl.setIsStatusBarVisible(true);
    }

    if (hasNavigationBarType(types)) {
      ShadowViewRootImpl.setIsNavigationBarVisible(true);
    }

    insetsControllerReflector.show(types);
  }

  /**
   * Intercepts calls to [InsetsController.hide] to detect requested changes to the system
   * status/nav bar visibility.
   */
  @Implementation
  public void hide(int types) {
    if (hasStatusBarType(types)) {
      ShadowViewRootImpl.setIsStatusBarVisible(false);
    }

    if (hasNavigationBarType(types)) {
      ShadowViewRootImpl.setIsNavigationBarVisible(false);
    }

    insetsControllerReflector.hide(types);
  }

  /** Returns true if the given flags contain the mask for the system status bar. */
  private boolean hasStatusBarType(int types) {
    return hasTypeMask(types, WindowInsets.Type.statusBars());
  }

  /** Returns true if the given flags contain the mask for the system navigation bar. */
  private boolean hasNavigationBarType(int types) {
    return hasTypeMask(types, WindowInsets.Type.navigationBars());
  }

  /** Returns true if the given flags contains the requested type mask. */
  private boolean hasTypeMask(int types, int typeMask) {
    return (types & typeMask) == typeMask;
  }

  /** Reflector for [InsetsController] to use for direct (non-intercepted) calls. */
  @ForType(InsetsController.class)
  interface InsetsControllerReflector {
    @Direct
    void show(int types);

    @Direct
    void hide(int types);
  }
}
