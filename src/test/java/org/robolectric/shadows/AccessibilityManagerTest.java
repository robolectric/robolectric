package org.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.accessibility.AccessibilityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class AccessibilityManagerTest {

  private AccessibilityManager accessibilityManager;

  @Before
  public void setUp() throws Exception {
    accessibilityManager = (AccessibilityManager) Robolectric.application.getSystemService(ACCESSIBILITY_SERVICE);
  }

  @Test
  public void shouldReturnFalseForEnabled() {
    assertFalse(accessibilityManager.isEnabled());
  }

  @Test
  public void shouldReturnFalseForTouchExploration() {
    assertFalse(accessibilityManager.isTouchExplorationEnabled());
  }

  @Test
  public void shouldReturnEmptyListForEnabledServices() {
    assertTrue(accessibilityManager.getEnabledAccessibilityServiceList(0).isEmpty());
  }

  @Test
  public void shouldReturnEmptyListForInstalledServices() {
    assertTrue(accessibilityManager.getInstalledAccessibilityServiceList().isEmpty());
  }

  @Test
  public void shouldReturnEmptyListForAccessibilityServices() {
    assertTrue(accessibilityManager.getAccessibilityServiceList().isEmpty());
  }
}
