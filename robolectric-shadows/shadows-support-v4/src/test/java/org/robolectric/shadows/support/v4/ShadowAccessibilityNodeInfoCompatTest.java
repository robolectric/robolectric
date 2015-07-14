package org.robolectric.shadows.support.v4;

import static org.assertj.core.api.Assertions.assertThat;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class ShadowAccessibilityNodeInfoCompatTest {
  private AccessibilityNodeInfoCompat node;

  private ShadowAccessibilityNodeInfoCompat shadow;

  @Before
  public void setUp() {
    ShadowAccessibilityNodeInfoCompat.resetObtainedInstances();
    assertThat(ShadowAccessibilityNodeInfoCompat.areThereUnrecycledNodes(true)).isEqualTo(false);
    node = ShadowAccessibilityNodeInfoCompat.obtain();
    shadow = (ShadowAccessibilityNodeInfoCompat) ShadowExtractor.extract(node);
  }

  @Test
  public void shouldObtainAndResetProperly() {
    assertThat(ShadowAccessibilityNodeInfoCompat.areThereUnrecycledNodes(false)).isEqualTo(true);
    ShadowAccessibilityNodeInfoCompat.resetObtainedInstances();
    assertThat(ShadowAccessibilityNodeInfoCompat.areThereUnrecycledNodes(true)).isEqualTo(false);
  }

  @Test
  public void shouldRecordCorrectAttribution() {
    node.setText("Test");
    node.setContentDescription("Description");
    assertThat(node.getText()).isEqualTo("Test");
    assertThat(node.getContentDescription()).isEqualTo("Description");
    node.setLongClickable(true);
    boolean hasLongClick = false;
    for (int i = 0; i < shadow.getActionList().size(); i++) {
      if (shadow.getActionList().get(i).getId() == AccessibilityAction.ACTION_LONG_CLICK.getId()) {
        hasLongClick = true;
        break;
      }
    }
    assertThat(hasLongClick).isEqualTo(true);
  }

  @Test
  public void shouldRetainNodeRelationship() {
    AccessibilityNodeInfoCompat child = ShadowAccessibilityNodeInfoCompat.obtain();
    shadow.addChild(child);
    assertThat(node.getChildCount()).isEqualTo(1);
    assertThat(node.getChild(0)).isEqualTo(child);
    assertThat(child.getParent()).isEqualTo(node);
  }

  @Test
  public void shouldEqualToClonedInstance() {
    AccessibilityNodeInfoCompat copy = ShadowAccessibilityNodeInfoCompat.obtain(node);
    assertThat(node.equals(copy));
  }

  @After
  public void tearDown() {
    ShadowAccessibilityNodeInfoCompat.resetObtainedInstances();
    assertThat(ShadowAccessibilityNodeInfoCompat.areThereUnrecycledNodes(true)).isEqualTo(false);
  }
}
