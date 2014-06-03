package org.robolectric.shadows;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.ServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.accessibility.AccessibilityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class AccessibilityManagerTest {

  private AccessibilityManager accessibilityManager;
  private ShadowAccessibilityManager shadowAccessibilityManager;

  @Before
  public void setUp() throws Exception {
    accessibilityManager = (AccessibilityManager) Robolectric.application.getSystemService(ACCESSIBILITY_SERVICE);
    shadowAccessibilityManager = shadowOf(accessibilityManager);
  }

  @Test
  public void shouldReturnTrueWhenEnabled() {
    shadowAccessibilityManager.setEnabled(true);
    assertTrue(accessibilityManager.isEnabled());
  }

  @Test
  public void shouldReturnTrueForTouchExplorationWhenEnabled() {
    shadowAccessibilityManager.setTouchExplorationEnabled(true);
    assertTrue(accessibilityManager.isTouchExplorationEnabled());
  }

  @Test
  public void shouldReturnExpectedEnabledServiceList() {
    List<AccessibilityServiceInfo> expected = new ArrayList<AccessibilityServiceInfo>();
    expected.add(new AccessibilityServiceInfo());
    shadowAccessibilityManager.setEnabledAccessibilityServiceList(expected);

    assertEquals(expected,accessibilityManager.getEnabledAccessibilityServiceList(0));
  }

  @Test
  public void shouldReturnExpectedInstalledServiceList() {
    List<AccessibilityServiceInfo> expected = new ArrayList<AccessibilityServiceInfo>();
    expected.add(new AccessibilityServiceInfo());
    shadowAccessibilityManager.setInstalledAccessibilityServiceList(expected);

    assertEquals(expected, accessibilityManager.getInstalledAccessibilityServiceList());
  }

  @Test
  public void shouldReturnExpectedAccessibilityServiceList() {

    List<ServiceInfo> expected = new ArrayList<ServiceInfo>();
    expected.add(new ServiceInfo());
    shadowAccessibilityManager.setAccessibilityServiceList(expected);

    assertEquals(expected, accessibilityManager.getAccessibilityServiceList());
  }
}
