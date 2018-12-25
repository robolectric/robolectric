package org.robolectric.shadows;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowAccessibilityManagerTest {

  private AccessibilityManager accessibilityManager;

  @Before
  public void setUp() throws Exception {
    accessibilityManager =
        (AccessibilityManager)
            ApplicationProvider.getApplicationContext().getSystemService(ACCESSIBILITY_SERVICE);
  }

  @Test
  public void shouldReturnTrueWhenEnabled() throws Exception {
    shadowOf(accessibilityManager).setEnabled(true);
    assertThat(accessibilityManager.isEnabled()).isTrue();
    assertThat(getAccessibilityManagerInstance().isEnabled()).isTrue();
  }

  // Emulates Android framework behavior, e.g.,
  // AccessibilityManager.getInstance(context).isEnabled().
  private static AccessibilityManager getAccessibilityManagerInstance() throws Exception {
    return ReflectionHelpers.callStaticMethod(
        AccessibilityManager.class,
        "getInstance",
        ReflectionHelpers.ClassParameter.from(
            Context.class, ApplicationProvider.getApplicationContext()));
  }

  @Test
  public void shouldReturnTrueForTouchExplorationWhenEnabled() {
    shadowOf(accessibilityManager).setTouchExplorationEnabled(true);
    assertThat(accessibilityManager.isTouchExplorationEnabled()).isTrue();
  }

  @Test
  public void shouldReturnExpectedEnabledServiceList() {
    List<AccessibilityServiceInfo> expected = new ArrayList<>(Arrays.asList(new AccessibilityServiceInfo()));
    shadowOf(accessibilityManager).setEnabledAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getEnabledAccessibilityServiceList(0)).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedInstalledServiceList() {
    List<AccessibilityServiceInfo> expected = new ArrayList<>(Arrays.asList(new AccessibilityServiceInfo()));
    shadowOf(accessibilityManager).setInstalledAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getInstalledAccessibilityServiceList()).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedAccessibilityServiceList() {
    List<ServiceInfo> expected = new ArrayList<>(Arrays.asList(new ServiceInfo()));
    shadowOf(accessibilityManager).setAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getAccessibilityServiceList()).isEqualTo(expected);
  }

  @Test
  @Config(minSdk = O_MR1)
  public void isAccessibilityButtonSupported() {
    assertThat(AccessibilityManager.isAccessibilityButtonSupported()).isTrue();

    ShadowAccessibilityManager.setAccessibilityButtonSupported(false);
    assertThat(AccessibilityManager.isAccessibilityButtonSupported()).isFalse();

    ShadowAccessibilityManager.setAccessibilityButtonSupported(true);
    assertThat(AccessibilityManager.isAccessibilityButtonSupported()).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void performAccessibilityShortcut_shouldEnableAccessibilityAndTouchExploration() {
    accessibilityManager.performAccessibilityShortcut();

    assertThat(accessibilityManager.isEnabled()).isTrue();
    assertThat(accessibilityManager.isTouchExplorationEnabled()).isTrue();
  }
}
