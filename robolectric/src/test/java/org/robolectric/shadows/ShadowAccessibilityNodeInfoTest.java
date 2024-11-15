package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Parcel;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityWindowInfo;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
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
  public void shouldHaveClonedCorrectly() {
    node.setAccessibilityFocused(true);
    node.setBoundsInParent(new Rect(0, 0, 100, 100));
    node.setContentDescription("test");
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      node.setTextEntryKey(true);
    }
    AccessibilityNodeInfo anotherNode = AccessibilityNodeInfo.obtain(node);
    assertThat(anotherNode).isEqualTo(node);
    assertThat(anotherNode.getContentDescription().toString()).isEqualTo("test");
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      assertThat(anotherNode.isTextEntryKey()).isTrue();
    }
  }

  @Test
  public void shouldWriteAndReadFromParcelCorrectly() {
    Parcel p = Parcel.obtain();
    node.setContentDescription("test");
    node.writeToParcel(p, 0);
    p.setDataPosition(0);
    AccessibilityNodeInfo anotherNode = AccessibilityNodeInfo.CREATOR.createFromParcel(p);
    assertThat(node.getContentDescription().toString())
        .isEqualTo(anotherNode.getContentDescription().toString());
  }

  @Test
  public void shouldNotHaveInfiniteLoopWithSameLoopedChildren() {
    node = AccessibilityNodeInfo.obtain();
    AccessibilityNodeInfo child = AccessibilityNodeInfo.obtain();
    shadowOf(node).addChild(child);
    shadowOf(child).addChild(node);
    AccessibilityNodeInfo anotherNode = AccessibilityNodeInfo.obtain(node);
    assertThat(node).isEqualTo(anotherNode);
  }

  @Test
  public void shouldNotHaveInfiniteLoopWithDifferentLoopedChildren() {
    node = AccessibilityNodeInfo.obtain();
    shadow = shadowOf(node);
    AccessibilityNodeInfo child1 = AccessibilityNodeInfo.obtain();
    shadow.addChild(child1);
    ShadowAccessibilityNodeInfo child1Shadow = shadowOf(child1);
    child1Shadow.addChild(node);
    AccessibilityNodeInfo anotherNode = ShadowAccessibilityNodeInfo.obtain();
    AccessibilityNodeInfo child2 = ShadowAccessibilityNodeInfo.obtain();
    child2.setText("test");
    ShadowAccessibilityNodeInfo child2Shadow = shadowOf(child2);
    ShadowAccessibilityNodeInfo anotherNodeShadow = shadowOf(anotherNode);
    anotherNodeShadow.addChild(child2);
    child2Shadow.addChild(anotherNode);
    assertThat(node).isNotEqualTo(anotherNode);
  }

  @Test
  public void shouldRecordFlagsProperly() {
    node = AccessibilityNodeInfo.obtain();
    node.setClickable(false);
    shadow = shadowOf(node);
    assertThat(node.isClickable()).isEqualTo(false);
    node.setText("Test");
    node.addAction(AccessibilityNodeInfo.ACTION_SET_SELECTION);
    node.setTextSelection(0, 1);
    assertThat(node.getActions()).isEqualTo(AccessibilityNodeInfo.ACTION_SET_SELECTION);
    assertThat(node.getTextSelectionStart()).isEqualTo(0);
    assertThat(node.getTextSelectionEnd()).isEqualTo(1);
    AccessibilityWindowInfo window = AccessibilityWindowInfo.obtain();
    shadow.setAccessibilityWindowInfo(window);
    assertThat(node.getWindow()).isEqualTo(window);
    shadow.setAccessibilityWindowInfo(null);
    // Remove action was added in API 21
    node.removeAction(AccessibilityAction.ACTION_SET_SELECTION);
    node.addAction(AccessibilityNodeInfo.ACTION_PASTE);
    assertThat(node.getActions()).isEqualTo(AccessibilityNodeInfo.ACTION_PASTE);
    node.setClickable(true);
    assertThat(node.isClickable()).isEqualTo(true);
    node.setClickable(false);
    node.removeAction(AccessibilityNodeInfo.ACTION_PASTE);
    node.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
    assertThat(node.getActions()).isEqualTo(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
    node.removeAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
  }

  @Test
  public void shouldRecordActionsPerformed() {
    node.setClickable(true);
    node.addAction(AccessibilityNodeInfo.ACTION_CLICK);
    shadow = shadowOf(node);
    shadow.setOnPerformActionListener(
        (action, arguments) -> action == AccessibilityNodeInfo.ACTION_CLICK);

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

  @Test
  public void equalsTest_unrelatedNodesAreUnequal() {
    AccessibilityNodeInfo nodeA = AccessibilityNodeInfo.obtain();
    AccessibilityNodeInfo nodeB = AccessibilityNodeInfo.obtain();
    nodeA.setText("test");
    nodeB.setText("test");

    assertThat(nodeA).isNotEqualTo(nodeB);
  }

  @Test
  public void equalsTest_nodesFromTheSameViewAreEqual() {
    View view = new View(ApplicationProvider.getApplicationContext());
    AccessibilityNodeInfo nodeA = AccessibilityNodeInfo.obtain(view);
    AccessibilityNodeInfo nodeB = AccessibilityNodeInfo.obtain(view);
    nodeA.setText("tomato");
    nodeB.setText("tomatoe");

    assertThat(nodeA).isEqualTo(nodeB);
  }

  @Test
  public void equalsTest_nodesFromDifferentViewsAreNotEqual() {
    View viewA = new View(ApplicationProvider.getApplicationContext());
    View viewB = new View(ApplicationProvider.getApplicationContext());
    AccessibilityNodeInfo nodeA = AccessibilityNodeInfo.obtain(viewA);
    AccessibilityNodeInfo nodeB = AccessibilityNodeInfo.obtain(viewB);
    nodeA.setText("test");
    nodeB.setText("test");

    assertThat(nodeA).isNotEqualTo(nodeB);
  }

  @Test
  public void equalsTest_nodeIsEqualToItsClone_evenWhenModified() {
    node = AccessibilityNodeInfo.obtain();
    AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(node);
    clone.setText("test");

    assertThat(node).isEqualTo(clone);
  }

  @Test
  public void shouldCloneExtrasCorrectly() {
    node.getExtras().putString("key", "value");

    AccessibilityNodeInfo nodeCopy = AccessibilityNodeInfo.obtain(node);

    assertThat(nodeCopy.getExtras().getString("key")).isEqualTo("value");
  }

  @Config(minSdk = N)
  @Test
  public void shouldClonePreserveImportance() {
    node.setImportantForAccessibility(true);

    AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(node);

    assertThat(clone.isImportantForAccessibility()).isTrue();
  }

  @Config(minSdk = O)
  @Test
  public void clone_preservesHintText() {
    String hintText = "tooltip hint";
    node.setHintText(hintText);

    AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(node);

    assertThat(clone.getHintText().toString()).isEqualTo(hintText);
  }

  @Config(minSdk = P)
  @Test
  public void clone_preservesTooltipText() {
    String tooltipText = "tooltip text";
    node.setTooltipText(tooltipText);

    AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(node);

    assertThat(clone.getTooltipText().toString()).isEqualTo(tooltipText);
  }

  @Test
  @Config(minSdk = P)
  public void clone_preservesPaneTitle() {
    String title = "pane title";
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    node.setPaneTitle(title);

    AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(node);

    assertThat(clone.getPaneTitle().toString()).isEqualTo(title);
  }

  @Test
  @Config(minSdk = R)
  public void clone_preservesStateDescription() {
    String description = "description";
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    node.setStateDescription(description);

    AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(node);

    assertThat(clone.getStateDescription().toString()).isEqualTo(description);
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void clone_preservesContainerTitle() {
    String title = "container title";
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    node.setContainerTitle(title);

    AccessibilityNodeInfo clone = AccessibilityNodeInfo.obtain(node);

    assertThat(clone.getContainerTitle().toString()).isEqualTo(title);
  }

  @Test
  public void testGetBoundsInScreen() {
    AccessibilityNodeInfo root = AccessibilityNodeInfo.obtain();
    Rect expected = new Rect(0, 0, 100, 100);
    root.setBoundsInScreen(expected);
    Rect actual = new Rect();
    root.getBoundsInScreen(actual);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @Config(minSdk = P)
  public void testIsHeading() {
    AccessibilityNodeInfo root = AccessibilityNodeInfo.obtain();
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    shadowOf(root).addChild(node);
    node.setHeading(true);
    assertThat(node.isHeading()).isTrue();
    assertThat(root.getChild(0).isHeading()).isTrue();
  }

  @Test
  public void getSourceNodeId_notZero() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    View view = activity.findViewById(android.R.id.content);
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(view);
    assertThat(node.getSourceNodeId()).isNotEqualTo(0);
  }

  @Test
  public void testConstructor() {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    assertThat(node.getWindowId())
        .isEqualTo(RuntimeEnvironment.getApiLevel() >= O ? -1 : Integer.MAX_VALUE);
    if (RuntimeEnvironment.getApiLevel() >= O) {
      // This constant does not exists pre-O.
      assertThat(node.getSourceNodeId()).isEqualTo(AccessibilityNodeInfo.UNDEFINED_NODE_ID);
    }
  }

  @Test
  public void obtainWithNode_afterSetSealed() {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    node.setSealed(true);
    assertThat(node.isSealed()).isTrue();
    AccessibilityNodeInfo node2 = AccessibilityNodeInfo.obtain(node);
    assertThat(node2.isSealed()).isTrue();
  }

  @After
  public void tearDown() {
    ShadowAccessibilityNodeInfo.resetObtainedInstances();
    assertThat(ShadowAccessibilityNodeInfo.areThereUnrecycledNodes(true)).isEqualTo(false);
  }
}
