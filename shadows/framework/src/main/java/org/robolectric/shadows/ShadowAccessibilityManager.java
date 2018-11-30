package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.IAccessibilityManager;
import java.util.List;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(AccessibilityManager.class)
public class ShadowAccessibilityManager {
  private static AccessibilityManager sInstance;
  private static final Object sInstanceSync = new Object();

  @RealObject AccessibilityManager realAccessibilityManager;
  private boolean enabled;
  private List<AccessibilityServiceInfo> installedAccessibilityServiceList;
  private List<AccessibilityServiceInfo> enabledAccessibilityServiceList;
  private List<ServiceInfo> accessibilityServiceList;
  private boolean touchExplorationEnabled;

  private static boolean isAccessibilityButtonSupported = true;

  @Resetter
  public static void reset() {
    synchronized (sInstanceSync) {
      sInstance = null;
    }
    isAccessibilityButtonSupported = true;
  }

  @HiddenApi
  @Implementation
  public static AccessibilityManager getInstance(Context context) throws Exception {
    synchronized (sInstanceSync) {
      if (sInstance == null) {
          sInstance = createInstance(context);
      }
    }
    return sInstance;
  }

  private static AccessibilityManager createInstance(Context context) throws Exception {
    if (getApiLevel() >= KITKAT) {
      AccessibilityManager accessibilityManager = Shadow.newInstance(AccessibilityManager.class,
          new Class[]{Context.class, IAccessibilityManager.class, int.class},
          new Object[]{context, ReflectionHelpers.createNullProxy(IAccessibilityManager.class), 0});
      ReflectionHelpers.setField(accessibilityManager, "mHandler", new MyHandler(context.getMainLooper(), accessibilityManager));
      return accessibilityManager;
    } else {
      AccessibilityManager accessibilityManager = Shadow.newInstance(AccessibilityManager.class, new Class[0], new Object[0]);
      ReflectionHelpers.setField(accessibilityManager, "mHandler", new MyHandler(context.getMainLooper(), accessibilityManager));
      return accessibilityManager;
    }
  }

  @Implementation
  protected boolean addAccessibilityStateChangeListener(
      AccessibilityManager.AccessibilityStateChangeListener listener) {
    return true;
  }

  @Implementation
  protected boolean removeAccessibilityStateChangeListener(
      AccessibilityManager.AccessibilityStateChangeListener listener) {
    return true;
  }

  @Implementation
  protected List<ServiceInfo> getAccessibilityServiceList() {
    return accessibilityServiceList;
  }

  public void setAccessibilityServiceList(List<ServiceInfo> accessibilityServiceList) {
    this.accessibilityServiceList = accessibilityServiceList;
  }

  @Implementation
  protected List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(
      int feedbackTypeFlags) {
    return enabledAccessibilityServiceList;
  }

  public void setEnabledAccessibilityServiceList(List<AccessibilityServiceInfo> enabledAccessibilityServiceList) {
    this.enabledAccessibilityServiceList = enabledAccessibilityServiceList;
  }

  @Implementation
  protected List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList() {
    return installedAccessibilityServiceList;
  }

  public void setInstalledAccessibilityServiceList(List<AccessibilityServiceInfo> installedAccessibilityServiceList) {
    this.installedAccessibilityServiceList = installedAccessibilityServiceList;
  }

  @Implementation
  protected boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    ReflectionHelpers.setField(realAccessibilityManager, "mIsEnabled", enabled);
  }

  @Implementation
  protected boolean isTouchExplorationEnabled() {
    return touchExplorationEnabled;
  }

  public void setTouchExplorationEnabled(boolean touchExplorationEnabled) {
    this.touchExplorationEnabled = touchExplorationEnabled;
  }

  /**
   * Returns {@code true} by default, or the value specified via {@link
   * #setAccessibilityButtonSupported(boolean)}
   */
  @Implementation(minSdk = O_MR1)
  protected static boolean isAccessibilityButtonSupported() {
    return isAccessibilityButtonSupported;
  }

  @HiddenApi
  @Implementation(minSdk = O)
  protected void performAccessibilityShortcut() {
    setEnabled(true);
    setTouchExplorationEnabled(true);
  }

  /**
   * Sets that the system navigation area is supported accessibility button; controls the return
   * value of {@link AccessibilityManager#isAccessibilityButtonSupported()}.
   */
  public static void setAccessibilityButtonSupported(boolean supported) {
    isAccessibilityButtonSupported = supported;
  }

  static class MyHandler extends Handler {
    private static final int DO_SET_STATE = 10;
    private final AccessibilityManager accessibilityManager;

    MyHandler(Looper mainLooper, AccessibilityManager accessibilityManager) {
      super(mainLooper);
      this.accessibilityManager = accessibilityManager;
    }

    @Override
    public void handleMessage(Message message) {
      switch (message.what) {
        case DO_SET_STATE:
          ReflectionHelpers.callInstanceMethod(accessibilityManager, "setState", ClassParameter.from(int.class, message.arg1));
          return;
        default:
          Log.w("AccessibilityManager", "Unknown message type: " + message.what);
      }
    }
  }
}
