package org.robolectric.android.internal;

import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.view.View;
import android.view.ViewRootImpl;
import androidx.test.internal.platform.os.ControlledLooper;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowViewRootImpl;
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
      ShadowViewRootImpl shadowViewRoot = Shadow.extract(viewRoot);
      shadowViewRoot.callWindowFocusChanged(true);
    }
  }
}
