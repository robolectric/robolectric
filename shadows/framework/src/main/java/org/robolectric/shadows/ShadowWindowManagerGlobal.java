package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.shadows.ShadowView.useRealGraphics;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Instrumentation;
import android.content.ClipData;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;
import android.view.IWindowSession;
import android.view.View;
import android.view.WindowManagerGlobal;
import androidx.annotation.Nullable;
import java.lang.reflect.Proxy;
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
  private static WindowSessionDelegate windowSessionDelegate = new WindowSessionDelegate();
  private static IWindowSession windowSession;

  @Resetter
  public static void reset() {
    reflector(WindowManagerGlobalReflector.class).setDefaultWindowManager(null);
    windowSessionDelegate = new WindowSessionDelegate();
    windowSession = null;
  }

  public static boolean getInTouchMode() {
    return windowSessionDelegate.getInTouchMode();
  }

  /**
   * Sets whether the window manager is in touch mode. Use {@link
   * Instrumentation#setInTouchMode(boolean)} to modify this from a test.
   */
  static void setInTouchMode(boolean inTouchMode) {
    windowSessionDelegate.setInTouchMode(inTouchMode);
  }

  /**
   * Returns the last {@link ClipData} passed to a drag initiated from a call to {@link
   * View#startDrag} or {@link View#startDragAndDrop}, or null if there isn't one.
   */
  @Nullable
  public static ClipData getLastDragClipData() {
    return windowSessionDelegate.lastDragClipData;
  }

  /** Clears the data returned by {@link #getLastDragClipData()}. */
  public static void clearLastDragClipData() {
    windowSessionDelegate.lastDragClipData = null;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static synchronized IWindowSession getWindowSession() {
    if (windowSession == null) {
      // Use Proxy.newProxyInstance instead of ReflectionHelpers.createDelegatingProxy as there are
      // too many variants of 'add', 'addToDisplay', and 'addToDisplayAsUser', some of which have
      // arg types that don't exist any more.
      windowSession =
          (IWindowSession)
              Proxy.newProxyInstance(
                  IWindowSession.class.getClassLoader(),
                  new Class<?>[] {IWindowSession.class},
                  (proxy, method, args) -> {
                    String methodName = method.getName();
                    switch (methodName) {
                      case "add": // SDK 16
                      case "addToDisplay": // SDK 17-29
                      case "addToDisplayAsUser": // SDK 30+
                        return windowSessionDelegate.getAddFlags();
                      case "getInTouchMode":
                        return windowSessionDelegate.getInTouchMode();
                      case "performDrag":
                        return windowSessionDelegate.performDrag(args);
                      case "prepareDrag":
                        return windowSessionDelegate.prepareDrag();
                      case "setInTouchMode":
                        windowSessionDelegate.setInTouchMode((boolean) args[0]);
                        return null;
                      default:
                        return ReflectionHelpers.defaultValueForType(
                            method.getReturnType().getName());
                    }
                  });
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
      if (RuntimeEnvironment.getApiLevel() >= R) {
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

  private static class WindowSessionDelegate {
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

    public IBinder prepareDrag() {
      return new Binder();
    }

    public Object performDrag(Object[] args) {
      // extract the clipData param
      for (int i = args.length - 1; i >= 0; i--) {
        if (args[i] instanceof ClipData) {
          lastDragClipData = (ClipData) args[i];
          // In P (SDK 28), the return type changed from boolean to Binder.
          return RuntimeEnvironment.getApiLevel() >= P ? new Binder() : true;
        }
      }
      throw new AssertionError("Missing ClipData param");
    }
  }
}
