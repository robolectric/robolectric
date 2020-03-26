package org.robolectric.pluginapi;

/**
 * Extension point for providing access to the old Accessibility Test Framework.
 *
 * Will be removed after Robolectric 4.4.
 *
 * @deprecated Use Espresso for view interactions.
 */
@Deprecated
@ExtensionPoint
public interface AccessibilityChecker {
  /** @deprecated Use Espresso for view interactions. */
  @Deprecated
  void checkViewAccessibility(Object realView);
}
