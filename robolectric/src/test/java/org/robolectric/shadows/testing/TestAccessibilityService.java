package org.robolectric.shadows.testing;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/** An accessibility service that does nothing */
public class TestAccessibilityService extends AccessibilityService {
  @Override
  public void onAccessibilityEvent(AccessibilityEvent arg0) {
    // Do nothing
  }

  @Override
  public void onInterrupt() {
    // Do nothing
  }
}
