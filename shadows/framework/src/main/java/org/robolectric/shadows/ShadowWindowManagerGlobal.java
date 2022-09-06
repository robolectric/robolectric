package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static org.robolectric.shadows.ShadowView.useRealGraphics;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Instrumentation;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.DisplayCutout;
import android.view.IWindow;
import android.view.IWindowManager;
import android.view.IWindowSession;
import android.view.InputChannel;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import android.view.InsetsVisibilities;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import androidx.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link WindowManagerGlobal}. */
@SuppressWarnings("unused") // Unused params are implementations of Android SDK methods.
@Implements(
    value = WindowManagerGlobal.class,
    isInAndroidSdk = false,
    minSdk = JELLY_BEAN_MR1,
    looseSignatures = true)
public class ShadowWindowManagerGlobal {
  private static WindowSessionDelegate windowSessionDelegate;
  private static IWindowSession windowSession;

  @Resetter
  public static void reset() {
    reflector(WindowManagerGlobalReflector.class).setDefaultWindowManager(null);
    windowSessionDelegate = null;
    windowSession = null;
  }

  private static synchronized WindowSessionDelegate getWindowSessionDelegate() {
    if (windowSessionDelegate == null) {
      int apiLevel = RuntimeEnvironment.getApiLevel();
      if (apiLevel >= S_V2) {
        windowSessionDelegate = new WindowSessionDelegateSV2();
      } else if (apiLevel >= S) {
        windowSessionDelegate = new WindowSessionDelegateS();
      } else if (apiLevel >= R) {
        windowSessionDelegate = new WindowSessionDelegateR();
      } else if (apiLevel >= Q) {
        windowSessionDelegate = new WindowSessionDelegateQ();
      } else if (apiLevel >= P) {
        windowSessionDelegate = new WindowSessionDelegateP();
      } else if (apiLevel >= M) {
        windowSessionDelegate = new WindowSessionDelegateM();
      } else if (apiLevel >= LOLLIPOP_MR1) {
        windowSessionDelegate = new WindowSessionDelegateLMR1();
      } else if (apiLevel >= JELLY_BEAN_MR1) {
        windowSessionDelegate = new WindowSessionDelegateJBMR1();
      } else {
        windowSessionDelegate = new WindowSessionDelegateJB();
      }
    }
    return windowSessionDelegate;
  }

  public static boolean getInTouchMode() {
    return getWindowSessionDelegate().getInTouchMode();
  }

  /**
   * Sets whether the window manager is in touch mode. Use {@link
   * Instrumentation#setInTouchMode(boolean)} to modify this from a test.
   */
  static void setInTouchMode(boolean inTouchMode) {
    getWindowSessionDelegate().setInTouchMode(inTouchMode);
  }

  /**
   * Returns the last {@link ClipData} passed to a drag initiated from a call to {@link
   * View#startDrag} or {@link View#startDragAndDrop}, or null if there isn't one.
   */
  @Nullable
  public static ClipData getLastDragClipData() {
    return windowSessionDelegate != null ? windowSessionDelegate.lastDragClipData : null;
  }

  /** Clears the data returned by {@link #getLastDragClipData()}. */
  public static void clearLastDragClipData() {
    if (windowSessionDelegate != null) {
      windowSessionDelegate.lastDragClipData = null;
    }
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static synchronized IWindowSession getWindowSession() {
    if (windowSession == null) {
      windowSession =
          ReflectionHelpers.createDelegatingProxy(IWindowSession.class, getWindowSessionDelegate());
    }
    return windowSession;
  }

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  protected static Object getWindowSession(Looper looper) {
    return getWindowSession();
  }

  @Implementation
  protected static synchronized IWindowSession peekWindowSession() {
    return windowSession;
  }

  @Implementation
  public static Object getWindowManagerService() throws RemoteException {
    IWindowManager service =
        reflector(WindowManagerGlobalReflector.class).getWindowManagerService();
    if (service == null) {
      service = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
      reflector(WindowManagerGlobalReflector.class).setWindowManagerService(service);
      if (VERSION.SDK_INT >= 30) {
        reflector(WindowManagerGlobalReflector.class).setUseBlastAdapter(service.useBLAST());
      }
    }
    return service;
  }

  @ForType(WindowManagerGlobal.class)
  interface WindowManagerGlobalReflector {
    @Accessor("sDefaultWindowManager")
    @Static
    void setDefaultWindowManager(WindowManagerGlobal global);

    @Static
    @Accessor("sWindowManagerService")
    IWindowManager getWindowManagerService();

    @Static
    @Accessor("sWindowManagerService")
    void setWindowManagerService(IWindowManager service);

    @Static
    @Accessor("sUseBLASTAdapter")
    void setUseBlastAdapter(boolean useBlastAdapter);
  }

  private abstract static class WindowSessionDelegate {
    // From WindowManagerGlobal (was WindowManagerImpl in JB).
    static final int ADD_FLAG_IN_TOUCH_MODE = 0x1;
    static final int ADD_FLAG_APP_VISIBLE = 0x2;

    // TODO: Default to touch mode always.
    private boolean inTouchMode = useRealGraphics();
    @Nullable protected ClipData lastDragClipData;

    protected int getAddFlags() {
      int res = 0;
      // Temporarily enable this based on a system property to allow for test migration. This will
      // eventually be updated to default and true and eventually removed, Robolectric's previous
      // behavior of not marking windows as visible by default is a bug. This flag should only be
      // used as a temporary toggle during migration.
      if (useRealGraphics()
          || "true".equals(System.getProperty("robolectric.areWindowsMarkedVisible", "false"))) {
        res |= ADD_FLAG_APP_VISIBLE;
      }
      if (getInTouchMode()) {
        res |= ADD_FLAG_IN_TOUCH_MODE;
      }
      return res;
    }

    public boolean getInTouchMode() {
      return inTouchMode;
    }

    public void setInTouchMode(boolean inTouchMode) {
      this.inTouchMode = inTouchMode;
    }

    // @Implementation(maxSdk = O_MR1)
    public IBinder prepareDrag(
        IWindow window, int flags, int thumbnailWidth, int thumbnailHeight, Surface outSurface) {
      return new Binder();
    }

    // @Implementation(maxSdk = M)
    public boolean performDrag(
        IWindow window,
        IBinder dragToken,
        float touchX,
        float touchY,
        float thumbCenterX,
        float thumbCenterY,
        ClipData data) {
      lastDragClipData = data;
      return true;
    }

    // @Implementation(minSdk = N, maxSdk = O_MR1)
    public boolean performDrag(
        IWindow window,
        IBinder dragToken,
        int touchSource,
        float touchX,
        float touchY,
        float thumbCenterX,
        float thumbCenterY,
        ClipData data) {
      lastDragClipData = data;
      return true;
    }
  }

  private static class WindowSessionDelegateJB extends WindowSessionDelegate {
    // @Implementation(maxSdk = JELLY_BEAN)
    public int add(
        IWindow window,
        int seq,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int layerStackId,
        Rect outContentInsets,
        InputChannel outInputChannel) {
      return getAddFlags();
    }
  }

  private static class WindowSessionDelegateJBMR1 extends WindowSessionDelegateJB {
    // @Implementation(minSdk = JELLY_BEAN_MR1, maxSdk = LOLLIPOP)
    public int addToDisplay(
        IWindow window,
        int seq,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int layerStackId,
        Rect outContentInsets,
        InputChannel outInputChannel) {
      return getAddFlags();
    }
  }

  private static class WindowSessionDelegateLMR1 extends WindowSessionDelegateJBMR1 {
    // @Implementation(sdk = LOLLIPOP_MR1)
    public int addToDisplay(
        IWindow window,
        int seq,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int layerStackId,
        Rect outContentInsets,
        Rect outStableInsets,
        InputChannel outInputChannel) {
      return getAddFlags();
    }
  }

  private static class WindowSessionDelegateM extends WindowSessionDelegateLMR1 {
    // @Implementation(minSdk = M, maxSdk = O_MR1)
    public int addToDisplay(
        IWindow window,
        int seq,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int layerStackId,
        Rect outContentInsets,
        Rect outStableInsets,
        Rect outInsets,
        InputChannel outInputChannel) {
      return getAddFlags();
    }
  }

  private static class WindowSessionDelegateP extends WindowSessionDelegateM {
    // @Implementation(sdk = P)
    public int addToDisplay(
        IWindow window,
        int seq,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int layerStackId,
        Rect outFrame,
        Rect outContentInsets,
        Rect outStableInsets,
        Rect outOutsets,
        DisplayCutout.ParcelableWrapper displayCutout,
        InputChannel outInputChannel) {
      return getAddFlags();
    }

    // @Implementation(minSdk = P)
    public IBinder performDrag(
        IWindow window,
        int flags,
        SurfaceControl surface,
        int touchSource,
        float touchX,
        float touchY,
        float thumbCenterX,
        float thumbCenterY,
        ClipData data) {
      lastDragClipData = data;
      return new Binder();
    }
  }

  private static class WindowSessionDelegateQ extends WindowSessionDelegateP {
    // @Implementation(sdk = Q)
    public int addToDisplay(
        IWindow window,
        int seq,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int layerStackId,
        Rect outFrame,
        Rect outContentInsets,
        Rect outStableInsets,
        Rect outOutsets,
        DisplayCutout.ParcelableWrapper displayCutout,
        InputChannel outInputChannel,
        InsetsState insetsState) {
      return getAddFlags();
    }
  }

  private static class WindowSessionDelegateR extends WindowSessionDelegateQ {
    // @Implementation(sdk = R)
    public int addToDisplayAsUser(
        IWindow window,
        int seq,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int layerStackId,
        int userId,
        Rect outFrame,
        Rect outContentInsets,
        Rect outStableInsets,
        DisplayCutout.ParcelableWrapper displayCutout,
        InputChannel outInputChannel,
        InsetsState insetsState,
        InsetsSourceControl[] activeControls) {
      return getAddFlags();
    }
  }

  private static class WindowSessionDelegateS extends WindowSessionDelegateR {
    // @Implementation(sdk = S)
    public int addToDisplayAsUser(
        IWindow window,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int layerStackId,
        int userId,
        InsetsState requestedVisibility,
        InputChannel outInputChannel,
        InsetsState insetsState,
        InsetsSourceControl[] activeControls) {
      return getAddFlags();
    }
  }

  private static class WindowSessionDelegateSV2 extends WindowSessionDelegateS {
    // @Implementation(minSdk = S_V2)
    public int addToDisplayAsUser(
        IWindow window,
        WindowManager.LayoutParams attrs,
        int viewVisibility,
        int displayId,
        int userId,
        InsetsVisibilities requestedVisibilities,
        InputChannel outInputChannel,
        InsetsState outInsetsState,
        InsetsSourceControl[] outActiveControls) {
      return getAddFlags();
    }
  }
}
