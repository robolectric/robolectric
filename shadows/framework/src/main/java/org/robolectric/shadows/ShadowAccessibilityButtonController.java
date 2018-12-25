package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.accessibilityservice.AccessibilityButtonController;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link AccessibilityButtonController}. */
@Implements(className = "android.accessibilityservice.AccessibilityButtonController", minSdk = P)
public class ShadowAccessibilityButtonController {

  @RealObject AccessibilityButtonController realObject;

  /** Performs click action for accessibility button. */
  public void performAccessibilityButtonClick() {
    ReflectionHelpers.callInstanceMethod(realObject, "dispatchAccessibilityButtonClicked");
  }
}
