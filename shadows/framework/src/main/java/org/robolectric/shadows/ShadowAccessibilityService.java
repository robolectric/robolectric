package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityService.GestureResultCallback;
import android.accessibilityservice.AccessibilityService.ScreenshotErrorCode;
import android.accessibilityservice.AccessibilityService.ScreenshotResult;
import android.accessibilityservice.AccessibilityService.TakeScreenshotCallback;
import android.accessibilityservice.GestureDescription;
import android.graphics.ColorSpace;
import android.graphics.ColorSpace.Named;
import android.hardware.HardwareBuffer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.Display;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import com.google.common.collect.ArrayListMultimap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow of AccessibilityService that tracks global actions and provides a mechanism to simulate
 * the window list.
 */
@Implements(AccessibilityService.class)
public class ShadowAccessibilityService extends ShadowService {

  private final List<Integer> globalActionsPerformed = new ArrayList<>();
  private List<AccessibilityNodeInfo.AccessibilityAction> systemActions;
  private final ArrayListMultimap<Integer, AccessibilityWindowInfo> windows =
      ArrayListMultimap.create();
  private final List<GestureDispatch> gesturesDispatched = new ArrayList<>();

  private boolean canDispatchGestures = true;

  @ScreenshotErrorCode
  private int takeScreenshotErrorCode = AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR;

  private boolean isScreenshotError = false;

  @Implementation
  protected boolean performGlobalAction(int action) {
    globalActionsPerformed.add(action);
    return true;
  }

  public List<Integer> getGlobalActionsPerformed() {
    return globalActionsPerformed;
  }

  @Implementation(minSdk = S)
  protected List<AccessibilityNodeInfo.AccessibilityAction> getSystemActions() {
    return systemActions;
  }

  public final void setSystemActions(
      List<AccessibilityNodeInfo.AccessibilityAction> systemActions) {
    this.systemActions = systemActions;
  }

  /**
   * Returns a representation of interactive windows shown on the device's default display. Mirrors
   * the values provided to {@link #setWindows(List<AccessibilityWindowInfo>)}. Returns an empty
   * list if not set.
   */
  @Implementation
  protected List<AccessibilityWindowInfo> getWindows() {
    List<AccessibilityWindowInfo> windowInfos = windows.get(Display.DEFAULT_DISPLAY);
    if (windowInfos != null) {
      return new ArrayList<>(windowInfos);
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Returns a representation of interactive windows shown on the device's all displays. An empty
   * list will be returned for default display and {@code null} will be return for other displays if
   * they are not set by {@link #setWindowsOnDisplay(int, List)}.
   */
  @Implementation(minSdk = R)
  protected SparseArray<List<AccessibilityWindowInfo>> getWindowsOnAllDisplays() {
    return cloneWindowOnAllDisplays();
  }

  private SparseArray<List<AccessibilityWindowInfo>> cloneWindowOnAllDisplays() {
    SparseArray<List<AccessibilityWindowInfo>> anotherArray = new SparseArray<>();
    for (int displayId : windows.keySet()) {
      anotherArray.put(displayId, windows.get(displayId));
    }

    if (anotherArray.get(Display.DEFAULT_DISPLAY) == null) {
      anotherArray.put(Display.DEFAULT_DISPLAY, new ArrayList<>());
    }

    return anotherArray;
  }

  @Implementation(minSdk = N)
  protected boolean dispatchGesture(
      GestureDescription gesture, GestureResultCallback callback, Handler handler) {
    if (canDispatchGestures) {
      gesturesDispatched.add(new GestureDispatch(gesture, callback));
    }
    return canDispatchGestures;
  }

  @Implementation(minSdk = R)
  protected void takeScreenshot(
      int displayId, Executor executor, AccessibilityService.TakeScreenshotCallback callback) {
    executor.execute(
        () -> {
          if (isScreenshotError) {
            callback.onFailure(takeScreenshotErrorCode);
            return;
          }

          HardwareBuffer hardwareBuffer =
              HardwareBuffer.create(1, 1, HardwareBuffer.RGBA_8888, 1, 0L);
          ColorSpace colorSpace = ColorSpace.get(Named.SRGB);
          long timestamp = SystemClock.elapsedRealtimeNanos();
          ScreenshotResult screenshotResult =
              ReflectionHelpers.callConstructor(
                  ScreenshotResult.class,
                  new ClassParameter<>(HardwareBuffer.class, hardwareBuffer),
                  new ClassParameter<>(ColorSpace.class, colorSpace),
                  new ClassParameter<>(long.class, timestamp));
          callback.onSuccess(screenshotResult);
        });
  }

  /**
   * Sets {@link AccessibilityService#takeScreenshot(int, Executor, TakeScreenshotCallback)} to
   * start returning the given {@code errorCode}.
   *
   * @see #unsetTakeScreenshotErrorCode() to unset the error condition.
   */
  public void setTakeScreenshotErrorCode(@ScreenshotErrorCode int errorCode) {
    this.isScreenshotError = true;
    this.takeScreenshotErrorCode = errorCode;
  }

  /**
   * Sets {@link AccessibilityService#takeScreenshot(int, Executor, TakeScreenshotCallback)} to
   * start returning successful results again.
   *
   * @see #setTakeScreenshotErrorCode(int) to set an error condition instead.
   */
  public void unsetTakeScreenshotErrorCode() {
    this.isScreenshotError = false;
  }

  /**
   * Sets the list of interactive windows shown on the device's default display as reported by
   * {@link #getWindows()}
   */
  public void setWindows(List<AccessibilityWindowInfo> windowList) {
    setWindowsOnDisplay(Display.DEFAULT_DISPLAY, windowList);
  }

  /**
   * Sets the list of interactive windows shown on the device's {@code displayId} display. If the
   * {@code windowList} is null, we will remove the list with given {@code displayId} display as
   * reported by {@link #getWindowsOnAllDisplays()}.
   */
  public void setWindowsOnDisplay(
      int displayId, @Nullable List<AccessibilityWindowInfo> windowList) {
    windows.removeAll(displayId);
    if (windowList != null) {
      windows.putAll(displayId, windowList);
    }
  }

  /**
   * Returns a list of gestures that have been dispatched.
   *
   * <p>Gestures are dispatched by calling {@link AccessibilityService#dispatchGesture}.
   */
  public List<GestureDispatch> getGesturesDispatched() {
    return gesturesDispatched;
  }

  /**
   * Sets whether the service is currently able to dispatch gestures.
   *
   * <p>If {@code false}, {@link AccessibilityService#dispatchGesture} will return {@code false}.
   */
  public void setCanDispatchGestures(boolean canDispatchGestures) {
    this.canDispatchGestures = canDispatchGestures;
  }

  /**
   * Represents a gesture that has been dispatched through the accessibility service.
   *
   * <p>Gestures are dispatched by calling {@link AccessibilityService#dispatchGesture}.
   */
  public static final class GestureDispatch {
    private final GestureDescription description;
    private final GestureResultCallback callback;

    public GestureDispatch(GestureDescription description, GestureResultCallback callback) {
      this.description = description;
      this.callback = callback;
    }

    /** The description of the gesture to be dispatched. Includes timestamps and the path. */
    public GestureDescription description() {
      return description;
    }

    /**
     * The callback that is to be invoked once the gesture has finished dispatching.
     *
     * <p>The shadow itself does not invoke this callback. You must manually invoke it to run it.
     */
    public GestureResultCallback callback() {
      return callback;
    }
  }
}
