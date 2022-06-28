package org.robolectric.shadows;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
  public void shouldReturnTrueWhenEnabled() {
    shadowOf(accessibilityManager).setEnabled(true);
    assertThat(accessibilityManager.isEnabled()).isTrue();
    assertThat(getAccessibilityManagerInstance().isEnabled()).isTrue();
  }

  // Emulates Android framework behavior, e.g.,
  // AccessibilityManager.getInstance(context).isEnabled().
  private static AccessibilityManager getAccessibilityManagerInstance() {
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
    List<AccessibilityServiceInfo> expected =
        new ArrayList<>(Collections.singletonList(new AccessibilityServiceInfo()));
    shadowOf(accessibilityManager).setEnabledAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getEnabledAccessibilityServiceList(0)).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedInstalledServiceList() {
    List<AccessibilityServiceInfo> expected =
        new ArrayList<>(Collections.singletonList(new AccessibilityServiceInfo()));
    shadowOf(accessibilityManager).setInstalledAccessibilityServiceList(expected);
    assertThat(accessibilityManager.getInstalledAccessibilityServiceList()).isEqualTo(expected);
  }

  @Test
  public void shouldReturnExpectedAccessibilityServiceList() {
    List<ServiceInfo> expected = new ArrayList<>(Collections.singletonList(new ServiceInfo()));
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

  @Test
  public void getSentAccessibilityEvents_returnsEmptyInitially() {
    assertThat(shadowOf(accessibilityManager).getSentAccessibilityEvents()).isEmpty();
  }

  @Test
  public void getSentAccessibilityEvents_returnsAllSentAccessibilityEventsInOrder() {
    AccessibilityEvent event1 = AccessibilityEvent.obtain();
    event1.setEventType(AccessibilityEvent.TYPE_VIEW_CLICKED);

    AccessibilityEvent event2 = AccessibilityEvent.obtain();
    event2.setEventType(AccessibilityEvent.TYPE_VIEW_FOCUSED);

    AccessibilityEvent event3 = AccessibilityEvent.obtain();
    event3.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);

    shadowOf(accessibilityManager).setEnabled(true);
    accessibilityManager.sendAccessibilityEvent(event1);
    accessibilityManager.sendAccessibilityEvent(event2);
    accessibilityManager.sendAccessibilityEvent(event3);

    assertThat(shadowOf(accessibilityManager).getSentAccessibilityEvents())
        .containsExactly(event1, event2, event3)
        .inOrder();
  }

  @Test
  public void addAccessibilityStateChangeListener_shouldAddListener() {
    TestAccessibilityStateChangeListener listener1 = new TestAccessibilityStateChangeListener();
    TestAccessibilityStateChangeListener listener2 = new TestAccessibilityStateChangeListener();

    accessibilityManager.addAccessibilityStateChangeListener(listener1);
    accessibilityManager.addAccessibilityStateChangeListener(listener2);

    shadowOf(accessibilityManager).setEnabled(true);

    assertThat(listener1.isEnabled()).isTrue();
    assertThat(listener2.isEnabled()).isTrue();
  }

  @Test
  public void removeAccessibilityStateChangeListener_shouldRemoveListeners() {
    // Add two different callbacks.
    TestAccessibilityStateChangeListener listener1 = new TestAccessibilityStateChangeListener();
    TestAccessibilityStateChangeListener listener2 = new TestAccessibilityStateChangeListener();
    accessibilityManager.addAccessibilityStateChangeListener(listener1);
    accessibilityManager.addAccessibilityStateChangeListener(listener2);

    shadowOf(accessibilityManager).setEnabled(true);

    assertThat(listener1.isEnabled()).isTrue();
    assertThat(listener2.isEnabled()).isTrue();
    // Remove one at the time.
    accessibilityManager.removeAccessibilityStateChangeListener(listener2);

    shadowOf(accessibilityManager).setEnabled(false);

    assertThat(listener1.isEnabled()).isFalse();
    assertThat(listener2.isEnabled()).isTrue();

    accessibilityManager.removeAccessibilityStateChangeListener(listener1);

    shadowOf(accessibilityManager).setEnabled(true);

    assertThat(listener1.isEnabled()).isFalse();
    assertThat(listener2.isEnabled()).isTrue();
  }

  @Test
  public void removeAccessibilityStateChangeListener_returnsTrueIfRemoved() {
    TestAccessibilityStateChangeListener listener = new TestAccessibilityStateChangeListener();
    accessibilityManager.addAccessibilityStateChangeListener(listener);

    assertThat(accessibilityManager.removeAccessibilityStateChangeListener(listener)).isTrue();
  }

  @Test
  public void removeAccessibilityStateChangeListener_returnsFalseIfNotRegistered() {
    assertThat(
            accessibilityManager.removeAccessibilityStateChangeListener(
                new TestAccessibilityStateChangeListener()))
        .isFalse();
    assertThat(accessibilityManager.removeAccessibilityStateChangeListener(null)).isFalse();
  }

  @Config(minSdk = KITKAT)
  @Test
  public void setTouchExplorationEnabled_invokesCallbacks() {
    AtomicBoolean enabled = new AtomicBoolean(false);
    accessibilityManager.addTouchExplorationStateChangeListener(val -> enabled.set(val));
    shadowOf(accessibilityManager).setTouchExplorationEnabled(true);
    assertThat(enabled.get()).isEqualTo(true);
    shadowOf(accessibilityManager).setTouchExplorationEnabled(false);
    assertThat(enabled.get()).isEqualTo(false);
  }

  private static class TestAccessibilityStateChangeListener
      implements AccessibilityManager.AccessibilityStateChangeListener {

    private boolean enabled = false;

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isEnabled() {
      return enabled;
    }
  }

  @Test
  public void getAccessibilityServiceList_doesNotNPE() {
    assertThat(accessibilityManager.getAccessibilityServiceList()).isEmpty();
    assertThat(accessibilityManager.getInstalledAccessibilityServiceList()).isEmpty();
    assertThat(
            accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_SPOKEN))
        .isEmpty();
  }
}
