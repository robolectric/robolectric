package org.robolectric.shadows;

import static android.app.UiAutomation.ROTATION_FREEZE_0;
import static android.app.UiAutomation.ROTATION_FREEZE_180;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Display;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link UiAutomation}. */
@Implements(value = UiAutomation.class)
public class ShadowUiAutomation {

  private static final Predicate<Root> IS_FOCUSABLE = hasLayoutFlag(FLAG_NOT_FOCUSABLE).negate();
  private static final Predicate<Root> IS_TOUCHABLE = hasLayoutFlag(FLAG_NOT_TOUCHABLE).negate();
  private static final Predicate<Root> IS_TOUCH_MODAL =
      IS_FOCUSABLE.and(hasLayoutFlag(FLAG_NOT_TOUCH_MODAL).negate());
  private static final Predicate<Root> WATCH_TOUCH_OUTSIDE =
      IS_TOUCH_MODAL.negate().and(hasLayoutFlag(FLAG_WATCH_OUTSIDE_TOUCH));

  /**
   * Sets the animation scale, see {@link UiAutomation#setAnimationScale(float)}. Provides backwards
   * compatible access to SDKs < T.
   */
  public static void setAnimationScaleCompat(float scale) {
    ContentResolver cr = RuntimeEnvironment.getApplication().getContentResolver();
    Settings.Global.putFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE, scale);
    Settings.Global.putFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE, scale);
    Settings.Global.putFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE, scale);
  }

  @Implementation(minSdk = TIRAMISU)
  protected void setAnimationScale(float scale) {
    setAnimationScaleCompat(scale);
  }

  @Implementation
  protected boolean setRotation(int rotation) {
    AtomicBoolean result = new AtomicBoolean(false);
    ShadowInstrumentation.runOnMainSyncNoIdle(
        () -> {
          if (rotation == UiAutomation.ROTATION_FREEZE_CURRENT
              || rotation == UiAutomation.ROTATION_UNFREEZE) {
            result.set(true);
            return;
          }
          Display display = ShadowDisplay.getDefaultDisplay();
          int currentRotation = display.getRotation();
          boolean isRotated =
              (rotation == ROTATION_FREEZE_0 || rotation == ROTATION_FREEZE_180)
                  != (currentRotation == ROTATION_FREEZE_0
                      || currentRotation == ROTATION_FREEZE_180);
          shadowOf(display).setRotation(rotation);
          if (isRotated) {
            int currentOrientation = Resources.getSystem().getConfiguration().orientation;
            String rotationQualifier =
                "+" + (currentOrientation == Configuration.ORIENTATION_PORTRAIT ? "land" : "port");
            ShadowDisplayManager.changeDisplay(display.getDisplayId(), rotationQualifier);
            RuntimeEnvironment.setQualifiers(rotationQualifier);
          }
          result.set(true);
        });
    return result.get();
  }

  @Implementation
  protected void throwIfNotConnectedLocked() {}

  @Implementation
  protected Bitmap takeScreenshot() throws Exception {
    if (!ShadowView.useRealGraphics()) {
      return null;
    }

    FutureTask<Bitmap> screenshotTask =
        new FutureTask<>(
            () -> {
              Point displaySize = new Point();
              ShadowDisplay.getDefaultDisplay().getRealSize(displaySize);
              Bitmap screenshot =
                  Bitmap.createBitmap(displaySize.x, displaySize.y, Bitmap.Config.ARGB_8888);
              Canvas screenshotCanvas = new Canvas(screenshot);
              Paint paint = new Paint();
              for (Root root : getViewRoots().reverse()) {
                View rootView = root.getRootView();
                if (rootView.getWidth() <= 0 || rootView.getHeight() <= 0) {
                  continue;
                }
                Bitmap window =
                    Bitmap.createBitmap(
                        rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
                if (HardwareRenderingScreenshot.canTakeScreenshot(rootView)) {
                  HardwareRenderingScreenshot.takeScreenshot(rootView, window);
                } else {
                  Canvas windowCanvas = new Canvas(window);
                  rootView.draw(windowCanvas);
                }
                screenshotCanvas.drawBitmap(
                    window, root.locationOnScreen.x, root.locationOnScreen.y, paint);
              }
              return screenshot;
            });

    ShadowInstrumentation.runOnMainSyncNoIdle(screenshotTask);
    return screenshotTask.get();
  }

  /**
   * Injects a motion event into the appropriate window, see {@link
   * UiAutomation#injectInputEvent(InputEvent, boolean)}. This can be used through the {@link
   * UiAutomation} API, this method is provided for backwards compatibility with SDK < 18.
   */
  public static boolean injectInputEvent(InputEvent event) {
    AtomicBoolean result = new AtomicBoolean(false);
    ShadowInstrumentation.runOnMainSyncNoIdle(
        () -> {
          if (event instanceof MotionEvent) {
            result.set(injectMotionEvent((MotionEvent) event));
          } else if (event instanceof KeyEvent) {
            result.set(injectKeyEvent((KeyEvent) event));
          } else {
            throw new IllegalArgumentException("Unrecognized event type: " + event);
          }
        });
    return result.get();
  }

  @Implementation
  protected boolean injectInputEvent(InputEvent event, boolean sync) {
    return injectInputEvent(event);
  }

  private static boolean injectMotionEvent(MotionEvent event) {
    // TODO(paulsowden): The real implementation will send a full event stream (a touch down
    //  followed by a series of moves, etc) to the same window/root even if the subsequent events
    //  leave the window bounds, and will split pointer down events based on the window flags.
    //  This will be necessary to support more sophisticated multi-window use cases.

    List<Root> touchableRoots = getViewRoots().stream().filter(IS_TOUCHABLE).collect(toList());
    for (int i = 0; i < touchableRoots.size(); i++) {
      Root root = touchableRoots.get(i);
      if (i == touchableRoots.size() - 1 || root.isTouchModal() || root.isTouchInside(event)) {
        event.offsetLocation(-root.locationOnScreen.x, -root.locationOnScreen.y);
        root.getRootView().dispatchTouchEvent(event);
        event.offsetLocation(root.locationOnScreen.x, root.locationOnScreen.y);
        break;
      } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN && root.watchTouchOutside()) {
        MotionEvent outsideEvent = MotionEvent.obtain(event);
        outsideEvent.setAction(MotionEvent.ACTION_OUTSIDE);
        outsideEvent.offsetLocation(-root.locationOnScreen.x, -root.locationOnScreen.y);
        root.getRootView().dispatchTouchEvent(outsideEvent);
        outsideEvent.recycle();
      }
    }
    return true;
  }

  private static boolean injectKeyEvent(KeyEvent event) {
    getViewRoots().stream()
        .filter(IS_FOCUSABLE)
        .findFirst()
        .ifPresent(root -> root.getRootView().dispatchKeyEvent(event));
    return true;
  }

  private static ImmutableList<Root> getViewRoots() {
    List<ViewRootImpl> viewRootImpls = getViewRootImpls();
    List<WindowManager.LayoutParams> params = getRootLayoutParams();
    checkState(
        params.size() == viewRootImpls.size(),
        "number params is not consistent with number of view roots!");
    Set<IBinder> startedActivityTokens = getStartedActivityTokens();
    ArrayList<Root> roots = new ArrayList<>();
    for (int i = 0; i < viewRootImpls.size(); i++) {
      Root root = new Root(viewRootImpls.get(i), params.get(i), i);
      // TODO: Should we also filter out sub-windows of non-started application windows?
      if (root.getType() != WindowManager.LayoutParams.TYPE_BASE_APPLICATION
          || startedActivityTokens.contains(root.impl.getView().getApplicationWindowToken())) {
        roots.add(root);
      }
    }
    roots.sort(
        comparingInt(Root::getType)
            .reversed()
            .thenComparing(comparingInt(Root::getIndex).reversed()));
    return ImmutableList.copyOf(roots);
  }

  @SuppressWarnings("unchecked")
  private static List<ViewRootImpl> getViewRootImpls() {
    Object windowManager = getViewRootsContainer();
    Object viewRootsObj = ReflectionHelpers.getField(windowManager, "mRoots");
    Class<?> viewRootsClass = viewRootsObj.getClass();
    if (ViewRootImpl[].class.isAssignableFrom(viewRootsClass)) {
      return Arrays.asList((ViewRootImpl[]) viewRootsObj);
    } else if (List.class.isAssignableFrom(viewRootsClass)) {
      return (List<ViewRootImpl>) viewRootsObj;
    } else {
      throw new IllegalStateException(
          "WindowManager.mRoots is an unknown type " + viewRootsClass.getName());
    }
  }

  @SuppressWarnings("unchecked")
  private static List<WindowManager.LayoutParams> getRootLayoutParams() {
    Object windowManager = getViewRootsContainer();
    Object paramsObj = ReflectionHelpers.getField(windowManager, "mParams");
    Class<?> paramsClass = paramsObj.getClass();
    if (WindowManager.LayoutParams[].class.isAssignableFrom(paramsClass)) {
      return Arrays.asList((WindowManager.LayoutParams[]) paramsObj);
    } else if (List.class.isAssignableFrom(paramsClass)) {
      return (List<WindowManager.LayoutParams>) paramsObj;
    } else {
      throw new IllegalStateException(
          "WindowManager.mParams is an unknown type " + paramsClass.getName());
    }
  }

  private static Object getViewRootsContainer() {
    return WindowManagerGlobal.getInstance();
  }

  private static Set<IBinder> getStartedActivityTokens() {
    Set<Activity> startedActivities = newConcurrentHashSet();
    ShadowInstrumentation.runOnMainSyncNoIdle(
        () -> {
          ActivityLifecycleMonitor monitor = ActivityLifecycleMonitorRegistry.getInstance();
          startedActivities.addAll(monitor.getActivitiesInStage(Stage.STARTED));
          startedActivities.addAll(monitor.getActivitiesInStage(Stage.RESUMED));
        });

    return startedActivities.stream()
        .map(activity -> activity.getWindow().getDecorView().getApplicationWindowToken())
        .collect(toSet());
  }

  private static Predicate<Root> hasLayoutFlag(int flag) {
    return root -> (root.params.flags & flag) == flag;
  }

  private static final class Root {
    final ViewRootImpl impl;
    final WindowManager.LayoutParams params;
    final int index;
    final Point locationOnScreen;

    Root(ViewRootImpl impl, WindowManager.LayoutParams params, int index) {
      this.impl = impl;
      this.params = params;
      this.index = index;

      int[] coords = new int[2];
      getRootView().getLocationOnScreen(coords);
      locationOnScreen = new Point(coords[0], coords[1]);
    }

    int getIndex() {
      return index;
    }

    int getType() {
      return params.type;
    }

    View getRootView() {
      return impl.getView();
    }

    boolean isTouchInside(MotionEvent event) {
      int index = event.getActionIndex();
      return event.getX(index) >= locationOnScreen.x
          && event.getX(index) <= locationOnScreen.x + impl.getView().getWidth()
          && event.getY(index) >= locationOnScreen.y
          && event.getY(index) <= locationOnScreen.y + impl.getView().getHeight();
    }

    boolean isTouchModal() {
      return IS_TOUCH_MODAL.test(this);
    }

    boolean watchTouchOutside() {
      return WATCH_TOUCH_OUTSIDE.test(this);
    }
  }
}
