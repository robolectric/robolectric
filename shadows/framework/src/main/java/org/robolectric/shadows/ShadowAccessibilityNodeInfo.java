package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.*;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeInfo.CollectionInfo;
import android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo;
import android.view.accessibility.AccessibilityNodeInfo.RangeInfo;
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
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityNodeInfo} that allows a test to set
 * properties that are locked in the original class. It also keeps track of calls to
 * {@code obtain()} and {@code recycle()} to look for bugs that mismatches.
 */
@Implements(AccessibilityNodeInfo.class)
public class ShadowAccessibilityNodeInfo {
  // Map of obtained instances of the class along with stack traces of how they were obtained
  private static final Map<StrictEqualityNodeWrapper, StackTraceElement[]> obtainedInstances =
      new HashMap<>();

  private static final SparseArray<StrictEqualityNodeWrapper> orderedInstances = new SparseArray<>();

  // Bitmasks for actions
  public static final int UNDEFINED_SELECTION_INDEX = -1;

  public static final Parcelable.Creator<AccessibilityNodeInfo> CREATOR =
      new Parcelable.Creator<AccessibilityNodeInfo>() {

    @Override
    public AccessibilityNodeInfo createFromParcel(Parcel source) {
      return obtain(orderedInstances.get(source.readInt()).mInfo);
    }

    @Override
    public AccessibilityNodeInfo[] newArray(int size) {
      return new AccessibilityNodeInfo[size];
    }};

  private static int sAllocationCount = 0;

  private static final int CLICKABLE_MASK = 0x00000001;

  private static final int LONGCLICKABLE_MASK = 0x00000002;

  private static final int FOCUSABLE_MASK = 0x00000004;

  private static final int FOCUSED_MASK = 0x00000008;

  private static final int VISIBLE_TO_USER_MASK = 0x00000010;

  private static final int SCROLLABLE_MASK = 0x00000020;

  private static final int PASTEABLE_MASK = 0x00000040;

  private static final int EDITABLE_MASK = 0x00000080;

  private static final int TEXT_SELECTION_SETABLE_MASK = 0x00000100;

  private static final int CHECKABLE_MASK = 0x00001000; //14

  private static final int CHECKED_MASK = 0x00002000; //14

  private static final int ENABLED_MASK = 0x00010000; //14

  private static final int PASSWORD_MASK = 0x00040000; //14

  private static final int SELECTED_MASK = 0x00080000; //14

  private static final int A11YFOCUSED_MASK = 0x00000800;  //16

  private static final int MULTILINE_MASK = 0x00020000; //19

  private static final int CONTENT_INVALID_MASK = 0x00004000; //19

  private static final int DISMISSABLE_MASK = 0x00008000; //19

  private static final int CAN_OPEN_POPUP_MASK = 0x00100000; //19

  /**
   * Uniquely identifies the origin of the AccessibilityNodeInfo for equality
   * testing. Two instances that come from the same node info should have the
   * same ID.
   */
  private long mOriginNodeId;

  private List<AccessibilityNodeInfo> children;

  private Rect boundsInScreen = new Rect();

  private Rect boundsInParent = new Rect();

  private List<Pair<Integer, Bundle>> performedActionAndArgsList;

  // In API prior to 21, actions are stored in a flag, after 21 they are stored in array of
  // AccessibilityAction so custom actions can be supported.
  private ArrayList<AccessibilityAction> actionsArray;
  private int actionsMask;
  // Storage of flags

  private int propertyFlags;

  private AccessibilityNodeInfo parent;

  private AccessibilityNodeInfo labelFor;

  private AccessibilityNodeInfo labeledBy;

  private View view;

  private CharSequence contentDescription;

  private CharSequence text;

  private CharSequence className;

  private int textSelectionStart = UNDEFINED_SELECTION_INDEX;

  private int textSelectionEnd = UNDEFINED_SELECTION_INDEX;

  private boolean refreshReturnValue = true;

  private int movementGranularities; //16

  private CharSequence packageName; //14

  private String viewIdResourceName; //18

  private CollectionInfo collectionInfo; //19

  private CollectionItemInfo collectionItemInfo; //19

  private int inputType; //19

  private int liveRegion; //19

  private RangeInfo rangeInfo; //19

  private int maxTextLength; //21

  private CharSequence error; //21
  
  private AccessibilityWindowInfo accessibilityWindowInfo;

  private AccessibilityNodeInfo traversalAfter; //22

  private AccessibilityNodeInfo traversalBefore; //22

  private OnPerformActionListener actionListener;

  @RealObject
  private AccessibilityNodeInfo realAccessibilityNodeInfo;

  @Implementation
  public void __constructor__() {
    ReflectionHelpers.setStaticField(AccessibilityNodeInfo.class, "CREATOR", ShadowAccessibilityNodeInfo.CREATOR);
  }

  @Implementation
  public static AccessibilityNodeInfo obtain(AccessibilityNodeInfo info) {
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
  public static AccessibilityNodeInfo obtain(View view) {
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
  public static AccessibilityNodeInfo obtain() {
    return obtain(new View(RuntimeEnvironment.application.getApplicationContext()));
  }

  @Implementation
  public static AccessibilityNodeInfo obtain(View root, int virtualDescendantId) {
    AccessibilityNodeInfo node = obtain(root);
    return node;
  }

  /**
   * Check for leaked objects that were {@code obtain}ed but never
   * {@code recycle}d.
   *
   * @param printUnrecycledNodesToSystemErr - if true, stack traces of calls
   *        to {@code obtain} that lack matching calls to {@code recycle} are
   *        dumped to System.err.
   * @return {@code true} if there are unrecycled nodes
   */
  public static boolean areThereUnrecycledNodes(boolean printUnrecycledNodesToSystemErr) {
    if (printUnrecycledNodesToSystemErr) {
      for (final StrictEqualityNodeWrapper wrapper : obtainedInstances.keySet()) {
        final ShadowAccessibilityNodeInfo shadow = Shadow.extract(wrapper.mInfo);

        System.err.println(String.format(
            "Leaked contentDescription = %s. Stack trace:", shadow.getContentDescription()));
        for (final StackTraceElement stackTraceElement : obtainedInstances.get(wrapper)) {
          System.err.println(stackTraceElement.toString());
        }
      }
    }

    return (obtainedInstances.size() != 0);
  }

  /**
   * Clear list of obtained instance objects. {@code areThereUnrecycledNodes}
   * will always return false if called immediately afterwards.
   */
  public static void resetObtainedInstances() {
    obtainedInstances.clear();
    orderedInstances.clear();
  }

  @Implementation
  public void recycle() {
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
  public int getChildCount() {
    if (children == null) {
      return 0;
    }

    return children.size();
  }

  @Implementation
  public AccessibilityNodeInfo getChild(int index) {
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
  public AccessibilityNodeInfo getParent() {
    if (parent == null) {
      return null;
    }

    return obtain(parent);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public boolean refresh() {
      return refreshReturnValue;
  }

  public void setRefreshReturnValue(boolean refreshReturnValue) {
    this.refreshReturnValue = refreshReturnValue;
  }

  @Implementation
  public boolean isClickable() {
    return ((propertyFlags & CLICKABLE_MASK) != 0);
  }

  @Implementation
  public boolean isLongClickable() {
    return ((propertyFlags & LONGCLICKABLE_MASK) != 0);
  }

  @Implementation
  public boolean isFocusable() {
    return ((propertyFlags & FOCUSABLE_MASK) != 0);
  }

  @Implementation
  public boolean isFocused() {
    return ((propertyFlags & FOCUSED_MASK) != 0);
  }

  @Implementation
  public boolean isVisibleToUser() {
    return ((propertyFlags & VISIBLE_TO_USER_MASK) != 0);
  }

  @Implementation
  public boolean isScrollable() {
    return ((propertyFlags & SCROLLABLE_MASK) != 0);
  }

  public boolean isPasteable() {
    return ((propertyFlags & PASTEABLE_MASK) != 0);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public boolean isEditable() {
    return ((propertyFlags & EDITABLE_MASK) != 0);
  }

  public boolean isTextSelectionSetable() {
    return ((propertyFlags & TEXT_SELECTION_SETABLE_MASK) != 0);
  }

  @Implementation
  public boolean isCheckable() {
    return ((propertyFlags & CHECKABLE_MASK) != 0);
  }

  @Implementation
  public void setCheckable(boolean checkable) {
    propertyFlags = (propertyFlags & ~CHECKABLE_MASK) |
        (checkable ? CHECKABLE_MASK : 0);
  }

  @Implementation
  public void setChecked(boolean checked) {
    propertyFlags = (propertyFlags & ~CHECKED_MASK) |
        (checked ? CHECKED_MASK : 0);
  }

  @Implementation
  public boolean isChecked() {
    return ((propertyFlags & CHECKED_MASK) != 0);
  }

  @Implementation
  public void setEnabled(boolean enabled) {
    propertyFlags = (propertyFlags & ~ENABLED_MASK) |
        (enabled ? ENABLED_MASK : 0);
  }

  @Implementation
  public boolean isEnabled() {
    return ((propertyFlags & ENABLED_MASK) != 0);
  }

  @Implementation
  public void setPassword(boolean password) {
    propertyFlags = (propertyFlags & ~PASSWORD_MASK) |
        (password ? PASSWORD_MASK : 0);
  }

  @Implementation
  public boolean isPassword() {
    return ((propertyFlags & PASSWORD_MASK) != 0);
  }

  @Implementation
  public void setSelected(boolean selected) {
    propertyFlags = (propertyFlags & ~SELECTED_MASK) |
        (selected ? SELECTED_MASK : 0);
  }

  @Implementation
  public boolean isSelected() {
    return ((propertyFlags & SELECTED_MASK) != 0);
  }

  @Implementation
  public void setAccessibilityFocused(boolean focused) {
    propertyFlags = (propertyFlags & ~A11YFOCUSED_MASK) |
        (focused ? A11YFOCUSED_MASK : 0);
  }

  @Implementation
  public boolean isAccessibilityFocused() {
    return ((propertyFlags & A11YFOCUSED_MASK) != 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setMultiLine(boolean multiLine) {
    propertyFlags = (propertyFlags & ~MULTILINE_MASK) |
        (multiLine ? MULTILINE_MASK : 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean isMultiLine() {
    return ((propertyFlags & MULTILINE_MASK) != 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setContentInvalid(boolean contentInvalid) {
    propertyFlags = (propertyFlags & ~CONTENT_INVALID_MASK) |
        (contentInvalid ? CONTENT_INVALID_MASK : 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean isContentInvalid() {
    return ((propertyFlags & CONTENT_INVALID_MASK) != 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setDismissable(boolean dismissable) {
    propertyFlags = (propertyFlags & ~DISMISSABLE_MASK) |
        (dismissable ? DISMISSABLE_MASK : 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean isDismissable() {
    return ((propertyFlags & DISMISSABLE_MASK) != 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setCanOpenPopup(boolean opensPopup) {
    propertyFlags = (propertyFlags & ~CAN_OPEN_POPUP_MASK) |
        (opensPopup ? CAN_OPEN_POPUP_MASK : 0);
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean canOpenPopup() {
    return ((propertyFlags & CAN_OPEN_POPUP_MASK) != 0);
  }

  public void setTextSelectionSetable(boolean isTextSelectionSetable) {
    propertyFlags = (propertyFlags & ~TEXT_SELECTION_SETABLE_MASK) |
        (isTextSelectionSetable ? TEXT_SELECTION_SETABLE_MASK : 0);
  }

  @Implementation
  public void setClickable(boolean isClickable) {
    propertyFlags = (propertyFlags & ~CLICKABLE_MASK) | (isClickable ? CLICKABLE_MASK : 0);
  }

  @Implementation
  public void setLongClickable(boolean isLongClickable) {
    propertyFlags =
        (propertyFlags & ~LONGCLICKABLE_MASK) | (isLongClickable ? LONGCLICKABLE_MASK : 0);
  }

  @Implementation
  public void setFocusable(boolean isFocusable) {
    propertyFlags = (propertyFlags & ~FOCUSABLE_MASK) | (isFocusable ? FOCUSABLE_MASK : 0);
  }

  @Implementation
  public void setFocused(boolean isFocused) {
    propertyFlags = (propertyFlags & ~FOCUSED_MASK) | (isFocused ? FOCUSED_MASK : 0);
  }

  @Implementation
  public void setScrollable(boolean isScrollable) {
    propertyFlags = (propertyFlags & ~SCROLLABLE_MASK) | (isScrollable ? SCROLLABLE_MASK : 0);
  }

  public void setPasteable(boolean isPasteable) {
    propertyFlags = (propertyFlags & ~PASTEABLE_MASK) | (isPasteable ? PASTEABLE_MASK : 0);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public void setEditable(boolean isEditable) {
    propertyFlags = (propertyFlags & ~EDITABLE_MASK) | (isEditable ? EDITABLE_MASK : 0);
  }

  @Implementation
  public void setVisibleToUser(boolean isVisibleToUser) {
    propertyFlags =
        (propertyFlags & ~VISIBLE_TO_USER_MASK) | (isVisibleToUser ? VISIBLE_TO_USER_MASK : 0);
  }

  @Implementation
  public void setContentDescription(CharSequence description) {
    contentDescription = description;
  }

  @Implementation
  public CharSequence getContentDescription() {
    return contentDescription;
  }

  @Implementation
  public void setClassName(CharSequence name) {
    className = name;
  }

  @Implementation
  public CharSequence getClassName() {
    return className;
  }

  @Implementation
  public void setText(CharSequence t) {
    text = t;
  }

  @Implementation
  public CharSequence getText() {
    return text;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public void setTextSelection(int start, int end) {
      textSelectionStart = start;
      textSelectionEnd = end;
  }

  /**
   * Gets the text selection start.
   *
   * @return The text selection start if there is selection or UNDEFINED_SELECTION_INDEX.
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  public int getTextSelectionStart() {
      return textSelectionStart;
  }

  /**
   * Gets the text selection end.
   *
   * @return The text selection end if there is selection or UNDEFINED_SELECTION_INDEX.
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  public int getTextSelectionEnd() {
      return textSelectionEnd;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public AccessibilityNodeInfo getLabelFor() {
    if (labelFor == null) {
      return null;
    }

    return obtain(labelFor);
  }

  public void setLabelFor(AccessibilityNodeInfo info) {
    if (labelFor != null) {
      labelFor.recycle();
    }

    labelFor = obtain(info);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  public AccessibilityNodeInfo getLabeledBy() {
    if (labeledBy == null) {
      return null;
    }

    return obtain(labeledBy);
  }

  public void setLabeledBy(AccessibilityNodeInfo info) {
    if (labeledBy != null) {
      labeledBy.recycle();
    }

    labeledBy = obtain(info);
  }

  @Implementation
  public int getMovementGranularities() {
    return movementGranularities;
  }

  @Implementation
  public void setMovementGranularities(int movementGranularities) {
    this.movementGranularities = movementGranularities;
  }

  @Implementation
  public CharSequence getPackageName() {
    return packageName;
  }

  @Implementation
  public void setPackageName(CharSequence packageName) {
    this.packageName = packageName;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public String getViewIdResourceName() {
    return viewIdResourceName;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public void setViewIdResourceName(String viewIdResourceName) {
    this.viewIdResourceName = viewIdResourceName;
  }

  @Implementation(minSdk = KITKAT)
  public CollectionInfo getCollectionInfo() {
    return collectionInfo;
  }

  @Implementation(minSdk = KITKAT)
  public void setCollectionInfo(CollectionInfo collectionInfo) {
    this.collectionInfo = collectionInfo;
  }

  @Implementation(minSdk = KITKAT)
  public CollectionItemInfo getCollectionItemInfo() {
    return collectionItemInfo;
  }

  @Implementation(minSdk = KITKAT)
  public void setCollectionItemInfo(CollectionItemInfo collectionItemInfo) {
    this.collectionItemInfo = collectionItemInfo;
  }

  @Implementation(minSdk = KITKAT)
  public int getInputType() {
    return inputType;
  }

  @Implementation(minSdk = KITKAT)
  public void setInputType(int inputType) {
    this.inputType = inputType;
  }

  @Implementation(minSdk = KITKAT)
  public int getLiveRegion() {
    return liveRegion;
  }

  @Implementation(minSdk = KITKAT)
  public void setLiveRegion(int liveRegion) {
    this.liveRegion = liveRegion;
  }

  @Implementation(minSdk = KITKAT)
  public RangeInfo getRangeInfo() {
    return rangeInfo;
  }

  @Implementation(minSdk = KITKAT)
  public void setRangeInfo(RangeInfo rangeInfo) {
    this.rangeInfo = rangeInfo;
  }

  @Implementation(minSdk = LOLLIPOP)
  public int getMaxTextLength() {
    return maxTextLength;
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setMaxTextLength(int maxTextLength) {
    this.maxTextLength = maxTextLength;
  }

  @Implementation(minSdk = LOLLIPOP)
  public CharSequence getError() {
    return error;
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setError(CharSequence error) {
    this.error = error;
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  public AccessibilityNodeInfo getTraversalAfter() {
    if (traversalAfter == null) {
      return null;
    }

    return obtain(traversalAfter);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  public void setTraversalAfter(AccessibilityNodeInfo info) {
    if (this.traversalAfter != null) {
      this.traversalAfter.recycle();
    }
    
    this.traversalAfter = obtain(info);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  public AccessibilityNodeInfo getTraversalBefore() {
    if (traversalBefore == null) {
      return null;
    }

    return obtain(traversalBefore);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  public void setTraversalBefore(AccessibilityNodeInfo info) {
    if (this.traversalBefore != null) {
      this.traversalBefore.recycle();
    }
    
    this.traversalBefore = obtain(info);
  }

  @Implementation
  public void setSource (View source) {
    this.view = source;
  }

  @Implementation
  public void setSource (View root, int virtualDescendantId) {
    this.view = root;
  }

  @Implementation
  public void getBoundsInScreen(Rect outBounds) {
    if (boundsInScreen == null) {
      boundsInScreen = new Rect();
    }
    outBounds.set(boundsInScreen);
  }

  @Implementation
  public void getBoundsInParent(Rect outBounds) {
    if (boundsInParent == null) {
      boundsInParent = new Rect();
    }
    outBounds.set(boundsInParent);
  }

  @Implementation
  public void setBoundsInScreen(Rect b) {
    if (boundsInScreen == null) {
      boundsInScreen = new Rect(b);
    } else {
      boundsInScreen.set(b);
    }
  }

  @Implementation
  public void setBoundsInParent(Rect b) {
    if (boundsInParent == null) {
      boundsInParent = new Rect(b);
    } else {
      boundsInParent.set(b);
    }
  }

  @Implementation
  public void addAction(int action) {
    if (getApiLevel() >= LOLLIPOP) {
      if ((action & getActionTypeMaskFromFramework()) != 0) {
        throw new IllegalArgumentException("Action is not a combination of the standard " +
            "actions: " + action);
      }
      int remainingIds = action;
      while (remainingIds > 0) {
        final int id = 1 << Integer.numberOfTrailingZeros(remainingIds);
        remainingIds &= ~id;
        AccessibilityAction convertedAction = getActionFromIdFromFrameWork(id);
        addAction(convertedAction);
      }
    } else {
      actionsMask |= action;
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  public void addAction(AccessibilityAction action) {
    if (action == null) {
      return;
    }

    if (actionsArray == null) {
      actionsArray = new ArrayList<>();
    }
    actionsArray.remove(action);
    actionsArray.add(action);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void removeAction(int action) {
    AccessibilityAction convertedAction = getActionFromIdFromFrameWork(action);
    removeAction(convertedAction);
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean removeAction(AccessibilityAction action) {
    if (action == null || actionsArray == null) {
      return false;
    }
    return actionsArray.remove(action);
  }

  /**
   * Obtain flags for actions supported. Currently only supports
   * {@link AccessibilityNodeInfo#ACTION_CLICK},
   * {@link AccessibilityNodeInfo#ACTION_LONG_CLICK},
   * {@link AccessibilityNodeInfo#ACTION_SCROLL_FORWARD},
   * {@link AccessibilityNodeInfo#ACTION_PASTE},
   * {@link AccessibilityNodeInfo#ACTION_FOCUS},
   * {@link AccessibilityNodeInfo#ACTION_SET_SELECTION},
   * {@link AccessibilityNodeInfo#ACTION_SCROLL_BACKWARD}
   * Returned value is derived from the getters.
   *
   * @return Action mask. 0 if no actions supported.
   */
  @Implementation
  public int getActions() {
    if (getApiLevel() >= LOLLIPOP) {
      int returnValue = 0;
      if (actionsArray == null) {
        return returnValue;
      }

      // Custom actions are only returned by getActionsList
      final int actionSize = actionsArray.size();
      for (int i = 0; i < actionSize; i++) {
        int actionId = actionsArray.get(i).getId();
        if (actionId <= getLastLegacyActionFromFrameWork()) {
          returnValue |= actionId;
        }
      }
      return returnValue;
    } else {
      return actionsMask;
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  public AccessibilityWindowInfo getWindow() {
    return accessibilityWindowInfo;
  }

  public void setAccessibilityWindowInfo(AccessibilityWindowInfo info) {
    accessibilityWindowInfo = info;
  }

  @Implementation(minSdk = LOLLIPOP)
  public List<AccessibilityAction> getActionList() {
    if (actionsArray == null) {
      return Collections.emptyList();
    }

    return actionsArray;
  }

  @Implementation
  public boolean performAction(int action) {
    return performAction(action, null);
  }

  @Implementation
  public boolean performAction(int action, Bundle arguments) {
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
    // This is 0 for a reason. If you change it, you will break the obtained
    // instances map in a manner that is remarkably difficult to debug.
    // Having a dynamic hash code keeps this object from being located
    // in the map if it was mutated after being obtained.
    return 0;
  }

  /**
   * Add a child node to this one. Also initializes the parent field of the
   * child.
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
  public void addChild(View child) {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(child);
    addChild(node);
  }

  @Implementation
  public void addChild(View root, int virtualDescendantId) {
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
    newShadow.boundsInScreen = new Rect(boundsInScreen);
    newShadow.propertyFlags = propertyFlags;
    newShadow.contentDescription = contentDescription;
    newShadow.text = text;
    newShadow.performedActionAndArgsList = performedActionAndArgsList;
    newShadow.parent = parent;
    newShadow.className = className;
    newShadow.labelFor = labelFor;
    newShadow.labeledBy = labeledBy;
    newShadow.view = view;
    newShadow.textSelectionStart = textSelectionStart;
    newShadow.textSelectionEnd = textSelectionEnd;
    newShadow.actionListener = actionListener;
    if (getApiLevel() >= LOLLIPOP) {
      if (actionsArray != null) {
        newShadow.actionsArray = new ArrayList<>();
        newShadow.actionsArray.addAll(actionsArray);
      } else {
        newShadow.actionsArray = null;
      }
    } else {
      newShadow.actionsMask = actionsMask;
    }

    if (children != null) {
      newShadow.children = new ArrayList<>();
      newShadow.children.addAll(children);
    } else {
      newShadow.children = null;
    }

    newShadow.refreshReturnValue = refreshReturnValue;
    newShadow.movementGranularities = movementGranularities;
    newShadow.packageName = packageName;
    if (getApiLevel() >= JELLY_BEAN_MR2) {
      newShadow.viewIdResourceName = viewIdResourceName;
    }
    if (getApiLevel() >= KITKAT) {
      newShadow.collectionInfo = collectionInfo;
      newShadow.collectionItemInfo = collectionItemInfo;
      newShadow.inputType = inputType;
      newShadow.liveRegion = liveRegion;
      newShadow.rangeInfo = rangeInfo;
    }
    if (getApiLevel() >= LOLLIPOP) {
      newShadow.maxTextLength = maxTextLength;
      newShadow.error = error;
    }
    if (getApiLevel() >= LOLLIPOP_MR1) {
      newShadow.traversalAfter = (traversalAfter == null) ? null : obtain(traversalAfter);
      newShadow.traversalBefore = (traversalBefore == null) ? null : obtain(traversalBefore);
    }
    return newInfo;
  }

  /**
   * Private class to keep different nodes referring to the same view straight
   * in the mObtainedInstances map.
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

      final StrictEqualityNodeWrapper wrapper = (StrictEqualityNodeWrapper) object;
      return mInfo == wrapper.mInfo;
    }

    @Override
    public int hashCode() {
      return mInfo.hashCode();
    }
  }

  /**
   * Shadow of AccessibilityAction.
   */
  @Implements(value = AccessibilityNodeInfo.AccessibilityAction.class, minSdk = LOLLIPOP)
  public static final class ShadowAccessibilityAction {
    private int id;
    private CharSequence label;

    @Implementation
    public void __constructor__(int id, CharSequence label) {
      if (((id & (int)ReflectionHelpers.getStaticField(AccessibilityNodeInfo.class, "ACTION_TYPE_MASK")) == 0) && Integer.bitCount(id) != 1) {
        throw new IllegalArgumentException("Invalid standard action id");
      }
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

    @Override
    @Implementation
    @SuppressWarnings("EqualsHashCode")
    public boolean equals(Object other) {
      if (other == null) {
        return false;
      }

      if (other == this) {
        return true;
      }

      if (other.getClass() != AccessibilityAction.class) {
        return false;
      }

      return id == ((AccessibilityAction) other).getId();
    }

    @Override
    public String toString() {
      String actionSybolicName = ReflectionHelpers.callStaticMethod(
          AccessibilityNodeInfo.class, "getActionSymbolicName", ClassParameter.from(int.class, id));
      return "AccessibilityAction: " + actionSybolicName + " - " + label;
    }
  }

  @Implementation
  public int describeContents() {
    return 0;
  }

  @Implementation
  public void writeToParcel(Parcel dest, int flags) {
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

  private static int getActionTypeMaskFromFramework() {
    // Get the mask to determine whether an int is a legit ID for an action, defined by Android
    return (int)ReflectionHelpers.getStaticField(AccessibilityNodeInfo.class, "ACTION_TYPE_MASK");
  }
  
  private static AccessibilityAction getActionFromIdFromFrameWork(int id) {
    // Convert an action ID to Android standard Accessibility Action defined by Android
    return ReflectionHelpers.callStaticMethod(
        AccessibilityNodeInfo.class, "getActionSingleton", ClassParameter.from(int.class, id));
  }
  
  private static int getLastLegacyActionFromFrameWork() {
    return (int)ReflectionHelpers.getStaticField(AccessibilityNodeInfo.class, "LAST_LEGACY_STANDARD_ACTION");
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
}
