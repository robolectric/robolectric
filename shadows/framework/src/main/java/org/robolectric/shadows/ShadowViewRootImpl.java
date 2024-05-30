package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S_V2;
import static org.robolectric.annotation.TextLayoutMode.Mode.REALISTIC;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.view.Display;
import android.view.HandlerActionQueue;
import android.view.InsetsState;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.window.ClientWindowFrames;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

@Implements(value = ViewRootImpl.class, isInAndroidSdk = false)
public class ShadowViewRootImpl {

  private static final int RELAYOUT_RES_IN_TOUCH_MODE = 0x1;

  @RealObject protected ViewRootImpl realObject;

  /**
   * The visibility of the system status bar.
   *
   * <p>The value will be read in the intercepted {@link #getWindowInsets(boolean)} method providing
   * the current state via the returned {@link WindowInsets} instance if it has been set..
   *
   * <p>NOTE: This state does not reflect the current state of system UI visibility flags or the
   * current window insets. Rather it tracks the latest known state provided via {@link
   * #setIsStatusBarVisible(boolean)}.
   */
  private static Optional<Boolean> isStatusBarVisible = Optional.empty();

  /**
   * The visibility of the system navigation bar.
   *
   * <p>The value will be read in the intercepted {@link #getWindowInsets(boolean)} method providing
   * the current state via the returned {@link WindowInsets} instance if it has been set.
   *
   * <p>NOTE: This state does not reflect the current state of system UI visibility flags or the
   * current window insets. Rather it tracks the latest known state provided via {@link
   * #setIsNavigationBarVisible(boolean)}.
   */
  private static Optional<Boolean> isNavigationBarVisible = Optional.empty();

  /** Allows other shadows to set the state of {@link #isStatusBarVisible}. */
  protected static void setIsStatusBarVisible(boolean isStatusBarVisible) {
    ShadowViewRootImpl.isStatusBarVisible = Optional.of(isStatusBarVisible);
  }

  /** Clears the last known state of {@link #isStatusBarVisible}. */
  protected static void clearIsStatusBarVisible() {
    ShadowViewRootImpl.isStatusBarVisible = Optional.empty();
  }

  /** Allows other shadows to set the state of {@link #isNavigationBarVisible}. */
  protected static void setIsNavigationBarVisible(boolean isNavigationBarVisible) {
    ShadowViewRootImpl.isNavigationBarVisible = Optional.of(isNavigationBarVisible);
  }

  /** Clears the last known state of {@link #isNavigationBarVisible}. */
  protected static void clearIsNavigationBarVisible() {
    ShadowViewRootImpl.isNavigationBarVisible = Optional.empty();
  }

  @Implementation
  public void playSoundEffect(int effectId) {}

  @Implementation
  protected int relayoutWindow(
      WindowManager.LayoutParams params, int viewVisibility, boolean insetsPending)
      throws RemoteException {
    // TODO(christianw): probably should return WindowManagerGlobal.RELAYOUT_RES_SURFACE_RESIZED?
    int result = 0;
    if (ShadowWindowManagerGlobal.getInTouchMode() && RuntimeEnvironment.getApiLevel() <= S_V2) {
      result |= RELAYOUT_RES_IN_TOUCH_MODE;
    }
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      // Simulate initializing the SurfaceControl member object, which happens during this method.
      SurfaceControl surfaceControl =
          reflector(ViewRootImplReflector.class, realObject).getSurfaceControl();
      ShadowSurfaceControl shadowSurfaceControl = Shadow.extract(surfaceControl);
      shadowSurfaceControl.initializeNativeObject();
    }
    return result;
  }

  public void callDispatchResized() {
    Optional<Class<?>> activityWindowInfoClass =
        ReflectionHelpers.attemptLoadClass(
            this.getClass().getClassLoader(), "android.window.ActivityWindowInfo");
    if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.UPSIDE_DOWN_CAKE
        && activityWindowInfoClass.isPresent()) {
      Display display = getDisplay();
      Rect frame = new Rect();
      display.getRectSize(frame);

      ClientWindowFrames frames = new ClientWindowFrames();
      // set the final field
      ReflectionHelpers.setField(frames, "frame", frame);
      final ClassParameter<?>[] parameters =
          new ClassParameter<?>[] {
            ClassParameter.from(ClientWindowFrames.class, frames),
            ClassParameter.from(boolean.class, true), /* reportDraw */
            ClassParameter.from(
                MergedConfiguration.class, new MergedConfiguration()), /* mergedConfiguration */
            ClassParameter.from(InsetsState.class, new InsetsState()), /* insetsState */
            ClassParameter.from(boolean.class, false), /* forceLayout */
            ClassParameter.from(boolean.class, false), /* alwaysConsumeSystemBars */
            ClassParameter.from(int.class, 0), /* displayId */
            ClassParameter.from(int.class, 0), /* syncSeqId */
            ClassParameter.from(boolean.class, false), /* dragResizing */
            ClassParameter.from(
                activityWindowInfoClass.get(),
                ReflectionHelpers.newInstance(
                    activityWindowInfoClass.get())) /* activityWindowInfo */
          };
      try {
        ReflectionHelpers.callInstanceMethod(
            ViewRootImpl.class, realObject, "dispatchResized", parameters);
      } catch (RuntimeException ex) {
        ReflectionHelpers.callInstanceMethod(
            ViewRootImpl.class,
            realObject,
            "dispatchResized",
            Arrays.copyOfRange(parameters, 0, parameters.length - 1));
      }
    } else if (RuntimeEnvironment.getApiLevel() > VERSION_CODES.TIRAMISU) {
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
          ClassParameter.from(InsetsState.class, new InsetsState()), /* insetsState */
          ClassParameter.from(boolean.class, false), /* forceLayout */
          ClassParameter.from(boolean.class, false), /* alwaysConsumeSystemBars */
          ClassParameter.from(int.class, 0), /* displayId */
          ClassParameter.from(int.class, 0), /* syncSeqId */
          ClassParameter.from(boolean.class, false) /* dragResizing */);
    } else if (RuntimeEnvironment.getApiLevel() > Build.VERSION_CODES.S_V2) {
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
          ClassParameter.from(InsetsState.class, new InsetsState()), /* insetsState */
          ClassParameter.from(boolean.class, false), /* forceLayout */
          ClassParameter.from(boolean.class, false), /* alwaysConsumeSystemBars */
          ClassParameter.from(int.class, 0), /* displayId */
          ClassParameter.from(int.class, 0), /* syncSeqId */
          ClassParameter.from(int.class, 0) /* resizeMode */);
    } else if (RuntimeEnvironment.getApiLevel() > Build.VERSION_CODES.R) {
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
    return reflector(ViewRootImplReflector.class, realObject).getDisplay();
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

  /**
   * On Android R+ {@link WindowInsets} supports checking visibility of specific inset types.
   *
   * <p>For those SDK levels, override the real {@link WindowInsets} with the tracked system bar
   * visibility status ({@link #isStatusBarVisible}/{@link #isNavigationBarVisible}), if set.
   *
   * <p>NOTE: We use state tracking in place of a longer term solution of implementing the insets
   * calculations and broadcast (via listeners) for now. Once we have insets calculations working we
   * should remove this mechanism.
   */
  @Implementation(minSdk = R)
  protected WindowInsets getWindowInsets(boolean forceConstruct) {
    WindowInsets realInsets =
        reflector(ViewRootImplReflector.class, realObject).getWindowInsets(forceConstruct);

    WindowInsets.Builder overridenInsetsBuilder = new WindowInsets.Builder(realInsets);

    if (isStatusBarVisible.isPresent()) {
      overridenInsetsBuilder =
          overridenInsetsBuilder.setVisible(
              WindowInsets.Type.statusBars(), isStatusBarVisible.get());
    }

    if (isNavigationBarVisible.isPresent()) {
      overridenInsetsBuilder =
          overridenInsetsBuilder.setVisible(
              WindowInsets.Type.navigationBars(), isNavigationBarVisible.get());
    }

    return overridenInsetsBuilder.build();
  }

  @Resetter
  public static void reset() {
    ViewRootImplReflector viewRootImplStatic = reflector(ViewRootImplReflector.class);
    viewRootImplStatic.setRunQueues(new ThreadLocal<>());
    viewRootImplStatic.setFirstDrawHandlers(new ArrayList<>());
    viewRootImplStatic.setFirstDrawComplete(false);
    viewRootImplStatic.setConfigCallbacks(new ArrayList<>());

    clearIsStatusBarVisible();
    clearIsNavigationBarVisible();
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
      if (apiLevel <= Build.VERSION_CODES.LOLLIPOP_MR1) {
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

    // SDK <= S_V2
    void windowFocusChanged(boolean hasFocus, boolean inTouchMode);

    // SDK >= T
    void windowFocusChanged(boolean hasFocus);

    // SDK >= M
    @Direct
    WindowInsets getWindowInsets(boolean forceConstruct);
  }
}
