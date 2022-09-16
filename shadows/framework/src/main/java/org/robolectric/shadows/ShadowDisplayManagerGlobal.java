package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Point;
import android.hardware.display.BrightnessChangeEvent;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.hardware.display.IDisplayManagerCallback;
import android.hardware.display.WifiDisplayStatus;
import android.os.Handler;
import android.os.RemoteException;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.robolectric.android.Bootstrap;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link DisplayManagerGlobal}. */
@Implements(
    value = DisplayManagerGlobal.class,
    isInAndroidSdk = false,
    minSdk = JELLY_BEAN_MR1,
    looseSignatures = true)
public class ShadowDisplayManagerGlobal {
  private static DisplayManagerGlobal instance;

  private float saturationLevel = 1f;
  private final SparseArray<BrightnessConfiguration> brightnessConfiguration = new SparseArray<>();
  private final List<BrightnessChangeEvent> brightnessChangeEvents = new ArrayList<>();
  private Object defaultBrightnessConfiguration;

  private MyDisplayManager mDm;

  @Resetter
  public static void reset() {
    instance = null;
  }

  @Implementation
  protected void __constructor__(IDisplayManager dm) {
    // No-op the constructor. The real constructor references the ColorSpace named constants, which
    // require native calls to instantiate. This will cause native graphics libraries to be loaded
    // any time an Application object is created. Instead override the constructor to avoid
    // referencing the ColorSpace named constants, making application creation around 0.75s faster.
  }

  @Implementation
  public static synchronized DisplayManagerGlobal getInstance() {
    if (instance == null) {
      MyDisplayManager myIDisplayManager = new MyDisplayManager();
      IDisplayManager proxy =
          ReflectionHelpers.createDelegatingProxy(IDisplayManager.class, myIDisplayManager);
      instance = newDisplayManagerGlobal(proxy);
      ShadowDisplayManagerGlobal shadow = Shadow.extract(instance);
      shadow.mDm = myIDisplayManager;
      Bootstrap.setUpDisplay();
    }
    return instance;
  }

  private static DisplayManagerGlobal newDisplayManagerGlobal(IDisplayManager displayManager) {
    instance = Shadow.newInstanceOf(DisplayManagerGlobal.class);
    DisplayManagerGlobalReflector displayManagerGlobal =
        reflector(DisplayManagerGlobalReflector.class, instance);
    displayManagerGlobal.setDm(displayManager);
    displayManagerGlobal.setLock(new Object());
    displayManagerGlobal.setDisplayListeners(new ArrayList<>());
    displayManagerGlobal.setDisplayInfoCache(new SparseArray<>());
    return instance;
  }

  @VisibleForTesting
  static DisplayManagerGlobal getGlobalInstance() {
    return instance;
  }

  @Implementation
  protected WifiDisplayStatus getWifiDisplayStatus() {
    return new WifiDisplayStatus();
  }

  /** Returns the 'natural' dimensions of the default display. */
  @Implementation(minSdk = O_MR1)
  public Point getStableDisplaySize() throws RemoteException {
    DisplayInfo defaultDisplayInfo = mDm.getDisplayInfo(Display.DEFAULT_DISPLAY);
    return new Point(defaultDisplayInfo.getNaturalWidth(), defaultDisplayInfo.getNaturalHeight());
  }

  int addDisplay(DisplayInfo displayInfo) {
    fixNominalDimens(displayInfo);

    return mDm.addDisplay(displayInfo);
  }

  private void fixNominalDimens(DisplayInfo displayInfo) {
    int min = Math.min(displayInfo.appWidth, displayInfo.appHeight);
    int max = Math.max(displayInfo.appWidth, displayInfo.appHeight);
    displayInfo.smallestNominalAppHeight = displayInfo.smallestNominalAppWidth = min;
    displayInfo.largestNominalAppHeight = displayInfo.largestNominalAppWidth = max;
  }

  void changeDisplay(int displayId, DisplayInfo displayInfo) {
    mDm.changeDisplay(displayId, displayInfo);
  }

  void removeDisplay(int displayId) {
    mDm.removeDisplay(displayId);
  }

  private static class MyDisplayManager {
    private final TreeMap<Integer, DisplayInfo> displayInfos = new TreeMap<>();
    private int nextDisplayId = 0;
    private final List<IDisplayManagerCallback> callbacks = new ArrayList<>();

    // @Override
    public DisplayInfo getDisplayInfo(int i) throws RemoteException {
      DisplayInfo displayInfo = displayInfos.get(i);
      return displayInfo == null ? null : new DisplayInfo(displayInfo);
    }

    // @Override // todo: use @Implements/@Implementation for signature checking
    public int[] getDisplayIds() {
      int[] ids = new int[displayInfos.size()];
      int i = 0;
      for (Integer displayId : displayInfos.keySet()) {
        ids[i++] = displayId;
      }
      return ids;
    }

    // Added in Android T
    @SuppressWarnings("unused")
    public int[] getDisplayIds(boolean ignoredIncludeDisabled) {
      return getDisplayIds();
    }

    // @Override
    public void registerCallback(IDisplayManagerCallback iDisplayManagerCallback)
        throws RemoteException {
      this.callbacks.add(iDisplayManagerCallback);
    }

    public void registerCallbackWithEventMask(
        IDisplayManagerCallback iDisplayManagerCallback, long ignoredEventsMask)
        throws RemoteException {
      registerCallback(iDisplayManagerCallback);
    }

    private synchronized int addDisplay(DisplayInfo displayInfo) {
      int nextId = nextDisplayId++;
      displayInfos.put(nextId, displayInfo);
      notifyListeners(nextId, DisplayManagerGlobal.EVENT_DISPLAY_ADDED);
      return nextId;
    }

    private synchronized void changeDisplay(int displayId, DisplayInfo displayInfo) {
      if (!displayInfos.containsKey(displayId)) {
        throw new IllegalStateException("no display " + displayId);
      }

      displayInfos.put(displayId, displayInfo);
      notifyListeners(displayId, DisplayManagerGlobal.EVENT_DISPLAY_CHANGED);
    }

    private synchronized void removeDisplay(int displayId) {
      if (!displayInfos.containsKey(displayId)) {
        throw new IllegalStateException("no display " + displayId);
      }

      displayInfos.remove(displayId);
      notifyListeners(displayId, DisplayManagerGlobal.EVENT_DISPLAY_REMOVED);
    }

    private void notifyListeners(int nextId, int event) {
      for (IDisplayManagerCallback callback : callbacks) {
        try {
          callback.onDisplayEvent(nextId, event);
        } catch (RemoteException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  @Implementation(minSdk = P, maxSdk = P)
  protected void setSaturationLevel(float level) {
    if (level < 0f || level > 1f) {
      throw new IllegalArgumentException("Saturation level must be between 0 and 1");
    }
    saturationLevel = level;
  }

  /**
   * Returns the current display saturation level; {@link android.os.Build.VERSION_CODES.P} only.
   */
  float getSaturationLevel() {
    return saturationLevel;
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected void setBrightnessConfigurationForUser(
      Object configObject, Object userId, Object packageName) {
    BrightnessConfiguration config = (BrightnessConfiguration) configObject;
    brightnessConfiguration.put((int) userId, config);
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected Object getBrightnessConfigurationForUser(int userId) {
    BrightnessConfiguration config = brightnessConfiguration.get(userId);
    if (config != null) {
      return config;
    } else {
      return getDefaultBrightnessConfiguration();
    }
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected Object getDefaultBrightnessConfiguration() {
    return defaultBrightnessConfiguration;
  }

  void setDefaultBrightnessConfiguration(@Nullable Object configObject) {
    BrightnessConfiguration config = (BrightnessConfiguration) configObject;
    defaultBrightnessConfiguration = config;
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected List<BrightnessChangeEvent> getBrightnessEvents(String callingPackage) {
    return brightnessChangeEvents;
  }

  void setBrightnessEvents(List<BrightnessChangeEvent> events) {
    brightnessChangeEvents.clear();
    brightnessChangeEvents.addAll(events);
  }

  @ForType(DisplayManagerGlobal.class)
  interface DisplayManagerGlobalReflector {
    @Accessor("mDm")
    void setDm(IDisplayManager displayManager);

    @Accessor("mLock")
    void setLock(Object lock);

    @Accessor("mDisplayListeners")
    void setDisplayListeners(ArrayList<Handler> list);

    @Accessor("mDisplayInfoCache")
    void setDisplayInfoCache(SparseArray<DisplayInfo> displayInfoCache);
  }
}
