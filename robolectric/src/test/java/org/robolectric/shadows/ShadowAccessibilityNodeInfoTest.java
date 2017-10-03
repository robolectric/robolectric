package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityWindowInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowAccessibilityNodeInfoTest {

  private AccessibilityNodeInfo node;

  private ShadowAccessibilityNodeInfo shadow;

  @Before
  public void setUp() {
    ShadowAccessibilityNodeInfo.resetObtainedInstances();
    assertThat(ShadowAccessibilityNodeInfo.areThereUnrecycledNodes(true)).isEqualTo(false);
    node = AccessibilityNodeInfo.obtain();
  }

  @Test
  public void shouldHaveObtainedNode() {
    assertThat(ShadowAccessibilityNodeInfo.areThereUnrecycledNodes(false)).isEqualTo(true);
  }

  @Test
  public void shouldHaveZeroBounds() {
    Rect outBounds = new Rect();
    node.getBoundsInParent(outBounds);
    assertThat(outBounds.left).isEqualTo(0);
  }

  @Test
  public void ShouldHaveClonedCorrectly() {
    node.setAccessibilityFocused(true);
    node.setBoundsInParent(new Rect(0, 0, 100, 100));
    node.setContentDescription("test");
    AccessibilityNodeInfo anotherNode = AccessibilityNodeInfo.obtain(node);
    assertEquals(node, anotherNode);
  }

  @Test
  public void shouldWriteAndReadFromParcelCorrectly() {
    Parcel p = Parcel.obtain();
    node.setContentDescription("test");
    node.writeToParcel(p, 0);
    p.setDataPosition(0);
    AccessibilityNodeInfo anotherNode = AccessibilityNodeInfo.CREATOR.createFromParcel(p);
    assertThat(node).isEqualTo(anotherNode);
    node.setContentDescription(null);
  }

  @Test
  public void shouldNotHaveInfiniteLoopWithSameLoopedChildren() {
    node = AccessibilityNodeInfo.obtain();
    AccessibilityNodeInfo child = AccessibilityNodeInfo.obtain();
    shadowOf(node).addChild(child);
    shadowOf(child).addChild(node);
    AccessibilityNodeInfo anotherNode = AccessibilityNodeInfo.obtain(node);
    assertThat(node.equals(anotherNode)).isEqualTo(true);
  }

  @Test
  public void shouldNotHaveInfiniteLoopWithDifferentLoopedChildren() {
    node = AccessibilityNodeInfo.obtain();
    shadow = shadowOf(node);
    AccessibilityNodeInfo child1 = AccessibilityNodeInfo.obtain();
    shadow.addChild(child1);
    ShadowAccessibilityNodeInfo child1Shadow = shadowOf(child1);
    child1Shadow.addChild(node);
    AccessibilityNodeInfo anotherNode = ShadowAccessibilityNodeInfo.obtain(node);
    AccessibilityNodeInfo child2 = ShadowAccessibilityNodeInfo.obtain();
    child2.setText("test");
    ShadowAccessibilityNodeInfo child2Shadow = shadowOf(child2);
    ShadowAccessibilityNodeInfo anotherNodeShadow = shadowOf(anotherNode);
    anotherNodeShadow.addChild(child2);
    child2Shadow.addChild(anotherNode);
    assertThat(node.equals(anotherNode)).isEqualTo(false);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldRecordFlagsProperly() {
    node = AccessibilityNodeInfo.obtain();
    node.setClickable(false);
    shadow = shadowOf(node);
    shadow.setPasteable(false);
    assertThat(shadow.isClickable()).isEqualTo(false);
    assertThat(shadow.isPasteable()).isEqualTo(false);
    node.setText("Test");
    shadow.setTextSelectionSetable(true);
    shadow.addAction(AccessibilityNodeInfo.ACTION_SET_SELECTION);
    node.setTextSelection(0, 1);
    assertThat(shadow.getActions()).isEqualTo(AccessibilityNodeInfo.ACTION_SET_SELECTION);
    assertThat(shadow.getTextSelectionStart()).isEqualTo(0);
    assertThat(shadow.getTextSelectionEnd()).isEqualTo(1);
    AccessibilityWindowInfo window = ShadowAccessibilityWindowInfo.obtain();
    shadow.setAccessibilityWindowInfo(window);
    assertThat(node.getWindow()).isEqualTo(window);
    shadow.setAccessibilityWindowInfo(null);
    // Remove action was added in API 21
    node.removeAction(AccessibilityAction.ACTION_SET_SELECTION);
    shadow.setPasteable(true);
    shadow.setTextSelectionSetable(false);
    node.addAction(AccessibilityNodeInfo.ACTION_PASTE);
    assertThat(shadow.getActions()).isEqualTo(AccessibilityNodeInfo.ACTION_PASTE);
    node.setClickable(true);
    assertThat(shadow.isClickable()).isEqualTo(true);
    node.setClickable(false);
    shadow.setPasteable(false);
    node.removeAction(AccessibilityNodeInfo.ACTION_PASTE);
    node.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
    assertThat(shadow.getActions()).isEqualTo(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
    node.removeAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
  }

  @Test
  public void shouldRecordActionsPerformed() {
    node.setClickable(true);
    node.addAction(AccessibilityNodeInfo.ACTION_CLICK);
    shadow = shadowOf(node);
    shadow.setOnPerformActionListener(new ShadowAccessibilityNodeInfo.OnPerformActionListener() {
      @Override
      public boolean onPerformAccessibilityAction(int action, Bundle arguments) {
        if (action == AccessibilityNodeInfo.ACTION_CLICK) {
          return true;
        } else {
          return false;
        }
      }
    });

    boolean clickResult = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    assertThat(clickResult).isEqualTo(true);
    assertThat(shadow.getPerformedActions().isEmpty()).isEqualTo(false);
    assertThat(shadow.getPerformedActions().get(0)).isEqualTo(AccessibilityNodeInfo.ACTION_CLICK);
    boolean longClickResult = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
    assertThat(longClickResult).isEqualTo(false);
    assertThat(shadow.getPerformedActions().size()).isEqualTo(2);
    assertThat(shadow.getPerformedActions().get(1))
        .isEqualTo(AccessibilityNodeInfo.ACTION_LONG_CLICK);
  }

  @After
  public void tearDown() {
    ShadowAccessibilityNodeInfo.resetObtainedInstances();
    assertThat(ShadowAccessibilityNodeInfo.areThereUnrecycledNodes(true)).isEqualTo(false);
  }
}
