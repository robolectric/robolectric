package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Rect;
import android.view.Display;
import android.view.HandlerActionQueue;
import android.view.IWindow;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowManager;
import java.util.ArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = ViewRootImpl.class, isInAndroidSdk = false)
public class ShadowViewRootImpl {

  static {
    if (RuntimeEnvironment.getApiLevel() == R) {
      ReflectionHelpers.setStaticField(ViewRootImpl.class, "sNewInsetsMode", 2);
    }
  }

  @RealObject protected ViewRootImpl realObject;

  @Implementation
  public void playSoundEffect(int effectId) {}

  // TODO: Deprecate and remove this method, resize should get dispatched automatically to all
  //  windows added in the window session when a display changes its size.
  public void callDispatchResized() {
    ShadowWindowManagerGlobal.notifyResize(
        reflector(ViewRootImplReflector.class, realObject).getWindow());
  }

  protected Display getDisplay() {
    return reflector(ViewRootImplReflector.class, realObject).getDisplay();
  }

  @Implementation(minSdk = TIRAMISU)
  protected void updateBlastSurfaceIfNeeded() {}

  @Resetter
  public static void reset() {
    ViewRootImplReflector viewRootImplStatic = reflector(ViewRootImplReflector.class);
    viewRootImplStatic.setRunQueues(new ThreadLocal<>());
    viewRootImplStatic.setFirstDrawHandlers(new ArrayList<>());
    viewRootImplStatic.setFirstDrawComplete(false);
    viewRootImplStatic.setConfigCallbacks(new ArrayList<>());
  }

  public void callWindowFocusChanged(boolean hasFocus) {
    if (RuntimeEnvironment.getApiLevel() <= S_V2) {
      reflector(ViewRootImplReflector.class, realObject)
          .windowFocusChanged(hasFocus, ShadowWindowManagerGlobal.getInTouchMode());
    } else {
      reflector(ViewRootImplReflector.class, realObject).windowFocusChanged(hasFocus);
    }
  }

  Surface getSurface() {
    return reflector(ViewRootImplReflector.class, realObject).getSurface();
  }

  /** Reflector interface for {@link ViewRootImpl}'s internals. */
  @ForType(ViewRootImpl.class)
  protected interface ViewRootImplReflector {
    @Accessor("mWindow")
    IWindow getWindow();

    @Direct
    void setView(View view, WindowManager.LayoutParams attrs, View panelParentView);

    @Direct
    void setView(View view, WindowManager.LayoutParams attrs, View panelParentView, int userId);

    @Static
    @Accessor("sRunQueues")
    void setRunQueues(ThreadLocal<HandlerActionQueue> threadLocal);

    @Static
    @Accessor("sFirstDrawHandlers")
    void setFirstDrawHandlers(ArrayList<Runnable> handlers);

    @Static
    @Accessor("sFirstDrawComplete")
    void setFirstDrawComplete(boolean isComplete);

    @Static
    @Accessor("sConfigCallbacks")
    void setConfigCallbacks(ArrayList<ViewRootImpl.ConfigChangedCallback> callbacks);

    @Accessor("sNewInsetsMode")
    @Static
    int getNewInsetsMode();

    @Accessor("mWinFrame")
    void setWinFrame(Rect winFrame);

    @Accessor("mDisplay")
    Display getDisplay();

    @Accessor("mSurfaceControl")
    SurfaceControl getSurfaceControl();

    @Accessor("mSurface")
    Surface getSurface();

    @Accessor("mWindowAttributes")
    WindowManager.LayoutParams getWindowAttributes();

    // SDK <= S_V2
    void windowFocusChanged(boolean hasFocus, boolean inTouchMode);

    // SDK >= T
    void windowFocusChanged(boolean hasFocus);

    // SDK >= M
    @Direct
    WindowInsets getWindowInsets(boolean forceConstruct);
  }
}
