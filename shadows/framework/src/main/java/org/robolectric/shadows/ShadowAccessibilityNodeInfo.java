package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Properties of {@link android.view.accessibility.AccessibilityNodeInfo} that are normally locked
 * may be changed using test APIs.
 *
 * <p>Calls to {@code obtain()} and {@code recycle()} are tracked to help spot bugs.
 */
@Implements(AccessibilityNodeInfo.class)
public class ShadowAccessibilityNodeInfo {
  // Map of obtained instances of the class along with stack traces of how they were obtained
  private static final Map<StrictEqualityNodeWrapper, StackTraceElement[]> obtainedInstances =
      new HashMap<>();

  private static final SparseArray<StrictEqualityNodeWrapper> orderedInstances =
      new SparseArray<>();

  public static final Parcelable.Creator<AccessibilityNodeInfo> CREATOR =
      new Parcelable.Creator<AccessibilityNodeInfo>() {

        @Override
        public AccessibilityNodeInfo createFromParcel(Parcel source) {
          return obtain(orderedInstances.get(source.readInt()).mInfo);
        }

        @Override
        public AccessibilityNodeInfo[] newArray(int size) {
          return new AccessibilityNodeInfo[size];
        }
      };

  private static int sAllocationCount = 0;

  private static final int PASTEABLE_MASK = 0x00000040;

  private static final int TEXT_SELECTION_SETABLE_MASK = 0x00000100;

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

  @RealObject private AccessibilityNodeInfo realAccessibilityNodeInfo;

  @ReflectorObject AccessibilityNodeInfoReflector accessibilityNodeInfoReflector;

  @Implementation
  protected void __constructor__() {
    reflector(AccessibilityNodeInfoReflector.class).setCreator(ShadowAccessibilityNodeInfo.CREATOR);
    Shadow.invokeConstructor(AccessibilityNodeInfo.class, realAccessibilityNodeInfo);
  }

  @Implementation
  protected static AccessibilityNodeInfo obtain(AccessibilityNodeInfo info) {
    if (useRealAni()) {
      return reflector(AccessibilityNodeInfoReflector.class).obtain(info);
    }
    final ShadowAccessibilityNodeInfo shadowInfo = Shadow.extract(info);
    final AccessibilityNodeInfo obtainedInstance = shadowInfo.getClone();

    sAllocationCount++;
    if (shadowInfo.mOriginNodeId == 0) {
      shadowInfo.mOriginNodeId = sAllocationCount;
    }
    StrictEqualityNodeWrapper wrapper = new StrictEqualityNodeWrapper(obtainedInstance);
    obtainedInstances.put(wrapper, Thread.currentThread().getStackTrace());
    orderedInstances.put(sAllocationCount, wrapper);
    return obtainedInstance;
  }

  @Implementation
  protected static AccessibilityNodeInfo obtain(View view) {
    if (useRealAni()) {
      return reflector(AccessibilityNodeInfoReflector.class).obtain(view);
    }
    // We explicitly avoid allocating the AccessibilityNodeInfo from the actual pool by using the
    // private constructor. Not doing so affects test suites which use both shadow and
    // non-shadow objects.
    final AccessibilityNodeInfo obtainedInstance =
        ReflectionHelpers.callConstructor(AccessibilityNodeInfo.class);
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

    shadowObtained.view = view;
    sAllocationCount++;
    if (shadowObtained.mOriginNodeId == 0) {
      shadowObtained.mOriginNodeId = sAllocationCount;
    }
    StrictEqualityNodeWrapper wrapper = new StrictEqualityNodeWrapper(obtainedInstance);
    obtainedInstances.put(wrapper, Thread.currentThread().getStackTrace());
    orderedInstances.put(sAllocationCount, wrapper);
    return obtainedInstance;
  }

  @Implementation
  protected static AccessibilityNodeInfo obtain() {
    if (useRealAni()) {
      return reflector(AccessibilityNodeInfoReflector.class).obtain();
    }
    return obtain(new View(RuntimeEnvironment.getApplication().getApplicationContext()));
  }

  @Implementation
  protected static AccessibilityNodeInfo obtain(View root, int virtualDescendantId) {
    if (useRealAni()) {
      return reflector(AccessibilityNodeInfoReflector.class).obtain(root, virtualDescendantId);
    }
    AccessibilityNodeInfo node = obtain(root);
    return node;
  }

  /**
   * Check for leaked objects that were {@code obtain}ed but never {@code recycle}d.
   *
   * @param printUnrecycledNodesToSystemErr - if true, stack traces of calls to {@code obtain} that
   *     lack matching calls to {@code recycle} are dumped to System.err.
   * @return {@code true} if there are unrecycled nodes
   */
  public static boolean areThereUnrecycledNodes(boolean printUnrecycledNodesToSystemErr) {
    if (printUnrecycledNodesToSystemErr) {
      for (final StrictEqualityNodeWrapper wrapper : obtainedInstances.keySet()) {
        final ShadowAccessibilityNodeInfo shadow = Shadow.extract(wrapper.mInfo);

        System.err.printf(
            "Leaked contentDescription = %s. Stack trace:%n",
            shadow.realAccessibilityNodeInfo.getContentDescription());
        for (final StackTraceElement stackTraceElement : obtainedInstances.get(wrapper)) {
          System.err.println(stackTraceElement.toString());
        }
      }
    }

    return (obtainedInstances.size() != 0);
  }

  /**
   * Clear list of obtained instance objects. {@code areThereUnrecycledNodes} will always return
   * false if called immediately afterwards.
   */
  @Resetter
  public static void resetObtainedInstances() {
    obtainedInstances.clear();
    orderedInstances.clear();
  }

  @Implementation
  protected void recycle() {
    if (useRealAni()) {
      accessibilityNodeInfoReflector.recycle();
      return;
    }
    final StrictEqualityNodeWrapper wrapper =
        new StrictEqualityNodeWrapper(realAccessibilityNodeInfo);
    if (!obtainedInstances.containsKey(wrapper)) {
      throw new IllegalStateException();
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

    obtainedInstances.remove(wrapper);
    int keyOfWrapper = -1;
    for (int i = 0; i < orderedInstances.size(); i++) {
      int key = orderedInstances.keyAt(i);
      if (orderedInstances.get(key).equals(wrapper)) {
        keyOfWrapper = key;
        break;
      }
    }
    orderedInstances.remove(keyOfWrapper);
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
    this.refreshReturnValue = refreshReturnValue;
  }

  public boolean isPasteable() {
    return (accessibilityNodeInfoReflector.getBooleanProperties() & PASTEABLE_MASK) != 0;
  }

  public boolean isTextSelectionSetable() {
    return (accessibilityNodeInfoReflector.getBooleanProperties() & TEXT_SELECTION_SETABLE_MASK)
        != 0;
  }

  public void setTextSelectionSetable(boolean isTextSelectionSetable) {
    accessibilityNodeInfoReflector.setBooleanProperty(
        TEXT_SELECTION_SETABLE_MASK, isTextSelectionSetable);
  }

  public void setPasteable(boolean isPasteable) {
    accessibilityNodeInfoReflector.setBooleanProperty(PASTEABLE_MASK, isPasteable);
  }

  @Implementation
  protected void setText(CharSequence t) {
    if (useRealAni()) {
      accessibilityNodeInfoReflector.setText(t);
      return;
    }
    text = t;
  }

  @Implementation
  protected CharSequence getText() {
    if (useRealAni()) {
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
    if (useRealAni()) {
      accessibilityNodeInfoReflector.setLabelFor(info);
      return;
    }
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
    if (useRealAni()) {
      accessibilityNodeInfoReflector.setLabeledBy(info);
      return;
    }
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
    if (this.traversalBefore != null) {
      this.traversalBefore.recycle();
    }

    this.traversalBefore = obtain(info);
  }

  @Implementation
  protected void setSource(View source) {
    accessibilityNodeInfoReflector.setSource(source);
    this.view = source;
  }

  @Implementation
  protected void setSource(View root, int virtualDescendantId) {
    accessibilityNodeInfoReflector.setSource(root, virtualDescendantId);
    this.view = root;
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
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.getWindowId();
    }
    return (accessibilityWindowInfo == null) ? -1 : accessibilityWindowInfo.getId();
  }

  public void setAccessibilityWindowInfo(AccessibilityWindowInfo info) {
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

  @Implementation
  @Override
  public int hashCode() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.hashCode();
    }
    // This is 0 for a reason. If you change it, you will break the obtained
    // instances map in a manner that is remarkably difficult to debug.
    // Having a dynamic hash code keeps this object from being located
    // in the map if it was mutated after being obtained.
    return 0;
  }

  /**
   * Add a child node to this one. Also initializes the parent field of the child.
   *
   * @param child The node to be added as a child.
   */
  public void addChild(AccessibilityNodeInfo child) {
    if (children == null) {
      children = new ArrayList<>();
    }

    children.add(child);
    ShadowAccessibilityNodeInfo shadowAccessibilityNodeInfo = Shadow.extract(child);
    shadowAccessibilityNodeInfo.parent = realAccessibilityNodeInfo;
  }

  @Implementation
  protected void addChild(View child) {
    if (useRealAni()) {
      accessibilityNodeInfoReflector.addChild(child);
      return;
    }
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(child);
    addChild(node);
  }

  @Implementation
  protected void addChild(View root, int virtualDescendantId) {
    if (useRealAni()) {
      accessibilityNodeInfoReflector.addChild(root, virtualDescendantId);
      return;
    }
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(root, virtualDescendantId);
    addChild(node);
  }

  /**
   * @return The list of arguments for the various calls to performAction. Unmodifiable.
   */
  public List<Integer> getPerformedActions() {
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
    if (performedActionAndArgsList == null) {
      performedActionAndArgsList = new ArrayList<>();
    }
    return Collections.unmodifiableList(performedActionAndArgsList);
  }

  /**
   * @return A shallow copy.
   */
  private AccessibilityNodeInfo getClone() {
    // We explicitly avoid allocating the AccessibilityNodeInfo from the actual pool by using
    // the private constructor. Not doing so affects test suites which use both shadow and
    // non-shadow objects.
    final AccessibilityNodeInfo newInfo =
        ReflectionHelpers.callConstructor(AccessibilityNodeInfo.class);
    final ShadowAccessibilityNodeInfo newShadow = Shadow.extract(newInfo);

    newShadow.mOriginNodeId = mOriginNodeId;
    Rect boundsInScreen = new Rect();
    realAccessibilityNodeInfo.getBoundsInScreen(boundsInScreen);
    newInfo.setBoundsInScreen(boundsInScreen);
    newShadow.accessibilityNodeInfoReflector.setBooleanProperties(
        accessibilityNodeInfoReflector.getBooleanProperties());
    newInfo.setContentDescription(realAccessibilityNodeInfo.getContentDescription());
    newShadow.text = text;
    newShadow.performedActionAndArgsList = performedActionAndArgsList;
    newShadow.parent = parent;
    newInfo.setClassName(realAccessibilityNodeInfo.getClassName());
    newShadow.labelFor = (labelFor == null) ? null : obtain(labelFor);
    newShadow.labeledBy = (labeledBy == null) ? null : obtain(labeledBy);
    newShadow.view = view;
    newShadow.actionListener = actionListener;
    newShadow.accessibilityNodeInfoReflector.setActionsList(
        new ArrayList<>(realAccessibilityNodeInfo.getActionList()));

    if (children != null) {
      newShadow.children = new ArrayList<>();
      newShadow.children.addAll(children);
    } else {
      newShadow.children = null;
    }

    newShadow.refreshReturnValue = refreshReturnValue;
    newInfo.setMovementGranularities(realAccessibilityNodeInfo.getMovementGranularities());
    newInfo.setPackageName(realAccessibilityNodeInfo.getPackageName());
    newInfo.setViewIdResourceName(realAccessibilityNodeInfo.getViewIdResourceName());
    newInfo.setTextSelection(
        realAccessibilityNodeInfo.getTextSelectionStart(),
        realAccessibilityNodeInfo.getTextSelectionEnd());
    newInfo.setCollectionInfo(realAccessibilityNodeInfo.getCollectionInfo());
    newInfo.setCollectionItemInfo(realAccessibilityNodeInfo.getCollectionItemInfo());
    newInfo.setInputType(realAccessibilityNodeInfo.getInputType());
    newInfo.setLiveRegion(realAccessibilityNodeInfo.getLiveRegion());
    newInfo.setRangeInfo(realAccessibilityNodeInfo.getRangeInfo());
    newShadow.realAccessibilityNodeInfo.getExtras().putAll(realAccessibilityNodeInfo.getExtras());
    newInfo.setMaxTextLength(realAccessibilityNodeInfo.getMaxTextLength());
    newInfo.setError(realAccessibilityNodeInfo.getError());

    if (getApiLevel() >= LOLLIPOP_MR1) {
      newShadow.traversalAfter = (traversalAfter == null) ? null : obtain(traversalAfter);
      newShadow.traversalBefore = (traversalBefore == null) ? null : obtain(traversalBefore);
    }
    if (accessibilityWindowInfo != null) {
      newShadow.accessibilityWindowInfo =
          ShadowAccessibilityWindowInfo.obtain(accessibilityWindowInfo);
    }
    if (getApiLevel() >= N) {
      newInfo.setDrawingOrder(realAccessibilityNodeInfo.getDrawingOrder());
    }
    if (getApiLevel() >= O) {
      newInfo.setHintText(realAccessibilityNodeInfo.getHintText());
    }
    if (getApiLevel() >= P) {
      newInfo.setTooltipText(realAccessibilityNodeInfo.getTooltipText());
      newInfo.setPaneTitle(realAccessibilityNodeInfo.getPaneTitle());
    }
    if (getApiLevel() >= R) {
      newInfo.setStateDescription(realAccessibilityNodeInfo.getStateDescription());
    }
    if (getApiLevel() >= UPSIDE_DOWN_CAKE) {
      newInfo.setContainerTitle(realAccessibilityNodeInfo.getContainerTitle());
    }

    return newInfo;
  }

  /**
   * Private class to keep different nodes referring to the same view straight in the
   * mObtainedInstances map.
   */
  private static class StrictEqualityNodeWrapper {
    public final AccessibilityNodeInfo mInfo;

    public StrictEqualityNodeWrapper(AccessibilityNodeInfo info) {
      mInfo = info;
    }

    @Override
    @SuppressWarnings("ReferenceEquality")
    public boolean equals(Object object) {
      if (object == null) {
        return false;
      }
      if (!(object instanceof StrictEqualityNodeWrapper)) {
        return false;
      }
      final StrictEqualityNodeWrapper wrapper = (StrictEqualityNodeWrapper) object;
      return mInfo == wrapper.mInfo;
    }

    @Override
    public int hashCode() {
      return mInfo.hashCode();
    }
  }

  @Implementation
  protected int describeContents() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.describeContents();
    }
    return 0;
  }

  @Implementation
  protected void writeToParcel(Parcel dest, int flags) {
    if (useRealAni()) {
      accessibilityNodeInfoReflector.writeToParcel(dest, flags);
      return;
    }
    StrictEqualityNodeWrapper wrapper = new StrictEqualityNodeWrapper(realAccessibilityNodeInfo);
    int keyOfWrapper = -1;
    for (int i = 0; i < orderedInstances.size(); i++) {
      if (orderedInstances.valueAt(i).equals(wrapper)) {
        keyOfWrapper = orderedInstances.keyAt(i);
        break;
      }
    }
    dest.writeInt(keyOfWrapper);
  }

  /**
   * Configure the return result of an action if it is performed
   *
   * @param listener The listener.
   */
  public void setOnPerformActionListener(OnPerformActionListener listener) {
    actionListener = listener;
  }

  public interface OnPerformActionListener {
    boolean onPerformAccessibilityAction(int action, Bundle arguments);
  }

  @Override
  @Implementation
  public String toString() {
    if (useRealAni()) {
      return accessibilityNodeInfoReflector.toString();
    }
    return "ShadowAccessibilityNodeInfo@"
        + System.identityHashCode(this)
        + ":{text:"
        + text
        + ", className:"
        + realAccessibilityNodeInfo.getClassName()
        + "}";
  }

  @ForType(AccessibilityNodeInfo.class)
  interface AccessibilityNodeInfoReflector {
    @Static
    @Accessor("CREATOR")
    void setCreator(Parcelable.Creator<AccessibilityNodeInfo> creator);

    @Static
    AccessibilityAction getActionSingleton(int id);

    @Accessor("mBooleanProperties")
    int getBooleanProperties();

    @Accessor("mBooleanProperties")
    void setBooleanProperties(int properties);

    void setBooleanProperty(int property, boolean value);

    @Accessor("mActions")
    void setActionsList(ArrayList<AccessibilityAction> actions);

    @Accessor("mActions")
    void setActionsMask(int actions); // pre-L

    @Direct
    void getBoundsInScreen(Rect outBounds);

    @Direct
    void getBoundsInParent(Rect outBounds);

    @Direct
    void setBoundsInScreen(Rect b);

    @Direct
    void setBoundsInParent(Rect b);

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
    void setLabelFor(AccessibilityNodeInfo info);

    @Direct
    AccessibilityNodeInfo getLabeledBy();

    @Direct
    void setLabeledBy(AccessibilityNodeInfo info);

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

    @Direct
    int describeContents();

    @Override
    @Direct
    String toString();

    @Direct
    void writeToParcel(Parcel dest, int flags);
  }

  static boolean useRealAni() {
    return Boolean.parseBoolean(System.getProperty("robolectric.useRealAni", "false"));
  }
}
