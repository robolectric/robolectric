package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MagnificationSpec;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import android.view.accessibility.IAccessibilityManager;
import android.view.accessibility.IAccessibilityManager.WindowTransformationSpec;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

@Implements(AccessibilityManager.class)
public class ShadowAccessibilityManager {
  private static AccessibilityManager sInstance;
  private static final Object sInstanceSync = new Object();

  @RealObject AccessibilityManager realAccessibilityManager;
  private static final List<AccessibilityEvent> sentAccessibilityEvents = new ArrayList<>();
  private static boolean enabled;
  private static List<AccessibilityServiceInfo> installedAccessibilityServiceList =
      new ArrayList<>();
  private static List<AccessibilityServiceInfo> enabledAccessibilityServiceList = new ArrayList<>();
  private static List<ServiceInfo> accessibilityServiceList = new ArrayList<>();
  private static final HashMap<AccessibilityStateChangeListener, Handler>
      onAccessibilityStateChangeListeners = new HashMap<>();
  private static boolean touchExplorationEnabled;

  private static boolean isAccessibilityButtonSupported = true;

  @Resetter
  public static void reset() {
    synchronized (sInstanceSync) {
      sInstance = null;
    }
    sentAccessibilityEvents.clear();
    enabled = false;
    installedAccessibilityServiceList.clear();
    enabledAccessibilityServiceList.clear();
    accessibilityServiceList.clear();
    onAccessibilityStateChangeListeners.clear();
    touchExplorationEnabled = false;
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

  private static AccessibilityManager createInstance(Context context) {
    AccessibilityManager accessibilityManager =
        Shadow.newInstance(
            AccessibilityManager.class,
            new Class[] {Context.class, IAccessibilityManager.class, int.class},
            new Object[] {
              context, ReflectionHelpers.createNullProxy(IAccessibilityManager.class), 0
            });
    ReflectionHelpers.setField(
        accessibilityManager,
        "mHandler",
        new MyHandler(context.getMainLooper(), accessibilityManager));
    return accessibilityManager;
  }

  @Implementation
  protected boolean addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
    addAccessibilityStateChangeListener(listener, null);
    return true;
  }

  @Implementation(minSdk = O)
  protected void addAccessibilityStateChangeListener(
      AccessibilityStateChangeListener listener, Handler handler) {
    onAccessibilityStateChangeListeners.put(listener, handler);
  }

  @Implementation
  protected boolean removeAccessibilityStateChangeListener(
      AccessibilityStateChangeListener listener) {
    final boolean wasRegistered = onAccessibilityStateChangeListeners.containsKey(listener);
    onAccessibilityStateChangeListeners.remove(listener);
    return wasRegistered;
  }

  @Implementation
  protected List<ServiceInfo> getAccessibilityServiceList() {
    return Collections.unmodifiableList(accessibilityServiceList);
  }

  public void setInteractiveUiTimeout(int interactiveUiTimeoutMillis) {
    ReflectionHelpers.setField(
        realAccessibilityManager, "mInteractiveUiTimeout", interactiveUiTimeoutMillis);
  }

  public void setNonInteractiveUiTimeout(int nonInteractiveUiTimeoutMillis) {
    ReflectionHelpers.setField(
        realAccessibilityManager, "mNonInteractiveUiTimeout", nonInteractiveUiTimeoutMillis);
  }

  public void setAccessibilityServiceList(List<ServiceInfo> accessibilityServiceList) {
    Preconditions.checkNotNull(accessibilityServiceList);
    this.accessibilityServiceList = new ArrayList<>(accessibilityServiceList);
  }

  @Nullable
  @Implementation
  protected List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(
      int feedbackTypeFlags) {
    return Collections.unmodifiableList(enabledAccessibilityServiceList);
  }

  public void setEnabledAccessibilityServiceList(
      List<AccessibilityServiceInfo> enabledAccessibilityServiceList) {
    Preconditions.checkNotNull(enabledAccessibilityServiceList);
    this.enabledAccessibilityServiceList = new ArrayList<>(enabledAccessibilityServiceList);
  }

  @Implementation
  protected List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList() {
    return Collections.unmodifiableList(installedAccessibilityServiceList);
  }

  public void setInstalledAccessibilityServiceList(
      List<AccessibilityServiceInfo> installedAccessibilityServiceList) {
    Preconditions.checkNotNull(installedAccessibilityServiceList);
    this.installedAccessibilityServiceList = new ArrayList<>(installedAccessibilityServiceList);
  }

  @Implementation
  protected void sendAccessibilityEvent(AccessibilityEvent event) {
    sentAccessibilityEvents.add(event);
    reflector(AccessibilityManagerReflector.class, realAccessibilityManager)
        .sendAccessibilityEvent(event);
  }

  /**
   * Returns a list of all {@linkplain AccessibilityEvent accessibility events} that have been sent
   * via {@link #sendAccessibilityEvent}.
   */
  public ImmutableList<AccessibilityEvent> getSentAccessibilityEvents() {
    return ImmutableList.copyOf(sentAccessibilityEvents);
  }

  @Implementation
  protected boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    ReflectionHelpers.setField(realAccessibilityManager, "mIsEnabled", enabled);
    for (AccessibilityStateChangeListener l : onAccessibilityStateChangeListeners.keySet()) {
      if (l != null) {
        l.onAccessibilityStateChanged(enabled);
      }
    }
  }

  @Implementation
  protected boolean isTouchExplorationEnabled() {
    return touchExplorationEnabled;
  }

  public void setTouchExplorationEnabled(boolean touchExplorationEnabled) {
    this.touchExplorationEnabled = touchExplorationEnabled;
    List<TouchExplorationStateChangeListener> listeners = new ArrayList<>();
    if (getApiLevel() >= O) {
      listeners =
          new ArrayList<>(
              reflector(AccessibilityManagerReflector.class, realAccessibilityManager)
                  .getTouchExplorationStateChangeListeners()
                  .keySet());
    } else {
      listeners =
          new ArrayList<>(
              reflector(AccessibilityManagerReflectorN.class, realAccessibilityManager)
                  .getTouchExplorationStateChangeListeners());
    }
    listeners.forEach(listener -> listener.onTouchExplorationStateChanged(touchExplorationEnabled));
  }

  /**
   * Returns {@code true} by default, or the value specified via {@link
   * #setAccessibilityButtonSupported(boolean)}.
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
   * This shadow method is required because {@link
   * android.view.accessibility.DirectAccessibilityConnection} calls it to determine if any
   * transformations have occurred on this window.
   */
  @Implementation(minSdk = U.SDK_INT)
  protected @ClassName("android.view.accessibility.IAccessibilityManager.WindowTransformationSpec")
  Object getWindowTransformationSpec(int windowId) {
    // Return a value that represents no transformation.
    WindowTransformationSpec spec = new WindowTransformationSpec();
    spec.magnificationSpec = new MagnificationSpec();
    float[] matrix = new float[9];
    Matrix.IDENTITY_MATRIX.getValues(matrix);
    spec.transformationMatrix = matrix;
    return spec;
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
          ReflectionHelpers.callInstanceMethod(
              accessibilityManager, "setState", ClassParameter.from(int.class, message.arg1));
          return;
        default:
          Log.w("AccessibilityManager", "Unknown message type: " + message.what);
      }
    }
  }

  @ForType(AccessibilityManager.class)
  interface AccessibilityManagerReflector {

    @Direct
    void sendAccessibilityEvent(AccessibilityEvent event);

    @Accessor("mTouchExplorationStateChangeListeners")
    ArrayMap<TouchExplorationStateChangeListener, Handler>
        getTouchExplorationStateChangeListeners();
  }

  @ForType(AccessibilityManager.class)
  interface AccessibilityManagerReflectorN {
    @Accessor("mTouchExplorationStateChangeListeners")
    CopyOnWriteArrayList<TouchExplorationStateChangeListener>
        getTouchExplorationStateChangeListeners();
  }
}
