package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import javax.annotation.Priority;
import org.robolectric.pluginapi.AccessibilityChecker;

/**
 * No-op implementation of AccessibilityChecker.
 *
 * Will be removed after Robolectric 4.4.
 *
 * @deprecated Use Espresso for view interactions.
 */
@Deprecated
@AutoService(AccessibilityChecker.class)
@Priority(Integer.MIN_VALUE)
class NoOpAccessibilityChecker implements AccessibilityChecker {
  public NoOpAccessibilityChecker() {
  }

  @Override
  public void checkViewAccessibility(Object realView) {
    // No-op.
  }
}
