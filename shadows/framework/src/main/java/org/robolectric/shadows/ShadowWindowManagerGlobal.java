package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static org.robolectric.shadows.ShadowView.useRealGraphics;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.FloatRange;
import android.annotation.Nullable;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowSession;
import android.view.MotionEvent;
import android.view.RemoteAnimationTarget;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManagerGlobal;
import android.window.BackEvent;
import android.window.BackMotionEvent;
import android.window.OnBackInvokedCallbackInfo;
import java.io.Closeable;
import java.lang.reflect.Proxy;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link WindowManagerGlobal}. */
@SuppressWarnings("unused") // Unused params are implementations of Android SDK methods.
@Implements(value = WindowManagerGlobal.class, isInAndroidSdk = false, looseSignatures = true)
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
                      case "setOnBackInvokedCallbackInfo":
                        windowSessionDelegate.onBackInvokedCallbackInfo =
                            (OnBackInvokedCallbackInfo) args[1];
                        return null;
                      case "reportSystemGestureExclusionChanged":
                        windowSessionDelegate.systemGestureExclusionRects = (List<Rect>) args[1];
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
    // From WindowManagerGlobal (was WindowManagerImpl in JB).
    static final int ADD_FLAG_IN_TOUCH_MODE = 0x1;
    static final int ADD_FLAG_APP_VISIBLE = 0x2;

    // TODO: Default to touch mode always.
    private boolean inTouchMode = useRealGraphics();
    @Nullable protected ClipData lastDragClipData;
    @Nullable private OnBackInvokedCallbackInfo onBackInvokedCallbackInfo;
    @Nullable private List<Rect> systemGestureExclusionRects;
    @Nullable private PredictiveBackGesture currentPredictiveBackGesture;

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
  }

  private static class BackMotionEvents {
    private BackMotionEvents() {}

    static BackMotionEvent newBackMotionEvent(
        @BackEvent.SwipeEdge int edge, float touchX, float touchY, float progress) {
      if (RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE) {
        try {
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
        } catch (Throwable t) {
          if (NoSuchMethodException.class.isInstance(t) || AssertionError.class.isInstance(t)) {
            // fall through, assuming (perhaps falsely?) this exception is thrown by reflector(),
            // and not the method reflected in to.
          } else {
            if (RuntimeException.class.isInstance(t)) {
              throw (RuntimeException) t;
            } else {
              throw new RuntimeException(t);
            }
          }
        }
      }
      return reflector(BackMotionEventReflector.class)
          .newBackMotionEvent(
              touchX, touchY, progress, 0f, // velocity x
              0f, // velocity y
              edge, // swipe edge
              null);
    }
  }
}
