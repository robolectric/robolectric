package org.robolectric.shadows;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import static org.robolectric.Robolectric.*;
import static org.fest.assertions.api.Assertions.*;

import java.util.List;
import com.google.android.collect.Lists;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;
import android.accessibilityservice.AccessibilityServiceInfo;
import static android.content.Context.ACCESSIBILITY_SERVICE;

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
    assertThat(accessibilityManager.isEnabled()).isTrue();
  }

  @Test
  public void shouldReturnTrueForTouchExplorationWhenEnabled() {
    shadowAccessibilityManager.setTouchExplorationEnabled(true);
    assertThat(accessibilityManager.isTouchExplorationEnabled()).isTrue();
  }

  @Test
  public void shouldReturnExpectedEnabledServiceList() {
    List<AccessibilityServiceInfo> expected = Lists.newArrayList(new AccessibilityServiceInfo());
    shadowAccessibilityManager.setEnabledAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getEnabledAccessibilityServiceList(0)).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedInstalledServiceList() {
    List<AccessibilityServiceInfo> expected = Lists.newArrayList(new AccessibilityServiceInfo());
    shadowAccessibilityManager.setInstalledAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getInstalledAccessibilityServiceList()).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedAccessibilityServiceList() {
    List<ServiceInfo> expected = Lists.newArrayList(new ServiceInfo());
    shadowAccessibilityManager.setAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getAccessibilityServiceList()).isEqualTo(expected);
  }
}
