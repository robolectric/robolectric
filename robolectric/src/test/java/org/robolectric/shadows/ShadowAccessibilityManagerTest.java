package org.robolectric.shadows;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.util.ReflectionHelpers;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowAccessibilityManagerTest {

  private ShadowAccessibilityManager shadowAccessibilityManager;

  @Before
  public void setUp() throws Exception {
    shadowAccessibilityManager = shadowOf(getAccessibilityManager());
  }

  // Emulates Android framework behavior, e.g.,
  // AccessibilityManager.getInstance(context).isEnabled().
  private AccessibilityManager getAccessibilityManager() throws Exception {
    return ReflectionHelpers.callStaticMethod(AccessibilityManager.class, "getInstance",
            ReflectionHelpers.ClassParameter.from(Context.class, RuntimeEnvironment.application));
  }

  @Test
  public void shouldReturnTrueWhenEnabled() {
    shadowAccessibilityManager.setEnabled(true);
    assertThat(getAccessibilityManager().isEnabled()).isTrue();
  }

  @Test
  public void shouldReturnTrueForTouchExplorationWhenEnabled() {
    shadowAccessibilityManager.setTouchExplorationEnabled(true);
    assertThat(getAccessibilityManager().isTouchExplorationEnabled()).isTrue();
  }

  @Test
  public void shouldReturnExpectedEnabledServiceList() {
    List<AccessibilityServiceInfo> expected = new ArrayList<>(Arrays.asList(new AccessibilityServiceInfo()));
    shadowAccessibilityManager.setEnabledAccessibilityServiceList(expected);
    assertThat(getAccessibilityManager().getEnabledAccessibilityServiceList(0)).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedInstalledServiceList() {
    List<AccessibilityServiceInfo> expected = new ArrayList<>(Arrays.asList(new AccessibilityServiceInfo()));
    shadowAccessibilityManager.setInstalledAccessibilityServiceList(expected);
    assertThat(getAccessibilityManager().getInstalledAccessibilityServiceList()).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedAccessibilityServiceList() {
    List<ServiceInfo> expected = new ArrayList<>(Arrays.asList(new ServiceInfo()));
    shadowAccessibilityManager.setAccessibilityServiceList(expected);
    assertThat(getAccessibilityManager().getAccessibilityServiceList()).isEqualTo(expected);
  }
}
