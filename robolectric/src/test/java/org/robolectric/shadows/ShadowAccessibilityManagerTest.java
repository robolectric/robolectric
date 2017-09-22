package org.robolectric.shadows;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class ShadowAccessibilityManagerTest {

  private AccessibilityManager accessibilityManager;
  private ShadowAccessibilityManager shadowAccessibilityManager;

  @Before
  public void setUp() throws Exception {
    accessibilityManager = (AccessibilityManager) RuntimeEnvironment.application.getSystemService(ACCESSIBILITY_SERVICE);
    shadowAccessibilityManager = shadowOf(accessibilityManager);
  }

  @Test
  public void shouldReturnTrueWhenEnabled() throws Exception {
    shadowAccessibilityManager.setEnabled(true);
    assertThat(accessibilityManager.isEnabled()).isTrue();
    assertThat(getAccessibilityManagerInstance().isEnabled()).isTrue();
  }

  // Emulates Android framework behavior, e.g.,
  // AccessibilityManager.getInstance(context).isEnabled().
  private static AccessibilityManager getAccessibilityManagerInstance() throws Exception {
    return ReflectionHelpers.callStaticMethod(AccessibilityManager.class, "getInstance",
            ReflectionHelpers.ClassParameter.from(Context.class, RuntimeEnvironment.application));
  }

  @Test
  public void shouldReturnTrueForTouchExplorationWhenEnabled() {
    shadowAccessibilityManager.setTouchExplorationEnabled(true);
    assertThat(accessibilityManager.isTouchExplorationEnabled()).isTrue();
  }

  @Test
  public void shouldReturnExpectedEnabledServiceList() {
    List<AccessibilityServiceInfo> expected = new ArrayList<>(Arrays.asList(new AccessibilityServiceInfo()));
    shadowAccessibilityManager.setEnabledAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getEnabledAccessibilityServiceList(0)).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedInstalledServiceList() {
    List<AccessibilityServiceInfo> expected = new ArrayList<>(Arrays.asList(new AccessibilityServiceInfo()));
    shadowAccessibilityManager.setInstalledAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getInstalledAccessibilityServiceList()).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedAccessibilityServiceList() {
    List<ServiceInfo> expected = new ArrayList<>(Arrays.asList(new ServiceInfo()));
    shadowAccessibilityManager.setAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getAccessibilityServiceList()).isEqualTo(expected);
  }
}
