package org.robolectric.shadows;

import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityRecord}.
 */
@Implements(AccessibilityRecord.class)
public class ShadowAccessibilityRecord {

  @RealObject private AccessibilityRecord realRecord;

  public static final int NO_VIRTUAL_ID = -1;

  private View sourceRoot;
  private int virtualDescendantId;
  private AccessibilityNodeInfo sourceNode;
  private int windowId = -1;

  @Implementation
  protected void setSource(View root, int virtualDescendantId) {
    this.sourceRoot = root;
    this.virtualDescendantId = virtualDescendantId;
    Shadow.directlyOn(realRecord, AccessibilityRecord.class, "setSource",
        ClassParameter.from(View.class, root),
        ClassParameter.from(Integer.TYPE, virtualDescendantId));
  }

  @Implementation
  protected void setSource(View root) {
    this.sourceRoot = root;
    this.virtualDescendantId = NO_VIRTUAL_ID;
    Shadow.directlyOn(realRecord, AccessibilityRecord.class, "setSource",
        ClassParameter.from(View.class, root));
  }

  /**
   * Sets the {@link AccessibilityNodeInfo} of the event source.
   *
   * @param node The node to set
   */
  public void setSourceNode(AccessibilityNodeInfo node) {
    sourceNode = node;
  }

  /**
   * Returns the {@link AccessibilityNodeInfo} of the event source or {@code null} if there is none.
   */
  @Implementation
  protected AccessibilityNodeInfo getSource() {
    if (sourceNode == null) {
      return null;
    }
    return AccessibilityNodeInfo.obtain(sourceNode);
  }

  /**
   * Sets the id of the window from which the event comes.
   *
   * @param id The id to set
   */
  public void setWindowId(int id) {
    windowId = id;
  }

  /** Returns the id of the window from which the event comes. */
  @Implementation
  protected int getWindowId() {
    return windowId;
  }

  public View getSourceRoot() {
    return sourceRoot;
  }

  public int getVirtualDescendantId() {
    return virtualDescendantId;
  }
}
