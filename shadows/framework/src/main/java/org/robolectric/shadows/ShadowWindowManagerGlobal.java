package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static android.view.WindowInsets.Type.navigationBars;
import static android.view.WindowInsets.Type.statusBars;
import static android.view.WindowInsets.Type.systemBars;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.util.Arrays.stream;
import static org.robolectric.shadows.ShadowView.useRealGraphics;
import static org.robolectric.shadows.SystemUi.systemUiForDisplay;
import static org.robolectric.util.ReflectionHelpers.callConstructor;
import static org.robolectric.util.ReflectionHelpers.callInstanceMethod;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.FloatRange;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.MergedConfiguration;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.IWindow;
import android.view.IWindowManager;
import android.view.IWindowSession;
import android.view.InsetsSource;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import android.view.MotionEvent;
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowRelayoutResult;
import android.window.ActivityWindowInfo;
import android.window.BackEvent;
import android.window.BackMotionEvent;
import android.window.ClientWindowFrames;
import android.window.OnBackInvokedCallbackInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.Closeable;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowInsetsState.InsetsStateReflector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link WindowManagerGlobal}. */
@SuppressWarnings("unused") // Unused params are implementations of Android SDK methods.
@Implements(value = WindowManagerGlobal.class, isInAndroidSdk = false)
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

  static void notifyResize(IWindow window) {
    windowSessionDelegate.sendResize(window);
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

  /**
   * Ongoing predictive back gesture.
   *
   * <p>Start a predictive back gesture by calling {@link
   * ShadowWindowManagerGlobal#startPredictiveBackGesture}. One or more drag progress events can be
   * dispatched by calling {@link #moveBy}. The gesture must be ended by either calling {@link
   * #cancel()} or {@link #close()}, if {@link #cancel()} is called a subsequent call to {@link
   * close()} will do nothing to allow using the gesture in a try with resources statement:
   *
   * <pre>
   * try (PredictiveBackGesture backGesture =
   *     ShadowWindowManagerGlobal.startPredictiveBackGesture(BackEvent.EDGE_LEFT)) {
   *   backGesture.moveBy(10, 10);
   * }
   * </pre>
   */
  public static final class PredictiveBackGesture implements Closeable {
    @BackEvent.SwipeEdge private final int edge;
    private final int displayWidth;
    private final float startTouchX;
    private final float progressThreshold;
    private float touchX;
    private float touchY;
    private boolean isCancelled;
    private boolean isFinished;

    private PredictiveBackGesture(
        @BackEvent.SwipeEdge int edge, int displayWidth, float touchX, float touchY) {
      this.edge = edge;
      this.displayWidth = displayWidth;
      this.progressThreshold =
          ViewConfiguration.get(RuntimeEnvironment.getApplication()).getScaledTouchSlop();
      this.startTouchX = touchX;
      this.touchX = touchX;
      this.touchY = touchY;
    }

    /** Dispatches drag progress for a predictive back gesture. */
    public void moveBy(float dx, float dy) {
      checkState(!isCancelled && !isFinished);
      try {
        touchX += dx;
        touchY += dy;
        ShadowWindowManagerGlobal.windowSessionDelegate
            .onBackInvokedCallbackInfo
            .getCallback()
            .onBackProgressed(
                BackMotionEvents.newBackMotionEvent(edge, touchX, touchY, caclulateProgress()));
        ShadowLooper.idleMainLooper();
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    }

    /** Cancels the back gesture. */
    public void cancel() {
      checkState(!isCancelled && !isFinished);
      isCancelled = true;
      try {
        ShadowWindowManagerGlobal.windowSessionDelegate
            .onBackInvokedCallbackInfo
            .getCallback()
            .onBackCancelled();
        ShadowWindowManagerGlobal.windowSessionDelegate.currentPredictiveBackGesture = null;
        ShadowLooper.idleMainLooper();
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Ends the back gesture. If the back gesture has not been cancelled by calling {@link
     * #cancel()} then the back handler is invoked.
     *
     * <p>Callers should always call either {@link #cancel()} or {@link #close()}. It is recommended
     * to use the result of {@link ShadowWindowManagerGlobal#startPredictiveBackGesture} in a try
     * with resources.
     */
    @Override
    public void close() {
      checkState(!isFinished);
      isFinished = true;
      if (!isCancelled) {
        try {
          ShadowWindowManagerGlobal.windowSessionDelegate
              .onBackInvokedCallbackInfo
              .getCallback()
              .onBackInvoked();
          ShadowWindowManagerGlobal.windowSessionDelegate.currentPredictiveBackGesture = null;
          ShadowLooper.idleMainLooper();
        } catch (RemoteException e) {
          throw new RuntimeException(e);
        }
      }
    }

    private float caclulateProgress() {
      // The real implementation anchors the progress on the start x and resets it each time the
      // threshold is lost, it also calculates a linear and non linear progress area. This
      // implementation is much simpler.
      int direction = (edge == BackEvent.EDGE_LEFT ? 1 : -1);
      float draggableWidth =
          (edge == BackEvent.EDGE_LEFT ? displayWidth - startTouchX : startTouchX)
              - progressThreshold;
      return max((((touchX - startTouchX) * direction) - progressThreshold) / draggableWidth, 0f);
    }
  }

  /**
   * Starts a predictive back gesture in the center of the edge. See {@link
   * #startPredictiveBackGesture(int, float)}.
   */
  @Nullable
  public static PredictiveBackGesture startPredictiveBackGesture(@BackEvent.SwipeEdge int edge) {
    return startPredictiveBackGesture(edge, 0.5f);
  }

  /**
   * Starts a predictive back gesture.
   *
   * <p>If no active activity with a back pressed callback that supports animations is registered
   * then null will be returned. See {@link PredictiveBackGesture}.
   *
   * <p>See {@link ShadowApplication#setEnableOnBackInvokedCallback}.
   *
   * @param position The position on edge of the window
   */
  @Nullable
  public static PredictiveBackGesture startPredictiveBackGesture(
      @BackEvent.SwipeEdge int edge, @FloatRange(from = 0f, to = 1f) float position) {
    checkArgument(position >= 0f && position <= 1f, "Invalid position: %s.", position);
    checkState(
        windowSessionDelegate.currentPredictiveBackGesture == null,
        "Current predictive back gesture in progress.");
    if (windowSessionDelegate.onBackInvokedCallbackInfo == null
        || !windowSessionDelegate.onBackInvokedCallbackInfo.isAnimationCallback()) {
      return null;
    } else {
      try {
        // Exclusion rects are sent to the window session by posting so idle the looper first.
        ShadowLooper.idleMainLooper();
        int touchSlop =
            ViewConfiguration.get(RuntimeEnvironment.getApplication()).getScaledTouchSlop();
        int displayWidth = ShadowDisplay.getDefaultDisplay().getWidth();
        float deltaX = (edge == BackEvent.EDGE_LEFT ? 1 : -1) * touchSlop / 2f;
        float downX = (edge == BackEvent.EDGE_LEFT ? 0 : displayWidth) + deltaX;
        float downY = ShadowDisplay.getDefaultDisplay().getHeight() * position;
        if (windowSessionDelegate.systemGestureExclusionRects != null) {
          // TODO: The rects should be offset based on the window's position in the display, most
          //  windows should be full screen which makes this naive logic work ok.
          for (Rect rect : windowSessionDelegate.systemGestureExclusionRects) {
            if (rect.contains(round(downX), round(downY))) {
              return null;
            }
          }
        }
        // A predictive back gesture starts as a user swipe which the window will receive the start
        // of the gesture before it gets intercepted by the window manager.
        MotionEvent downEvent =
            MotionEvent.obtain(
                /* downTime= */ SystemClock.uptimeMillis(),
                /* eventTime= */ SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                downX,
                downY,
                /* metaState= */ 0);
        MotionEvent moveEvent = MotionEvent.obtain(downEvent);
        moveEvent.setAction(MotionEvent.ACTION_MOVE);
        moveEvent.offsetLocation(deltaX, 0);
        MotionEvent cancelEvent = MotionEvent.obtain(moveEvent);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
        ShadowUiAutomation.injectInputEvent(downEvent);
        ShadowUiAutomation.injectInputEvent(moveEvent);
        ShadowUiAutomation.injectInputEvent(cancelEvent);
        windowSessionDelegate
            .onBackInvokedCallbackInfo
            .getCallback()
            .onBackStarted(
                BackMotionEvents.newBackMotionEvent(
                    edge, downX + 2 * deltaX, downY, /* progress= */ 0));
        ShadowLooper.idleMainLooper();
        PredictiveBackGesture backGesture =
            new PredictiveBackGesture(edge, displayWidth, downX + 2 * deltaX, downY);
        windowSessionDelegate.currentPredictiveBackGesture = backGesture;
        return backGesture;
      } catch (RemoteException e) {
        Log.e("ShadowWindowManagerGlobal", "Failed to start back gesture", e);
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked") // Cast args to IWindowSession methods
  @Implementation
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
                        return windowSessionDelegate.addToDisplay(args);
                      case "remove":
                        windowSessionDelegate.remove(args);
                        return null;
                      case "relayout":
                        return windowSessionDelegate.relayout(args);
                      case "getInTouchMode":
                        return windowSessionDelegate.getInTouchMode();
                      case "performDrag":
                        return windowSessionDelegate.performDrag(args);
                      case "prepareDrag":
                        return windowSessionDelegate.prepareDrag();
                      case "setInTouchMode":
                        windowSessionDelegate.setInTouchMode((boolean) args[0]);
                        return null;
                      case "setOnBackInvokedCallbackInfo":
                        windowSessionDelegate.onBackInvokedCallbackInfo =
                            (OnBackInvokedCallbackInfo) args[1];
                        return null;
                      case "reportSystemGestureExclusionChanged":
                        windowSessionDelegate.systemGestureExclusionRects = (List<Rect>) args[1];
                        return null;
                      case "insetsModified":
                      case "updateRequestedVisibilities":
                      case "updateRequestedVisibleTypes":
                        windowSessionDelegate.updateInsets(args);
                        return null;
                      default:
                        return ReflectionHelpers.defaultValueForType(
                            method.getReturnType().getName());
                    }
                  });
    }
    return windowSession;
  }

  @Implementation
  protected static synchronized IWindowSession peekWindowSession() {
    return windowSession;
  }

  @Implementation
  public static @ClassName("android.view.IWindowManager") Object getWindowManagerService()
      throws RemoteException {
    IWindowManager service =
        reflector(WindowManagerGlobalReflector.class).getWindowManagerService();
    if (service == null) {
      service = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
      reflector(WindowManagerGlobalReflector.class).setWindowManagerService(service);
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

    @Accessor("mViews")
    List<View> getWindowViews();
  }

  private static class WindowSessionDelegate {
    private final LinkedHashMap<IWindow, WindowInfo> windows = new LinkedHashMap<>();

    // From WindowManagerGlobal (was WindowManagerImpl in JB).
    static final int ADD_FLAG_IN_TOUCH_MODE = 0x1;
    static final int ADD_FLAG_APP_VISIBLE = 0x2;
    static final int RELAYOUT_RES_IN_TOUCH_MODE = 0x1;

    // TODO: Default to touch mode always.
    private boolean inTouchMode = useRealGraphics();
    @Nullable protected ClipData lastDragClipData;
    @Nullable private OnBackInvokedCallbackInfo onBackInvokedCallbackInfo;
    @Nullable private List<Rect> systemGestureExclusionRects;
    @Nullable private PredictiveBackGesture currentPredictiveBackGesture;

    protected int addToDisplay(Object[] args) {
      int sdk = RuntimeEnvironment.getApiLevel();
      WindowInfo windowInfo = windows.computeIfAbsent((IWindow) args[0], id -> new WindowInfo());
      int displayId = (int) args[sdk <= R ? 4 : 3];
      // TODO: This is to allow window insets to be keyed per display, i.e. a window has requested
      //  insets visibility changed before it was added to a display, does android actually allow
      //  per display window inset visibilities?
      if (sdk >= R) {
        applyInsets(displayId, windowInfo.requestedVisibleTypes);
      }
      windowInfo.displayId = displayId;

      // Create some insets source controls otherwise the insets controller will not apply changes.
      if (sdk >= R && sdk < UPSIDE_DOWN_CAKE) {
        populateInsetSourceControls(windowInfo, findFirst(InsetsSourceControl[].class, args));
        windowInfo.hasInsetsControl = true;
        transferWindowInsetsControlTo(windowInfo);
      }
      Rect[] rects = findAll(Rect.class, args);
      int rectIdx = 0;
      configureWindowFrames(
          windowInfo,
          /* inAttrs= */ (WindowManager.LayoutParams) args[sdk <= R ? 2 : 1],
          /* requestedSize= */ null,
          /* outFrame= */ sdk >= P && rects.length > rectIdx ? rects[rectIdx++] : null,
          /* outContentInsets= */ sdk <= R ? rects[rectIdx++] : null,
          /* outVisibleInsets= */ null,
          /* outStableInsets= */ sdk >= LOLLIPOP_MR1 && sdk <= R ? rects[rectIdx] : null,
          /* outInsetsState= */ sdk >= Q ? findFirst(InsetsState.class, args) : null);

      int res = 0;
      // Temporarily enable this based on a system property to allow for test migration. This will
      // eventually be updated to default and true and eventually removed, Robolectric's previous
      // behavior of not marking windows as visible by default is a bug. This flag should only be
      // used as a temporary toggle during migration.
      if (useRealGraphics()
          || "true".equals(System.getProperty("robolectric.areWindowsMarkedVisible", "false"))) {
        res |= ADD_FLAG_APP_VISIBLE;
      }
      res |= inTouchMode ? ADD_FLAG_IN_TOUCH_MODE : 0;
      return res;
    }

    protected void remove(Object[] args) {
      IWindow window = (IWindow) args[0];
      windows.remove(window);
      // TODO: This transfers control to the last window, should there be another heuristic here?
      // TODO: Streams.findLast is not available in Android Guava yet.
      transferWindowInsetsControlTo(windows.values().stream().reduce((a, b) -> b).orElse(null));
    }

    protected int relayout(Object[] args) {
      int sdk = RuntimeEnvironment.getApiLevel();
      WindowRelayoutResult windowLayoutResult =
          sdk >= VANILLA_ICE_CREAM ? findFirst(WindowRelayoutResult.class, args) : null;

      // Simulate initializing the SurfaceControl member object, which happens during this method.
      if (sdk >= Q) {
        SurfaceControl surfaceControl =
            sdk >= VANILLA_ICE_CREAM
                ? windowLayoutResult.surfaceControl
                : findFirst(SurfaceControl.class, args);
        Shadow.<ShadowSurfaceControl>extract(surfaceControl).initializeNativeObject();
      }

      IWindow window = (IWindow) args[0];
      WindowInfo windowInfo = windows.get(window);
      // In legacy looper mode relayout can be called out of order with add so just ignore it.
      // TODO: In paused looper mode the material SnackbarManager static instance leaks state
      //  between tests and triggers relayout on window roots that are cleared, for now just ignore
      //  them here, but ideally this library would not leak state between tests.
      if (windowInfo != null) {
        if (sdk >= R && sdk < UPSIDE_DOWN_CAKE) {
          InsetsSourceControl[] controls = findFirst(InsetsSourceControl[].class, args);
          if (windowInfo.hasInsetsControl) {
            populateInsetSourceControls(windowInfo, controls);
          } else {
            Arrays.setAll(controls, i -> null);
          }
        }
        Rect[] rects = findAll(Rect.class, args);
        int requestedSizeIdx = sdk < S ? 3 : 2;
        configureWindowFrames(
            checkNotNull(windowInfo),
            /* inAttrs= */ (WindowManager.LayoutParams) args[sdk <= R ? 2 : 1],
            /* requestedSize= */ new Point(
                (int) args[requestedSizeIdx], (int) args[requestedSizeIdx + 1]),
            /* outFrame= */ rects.length > 0
                ? rects[0]
                : (windowLayoutResult != null
                        ? windowLayoutResult.frames
                        : findFirst(ClientWindowFrames.class, args))
                    .frame,
            /* outContentInsets= */ sdk <= R ? rects[2] : null,
            /* outVisibleInsets= */ sdk <= R ? rects[3] : null,
            /* outStableInsets= */ sdk <= R ? rects[4] : null,
            /* outInsetsState= */ sdk >= Q
                ? (windowLayoutResult != null
                    ? windowLayoutResult.insetsState
                    : findFirst(InsetsState.class, args))
                : null);
      }

      return inTouchMode ? RELAYOUT_RES_IN_TOUCH_MODE : 0;
    }

    private void configureWindowFrames(
        WindowInfo windowInfo,
        @Nullable WindowManager.LayoutParams inAttrs,
        Point requestedSize,
        Rect outFrame,
        Rect outContentInsets,
        Rect outVisibleInsets,
        Rect outStableInsets,
        InsetsState outInsetsState) {
      SystemUi systemUi = systemUiForDisplay(windowInfo.displayId);
      DisplayInfo displayInfo =
          DisplayManagerGlobal.getInstance().getDisplayInfo(windowInfo.displayId);
      WindowManager.LayoutParams attrs = windowInfo.attrs;
      if (inAttrs != null) {
        attrs.copyFrom(inAttrs);
      }
      windowInfo.displayFrame.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
      Rect contentFrame = new Rect(windowInfo.displayFrame);
      systemUi.adjustFrameForInsets(attrs, contentFrame);
      // TODO: Remove this and respect the requested size as real Android does. For back compat
      //  reasons temporarily ignore requested size.
      boolean useRequestedSize = Boolean.getBoolean("robolectric.windowManager.useRequestedSize");
      int width =
          useRequestedSize && requestedSize != null && attrs.width != LayoutParams.MATCH_PARENT
              ? requestedSize.x
              : (attrs.width > 0 ? attrs.width : contentFrame.width());
      int height =
          useRequestedSize && requestedSize != null && attrs.height != LayoutParams.MATCH_PARENT
              ? requestedSize.y
              : (attrs.height > 0 ? attrs.height : contentFrame.height());
      // TODO: Take account of parent frame for child windows.
      Gravity.apply(
          attrs.gravity,
          width,
          height,
          contentFrame,
          (int) (attrs.x + attrs.horizontalMargin * contentFrame.width()),
          (int) (attrs.y + attrs.verticalMargin * contentFrame.height()),
          windowInfo.frame);
      if (!useRequestedSize) {
        // If we are not respecting the requested size, for backwards compatibility allow the window
        // to offset to the requested position ignoring the gravity and display bounds.
        windowInfo.frame.offsetTo(attrs.x, attrs.y);
      } else {
        Gravity.applyDisplay(attrs.gravity, contentFrame, windowInfo.frame);
      }
      systemUiForDisplay(windowInfo.displayId).putInsets(windowInfo);
      windowInfo.put(outFrame, outContentInsets, outVisibleInsets, outStableInsets, outInsetsState);
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

    public void updateInsets(Object[] args) {
      int sdk = RuntimeEnvironment.getApiLevel();
      checkState(sdk >= R);
      IWindow window = (IWindow) args[0];
      WindowInfo windowInfo = windows.computeIfAbsent(window, id -> new WindowInfo());
      if (sdk <= S) {
        InsetsState state = (InsetsState) args[1];
        InsetsSource statusBar = state.peekSource(ShadowInsetsState.STATUS_BARS);
        InsetsSource navBar = state.peekSource(ShadowInsetsState.NAVIGATION_BARS);
        windowInfo.requestedVisibleTypes =
            (statusBar == null || statusBar.isVisible() ? statusBars() : 0)
                | (navBar == null || navBar.isVisible() ? navigationBars() : 0);
      } else if (sdk <= TIRAMISU) {
        InsetsVisibilitiesReflector visibilities =
            reflector(InsetsVisibilitiesReflector.class, args[1]);
        boolean statusBar = visibilities.getVisibility(ShadowInsetsState.STATUS_BARS);
        boolean navBar = visibilities.getVisibility(ShadowInsetsState.NAVIGATION_BARS);
        windowInfo.requestedVisibleTypes =
            (statusBar ? statusBars() : 0) | (navBar ? navigationBars() : 0);
      } else {
        windowInfo.requestedVisibleTypes = (int) args[1];
      }
      if (windowInfo.displayId != -1) {
        applyInsets(windowInfo.displayId, windowInfo.requestedVisibleTypes);
      }
    }

    void applyInsets(int displayId, int requestedVisibleTypes) {
      checkState(displayId != -1);
      SystemUi systemUi = systemUiForDisplay(displayId);
      boolean statusBarVisible = (requestedVisibleTypes & statusBars()) != 0;
      if (systemUi.getStatusBar().isVisible() != statusBarVisible) {
        systemUi.getStatusBar().setVisible(statusBarVisible);
        notifyInsetsChanges(systemUi, systemUi.getStatusBar().getId());
      }
      boolean navBarVisible = (requestedVisibleTypes & navigationBars()) != 0;
      if (systemUi.getNavigationBar().isVisible() != navBarVisible) {
        systemUi.getNavigationBar().setVisible(navBarVisible);
        notifyInsetsChanges(systemUi, systemUi.getNavigationBar().getId());
      }
    }

    void notifyInsetsChanges(SystemUi systemUi, @Nullable Integer type) {
      for (Entry<IWindow, WindowInfo> windowEntry : windows.entrySet()) {
        if (windowEntry.getValue().displayId == systemUi.getDisplayId()) {
          systemUi.putInsets(windowEntry.getValue());
          sendInsetsControlChanged(windowEntry.getKey(), type, false);
          // TODO: only send resize if the window resized.
          sendResize(windowEntry.getKey());
        }
      }
    }

    void sendInsetsControlChanged(
        IWindow window, @Nullable Integer type, boolean hasControlsChanged) {
      int sdk = RuntimeEnvironment.getApiLevel();
      WindowInfo windowInfo = checkNotNull(windows.get(window));
      InsetsState insetsState = new InsetsState(windowInfo.insetsState);
      // On R if we don't remove the sources that aren't changing we'll infinite loop when toggling
      // visibility of multiple bars.
      if (sdk == R && type != null) {
        for (int i = 0; i < Shadow.<ShadowInsetsState>extract(insetsState).getSourceSize(); i++) {
          if (i != type) {
            insetsState.removeSource(i);
          }
        }
      }
      if ((sdk == R && !hasControlsChanged) || sdk >= S && sdk <= S_V2) {
        ClassParameterBuilder params = new ClassParameterBuilder();
        params.add(InsetsState.class, windowInfo.insetsState);
        /* willMove */ params.addIf(sdk >= S, boolean.class, false);
        /* willResize */ params.addIf(sdk >= S, boolean.class, false);
        callInstanceMethod(window, "insetsChanged", params.build());
      } else {
        ClassParameterBuilder params = new ClassParameterBuilder();
        params.add(InsetsState.class, windowInfo.insetsState);
        // TODO: We should give control to the active window.
        if (sdk >= VANILLA_ICE_CREAM) {
          params.add(InsetsSourceControl.Array.class, new InsetsSourceControl.Array());
        } else {
          params.add(
              InsetsSourceControl[].class,
              windowInfo.hasInsetsControl ? populateInsetSourceControls(windowInfo, null) : null);
        }
        callInstanceMethod(window, "insetsControlChanged", params.build());
      }
    }

    void sendResize(IWindow window) {
      int sdk = RuntimeEnvironment.getApiLevel();
      WindowInfo windowInfo = checkNotNull(windows.get(window));
      configureWindowFrames(
          windowInfo,
          windowInfo.attrs,
          /* requestedSize= */ null,
          /* outFrame= */ null,
          /* outContentInsets= */ null,
          /* outVisibleInsets= */ null,
          /* outStableInsets= */ null,
          /* outInsetsState= */ null);
      Configuration configuration =
          RuntimeEnvironment.getApplication().getResources().getConfiguration();
      ClassParameterBuilder args = new ClassParameterBuilder();

      // The resized method has changed pretty much every other release, this is a canonicalize-d
      // set of all the parameters it has ever taken.
      if (sdk >= S) {
        /* frames */ args.add(ClientWindowFrames.class, windowInfo.frames);
      } else {
        /* frame */ args.add(Rect.class, windowInfo.frame);
        /* overscanInsets */ args.addIf(sdk <= Q, Rect.class, new Rect());
        /* contentInsets */ args.add(Rect.class, windowInfo.contentInsets);
        /* visibleInsets */ args.add(Rect.class, windowInfo.visibleInsets);
        /* stableInsets */ args.add(Rect.class, windowInfo.stableInsets);
        /* outsets */ args.addIf(sdk >= M && sdk <= Q, Rect.class, new Rect());
      }
      /* reportDraw */ args.add(boolean.class, false);
      if (sdk <= N_MR1) {
        /* newConfig */ args.add(Configuration.class, configuration);
      } else {
        /* newMergedConfiguration */ args.add(
            MergedConfiguration.class, new MergedConfiguration(configuration));
      }
      /* backDropFrame */ args.addIf(sdk >= N && sdk <= R, Rect.class, new Rect());
      if (sdk >= TIRAMISU) {
        /* insetsState */ args.add(InsetsState.class, windowInfo.insetsState);
      }
      /* forceLayout */ args.addIf(sdk >= N, boolean.class, false);
      /* alwaysConsumeNavBar */ args.addIf(sdk >= N, boolean.class, false);
      /* displayId */ args.addIf(sdk >= O, int.class, windowInfo.displayId);
      if (sdk >= P && sdk <= R) {
        /* displayCutout */ args.add(
            DisplayCutout.ParcelableWrapper.class, new DisplayCutout.ParcelableWrapper());
      }
      /* syncSeqId */ args.addIf(sdk >= TIRAMISU, int.class, 0);
      /* resizeMode */ args.addIf(sdk == TIRAMISU, int.class, 0);
      /* dragResizing */ args.addIf(sdk >= UPSIDE_DOWN_CAKE, boolean.class, false);
      if (sdk > UPSIDE_DOWN_CAKE) {
        /* activityWindowInfo */ args.add(ActivityWindowInfo.class, null);
      }
      callInstanceMethod(window, "resized", args.build());
    }

    private void transferWindowInsetsControlTo(WindowInfo windowInfo) {
      // If we don't transfer the controls on R then windows conflict when their insets mismatch,
      // resulting in infinite loops, and if no window has control then insets are not updated.
      // TODO: This is almost certainly not the correct logic for determining which window has
      //  control.
      if (RuntimeEnvironment.getApiLevel() != R) {
        return;
      }
      for (Entry<IWindow, WindowInfo> entry : windows.entrySet()) {
        boolean hasControl = entry.getValue() == windowInfo;
        if (entry.getValue().hasInsetsControl != hasControl) {
          entry.getValue().hasInsetsControl = hasControl;
          sendInsetsControlChanged(entry.getKey(), null, true);
        }
      }
    }

    @CanIgnoreReturnValue
    private InsetsSourceControl[] populateInsetSourceControls(
        WindowInfo windowInfo, InsetsSourceControl[] controls) {
      int sdk = RuntimeEnvironment.getApiLevel();
      // Skip bars after IME as they have the same public types as navigation/status bars and
      // their visibility combines.
      int lastControl = reflector(InsetsStateReflector.class).getImeType();
      if (controls == null) {
        controls = new InsetsSourceControl[lastControl + 1];
      }
      for (int i = 0; i <= lastControl; i++) {
        ClassParameterBuilder params = new ClassParameterBuilder();
        /* type */ params.add(int.class, i);
        /* leash */ params.add(SurfaceControl.class, null);
        /* surfacePosition */ params.add(Point.class, new Point());
        /* insetsHint */ params.addIf(sdk >= S, Insets.class, Insets.of(0, 0, 0, 0));
        controls[i] = callConstructor(InsetsSourceControl.class, params.build());
        // Populate the same insets as we did controls, otherwise the insets controller can
        // infinite loop as it sees the insets being added and removed every time.
        Shadow.<ShadowInsetsState>extract(windowInfo.insetsState).getOrCreateSource(i);
      }
      return controls;
    }
  }

  static final class WindowInfo {
    final Rect displayFrame = new Rect();
    final ClientWindowFrames frames;
    final Rect frame;
    final InsetsState insetsState =
        RuntimeEnvironment.getApiLevel() >= Q ? new InsetsState() : null;
    final Rect contentInsets = new Rect();
    final Rect visibleInsets = new Rect();
    final Rect stableInsets = new Rect();
    final WindowManager.LayoutParams attrs = new WindowManager.LayoutParams();
    int displayId = -1;
    int requestedVisibleTypes = RuntimeEnvironment.getApiLevel() >= R ? systemBars() : 0;
    boolean hasInsetsControl;

    WindowInfo() {
      if (RuntimeEnvironment.getApiLevel() >= S) {
        frames = new ClientWindowFrames();
        frame = frames.frame;
      } else {
        frames = null;
        frame = new Rect();
      }
    }

    void put(
        Rect outFrame,
        Rect outContentInsets,
        Rect outVisibleInsets,
        Rect outStableInsets,
        InsetsState outInsetsState) {
      if (outFrame != null) {
        outFrame.set(frame);
      }
      if (outContentInsets != null) {
        outContentInsets.set(contentInsets);
      }
      if (outVisibleInsets != null) {
        outVisibleInsets.set(visibleInsets);
      }
      if (outStableInsets != null) {
        outStableInsets.set(stableInsets);
      }
      if (outInsetsState != null) {
        outInsetsState.set(insetsState, /* copySources= */ true);
      }
    }
  }

  private static <T> T findFirst(Class<T> type, Object[] args) {
    return type.cast(stream(args).filter(type::isInstance).findFirst().get());
  }

  private static <T> T[] findAll(Class<T> type, Object[] args) {
    return stream(args).filter(type::isInstance).toArray(len -> (T[]) Array.newInstance(type, len));
  }

  private static final class ClassParameterBuilder {
    private final List<ClassParameter<?>> parameters = new ArrayList<>();

    <T> void add(Class<T> type, T parameter) {
      parameters.add(ClassParameter.from(type, parameter));
    }

    <T> void addIf(boolean shouldAdd, Class<T> type, T parameter) {
      if (shouldAdd) {
        add(type, parameter);
      }
    }

    ClassParameter<?>[] build() {
      return parameters.toArray(new ClassParameter<?>[0]);
    }
  }

  @ForType(BackMotionEvent.class)
  interface BackMotionEventReflector {
    @Constructor
    BackMotionEvent newBackMotionEvent(
        float touchX,
        float touchY,
        float progress,
        float velocityX,
        float velocityY,
        int swipeEdge,
        RemoteAnimationTarget departingAnimationTarget);

    @Constructor
    BackMotionEvent newBackMotionEventV(
        float touchX,
        float touchY,
        float progress,
        float velocityX,
        float velocityY,
        boolean triggerBack,
        int swipeEdge,
        RemoteAnimationTarget departingAnimationTarget);

    @Constructor
    BackMotionEvent newBackMotionEventPostV(
        float touchX,
        float touchY,
        long frameTime,
        float progress,
        boolean triggerBack,
        int swipeEdge,
        RemoteAnimationTarget departingAnimationTarget);
  }

  @ForType(className = "android.view.InsetsVisibilities")
  interface InsetsVisibilitiesReflector {
    boolean getVisibility(int type);
  }

  private static class BackMotionEvents {
    private BackMotionEvents() {}

    static BackMotionEvent newBackMotionEvent(
        @BackEvent.SwipeEdge int edge, float touchX, float touchY, float progress) {
      if (RuntimeEnvironment.getApiLevel() < VANILLA_ICE_CREAM) {
        return reflector(BackMotionEventReflector.class)
            .newBackMotionEvent(
                touchX, touchY, progress, 0f, // velocity x
                0f, // velocity y
                edge, // swipe edge
                null);
      }
      // normally we would consistently determine which constructor to call based on API level,
      // but that is tricky for in development SDKS. So just determine
      // what constructor to call based on the constructors we find reflectively
      java.lang.reflect.Constructor<?> theConstructor = findPublicConstructor();
      if (theConstructor.getParameterTypes()[2].equals(float.class)) {
        return reflector(BackMotionEventReflector.class)
            .newBackMotionEventV(
                touchX,
                touchY,
                progress,
                0f, // velocity x
                0f, // velocity y
                Boolean.FALSE, // trigger back
                edge, // swipe edge
                null);
      } else if (theConstructor.getParameterTypes()[2].equals(long.class)) {
        return reflector(BackMotionEventReflector.class)
            .newBackMotionEventPostV(
                touchX,
                touchY,
                SystemClock.uptimeMillis(), /* frameTime */
                progress,
                Boolean.FALSE, // trigger back
                edge, // swipe edge
                null);
      } else {
        throw new IllegalStateException("Could not find a BackMotionEvent constructor to call");
      }
    }

    private static java.lang.reflect.Constructor<?> findPublicConstructor() {
      for (java.lang.reflect.Constructor<?> constructor :
          BackMotionEvent.class.getDeclaredConstructors()) {
        if (constructor.getParameterCount() > 0 && Modifier.isPublic(constructor.getModifiers())) {
          return constructor;
        }
      }
      throw new IllegalStateException("Could not find a BackMotionEvent constructor");
    }
  }
}
