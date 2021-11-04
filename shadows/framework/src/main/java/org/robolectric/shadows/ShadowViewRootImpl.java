package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.annotation.TextLayoutMode.Mode.REALISTIC;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.view.Display;
import android.view.HandlerActionQueue;
import android.view.IWindowSession;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.window.ClientWindowFrames;
import java.util.ArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

@Implements(value = ViewRootImpl.class, isInAndroidSdk = false)
public class ShadowViewRootImpl {

  @RealObject protected ViewRootImpl realObject;

  @Implementation(maxSdk = VERSION_CODES.JELLY_BEAN)
  public static IWindowSession getWindowSession(Looper mainLooper) {
    return null;
  }

  @Implementation
  public void playSoundEffect(int effectId) {}

  @Implementation
  protected int relayoutWindow(
      WindowManager.LayoutParams params, int viewVisibility, boolean insetsPending)
      throws RemoteException {
    // TODO(christianw): probably should return WindowManagerGlobal.RELAYOUT_RES_SURFACE_RESIZED?
    return 0;
  }

  public void callDispatchResized() {
    if (RuntimeEnvironment.getApiLevel() > Build.VERSION_CODES.R) {
      Display display = getDisplay();
      Rect frame = new Rect();
      display.getRectSize(frame);

      ClientWindowFrames frames = new ClientWindowFrames();
      // set the final field
      ReflectionHelpers.setField(frames, "frame", frame);

      ReflectionHelpers.callInstanceMethod(
          ViewRootImpl.class,
          realObject,
          "dispatchResized",
          ClassParameter.from(ClientWindowFrames.class, frames),
          ClassParameter.from(boolean.class, true), /* reportDraw */
          ClassParameter.from(
              MergedConfiguration.class, new MergedConfiguration()), /* mergedConfiguration */
          ClassParameter.from(boolean.class, false), /* forceLayout */
          ClassParameter.from(boolean.class, false), /* alwaysConsumeSystemBars */
          ClassParameter.from(int.class, 0) /* displayId */);
    } else if (RuntimeEnvironment.getApiLevel() > Build.VERSION_CODES.Q) {
      Display display = getDisplay();
      Rect frame = new Rect();
      display.getRectSize(frame);

      Rect emptyRect = new Rect(0, 0, 0, 0);
      ReflectionHelpers.callInstanceMethod(
          ViewRootImpl.class,
          realObject,
          "dispatchResized",
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(Rect.class, emptyRect),
          ClassParameter.from(Rect.class, emptyRect),
          ClassParameter.from(Rect.class, emptyRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(MergedConfiguration.class, new MergedConfiguration()),
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(int.class, 0),
          ClassParameter.from(
              android.view.DisplayCutout.ParcelableWrapper.class,
              new android.view.DisplayCutout.ParcelableWrapper()));
    } else {
      Display display = getDisplay();
      Rect frame = new Rect();
      display.getRectSize(frame);
      reflector(ViewRootImplReflector.class, realObject).dispatchResized(frame);
    }
  }

  protected Display getDisplay() {
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.JELLY_BEAN_MR1) {
      return reflector(ViewRootImplReflector.class, realObject).getDisplay();
    } else {
      WindowManager windowManager =
          (WindowManager)
              realObject.getView().getContext().getSystemService(Context.WINDOW_SERVICE);
      return windowManager.getDefaultDisplay();
    }
  }

  @Implementation
  protected void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
    reflector(ViewRootImplReflector.class, realObject).setView(view, attrs, panelParentView);
    if (ConfigurationRegistry.get(TextLayoutMode.Mode.class) == REALISTIC) {
      Rect winFrame = new Rect();
      getDisplay().getRectSize(winFrame);
      reflector(ViewRootImplReflector.class, realObject).setWinFrame(winFrame);
    }
  }

  @Implementation(minSdk = R)
  protected void setView(
      View view, WindowManager.LayoutParams attrs, View panelParentView, int userId) {
    reflector(ViewRootImplReflector.class, realObject)
        .setView(view, attrs, panelParentView, userId);
    if (ConfigurationRegistry.get(TextLayoutMode.Mode.class) == REALISTIC) {
      Rect winFrame = new Rect();
      getDisplay().getRectSize(winFrame);
      reflector(ViewRootImplReflector.class, realObject).setWinFrame(winFrame);
    }
  }

  @Resetter
  public static void reset() {
    ViewRootImplReflector viewRootImplStatic = reflector(ViewRootImplReflector.class);
    viewRootImplStatic.setRunQueues(new ThreadLocal<>());
    viewRootImplStatic.setFirstDrawHandlers(new ArrayList<>());
    viewRootImplStatic.setFirstDrawComplete(false);
    viewRootImplStatic.setConfigCallbacks(new ArrayList<>());
  }

  /** Reflector interface for {@link ViewRootImpl}'s internals. */
  @ForType(ViewRootImpl.class)
  protected interface ViewRootImplReflector {

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

    // <= JELLY_BEAN
    void dispatchResized(
        int w,
        int h,
        Rect contentInsets,
        Rect visibleInsets,
        boolean reportDraw,
        Configuration newConfig);

    // <= JELLY_BEAN_MR1
    void dispatchResized(
        Rect frame,
        Rect contentInsets,
        Rect visibleInsets,
        boolean reportDraw,
        Configuration newConfig);

    // <= KITKAT
    void dispatchResized(
        Rect frame,
        Rect overscanInsets,
        Rect contentInsets,
        Rect visibleInsets,
        boolean reportDraw,
        Configuration newConfig);

    // <= LOLLIPOP_MR1
    void dispatchResized(
        Rect frame,
        Rect overscanInsets,
        Rect contentInsets,
        Rect visibleInsets,
        Rect stableInsets,
        boolean reportDraw,
        Configuration newConfig);

    // <= M
    void dispatchResized(
        Rect frame,
        Rect overscanInsets,
        Rect contentInsets,
        Rect visibleInsets,
        Rect stableInsets,
        Rect outsets,
        boolean reportDraw,
        Configuration newConfig);

    // <= N_MR1
    void dispatchResized(
        Rect frame,
        Rect overscanInsets,
        Rect contentInsets,
        Rect visibleInsets,
        Rect stableInsets,
        Rect outsets,
        boolean reportDraw,
        Configuration newConfig,
        Rect backDropFrame,
        boolean forceLayout,
        boolean alwaysConsumeNavBar);

    // <= O_MR1
    void dispatchResized(
        Rect frame,
        Rect overscanInsets,
        Rect contentInsets,
        Rect visibleInsets,
        Rect stableInsets,
        Rect outsets,
        boolean reportDraw,
        @WithType("android.util.MergedConfiguration") Object mergedConfiguration,
        Rect backDropFrame,
        boolean forceLayout,
        boolean alwaysConsumeNavBar,
        int displayId);

    // >= P
    void dispatchResized(
        Rect frame,
        Rect overscanInsets,
        Rect contentInsets,
        Rect visibleInsets,
        Rect stableInsets,
        Rect outsets,
        boolean reportDraw,
        @WithType("android.util.MergedConfiguration") Object mergedConfiguration,
        Rect backDropFrame,
        boolean forceLayout,
        boolean alwaysConsumeNavBar,
        int displayId,
        @WithType("android.view.DisplayCutout$ParcelableWrapper") Object displayCutout);

    default void dispatchResized(Rect frame) {
      Rect emptyRect = new Rect(0, 0, 0, 0);

      int apiLevel = RuntimeEnvironment.getApiLevel();
      if (apiLevel <= Build.VERSION_CODES.JELLY_BEAN) {
        dispatchResized(frame.width(), frame.height(), emptyRect, emptyRect, true, null);
      } else if (apiLevel <= VERSION_CODES.JELLY_BEAN_MR1) {
        dispatchResized(frame, emptyRect, emptyRect, true, null);
      } else if (apiLevel <= Build.VERSION_CODES.KITKAT) {
        dispatchResized(frame, emptyRect, emptyRect, emptyRect, true, null);
      } else if (apiLevel <= Build.VERSION_CODES.LOLLIPOP_MR1) {
        dispatchResized(frame, emptyRect, emptyRect, emptyRect, emptyRect, true, null);
      } else if (apiLevel <= Build.VERSION_CODES.M) {
        dispatchResized(frame, emptyRect, emptyRect, emptyRect, emptyRect, emptyRect, true, null);
      } else if (apiLevel <= Build.VERSION_CODES.N_MR1) {
        dispatchResized(
            frame, emptyRect, emptyRect, emptyRect, emptyRect, emptyRect, true, null, frame, false,
            false);
      } else if (apiLevel <= Build.VERSION_CODES.O_MR1) {
        dispatchResized(
            frame,
            emptyRect,
            emptyRect,
            emptyRect,
            emptyRect,
            emptyRect,
            true,
            new MergedConfiguration(),
            frame,
            false,
            false,
            0);
      } else { // apiLevel >= Build.VERSION_CODES.P
        dispatchResized(
            frame,
            emptyRect,
            emptyRect,
            emptyRect,
            emptyRect,
            emptyRect,
            true,
            new MergedConfiguration(),
            frame,
            false,
            false,
            0,
            new android.view.DisplayCutout.ParcelableWrapper());
      }
    }
  }
}
