package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowAccessibilityWindowInfoTest {
  private ShadowAccessibilityWindowInfo shadow;

  @Before
  public void setUp() {
    ShadowAccessibilityWindowInfo.resetObtainedInstances();
    assertThat(ShadowAccessibilityWindowInfo.areThereUnrecycledWindows(true)).isEqualTo(false);
    AccessibilityWindowInfo window = ShadowAccessibilityWindowInfo.obtain();
    assertThat(window).isNotNull();
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

  @Test
  public void testSetAnchor() {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    shadow.setAnchor(node);
    assertThat(shadow.getAnchor()).isEqualTo(node);
  }

  @Test
  public void testSetTitle() {
    assertThat(shadow.getTitle()).isNull();
    CharSequence title = "Title";
    shadow.setTitle(title);
    assertThat(shadow.getTitle().toString()).isEqualTo(title.toString());
  }

  @Test
  public void testSetChild() {
    AccessibilityWindowInfo window = AccessibilityWindowInfo.obtain();
    shadow.addChild(window);
    assertThat(shadow.getChild(0)).isEqualTo(window);
  }
}
