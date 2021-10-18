package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build.VERSION;
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
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = WindowManagerGlobal.class, isInAndroidSdk = false,
    minSdk = JELLY_BEAN_MR1, looseSignatures = true)
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
      if (apiLevel >= S) {
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

    boolean inTouchMode;

    protected int getAddFlags() {
      int res = 0;
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
  }

  // maxSdk = JELLY_BEAN
  private static final class WindowSessionDelegateJB extends WindowSessionDelegate {
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

  // minSdk = JELLY_BEAN_MR1, maxSdk = LOLLIPOP
  private static final class WindowSessionDelegateJBMR1 extends WindowSessionDelegate {
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

  // sdk = LOLLIPOP_MR1
  private static final class WindowSessionDelegateLMR1 extends WindowSessionDelegate {
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

  // minSdk = M, maxSdk = O_MR1
  private static final class WindowSessionDelegateM extends WindowSessionDelegate {
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

  // sdk = P
  private static final class WindowSessionDelegateP extends WindowSessionDelegate {
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
  }

  // sdk = Q
  private static final class WindowSessionDelegateQ extends WindowSessionDelegate {
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

  // sdk = R
  private static final class WindowSessionDelegateR extends WindowSessionDelegate {
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

  // minSdk = S
  private static final class WindowSessionDelegateS extends WindowSessionDelegate {
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
}
