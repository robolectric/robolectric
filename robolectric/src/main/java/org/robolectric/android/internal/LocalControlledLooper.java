package org.robolectric.android.internal;

import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.view.View;
import android.view.ViewRootImpl;
import androidx.test.internal.platform.os.ControlledLooper;
import org.robolectric.shadows.ShadowWindowManagerGlobal;
import org.robolectric.util.ReflectionHelpers;

/** A Robolectric implementation for {@link ControlledLooper}. */
@SuppressWarnings("RestrictTo")
public class LocalControlledLooper implements ControlledLooper {

  @Override
  public void drainMainThreadUntilIdle() {
    shadowMainLooper().idle();
  }

  @Override
  public void simulateWindowFocus(View decorView) {
    ViewRootImpl viewRoot = ReflectionHelpers.callInstanceMethod(decorView, "getViewRootImpl");
    if (viewRoot != null) {
      ReflectionHelpers.callInstanceMethod(
          viewRoot,
          "windowFocusChanged",
          ReflectionHelpers.ClassParameter.from(boolean.class, true), /* hasFocus */
          ReflectionHelpers.ClassParameter.from(
              boolean.class, ShadowWindowManagerGlobal.getInTouchMode()));
    }
  }
}
