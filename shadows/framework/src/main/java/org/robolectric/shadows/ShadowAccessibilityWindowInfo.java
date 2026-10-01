package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.shadows.ShadowAccessibilityNodeInfo.checkRealAniDisabled;
import static org.robolectric.shadows.ShadowAccessibilityNodeInfo.useRealAni;
import static org.robolectric.util.reflector.Reflector.reflector;
import static org.robolectric.versioning.VersionCalculator.POST_CINNAMON_BUN;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build.VERSION_CODES;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Shadow of {@link android.view.accessibility.AccessibilityWindowInfo} that allows a test to set
 * properties that are locked in the original class.
 */
@Implements(AccessibilityWindowInfo.class)
public class ShadowAccessibilityWindowInfo {
  private List<AccessibilityWindowInfo> children = null;

  private AccessibilityWindowInfo parent = null;

  private AccessibilityNodeInfo rootNode = null;

  private AccessibilityNodeInfo anchorNode = null;

  private AccessibilityWindowInfo controllingWindow = null;

  private List<AccessibilityWindowInfo> controlledWindows = null;

  private Rect boundsInScreenOverride;

  @RealObject private AccessibilityWindowInfo realAccessibilityWindowInfo;

  @Implementation
  protected static AccessibilityWindowInfo obtain(AccessibilityWindowInfo window) {
    final AccessibilityWindowInfo newInstance =
        reflector(AccessibilityWindowInfoReflector.class).obtain(window);

    if (useRealAni()) {
      return newInstance;
    }

    final ShadowAccessibilityWindowInfo shadowInfo = Shadow.extract(window);
    final ShadowAccessibilityWindowInfo newShadow = Shadow.extract(newInstance);

    if (shadowInfo.boundsInScreenOverride != null) {
      newShadow.boundsInScreenOverride = new Rect(shadowInfo.boundsInScreenOverride);
    }
    newShadow.parent = shadowInfo.parent;
    newShadow.rootNode = shadowInfo.rootNode;
    newShadow.anchorNode = shadowInfo.anchorNode;
    newShadow.controllingWindow = shadowInfo.controllingWindow;

    if (shadowInfo.children != null) {
      newShadow.children = new ArrayList<>(shadowInfo.children);
    } else {
      newShadow.children = null;
    }
    if (shadowInfo.controlledWindows != null) {
      newShadow.controlledWindows = new ArrayList<>(shadowInfo.controlledWindows);
    } else {
      newShadow.controlledWindows = null;
    }
    return newInstance;
  }

  @Implementation
  protected int getChildCount() {
    if (useRealAni()) {
      return reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getChildCount();
    }
    if (children == null) {
      return 0;
    }

    return children.size();
  }

  @Implementation
  protected AccessibilityWindowInfo getChild(int index) {
    if (useRealAni()) {
      return reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getChild(index);
    }
    if (children == null) {
      return null;
    }

    return children.get(index);
  }

  @Implementation
  protected AccessibilityWindowInfo getParent() {
    if (useRealAni()) {
      return reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getParent();
    }
    return parent;
  }

  @Implementation
  protected AccessibilityNodeInfo getRoot() {
    if (useRealAni()) {
      return reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getRoot();
    }
    return (rootNode == null) ? null : AccessibilityNodeInfo.obtain(rootNode);
  }

  @Implementation(minSdk = N)
  protected AccessibilityNodeInfo getAnchor() {
    if (useRealAni()) {
      return reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getAnchor();
    }
    return (anchorNode == null) ? null : AccessibilityNodeInfo.obtain(anchorNode);
  }

  @Implementation(minSdk = POST_CINNAMON_BUN)
  protected AccessibilityWindowInfo getControllingWindow() {
    if (useRealAni()) {
      return reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getControllingWindow();
    }
    return controllingWindow;
  }

  @Implementation(minSdk = POST_CINNAMON_BUN)
  protected int getControlledWindowsCount() {
    if (useRealAni()) {
      return reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getControlledWindowsCount();
    }
    return (controlledWindows == null) ? 0 : controlledWindows.size();
  }

  @Implementation(minSdk = POST_CINNAMON_BUN)
  protected AccessibilityWindowInfo getControlledWindow(int index) {
    if (useRealAni()) {
      return reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getControlledWindow(index);
    }
    if (controlledWindows == null) {
      throw new IndexOutOfBoundsException();
    }
    return controlledWindows.get(index);
  }

  @Implementation
  protected void getBoundsInScreen(Rect outBounds) {
    if (useRealAni() || boundsInScreenOverride == null) {
      reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getBoundsInScreen(outBounds);
    } else {
      outBounds.set(boundsInScreenOverride);
    }
  }

  @Implementation(minSdk = R)
  protected void getRegionInScreen(Region outRegion) {
    if (useRealAni() || boundsInScreenOverride == null) {
      reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .getRegionInScreen(outRegion);
    } else {
      outRegion.set(new Region(boundsInScreenOverride));
    }
  }

  @Filter(order = Filter.Order.AFTER)
  protected void recycle() {
    // Clear shadow fields.
    children = null;
    parent = null;
    rootNode = null;
    anchorNode = null;
    controllingWindow = null;
    controlledWindows = null;
    boundsInScreenOverride = null;
  }

  public void setRoot(AccessibilityNodeInfo root) {
    checkRealAniDisabled();
    rootNode = root;
  }

  public void setAnchor(AccessibilityNodeInfo anchor) {
    checkRealAniDisabled();
    anchorNode = anchor;
  }

  @Implementation
  public void setType(int value) {
    reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo).setType(value);
  }

  @Implementation(maxSdk = Q)
  public void setBoundsInScreen(Rect bounds) {
    if (RuntimeEnvironment.getApiLevel() <= Q) {
      reflector(AccessibilityWindowInfoReflector.class, realAccessibilityWindowInfo)
          .setBoundsInScreen(bounds);
    } else {
      boundsInScreenOverride = bounds;
    }
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
    checkRealAniDisabled();
    if (children == null) {
      children = new ArrayList<>();
    }

    children.add(child);
    ((ShadowAccessibilityWindowInfo) Shadow.extract(child)).parent = realAccessibilityWindowInfo;
  }

  /**
   * Adds a window controlled by this window, as returned by {@code getControlledWindow(int)}.
   *
   * <p>The controlled window's controlling window is set to this window. A {@code null} value may
   * be added to simulate a controlled window that is no longer available.
   *
   * @param controlledWindow the controlled window, or {@code null}
   */
  public void addControlledWindow(AccessibilityWindowInfo controlledWindow) {
    checkRealAniDisabled();
    if (controlledWindows == null) {
      controlledWindows = new ArrayList<>();
    }

    controlledWindows.add(controlledWindow);
    if (controlledWindow != null) {
      ((ShadowAccessibilityWindowInfo) Shadow.extract(controlledWindow)).controllingWindow =
          realAccessibilityWindowInfo;
    }
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
    void setBoundsInScreen(Rect bounds);

    @Direct
    void getBoundsInScreen(Rect outBounds);

    @Direct
    void getRegionInScreen(Region outRegion);

    @Direct
    int getChildCount();

    @Direct
    AccessibilityWindowInfo getChild(int index);

    @Direct
    AccessibilityWindowInfo getParent();

    @Direct
    AccessibilityNodeInfo getRoot();

    @Direct
    AccessibilityNodeInfo getAnchor();

    @Direct
    AccessibilityWindowInfo getControllingWindow();

    @Direct
    int getControlledWindowsCount();

    @Direct
    AccessibilityWindowInfo getControlledWindow(int index);

    @Direct
    @Static
    AccessibilityWindowInfo obtain(AccessibilityWindowInfo window);
  }
}
