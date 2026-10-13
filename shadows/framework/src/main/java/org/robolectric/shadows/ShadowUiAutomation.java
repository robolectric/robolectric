package org.robolectric.shadows;

import static android.app.UiAutomation.ROTATION_FREEZE_0;
import static android.app.UiAutomation.ROTATION_FREEZE_180;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.internal.WindowConfigurations;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link UiAutomation}. */
@Implements(UiAutomation.class)
public class ShadowUiAutomation {

  private static final Predicate<Root> IS_FOCUSABLE = hasLayoutFlag(FLAG_NOT_FOCUSABLE).negate();
  private static final Predicate<Root> IS_TOUCHABLE = hasLayoutFlag(FLAG_NOT_TOUCHABLE).negate();
  private static final Predicate<Root> IS_TOUCH_MODAL =
      IS_FOCUSABLE.and(hasLayoutFlag(FLAG_NOT_TOUCH_MODAL).negate());
  private static final Predicate<Root> WATCH_TOUCH_OUTSIDE =
      IS_TOUCH_MODAL.negate().and(hasLayoutFlag(FLAG_WATCH_OUTSIDE_TOUCH));
  // The sizes and densities displays had before a shell command overrode them.
  private static final Map<Integer, Point> initialDisplaySizes = new HashMap<>();
  private static final Map<Integer, Integer> initialDisplayDensities = new HashMap<>();

  private static final Predicate<Root> IS_VISIBLE =
      root -> root.getRootView().getWidth() > 0 && root.getRootView().getHeight() > 0;

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

  @Implementation(minSdk = P)
  protected void grantRuntimePermission(String packageName, String permission) {
    getShadowInstrumentation().grantPermissions(permission);
  }

  static ShadowInstrumentation getShadowInstrumentation() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    return Shadow.extract(activityThread.getInstrumentation());
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

  /**
   * Runs a shell command, as {@code adb shell} would on a device, and returns its output.
   *
   * <p>Robolectric runs these window manager commands, which take the display they apply to with
   * {@code -d DISPLAY_ID}:
   *
   * <ul>
   *   <li>{@code wm size [reset|WxH|WdpxHdp]}
   *   <li>{@code wm density [reset|DENSITY]}
   *   <li>{@code wm set-ignore-orientation-request true|false}
   *   <li>{@code wm get-ignore-orientation-request}
   * </ul>
   *
   * <p>Other commands output nothing.
   */
  @Implementation
  protected ParcelFileDescriptor executeShellCommand(String command) {
    AtomicReference<String> output = new AtomicReference<>("");
    ShadowInstrumentation.runOnMainSyncNoIdle(() -> output.set(runShellCommand(command)));
    shadowMainLooper().idle();
    try {
      File file = File.createTempFile("robolectric-shell", ".out");
      file.deleteOnExit();
      Files.write(file.toPath(), output.get().getBytes(UTF_8));
      return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String runShellCommand(String command) {
    List<String> args = new ArrayList<>(Arrays.asList(command.trim().split("\\s+")));
    if (args.size() < 2 || !args.get(0).equals("wm")) {
      return "";
    }
    String windowManagerCommand = args.get(1);
    args = new ArrayList<>(args.subList(2, args.size()));
    int displayId = Display.DEFAULT_DISPLAY;
    int displayOption = args.indexOf("-d");
    if (displayOption >= 0 && displayOption + 1 < args.size()) {
      displayId = Integer.parseInt(args.get(displayOption + 1));
      args.subList(displayOption, displayOption + 2).clear();
    }
    switch (windowManagerCommand) {
      case "size":
        return runDisplaySize(displayId, args.isEmpty() ? null : args.get(0));
      case "density":
        return runDisplayDensity(displayId, args.isEmpty() ? null : args.get(0));
      case "set-ignore-orientation-request":
        if (args.isEmpty()) {
          return "Error: expecting true, 1, false, 0, but we get null\n";
        }
        boolean ignoreOrientationRequest;
        switch (args.get(0)) {
          case "true":
          case "1":
            ignoreOrientationRequest = true;
            break;
          case "false":
          case "0":
            ignoreOrientationRequest = false;
            break;
          default:
            return "Error: expecting true, 1, false, 0, but we get " + args.get(0) + "\n";
        }
        WindowConfigurations.setIgnoreOrientationRequest(displayId, ignoreOrientationRequest);
        DisplayChanges.onIgnoreOrientationRequestChanged(displayId);
        return "";
      case "get-ignore-orientation-request":
        return "ignoreOrientationRequest "
            + WindowConfigurations.isIgnoringOrientationRequest(displayId)
            + " for displayId="
            + displayId
            + "\n";
      default:
        return "";
    }
  }

  private static String runDisplaySize(int displayId, @Nullable String size) {
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return "";
    }
    Point currentSize = new Point(displayInfo.logicalWidth, displayInfo.logicalHeight);
    Point initialSize = initialDisplaySizes.getOrDefault(displayId, currentSize);
    if (size == null) {
      return "Physical size: "
          + initialSize.x
          + "x"
          + initialSize.y
          + "\n"
          + (initialSize.equals(currentSize)
              ? ""
              : "Override size: " + currentSize.x + "x" + currentSize.y + "\n");
    }
    Point newSize = initialSize;
    if (!size.equals("reset")) {
      int separator = size.indexOf('x');
      if (separator <= 0 || separator >= size.length() - 1) {
        return "Error: bad size " + size + "\n";
      }
      try {
        newSize =
            new Point(
                parseDimension(size.substring(0, separator), displayInfo.logicalDensityDpi),
                parseDimension(size.substring(separator + 1), displayInfo.logicalDensityDpi));
      } catch (NumberFormatException e) {
        return "Error: bad number " + e + "\n";
      }
    }
    initialDisplaySizes.putIfAbsent(displayId, currentSize);
    changeDisplay(displayId, newSize.x, newSize.y, displayInfo.logicalDensityDpi);
    return "";
  }

  private static int parseDimension(String dimension, int densityDpi) {
    if (dimension.endsWith("px")) {
      return Integer.parseInt(dimension.substring(0, dimension.length() - 2));
    }
    if (dimension.endsWith("dp")) {
      return Integer.parseInt(dimension.substring(0, dimension.length() - 2))
          * densityDpi
          / DisplayMetrics.DENSITY_DEFAULT;
    }
    return Integer.parseInt(dimension);
  }

  private static String runDisplayDensity(int displayId, @Nullable String density) {
    DisplayInfo displayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(displayId);
    if (displayInfo == null) {
      return "";
    }
    int currentDensity = displayInfo.logicalDensityDpi;
    int initialDensity = initialDisplayDensities.getOrDefault(displayId, currentDensity);
    if (density == null) {
      return "Physical density: "
          + initialDensity
          + "\n"
          + (initialDensity == currentDensity ? "" : "Override density: " + currentDensity + "\n");
    }
    int newDensity = initialDensity;
    if (!density.equals("reset")) {
      try {
        newDensity = Integer.parseInt(density);
      } catch (NumberFormatException e) {
        return "Error: bad number " + e + "\n";
      }
      if (newDensity < 72) {
        return "Error: density must be >= 72\n";
      }
    }
    initialDisplayDensities.putIfAbsent(displayId, currentDensity);
    changeDisplay(displayId, displayInfo.logicalWidth, displayInfo.logicalHeight, newDensity);
    return "";
  }

  /** Gives a display a size in pixels and a density, and its windows the configuration for them. */
  private static void changeDisplay(int displayId, int width, int height, int densityDpi) {
    float density = densityDpi / (float) DisplayMetrics.DENSITY_DEFAULT;
    String qualifiers =
        "+w"
            + (int) (width / density + 0.5f)
            + "dp-h"
            + (int) (height / density + 0.5f)
            + "dp-"
            + (width > height ? "land" : "port")
            + "-"
            + densityDpi
            + "dpi";
    if (displayId == Display.DEFAULT_DISPLAY) {
      RuntimeEnvironment.setQualifiers(qualifiers);
    } else {
      ShadowDisplayManager.changeDisplay(displayId, qualifiers);
    }
  }

  @Resetter
  public static void reset() {
    initialDisplaySizes.clear();
    initialDisplayDensities.clear();
  }

  /**
   * Real Android will via a series of IPCs that eventually obtain an image from SurfaceFlinger.
   *
   * <p>Since Robolectric doesn't run any of those backend services, this shadow simulates the
   * result by capturing an image from each active Window listed in WindowManager, and stiches them
   * together into a resuting image.
   *
   * <p>Also unlike real Android, this method can be called from on and outside the main Looper
   * thread.
   *
   * @return a Bitmap of the display
   */
  @Implementation
  protected Bitmap takeScreenshot() throws Exception {
    if (!ShadowView.useRealGraphics()) {
      return null;
    }

    FutureTask<Bitmap> screenshotTask =
        new FutureTask<>(
            () -> {
              List<Root> visibleRoots =
                  getViewRoots().reverse().stream().filter(IS_VISIBLE).collect(toList());
              Point displaySize = new Point();
              ShadowDisplay.getDefaultDisplay().getRealSize(displaySize);
              Bitmap screenshot =
                  Bitmap.createBitmap(displaySize.x, displaySize.y, Bitmap.Config.ARGB_8888);

              if (visibleRoots.isEmpty()) {
                // short circuit, nothing to do
                return screenshot;
              } else if (visibleRoots.size() == 1
                  && visibleRoots.getFirst().getRootView().getWidth() == displaySize.x
                  && visibleRoots.getFirst().getRootView().getHeight() == displaySize.y) {
                // optimized path - there is only a single full screen window. Just draw into
                // screenshot and return
                drawIntoBitmap(screenshot, visibleRoots.getFirst());
                return screenshot;
              }

              // multiple windows, draw them independently
              Canvas screenshotCanvas = new Canvas(screenshot);
              Paint paint = new Paint();
              for (Root root : visibleRoots) {
                Bitmap window =
                    Bitmap.createBitmap(
                        root.getRootView().getWidth(),
                        root.getRootView().getHeight(),
                        Bitmap.Config.ARGB_8888);
                drawIntoBitmap(window, root);
                screenshotCanvas.drawBitmap(
                    window, root.locationOnScreen.x, root.locationOnScreen.y, paint);
                window.recycle();
              }
              return screenshot;
            });

    ShadowInstrumentation.runOnMainSyncNoIdle(screenshotTask);
    return screenshotTask.get();
  }

  private static void drawIntoBitmap(Bitmap bitmap, Root root) {
    View rootView = root.getRootView();
    if (ShadowView.useRealViewAnimations()) {
      ((ShadowView) Shadow.extract(rootView)).setDrawingTime(SystemClock.uptimeMillis());
    }
    if (ShadowView.areRealDrawTraversalsEnabled()) {
      Surface surface = root.impl.mSurface;
      ShadowPixelCopy.captureImageFromSurface(surface, bitmap, getBoundsInSurface(rootView));
    } else if (HardwareRenderingScreenshot.canTakeScreenshot(rootView)) {
      HardwareRenderingScreenshot.takeScreenshot(rootView, bitmap);
    } else {
      Canvas windowCanvas = new Canvas(bitmap);
      rootView.draw(windowCanvas);
    }
  }

  private static Rect getBoundsInSurface(View view) {
    int[] locationInSurface = new int[2];
    view.getLocationInSurface(locationInSurface);

    int x = locationInSurface[0];
    int y = locationInSurface[1];
    return new Rect(x, y, x + view.getWidth(), y + view.getHeight());
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
      if (i == touchableRoots.size() - 1
          || (root.isTouchModal() && root.isTouchInsideActivityWindow(event))
          || root.isTouchInside(event)) {
        Activity activity = root.getActivity();
        if (activity != null && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
          Shadow.<ShadowActivity>extract(activity).onTouched();
        }
        event.offsetLocation(-root.locationOnScreen.x, -root.locationOnScreen.y);
        if (shouldDispatchGenericMotionEvent(event)) {
          root.getRootView().dispatchGenericMotionEvent(event);
        } else {
          root.getRootView().dispatchTouchEvent(event);
        }
        event.offsetLocation(root.locationOnScreen.x, root.locationOnScreen.y);
        break;
      } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN && root.watchTouchOutside()) {
        MotionEvent outsideEvent = MotionEvent.obtain(event);
        outsideEvent.setAction(MotionEvent.ACTION_OUTSIDE);
        outsideEvent.offsetLocation(-root.locationOnScreen.x, -root.locationOnScreen.y);
        if (shouldDispatchGenericMotionEvent(outsideEvent)) {
          root.getRootView().dispatchGenericMotionEvent(outsideEvent);
        } else {
          root.getRootView().dispatchTouchEvent(outsideEvent);
        }
        outsideEvent.recycle();
      }
    }
    return true;
  }

  // Mouse scroll events should be dispatched through dispatchGenericMotionEvent instead of the
  // default dispatchTouchEvent.
  private static boolean shouldDispatchGenericMotionEvent(MotionEvent event) {
    return event.isFromSource(InputDevice.SOURCE_MOUSE)
        && event.getActionMasked() == MotionEvent.ACTION_SCROLL;
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

    /** Returns the activity the window belongs to, or null if it doesn't belong to one. */
    @Nullable
    Activity getActivity() {
      return LiveActivities.get(params.token);
    }

    /**
     * Returns whether the touch is in the window of the activity that this window belongs to. An
     * activity doesn't get the touches outside of its own window, such as a freeform window or one
     * half of split screen.
     */
    boolean isTouchInsideActivityWindow(MotionEvent event) {
      Activity activity = getActivity();
      Rect activityWindowBounds =
          activity != null
              ? WindowConfigurations.getWindowBounds(
                  impl.getView().getDisplay().getDisplayId(),
                  activity.getResources().getConfiguration())
              : null;
      int index = event.getActionIndex();
      return activityWindowBounds == null
          || activityWindowBounds.contains((int) event.getX(index), (int) event.getY(index));
    }

    boolean watchTouchOutside() {
      return WATCH_TOUCH_OUTSIDE.test(this);
    }
  }
}
