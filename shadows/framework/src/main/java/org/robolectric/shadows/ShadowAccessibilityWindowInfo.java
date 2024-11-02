package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityWindowInfo} that allows a test to set
 * properties that are locked in the original class.
 */
@Implements(value = AccessibilityWindowInfo.class)
public class ShadowAccessibilityWindowInfo {

  private static final Map<StrictEqualityWindowWrapper, StackTraceElement[]> obtainedInstances =
      new HashMap<>();

  private List<AccessibilityWindowInfo> children = null;

  private AccessibilityWindowInfo parent = null;

  private AccessibilityNodeInfo rootNode = null;

  private AccessibilityNodeInfo anchorNode = null;

  private Rect boundsInScreen = new Rect();

  @RealObject private AccessibilityWindowInfo realAccessibilityWindowInfo;

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
    final AccessibilityWindowInfo newInstance =
        reflector(AccessibilityWindowInfoReflector.class).obtain(window);
    StrictEqualityWindowWrapper wrapper = new StrictEqualityWindowWrapper(newInstance);
    obtainedInstances.put(wrapper, Thread.currentThread().getStackTrace());

    final ShadowAccessibilityWindowInfo shadowInfo = Shadow.extract(window);
    final ShadowAccessibilityWindowInfo newShadow = Shadow.extract(newInstance);

    newShadow.boundsInScreen = new Rect(shadowInfo.boundsInScreen);
    newShadow.parent = shadowInfo.parent;
    newShadow.rootNode = shadowInfo.rootNode;
    newShadow.anchorNode = shadowInfo.anchorNode;

    if (shadowInfo.children != null) {
      newShadow.children = new ArrayList<>(shadowInfo.children);
    } else {
      newShadow.children = null;
    }
    return newInstance;
  }

  /**
   * Clear list of obtained instance objects. {@code areThereUnrecycledWindows} will always return
   * false if called immediately afterwards.
   */
  public static void resetObtainedInstances() {
    obtainedInstances.clear();
  }

  /**
   * Check for leaked objects that were {@code obtain}ed but never {@code recycle}d.
   *
   * @param printUnrecycledWindowsToSystemErr - if true, stack traces of calls to {@code obtain}
   *     that lack matching calls to {@code recycle} are dumped to System.err.
   * @return {@code true} if there are unrecycled windows
   */
  public static boolean areThereUnrecycledWindows(boolean printUnrecycledWindowsToSystemErr) {
    if (printUnrecycledWindowsToSystemErr) {
      for (final StrictEqualityWindowWrapper wrapper : obtainedInstances.keySet()) {
        final ShadowAccessibilityWindowInfo shadow = Shadow.extract(wrapper.mInfo);

        System.err.println(
            String.format(
                "Leaked type = %d, id = %d. Stack trace:",
                shadow.realAccessibilityWindowInfo.getType(), wrapper.mInfo.getId()));
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

    boolean areEqual = (realAccessibilityWindowInfo.getType() == window.getType());
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
    areEqual &= (realAccessibilityWindowInfo.getLayer() == window.getLayer());
    areEqual &= (realAccessibilityWindowInfo.getId() == window.getId());
    if (RuntimeEnvironment.getApiLevel() >= N) {
      areEqual &= (realAccessibilityWindowInfo.getTitle() == window.getTitle());
    }
    areEqual &=
        (realAccessibilityWindowInfo.isAccessibilityFocused() == window.isAccessibilityFocused());
    areEqual &= (realAccessibilityWindowInfo.isActive() == window.isActive());
    areEqual &= (realAccessibilityWindowInfo.isFocused() == window.isFocused());
    Rect anotherBounds = new Rect();
    otherShadow.getBoundsInScreen(anotherBounds);
    areEqual &= boundsInScreen.equals(anotherBounds);
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
  protected void getBoundsInScreen(Rect outBounds) {
    if (boundsInScreen == null) {
      outBounds.setEmpty();
    } else {
      outBounds.set(boundsInScreen);
    }
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

  @Implementation
  public void setType(int value) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo).setType(value);
  }

  @Implementation(maxSdk = Q)
  public void setBoundsInScreen(Rect bounds) {
    boundsInScreen.set(bounds);
  }

  @Implementation
  public void setAccessibilityFocused(boolean value) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
        .setAccessibilityFocused(value);
  }

  @Implementation
  public void setActive(boolean value) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo).setActive(value);
  }

  @Implementation
  public void setId(int value) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo).setId(value);
  }

  @Implementation
  public void setLayer(int value) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo).setLayer(value);
  }

  /**
   * Sets the title of this window.
   *
   * @param value The {@link CharSequence} to set as the title of this window
   */
  @Implementation(minSdk = N)
  public void setTitle(CharSequence value) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo).setTitle(value);
  }

  @Implementation
  public void setFocused(boolean focused) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
        .setFocused(focused);
  }

  @Implementation(minSdk = O)
  public void setPictureInPicture(boolean pictureInPicture) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
        .setPictureInPicture(pictureInPicture);
  }

  @Implementation(minSdk = VERSION_CODES.R)
  public void setDisplayId(int displayId) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
        .setDisplayId(displayId);
  }

  public void addChild(AccessibilityWindowInfo child) {
    if (children == null) {
      children = new ArrayList<>();
    }

    children.add(child);
    ((ShadowAccessibilityWindowInfo) Shadow.extract(child)).parent = realAccessibilityWindowInfo;
  }

  /**
   * Private class to keep different windows referring to the same window straight in the
   * mObtainedInstances map.
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
        + realAccessibilityWindowInfo.getId()
        + ", title:"
        + realAccessibilityWindowInfo.getTitle()
        + "}";
  }

  @ForType(AccessibilityWindowInfo.class)
  interface AccessibilityWindowInfoReflector {

    @Direct
    void setId(int value);

    @Direct
    void setType(int value);

    @Direct
    void setAccessibilityFocused(boolean value);

    @Direct
    void setActive(boolean value);

    @Direct
    void setLayer(int value);

    @Direct
    void setTitle(CharSequence value);

    @Direct
    void setFocused(boolean focused);

    @Direct
    void setPictureInPicture(boolean pictureInPicture);

    @Direct
    void setDisplayId(int displayId);

    @Direct
    @Static
    AccessibilityWindowInfo obtain(AccessibilityWindowInfo window);
  }
}
