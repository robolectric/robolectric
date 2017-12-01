package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = LOLLIPOP)
public class ShadowAccessibilityWindowInfoTest {
  private AccessibilityWindowInfo window;
  private ShadowAccessibilityWindowInfo shadow;

  @Before
  public void setUp() {
    ShadowAccessibilityWindowInfo.resetObtainedInstances();
    assertThat(ShadowAccessibilityWindowInfo.areThereUnrecycledWindows(true)).isEqualTo(false);
    window = ShadowAccessibilityWindowInfo.obtain();
    assertThat(window == null).isEqualTo(false);
    shadow = shadowOf(window);
  }

  @Test
  public void shouldNotHaveRootNode() {
    assertThat(shadow.getRoot() == null).isEqualTo(true);
  }

  @Test
  public void shouldHaveAssignedRoot() {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    shadow.setRoot(node);
    assertThat(shadow.getRoot()).isEqualTo(node);
  }
}
