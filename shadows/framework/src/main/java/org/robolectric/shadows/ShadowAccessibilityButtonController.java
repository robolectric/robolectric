package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.accessibilityservice.AccessibilityButtonController;
import android.os.Handler;
import android.os.Looper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow for {@link AccessibilityButtonController}. */
@Implements(className = "android.accessibilityservice.AccessibilityButtonController", minSdk = P)
public class ShadowAccessibilityButtonController {

  @RealObject AccessibilityButtonController realAccessibilityButtonController;
  private AccessibilityButtonController.AccessibilityButtonCallback accessibilityButtonCallback;

  @Implementation
  public void registerAccessibilityButtonCallback(
      AccessibilityButtonController.AccessibilityButtonCallback accessibilityButtonCallback) {
    this.accessibilityButtonCallback = accessibilityButtonCallback;
    realAccessibilityButtonController.registerAccessibilityButtonCallback(
        accessibilityButtonCallback, new Handler(Looper.getMainLooper()));
  }

  /** Returns AccessibilityButtonCallback for this test case. */
  public AccessibilityButtonController.AccessibilityButtonCallback getCallback() {
    return accessibilityButtonCallback;
  }
}
