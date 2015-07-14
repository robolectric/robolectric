package org.robolectric.shadows.support.v4;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.view.accessibility.AccessibilityNodeInfo;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowAccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadow of {@link android.support.v4.view.accessibility.AccessibilityNodeInfoCompat}.
 */
@Implements(AccessibilityNodeInfoCompat.class)
public class ShadowAccessibilityNodeInfoCompat {
  @RealObject
  AccessibilityNodeInfoCompat realObject;

  @Implementation
  public static AccessibilityNodeInfoCompat obtain(AccessibilityNodeInfoCompat compat) {
    final AccessibilityNodeInfo newInfo =
        AccessibilityNodeInfo.obtain((AccessibilityNodeInfo) compat.getInfo());
    return new AccessibilityNodeInfoCompat(newInfo);
  }

  @Implementation
  public static AccessibilityNodeInfoCompat obtain() {
    return new AccessibilityNodeInfoCompat(AccessibilityNodeInfo.obtain());
  }

  /**
   * Check for leaked objects that were {@code obtain}ed but never {@code recycle}d.
   * @param printUnrecycledNodesToSystemErr - if true, stack traces of calls to {@code obtain}
   * that lack matching calls to {@code recycle} are dumped to System.err.
   *
   * @return {@code true} if there are unrecycled nodes
   */
  public static boolean areThereUnrecycledNodes(boolean printUnrecycledNodesToSystemErr) {
    return ShadowAccessibilityNodeInfo.areThereUnrecycledNodes(printUnrecycledNodesToSystemErr);
  }

  /**
   * Clear list of obtained instance objects. {@code areThereUnrecycledNodes} will always
   * return false if called immediately afterwards.
   */
  public static void resetObtainedInstances() {
    ShadowAccessibilityNodeInfo.resetObtainedInstances();
  }

  public boolean isPasteable() {
    ShadowAccessibilityNodeInfo info =
        (ShadowAccessibilityNodeInfo) ShadowExtractor.extract(realObject.getInfo());
    return info.isPasteable();
  }

  public void setPasteable(boolean isPasteable) {
    ShadowAccessibilityNodeInfo info =
        (ShadowAccessibilityNodeInfo) ShadowExtractor.extract(realObject.getInfo());
    info.setPasteable(isPasteable);
  }

  @Implementation
  public int getActions() {
    final AccessibilityNodeInfo info = (AccessibilityNodeInfo) realObject.getInfo();
    return info.getActions();
  }

  @Implementation
  public List<AccessibilityNodeInfoCompat.AccessibilityActionCompat> getActionList() {
    /* Robolectric doesn't handle the AccessibilityNodeInfo.AccessibilityAction. Make do. */
    List<AccessibilityNodeInfoCompat.AccessibilityActionCompat> result = new ArrayList<>();
    int actionsInt = getActions();
    int mask = 1;
    while (actionsInt != 0) {
      if ((actionsInt & mask) != 0) {
        actionsInt &= ~mask;
        result.add(new AccessibilityActionCompat(mask, String.format("Action_%d", mask)));
      }

      mask = mask << 1;
    }

    return result;
  }

  /**
   * Add a child node to this one. Also initializes the parent field of the child.
   *
   * @param child The node to be added as a child.
   */
  public void addChild(AccessibilityNodeInfoCompat child) {
    final AccessibilityNodeInfo info = (AccessibilityNodeInfo) realObject.getInfo();
    final ShadowAccessibilityNodeInfo shadowInfo =
        ((ShadowAccessibilityNodeInfo) ShadowExtractor.extract(info));
    shadowInfo.addChild((AccessibilityNodeInfo) child.getInfo());
  }

  /**
   * Shadow of {@link
   * android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat}
   */
  @Implements(AccessibilityActionCompat.class)
  public static final class ShadowAccessibilityActionCompat {
    private int id;
    private CharSequence label;

    public void __constructor__(int id, CharSequence label) {
      this.id = id;
      this.label = label;
    }

    @Implementation
    public int getId() {
      return id;
    }

    @Implementation
    public CharSequence getLabel() {
      return label;
    }
  }
}
