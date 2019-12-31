package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.util.MergedConfiguration;
import android.view.Display;
import android.view.HandlerActionQueue;
import android.view.IWindowSession;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import java.util.ArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

@Implements(value = ViewRootImpl.class, isInAndroidSdk = false)
public class ShadowViewRootImpl {

  @RealObject private ViewRootImpl realObject;

  @Implementation(maxSdk = VERSION_CODES.JELLY_BEAN)
  public static IWindowSession getWindowSession(Looper mainLooper) {
    return null;
  }

  @Implementation
  public void playSoundEffect(int effectId) {
  }

  public void callDispatchResized() {
    Display display = getDisplay();
    Rect frame = new Rect();
    display.getRectSize(frame);

    reflector(_ViewRootImpl_.class, realObject).dispatchResized(frame);
  }

  private Display getDisplay() {
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.JELLY_BEAN_MR1) {
      return realObject.getView().getDisplay();
    } else {
      WindowManager windowManager = (WindowManager) realObject.getView().getContext()
          .getSystemService(Context.WINDOW_SERVICE);
      return windowManager.getDefaultDisplay();
    }
  }

  @Resetter
  public static void reset() {
    _ViewRootImpl_ _viewRootImplStatic_ = reflector(_ViewRootImpl_.class);
    _viewRootImplStatic_.setRunQueues(new ThreadLocal<>());
    _viewRootImplStatic_.setFirstDrawHandlers(new ArrayList<>());
    _viewRootImplStatic_.setFirstDrawComplete(false);
    _viewRootImplStatic_.setConfigCallbacks(new ArrayList<>());
  }

  /** Accessor interface for {@link ViewRootImpl}'s internals. */
  @ForType(ViewRootImpl.class)
  interface _ViewRootImpl_ {
    @Static @Accessor("sRunQueues")
    void setRunQueues(ThreadLocal<HandlerActionQueue> threadLocal);

    @Static @Accessor("sFirstDrawHandlers")
    void setFirstDrawHandlers(ArrayList<Runnable> handlers);

    @Static @Accessor("sFirstDrawComplete")
    void setFirstDrawComplete(boolean isComplete);

    @Static @Accessor("sConfigCallbacks")
    void setConfigCallbacks(ArrayList<ViewRootImpl.ConfigChangedCallback> callbacks);

    // <= JELLY_BEAN
    void dispatchResized(
        int w, int h, Rect contentInsets,
        Rect visibleInsets, boolean reportDraw, Configuration newConfig);

    // <= JELLY_BEAN_MR1
    void dispatchResized(
        Rect frame, Rect contentInsets,
        Rect visibleInsets, boolean reportDraw, Configuration newConfig);

    // <= KITKAT
    void dispatchResized(
        Rect frame, Rect overscanInsets, Rect contentInsets,
        Rect visibleInsets, boolean reportDraw, Configuration newConfig);

    // <= LOLLIPOP_MR1
    void dispatchResized(
        Rect frame, Rect overscanInsets, Rect contentInsets,
        Rect visibleInsets, Rect stableInsets, boolean reportDraw, Configuration newConfig);

    // <= M
    void dispatchResized(
        Rect frame, Rect overscanInsets, Rect contentInsets,
        Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
        Configuration newConfig);

    // <= N_MR1
    void dispatchResized(
        Rect frame, Rect overscanInsets, Rect contentInsets,
        Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
        Configuration newConfig, Rect backDropFrame, boolean forceLayout,
        boolean alwaysConsumeNavBar);

    // <= O_MR1
    void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets,
        Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
        @WithType("android.util.MergedConfiguration")
            Object mergedConfiguration,
        Rect backDropFrame, boolean forceLayout,
        boolean alwaysConsumeNavBar, int displayId);

    // >= P
    void dispatchResized(
        Rect frame, Rect overscanInsets, Rect contentInsets,
        Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
        @WithType("android.util.MergedConfiguration")
            Object mergedConfiguration,
        Rect backDropFrame, boolean forceLayout,
        boolean alwaysConsumeNavBar, int displayId,
        @WithType("android.view.DisplayCutout$ParcelableWrapper")
            Object displayCutout);


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
        dispatchResized(frame, emptyRect, emptyRect, emptyRect, emptyRect, emptyRect, true, null,
            frame, false, false);
      } else if (apiLevel <= Build.VERSION_CODES.O_MR1) {
        dispatchResized(frame, emptyRect, emptyRect, emptyRect, emptyRect, emptyRect, true,
            new MergedConfiguration(), frame, false, false, 0);
      } else { // apiLevel >= Build.VERSION_CODES.P
        dispatchResized(frame, emptyRect, emptyRect, emptyRect, emptyRect, emptyRect, true,
            new MergedConfiguration(), frame, false, false, 0,
            new android.view.DisplayCutout.ParcelableWrapper());
      }
    }

  }
}
