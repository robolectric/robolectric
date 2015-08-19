package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import android.os.Parcel;
import android.view.accessibility.AccessibilityNodeInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.ShadowExtractor;

@RunWith(TestRunners.WithDefaults.class)
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
  public void ShouldHaveClonedCorrectly() {
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
  public void shouldRecordFlagsProperly() {
    node = AccessibilityNodeInfo.obtain();
    node.setClickable(false);
    shadow = (ShadowAccessibilityNodeInfo) ShadowExtractor.extract(node);
    shadow.setPasteable(false);
    assertThat(shadow.isClickable()).isEqualTo(false);
    assertThat(shadow.isPasteable()).isEqualTo(false);
    node.setText("Test");
    shadow.setTextSelectionSetable(true);
    node.setTextSelection(0, 1);
    assertThat(shadow.getActions()).isEqualTo(AccessibilityNodeInfo.ACTION_SET_SELECTION);
    assertThat(shadow.getTextSelectionStart()).isEqualTo(0);
    assertThat(shadow.getTextSelectionEnd()).isEqualTo(1);
    shadow.setPasteable(true);
    shadow.setTextSelectionSetable(false);
    assertThat(shadow.getActions()).isEqualTo(AccessibilityNodeInfo.ACTION_PASTE);
    node.setClickable(true);
    assertThat(shadow.isClickable()).isEqualTo(true);
  }

  @Test
  public void shouldRecordActionsPerformed() {
    node.setClickable(true);
    shadow = (ShadowAccessibilityNodeInfo) ShadowExtractor.extract(node);
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
