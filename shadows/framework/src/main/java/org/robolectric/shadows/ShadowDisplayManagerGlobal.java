package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;

import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.hardware.display.IDisplayManagerCallback;
import android.hardware.display.WifiDisplayStatus;
import android.os.RemoteException;
import android.view.DisplayInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = DisplayManagerGlobal.class, isInAndroidSdk = false, minSdk = JELLY_BEAN_MR1)
public class ShadowDisplayManagerGlobal {
  private static DisplayManagerGlobal instance;

  private float saturationLevel = 1f;

  private MyDisplayManager mDm;

  @Resetter
  public static void reset() {
    instance = null;
  }

  @Implementation
  synchronized public static DisplayManagerGlobal getInstance() {
    if (instance == null) {
      MyDisplayManager myIDisplayManager = new MyDisplayManager();
      IDisplayManager proxy = ReflectionHelpers.createDelegatingProxy(IDisplayManager.class, myIDisplayManager);
      instance = ReflectionHelpers.callConstructor(DisplayManagerGlobal.class,
          ClassParameter.from(IDisplayManager.class, proxy));
      ShadowDisplayManagerGlobal shadow = Shadow.extract(instance);
      shadow.mDm = myIDisplayManager;
    }
    return instance;
  }

  @Implementation
  protected WifiDisplayStatus getWifiDisplayStatus() {
    return new WifiDisplayStatus();
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
    public int[] getDisplayIds() throws RemoteException {
      int[] ids = new int[displayInfos.size()];
      int i = 0;
      for (Integer displayId : displayInfos.keySet()) {
        ids[i++] = displayId;
      }
      return ids;
    }

    // @Override
    public void registerCallback(IDisplayManagerCallback iDisplayManagerCallback) throws RemoteException {
      this.callbacks.add(iDisplayManagerCallback);
    }

    synchronized private int addDisplay(DisplayInfo displayInfo) {
      int nextId = nextDisplayId++;
      displayInfos.put(nextId, displayInfo);
      notifyListeners(nextId, DisplayManagerGlobal.EVENT_DISPLAY_ADDED);
      return nextId;
    }

    synchronized private void changeDisplay(int displayId, DisplayInfo displayInfo) {
      if (!displayInfos.containsKey(displayId)) {
        throw new IllegalStateException("no display " + displayId);
      }

      displayInfos.put(displayId, displayInfo);
      notifyListeners(displayId, DisplayManagerGlobal.EVENT_DISPLAY_CHANGED);
    }

    synchronized private void removeDisplay(int displayId) {
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
}
