package org.robolectric.simulator;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Looper.getMainLooper;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnDrawListener;
import android.view.inspector.WindowInspector;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.FormatMethod;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.robolectric.shadows.ShadowViewRootImpl;
import org.robolectric.util.PerfStatsCollector;

/**
 * A handler that sends events to take a screenshot and update the Swing UI/VideoRecorder when
 * needed.
 *
 * <p>Requires ShadowView.useRealDrawTraversals to be enabled.
 */
public class ScreenUpdateHandler extends Handler
    implements OnDrawListener, Consumer<List<View>>, Executor {

  private static final int UPDATE_SCREEN = 102;
  private static final int START_UI = 101;

  private static final boolean DEBUG_LOGS = false;

  private final ImmutableList<FrameListener> frameListeners;

  private long lastCaptureTime = -1;

  private ScreenUpdateHandler(ImmutableList<FrameListener> frameListeners) {
    super(Looper.getMainLooper());
    this.frameListeners = frameListeners;
    log("create");
  }

  /**
   * Creates a ScreenUpdateHandler if any non-null FrameListeners are provided. Otherwise, returns
   * null.
   */
  @Nullable
  public static ScreenUpdateHandler create(FrameListener... frameListeners) {
    ImmutableList<FrameListener> frameListenerList =
        Stream.of(frameListeners).filter(Objects::nonNull).collect(toImmutableList());
    if (frameListenerList.isEmpty()) {
      return null;
    }
    return new ScreenUpdateHandler(frameListenerList);
  }

  public void requestStartUi() {
    log("requestStartUi");
    Message startUi = Message.obtain(this, START_UI);
    this.sendMessage(startUi);
    registerDrawListener();
    registerWindowListener();
    requestUpdateScreen();
    shadowOf(getMainLooper()).idle();
  }

  void requestUpdateScreen() {
    Message nextUpdate = Message.obtain(this, UPDATE_SCREEN);
    this.sendMessage(nextUpdate);
  }

  @Override
  public void handleMessage(Message msg) {
    if (msg.what == UPDATE_SCREEN) {
      captureScreen();
    } else if (msg.what == START_UI) {
      startUi();
    } else {
      super.handleMessage(msg);
    }
  }

  private void startUi() {
    for (FrameListener frameListener : frameListeners) {
      frameListener.onInitialize();
    }
  }

  /**
   * Register a listener for draw events.
   *
   * <p>This is a global listener that is not reset between tests, so only needs to be called once.
   */
  public void registerDrawListener() {
    log("registerDrawListener");
    ShadowViewRootImpl.internalRegisterGlobalOnDrawListener(this);
  }

  /**
   * Register a Window listener
   *
   * <p>This is to support rendering a blank screen when all views have been removed,
   *
   * <p>Window listeners are reset between tests so this must be called at the start of each test.
   */
  public void registerWindowListener() {
    if (getApiLevel() > BAKLAVA) {
      // Window view listener only available on POST_BAKLAVA because
      // WindowInspector.addGlobalWindowViewsListener
      // is only available on that SDK. Consider backporting to older SDKs in future. Arguably it
      // might not be worth the extra complexity to do so as rendering blank screens is not
      // critical
      WindowInspector.addGlobalWindowViewsListener(this, this);
    }
  }

  private void captureScreen() {
    // We don't want to capture the screen unnecessarily, such as in cases where a draw for
    // multiple windows has occurred on the same frame
    // So this records the last capture time, and we only capture the screen if the clock has
    // advanced.
    // This logic relies on the following facts:
    // a. Robolectric uses a fixed SystemClock
    // b. draws can only occur at the start of a vsync frame aka at a specific SystemClock time
    // c. capture screens are scheduled asynchronously on the main thread. Thus this code can
    //    only run when all draws for all windows are complete
    if (SystemClock.uptimeMillis() > lastCaptureTime) {
      log("captureScreen");
      lastCaptureTime = SystemClock.uptimeMillis();
      final Bitmap bitmap = takeScreenshot();
      ListIterator<FrameListener> listenerIterator = frameListeners.listIterator();
      while (listenerIterator.hasNext()) {
        FrameListener frameListener = listenerIterator.next();
        // only copy the bitmap if necessary. If this is the last frameListener in the list, use
        // original
        Bitmap bitmapForListener = listenerIterator.hasNext() ? copyBitmap(bitmap) : bitmap;
        frameListener.onFrame(bitmapForListener);
      }
    } else {
      log("skipping captureScreen");
    }
  }

  private Bitmap takeScreenshot() {
    return PerfStatsCollector.getInstance()
        .measure(
            "ScreenUpdateHandler-takeScreenshot",
            () -> InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot());
  }

  private Bitmap copyBitmap(Bitmap orig) {
    return PerfStatsCollector.getInstance()
        .measure("ScreenUpdateHandler-copyBitmap", () -> orig.copy(orig.getConfig(), false));
  }

  /** The global onDraw listener callback */
  @Override
  public void onDraw() {
    // Here we make the assumption that the draw will be done and complete when this is called.
    // This is a safe assumption for now since all drawing occurs on main Looper thread, but in
    // future, consider using a FrameCommitListener instead. Doing so will be needed if drawing
    // ever moves to a Render thread.
    log("onDraw update. Requested screen update");
    requestUpdateScreen();
  }

  /** The GlobalWindowViewsListener callback. Currently only supported on POST_BAKLAVA */
  @Override
  public void accept(List<View> views) {
    // A screen update is only needed in cases where there are no active views aka an empty screen
    // All other cases will be handled by onDraw listener
    if (views.isEmpty()) {
      log("no window views. Requested screen update");
      requestUpdateScreen();
    }
  }

  @FormatMethod
  private static void log(String formatString, Object... params) {
    if (DEBUG_LOGS) {
      Log.d("ScreenUpdateHandler", String.format(formatString, params));
    }
  }

  /** The executor callback for GlobalWindowViewsListener */
  @Override
  public void execute(Runnable command) {
    if (this.getLooper().isCurrentThread()) {
      command.run();
    } else {
      this.post(command);
    }
  }

  public void newTestStarted() {
    lastCaptureTime = -1;
    registerWindowListener();
    requestUpdateScreen();
  }
}
