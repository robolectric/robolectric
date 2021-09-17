package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityWindowInfo} that allows a test to set
 * properties that are locked in the original class.
 */
@Implements(value = AccessibilityWindowInfo.class, minSdk = LOLLIPOP)
public class ShadowAccessibilityWindowInfo {

  private static final Map<StrictEqualityWindowWrapper, StackTraceElement[]> obtainedInstances =
      new HashMap<>();

  private List<AccessibilityWindowInfo> children = null;

  private AccessibilityWindowInfo parent = null;

  private AccessibilityNodeInfo rootNode = null;

  private AccessibilityNodeInfo anchorNode = null;

  private Rect boundsInScreen = new Rect();

  private int type = AccessibilityWindowInfo.TYPE_APPLICATION;

  private int layer = 0;

  private CharSequence title = null;

  private boolean isAccessibilityFocused = false;

  private boolean isActive = false;

  private boolean isFocused = false;

  @RealObject private AccessibilityWindowInfo mRealAccessibilityWindowInfo;

  @Implementation
  protected void __constructor__() {}

  @Implementation
  protected static AccessibilityWindowInfo obtain() {
    final AccessibilityWindowInfo obtainedInstance =
        ReflectionHelpers.callConstructor(AccessibilityWindowInfo.class);
    StrictEqualityWindowWrapper wrapper = new StrictEqualityWindowWrapper(obtainedInstance);
    obtainedInstances.put(wrapper, Thread.currentThread().getStackTrace());
    return obtainedInstance;
  }

  @Implementation
  protected static AccessibilityWindowInfo obtain(AccessibilityWindowInfo window) {
    final ShadowAccessibilityWindowInfo shadowInfo = Shadow.extract(window);
    final AccessibilityWindowInfo obtainedInstance = shadowInfo.getClone();
    StrictEqualityWindowWrapper wrapper = new StrictEqualityWindowWrapper(obtainedInstance);
    obtainedInstances.put(wrapper, Thread.currentThread().getStackTrace());
    return obtainedInstance;
  }

  private AccessibilityWindowInfo getClone() {
    final AccessibilityWindowInfo newInfo =
        ReflectionHelpers.callConstructor(AccessibilityWindowInfo.class);
    final ShadowAccessibilityWindowInfo newShadow = Shadow.extract(newInfo);

    newShadow.boundsInScreen = new Rect(boundsInScreen);
    newShadow.parent = parent;
    newShadow.rootNode = rootNode;
    newShadow.anchorNode = anchorNode;
    newShadow.type = type;
    newShadow.layer = layer;
    newShadow.setId(getId());
    newShadow.title = title;
    newShadow.isAccessibilityFocused = isAccessibilityFocused;
    newShadow.isActive = isActive;
    newShadow.isFocused = isFocused;
    if (children != null) {
      newShadow.children = new ArrayList<>(children);
    } else {
      newShadow.children = null;
    }

    return newInfo;
  }

  /**
   * Clear list of obtained instance objects. {@code areThereUnrecycledWindows} will always
   * return false if called immediately afterwards.
   */
  public static void resetObtainedInstances() {
    obtainedInstances.clear();
  }

  /**
   * Check for leaked objects that were {@code obtain}ed but never
   * {@code recycle}d.
   *
   * @param printUnrecycledWindowsToSystemErr - if true, stack traces of calls
   *        to {@code obtain} that lack matching calls to {@code recycle} are
   *        dumped to System.err.
   * @return {@code true} if there are unrecycled windows
   */
  public static boolean areThereUnrecycledWindows(boolean printUnrecycledWindowsToSystemErr) {
    if (printUnrecycledWindowsToSystemErr) {
      for (final StrictEqualityWindowWrapper wrapper : obtainedInstances.keySet()) {
        final ShadowAccessibilityWindowInfo shadow = Shadow.extract(wrapper.mInfo);

        System.err.println(
            String.format(
                "Leaked type = %d, id = %d. Stack trace:",
                shadow.getType(), wrapper.mInfo.getId()));
        for (final StackTraceElement stackTraceElement : obtainedInstances.get(wrapper)) {
          System.err.println(stackTraceElement.toString());
        }
      }
    }

    return (obtainedInstances.size() != 0);
  }

  @SuppressWarnings("ReferenceEquality")
  public boolean deepEquals(Object object) {
    if (!(object instanceof AccessibilityWindowInfo)) {
      return false;
    }

    final AccessibilityWindowInfo window = (AccessibilityWindowInfo) object;
    final ShadowAccessibilityWindowInfo otherShadow = Shadow.extract(window);

    boolean areEqual = (type == otherShadow.getType());
    areEqual &=
        (parent == null)
            ? (otherShadow.getParent() == null)
            : parent.equals(otherShadow.getParent());
    areEqual &=
        (rootNode == null)
            ? (otherShadow.getRoot() == null)
            : rootNode.equals(otherShadow.getRoot());
    areEqual &=
        (anchorNode == null)
            ? (otherShadow.getAnchor() == null)
            : anchorNode.equals(otherShadow.getAnchor());
    areEqual &= (layer == otherShadow.getLayer());
    areEqual &= (getId() == otherShadow.getId());
    areEqual &= (title == otherShadow.getTitle());
    areEqual &= (isAccessibilityFocused == otherShadow.isAccessibilityFocused());
    areEqual &= (isActive == otherShadow.isActive());
    areEqual &= (isFocused == otherShadow.isFocused());
    Rect anotherBounds = new Rect();
    otherShadow.getBoundsInScreen(anotherBounds);
    areEqual &= (boundsInScreen.equals(anotherBounds));
    return areEqual;
  }

  @Override
  @Implementation
  public int hashCode() {
    // This is 0 for a reason. If you change it, you will break the obtained instances map in
    // a manner that is remarkably difficult to debug. Having a dynamic hash code keeps this
    // object from being located in the map if it was mutated after being obtained.
    return 0;
  }

  @Implementation
  protected int getType() {
    return type;
  }

  @Implementation
  protected int getChildCount() {
    if (children == null) {
      return 0;
    }

    return children.size();
  }

  @Implementation
  protected AccessibilityWindowInfo getChild(int index) {
    if (children == null) {
      return null;
    }

    return children.get(index);
  }

  @Implementation
  protected AccessibilityWindowInfo getParent() {
    return parent;
  }

  @Implementation
  protected AccessibilityNodeInfo getRoot() {
    return (rootNode == null) ? null : AccessibilityNodeInfo.obtain(rootNode);
  }

  @Implementation(minSdk = N)
  protected AccessibilityNodeInfo getAnchor() {
    return (anchorNode == null) ? null : AccessibilityNodeInfo.obtain(anchorNode);
  }

  @Implementation
  protected boolean isActive() {
    return isActive;
  }

  @Implementation
  protected int getId() {
    return reflector(AccessibilityWindowInfoReflector.class, mRealAccessibilityWindowInfo).getId();
  }

  @Implementation
  protected void getBoundsInScreen(Rect outBounds) {
    if (boundsInScreen == null) {
      outBounds.setEmpty();
    } else {
      outBounds.set(boundsInScreen);
    }
  }

  @Implementation
  protected int getLayer() {
    return layer;
  }

  /** Returns the title of this window, or {@code null} if none is available. */
  @Implementation(minSdk = N)
  protected CharSequence getTitle() {
    return title;
  }

  @Implementation
  protected boolean isFocused() {
    return isFocused;
  }

  @Implementation
  protected boolean isAccessibilityFocused() {
    return isAccessibilityFocused;
  }

  @Implementation
  protected void recycle() {
    // This shadow does not track recycling of windows.
  }

  public void setRoot(AccessibilityNodeInfo root) {
    rootNode = root;
  }

  public void setAnchor(AccessibilityNodeInfo anchor) {
    anchorNode = anchor;
  }

  public void setType(int value) {
    type = value;
  }

  public void setBoundsInScreen(Rect bounds) {
    boundsInScreen.set(bounds);
  }

  public void setAccessibilityFocused(boolean value) {
    isAccessibilityFocused = value;
  }

  public void setActive(boolean value) {
    isActive = value;
  }

  public void setId(int value) {
    reflector(AccessibilityWindowInfoReflector.class, mRealAccessibilityWindowInfo).setId(value);
  }

  public void setLayer(int value) {
    layer = value;
  }

  /**
   * Sets the title of this window.
   *
   * @param value The {@link CharSequence} to set as the title of this window
   */
  public void setTitle(CharSequence value) {
    title = value;
  }

  public void setFocused(boolean focused) {
    isFocused = focused;
  }

  public void addChild(AccessibilityWindowInfo child) {
    if (children == null) {
      children = new ArrayList<>();
    }

    children.add(child);
    ((ShadowAccessibilityWindowInfo) Shadow.extract(child)).parent =
        mRealAccessibilityWindowInfo;
  }

  /**
   * Private class to keep different windows referring to the same window straight
   * in the mObtainedInstances map.
   */
  private static class StrictEqualityWindowWrapper {
    public final AccessibilityWindowInfo mInfo;

    public StrictEqualityWindowWrapper(AccessibilityWindowInfo info) {
      mInfo = info;
    }

    @Override
    @SuppressWarnings("ReferenceEquality")
    public boolean equals(Object object) {
      if (object == null) {
        return false;
      }

      if (!(object instanceof StrictEqualityWindowWrapper)) {
        return false;
      }
      final StrictEqualityWindowWrapper wrapper = (StrictEqualityWindowWrapper) object;
      return mInfo == wrapper.mInfo;
    }

    @Override
    public int hashCode() {
      return mInfo.hashCode();
    }
  }

  @Override
  @Implementation
  public String toString() {
    return "ShadowAccessibilityWindowInfo@"
        + System.identityHashCode(this)
        + ":{id:"
        + getId()
        + ", title:"
        + title
        + "}";
  }

  @ForType(AccessibilityWindowInfo.class)
  interface AccessibilityWindowInfoReflector {

    @Direct
    int getId();

    @Direct
    void setId(int value);
  }
}
