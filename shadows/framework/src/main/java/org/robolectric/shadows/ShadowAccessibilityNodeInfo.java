package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.versioning.AndroidVersions.U;

/**
 * Properties of {@link android.view.accessibility.AccessibilityNodeInfo} that are normally locked
 * may be changed using test APIs.
 *
 * <p>Calls to {@code obtain()} and {@code recycle()} are tracked to help spot bugs.
 */
@Implements(AccessibilityNodeInfo.class)
public class ShadowAccessibilityNodeInfo {

  private static int sAllocationCount = 0;

  /**
   * Uniquely identifies the origin of the AccessibilityNodeInfo for equality testing. Two instances
   * that come from the same node info should have the same ID.
   */
  private long mOriginNodeId;

  private List<AccessibilityNodeInfo> children;

  private List<Pair<Integer, Bundle>> performedActionAndArgsList;

  private AccessibilityNodeInfo parent;

  private AccessibilityNodeInfo labelFor;

  private AccessibilityNodeInfo labeledBy;

  private View view;

  private CharSequence text;

  private boolean refreshReturnValue = true;

  private AccessibilityWindowInfo accessibilityWindowInfo;

  private AccessibilityNodeInfo traversalAfter; // 22

  private AccessibilityNodeInfo traversalBefore; // 22

  private OnPerformActionListener actionListener;

  private static boolean queryFromAppProcessWasEnabled;

  @RealObject private AccessibilityNodeInfo realAccessibilityNodeInfo;

  @ReflectorObject AccessibilityNodeInfoReflector accessibilityNodeInfoReflector;

  @Implementation
  protected static AccessibilityNodeInfo obtain(AccessibilityNodeInfo info) {
    if (useRealAni()) {
      return reflector(AccessibilityNodeInfoReflector.class).obtain(info);
    }
    // We explicitly avoid allocating the AccessibilityNodeInfo from the actual pool by using
    // the private constructor. Not doing so affects test suites which use both shadow and
    // non-shadow objects.
    final AccessibilityNodeInfo newInfo;
    if (RuntimeEnvironment.getApiLevel() >= R) {
      newInfo = reflector(AccessibilityNodeInfoReflector.class).newInstance(info);
    } else {
      newInfo = Shadow.newInstanceOf(AccessibilityNodeInfo.class);
      reflector(AccessibilityNodeInfoReflector.class, newInfo).init(info);
    }

    final ShadowAccessibilityNodeInfo newShadow = Shadow.extract(newInfo);
    final ShadowAccessibilityNodeInfo shadowInfo = Shadow.extract(info);
    newShadow.mOriginNodeId = shadowInfo.mOriginNodeId;
    newShadow.text = shadowInfo.text;
    newShadow.performedActionAndArgsList = shadowInfo.performedActionAndArgsList;
    newShadow.parent = shadowInfo.parent;
    newShadow.labelFor = (shadowInfo.labelFor == null) ? null : obtain(shadowInfo.labelFor);
    newShadow.labeledBy = (shadowInfo.labeledBy == null) ? null : obtain(shadowInfo.labeledBy);
    newShadow.view = shadowInfo.view;
    newShadow.actionListener = shadowInfo.actionListener;

    if (shadowInfo.children != null) {
      newShadow.children = new ArrayList<>();
      newShadow.children.addAll(shadowInfo.children);
    } else {
      newShadow.children = null;
    }

    newShadow.refreshReturnValue = shadowInfo.refreshReturnValue;

    if (getApiLevel() >= LOLLIPOP_MR1) {
      newShadow.traversalAfter =
          (shadowInfo.traversalAfter == null) ? null : obtain(shadowInfo.traversalAfter);
      newShadow.traversalBefore =
          (shadowInfo.traversalBefore == null) ? null : obtain(shadowInfo.traversalBefore);
    }
    if (shadowInfo.accessibilityWindowInfo != null) {
      newShadow.accessibilityWindowInfo =
          ShadowAccessibilityWindowInfo.obtain(shadowInfo.accessibilityWindowInfo);
    }

    ShadowAccessibilityNodeInfo.sAllocationCount++;
    if (shadowInfo.mOriginNodeId == 0) {
      shadowInfo.mOriginNodeId = sAllocationCount;
    }
    return newInfo;
  }

  @Implementation
  protected static AccessibilityNodeInfo obtain(View view) {
    if (useRealAni()) {
      return reflector(AccessibilityNodeInfoReflector.class).obtain(view);
    }
    // Call the constructor directly to avoid using the object pool.
    final AccessibilityNodeInfo obtainedInstance =
        ReflectionHelpers.callConstructor(AccessibilityNodeInfo.class);
    obtainedInstance.setSource(view);
    initShadow(obtainedInstance);
    return obtainedInstance;
  }

  @Implementation
  protected static AccessibilityNodeInfo obtain(View root, int virtualDescendantId) {
    if (useRealAni()) {
      return reflector(AccessibilityNodeInfoReflector.class).obtain(root, virtualDescendantId);
    }

    // Call the constructor directly to avoid using the object pool.
    final AccessibilityNodeInfo obtainedInstance =
        ReflectionHelpers.callConstructor(AccessibilityNodeInfo.class);
    obtainedInstance.setSource(root, virtualDescendantId);
    initShadow(obtainedInstance);
    return obtainedInstance;
  }

  @Implementation
  protected static AccessibilityNodeInfo obtain() {
    if (useRealAni()) {
      return reflector(AccessibilityNodeInfoReflector.class).obtain();
    }
    AccessibilityNodeInfo obtainedInstance =
        ReflectionHelpers.callConstructor(AccessibilityNodeInfo.class);
    initShadow(obtainedInstance);
    // TODO(hoisie): Remove this hack. It was added many years ago for and is highly inconsistent
    // with real Android. It is a broken and arbitrary way to make ANI objects not be
    // considered equal to each other.
    ShadowAccessibilityNodeInfo shadowObtained = Shadow.extract(obtainedInstance);
    shadowObtained.view = new View(RuntimeEnvironment.getApplication().getApplicationContext());
    return obtainedInstance;
  }

  private static void initShadow(AccessibilityNodeInfo obtainedInstance) {
    final ShadowAccessibilityNodeInfo shadowObtained = Shadow.extract(obtainedInstance);

    /*
     * We keep a separate list of actions for each object newly obtained
     * from a view, and perform a shallow copy during getClone. That way the
     * list of actions performed contains all actions performed on the view
     * by the tree of nodes initialized from it. Note that initializing two
     * nodes with the same view will not merge the two lists, as so the list
     * of performed actions will not contain all actions performed on the
     * underlying view.
     */
    shadowObtained.performedActionAndArgsList = new ArrayList<>();

    sAllocationCount++;
    if (shadowObtained.mOriginNodeId == 0) {
      shadowObtained.mOriginNodeId = sAllocationCount;
    }
  }

  @Resetter
  public static void resetObtainedInstances() {
    queryFromAppProcessWasEnabled = false;
  }

  @Implementation
  protected void recycle() {
    if (useRealAni()) {
      accessibilityNodeInfoReflector.recycle();
      return;
    }

    if (labelFor != null) {
      labelFor.recycle();
    }

    if (labeledBy != null) {
      labeledBy.recycle();
    }
    if (getApiLevel() >= LOLLIPOP_MR1) {
      if (traversalAfter != null) {
        traversalAfter.recycle();
      }

      if (traversalBefore != null) {
        traversalBefore.recycle();
      }
    }
  }

  @Implementation
  protected int getChildCount() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getChildCount();
    }
    if (children == null) {
      return 0;
    }

    return children.size();
  }

  @Implementation
  protected AccessibilityNodeInfo getChild(int index) {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getChild(index);
    }
    if (children == null) {
      return null;
    }

    final AccessibilityNodeInfo child = children.get(index);
    if (child == null) {
      return null;
    }

    return obtain(child);
  }

  @Implementation
  protected AccessibilityNodeInfo getParent() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getParent();
    }
    if (parent == null) {
      return null;
    }

    return obtain(parent);
  }

  @Implementation
  protected boolean refresh() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.refresh();
    }
    return refreshReturnValue;
  }

  public void setRefreshReturnValue(boolean refreshReturnValue) {
    checkRealAniDisabled();
    this.refreshReturnValue = refreshReturnValue;
  }

  @Implementation
  protected void setText(CharSequence t) {
    // Call the original method to set the underlying fields.
    accessibilityNodeInfoReflector.setText(t);
    if (!useRealAni()) {
      text = t;
    }
  }

  @Implementation
  protected CharSequence getText() {
    if (useRealAni() || text == null) {
      return accessibilityNodeInfoReflector.getText();
    }
    return text;
  }

  @Implementation
  protected AccessibilityNodeInfo getLabelFor() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getLabelFor();
    }
    if (labelFor == null) {
      return null;
    }

    return obtain(labelFor);
  }

  public void setLabelFor(AccessibilityNodeInfo info) {
    checkRealAniDisabled();

    if (labelFor != null) {
      labelFor.recycle();
    }

    labelFor = obtain(info);
  }

  @Implementation
  protected AccessibilityNodeInfo getLabeledBy() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getLabeledBy();
    }
    if (labeledBy == null) {
      return null;
    }

    return obtain(labeledBy);
  }

  public void setLabeledBy(AccessibilityNodeInfo info) {
    checkRealAniDisabled();
    if (labeledBy != null) {
      labeledBy.recycle();
    }

    labeledBy = obtain(info);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected AccessibilityNodeInfo getTraversalAfter() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getTraversalAfter();
    }
    if (traversalAfter == null) {
      return null;
    }

    return obtain(traversalAfter);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected void setTraversalAfter(View view, int virtualDescendantId) {
    if (useRealAni()) {
      accessibilityNodeInfoReflector.setTraversalAfter(view, virtualDescendantId);
      return;
    }
    if (this.traversalAfter != null) {
      this.traversalAfter.recycle();
    }

    this.traversalAfter = obtain(view);
  }

  /**
   * Sets the view whose node is visited after this one in accessibility traversal.
   *
   * <p>This may be useful for configuring traversal order in tests before the corresponding views
   * have been inflated.
   *
   * @param info The previous node.
   * @see #getTraversalAfter()
   */
  public void setTraversalAfter(AccessibilityNodeInfo info) {
    checkRealAniDisabled();
    if (this.traversalAfter != null) {
      this.traversalAfter.recycle();
    }

    this.traversalAfter = obtain(info);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected AccessibilityNodeInfo getTraversalBefore() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getTraversalBefore();
    }
    if (traversalBefore == null) {
      return null;
    }

    return obtain(traversalBefore);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected void setTraversalBefore(View info, int virtualDescendantId) {
    if (useRealAni()) {
      accessibilityNodeInfoReflector.setTraversalBefore(info, virtualDescendantId);
      return;
    }
    if (this.traversalBefore != null) {
      this.traversalBefore.recycle();
    }

    this.traversalBefore = obtain(info);
  }

  /**
   * Sets the view before whose node this one should be visited during traversal.
   *
   * <p>This may be useful for configuring traversal order in tests before the corresponding views
   * have been inflated.
   *
   * @param info The view providing the preceding node.
   * @see #getTraversalBefore()
   */
  public void setTraversalBefore(AccessibilityNodeInfo info) {
    checkRealAniDisabled();
    if (this.traversalBefore != null) {
      this.traversalBefore.recycle();
    }

    this.traversalBefore = obtain(info);
  }

  @Implementation
  protected void setSource(View source) {
    accessibilityNodeInfoReflector.setSource(source);
    if (!useRealAni()) {
      this.view = source;
    }
  }

  @Implementation
  protected void setSource(View root, int virtualDescendantId) {
    accessibilityNodeInfoReflector.setSource(root, virtualDescendantId);
    if (!useRealAni()) {
      this.view = root;
    }
  }

  @Implementation
  protected AccessibilityWindowInfo getWindow() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getWindow();
    }
    return accessibilityWindowInfo;
  }

  /** Returns the id of the window from which the info comes. */
  @Implementation
  protected int getWindowId() {
    if (useRealAni() || accessibilityWindowInfo == null) {
      return accessibilityNodeInfoReflector.getWindowId();
    }
    return accessibilityWindowInfo.getId();
  }

  public void setAccessibilityWindowInfo(AccessibilityWindowInfo info) {
    checkRealAniDisabled();
    accessibilityWindowInfo = info;
  }

  @Implementation
  protected boolean performAction(int action) {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.performAction(action);
    }
    return performAction(action, null);
  }

  @Implementation
  protected boolean performAction(int action, Bundle arguments) {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.performAction(action, arguments);
    }
    if (performedActionAndArgsList == null) {
      performedActionAndArgsList = new ArrayList<>();
    }

    performedActionAndArgsList.add(new Pair<>(action, arguments));
    return actionListener == null || actionListener.onPerformAccessibilityAction(action, arguments);
  }

  /**
   * Equality check based on reference equality of the Views from which these instances were
   * created, or the equality of their assigned IDs.
   */
  @SuppressWarnings("EqualsHashCode")
  @Implementation
  @Override
  public boolean equals(Object object) {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.equals(object);
    }
    if (!(object instanceof AccessibilityNodeInfo)) {
      return false;
    }

    final AccessibilityNodeInfo info = (AccessibilityNodeInfo) object;
    final ShadowAccessibilityNodeInfo otherShadow = Shadow.extract(info);

    if (this.view != null) {
      return this.view == otherShadow.view;
    }
    if (this.mOriginNodeId != 0) {
      return this.mOriginNodeId == otherShadow.mOriginNodeId;
    }
    throw new IllegalStateException("Node has neither an ID nor View");
  }

  /**
   * Add a child node to this one. Also initializes the parent field of the child.
   *
   * @param child The node to be added as a child.
   */
  public void addChild(AccessibilityNodeInfo child) {
    checkRealAniDisabled();
    if (children == null) {
      children = new ArrayList<>();
    }

    children.add(child);
    ShadowAccessibilityNodeInfo shadowAccessibilityNodeInfo = Shadow.extract(child);
    shadowAccessibilityNodeInfo.parent = realAccessibilityNodeInfo;
  }

  @Implementation
  protected void addChild(View child) {
    accessibilityNodeInfoReflector.addChild(child);
    if (!useRealAni()) {
      AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(child);
      addChild(node);
    }
  }

  @Implementation
  protected void addChild(View root, int virtualDescendantId) {
    accessibilityNodeInfoReflector.addChild(root, virtualDescendantId);
    if (!useRealAni()) {
      AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(root, virtualDescendantId);
      addChild(node);
    }
  }

  /**
   * @return The list of arguments for the various calls to performAction. Unmodifiable.
   */
  public List<Integer> getPerformedActions() {
    checkRealAniDisabled();
    if (performedActionAndArgsList == null) {
      performedActionAndArgsList = new ArrayList<>();
    }

    // Here we take the actions out of the pairs and stick them into a separate LinkedList to return
    List<Integer> actionsOnly = new ArrayList<>();
    Iterator<Pair<Integer, Bundle>> iter = performedActionAndArgsList.iterator();
    while (iter.hasNext()) {
      actionsOnly.add(iter.next().first);
    }

    return Collections.unmodifiableList(actionsOnly);
  }

  /**
   * @return The list of arguments for the various calls to performAction. Unmodifiable.
   */
  public List<Pair<Integer, Bundle>> getPerformedActionsWithArgs() {
    checkRealAniDisabled();
    if (performedActionAndArgsList == null) {
      performedActionAndArgsList = new ArrayList<>();
    }
    return Collections.unmodifiableList(performedActionAndArgsList);
  }

  /**
   * After {@link AccessibilityNodeInfo#setQueryFromAppProcessEnabled(View, boolean)} is called, we
   * will have direct access to the real {@link AccessibilityNodeInfo} hierarchy, so we want all
   * future interactions with ANI to use the real object.
   */
  @Implementation(minSdk = U.SDK_INT)
  protected void setQueryFromAppProcessEnabled(View view, boolean enabled) {
    accessibilityNodeInfoReflector.setQueryFromAppProcessEnabled(view, enabled);
    if (enabled) {
      queryFromAppProcessWasEnabled = true;
    }
  }

  /**
   * Configure the return result of an action if it is performed
   *
   * @param listener The listener.
   */
  public void setOnPerformActionListener(OnPerformActionListener listener) {
    checkRealAniDisabled();
    actionListener = listener;
  }

  public interface OnPerformActionListener {
    boolean onPerformAccessibilityAction(int action, Bundle arguments);
  }

  @ForType(AccessibilityNodeInfo.class)
  interface AccessibilityNodeInfoReflector {
    @Direct
    @Static
    AccessibilityNodeInfo obtain(AccessibilityNodeInfo info);

    @Direct
    @Static
    AccessibilityNodeInfo obtain(View view);

    @Direct
    @Static
    AccessibilityNodeInfo obtain();

    @Direct
    @Static
    AccessibilityNodeInfo obtain(View root, int virtualDescendantId);

    @Direct
    void recycle();

    @Direct
    int getChildCount();

    @Direct
    AccessibilityNodeInfo getChild(int index);

    @Direct
    AccessibilityNodeInfo getParent();

    @Direct
    boolean refresh();

    @Direct
    void setText(CharSequence t);

    @Direct
    CharSequence getText();

    @Direct
    AccessibilityNodeInfo getLabelFor();

    @Direct
    AccessibilityNodeInfo getLabeledBy();

    @Direct
    AccessibilityNodeInfo getTraversalAfter();

    @Direct
    void setTraversalAfter(View view, int virtualDescendantId);

    @Direct
    AccessibilityNodeInfo getTraversalBefore();

    @Direct
    void setTraversalBefore(View info, int virtualDescendantId);

    @Direct
    void setSource(View source);

    @Direct
    void setSource(View root, int virtualDescendantId);

    @Direct
    AccessibilityWindowInfo getWindow();

    @Direct
    int getWindowId();

    @Direct
    boolean performAction(int action);

    @Direct
    boolean performAction(int action, Bundle arguments);

    @Override
    @Direct
    boolean equals(Object object);

    @Override
    @Direct
    int hashCode();

    @Direct
    void addChild(View child);

    @Direct
    void addChild(View child, int id);

    @Override
    @Direct
    String toString();

    @Constructor
    AccessibilityNodeInfo newInstance(AccessibilityNodeInfo other);

    void init(AccessibilityNodeInfo other);

    @Direct
    void setQueryFromAppProcessEnabled(View view, boolean enabled);
  }

  static boolean useRealAni() {
    return queryFromAppProcessWasEnabled
        || Boolean.parseBoolean(System.getProperty("robolectric.useRealAni", "false"));
  }

  static void checkRealAniDisabled() {
    Preconditions.checkState(
        !queryFromAppProcessWasEnabled,
        "This API is not supported after a call to"
            + " AccessibilityNodeInfo#setQueryFromAppProcessEnabled.");
    boolean useRealAni =
        Boolean.parseBoolean(System.getProperty("robolectric.useRealAni", "false"));
    Preconditions.checkState(
        !useRealAni, "This API is not supported when 'robolectric.useRealAni' is true");
  }
}
