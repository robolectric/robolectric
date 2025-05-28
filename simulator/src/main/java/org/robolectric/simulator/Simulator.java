package org.robolectric.simulator;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.app.Activity;
import android.app.Application;
import android.app.UiAutomation;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.time.Duration;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowChoreographer;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPausedLooper;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.shadows.ShadowView;

/** The main entry point for the Robolectric Simulator for use in existing Robolectric tests. */
@Beta
public final class Simulator {

  private SimulatorFrame simulatorFrame;
  private float displayWidth;
  private float displayHeight;

  private final Class<? extends Activity> activityClassToLaunch;

  public Simulator() {
    activityClassToLaunch = null;
  }

  public Simulator(Class<? extends Activity> activityClassToLaunch) {
    this.activityClassToLaunch = activityClassToLaunch;
  }

  public void start() {
    Preconditions.checkState(ShadowView.useRealGraphics());
    Preconditions.checkState(ShadowLooper.looperMode() != Mode.LEGACY);
    System.setProperty("java.awt.headless", "false");
    ShadowView.setUseRealViewAnimations(true);
    ShadowChoreographer.setPaused(true);
    ShadowChoreographer.setFrameDelay(Duration.ofMillis(15));

    if (this.activityClassToLaunch != null) {
      System.err.println("Launching " + this.activityClassToLaunch.getName());
      Robolectric.setupActivity(this.activityClassToLaunch);
    }
    // Inject an off-screen motion event to avoid a blank screen when the simulator first starts.
    postMotionEvent();

    startUi();
    captureScreen();
    loop();
  }

  private void loop() {
    ShadowPausedLooper shadowLooper = Shadow.extract(Looper.myLooper());
    shadowLooper.idle();
    Choreographer.getInstance().postFrameCallback(new SimulatorFrameCallback());
    while (true) {
      long currentSystemTime = System.nanoTime();
      long nextTaskTime = shadowLooper.getNextScheduledTaskTime().toMillis();
      long nextVsyncTime = ShadowChoreographer.getNextVsyncTime();
      long timeoutTime =
          nextTaskTime == 0 || nextVsyncTime == 0
              ? max(nextTaskTime, nextVsyncTime)
              : min(nextTaskTime, nextVsyncTime);
      shadowLooper.poll(timeoutTime == 0 ? 0 : max(1, timeoutTime - SystemClock.uptimeMillis()));
      ShadowSystemClock.advanceBy(Duration.ofNanos(System.nanoTime() - currentSystemTime));
      shadowLooper.idle();
      captureScreen();
    }
  }

  private void startUi() {
    Application application = RuntimeEnvironment.getApplication();
    DisplayManager displayManager =
        (DisplayManager) application.getSystemService(Context.DISPLAY_SERVICE);
    Display display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
    this.displayWidth = display.getWidth();
    this.displayHeight = display.getHeight();
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    final int apiLevel = RuntimeEnvironment.getApiLevel();
    SwingUtilities.invokeLater(
        () -> {
          simulatorFrame =
              new SimulatorFrame((int) this.displayWidth, (int) this.displayHeight, apiLevel);
          simulatorFrame.setVisible(true);
          simulatorFrame.toFront();
        });
  }

  private void captureScreen() {
    final Bitmap bitmap =
        InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
    SwingUtilities.invokeLater(() -> simulatorFrame.getCanvas().drawBitmap(bitmap));
  }

  private void postMotionEvent() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    MotionEvent androidEvent =
        MotionEvent.obtain(
            /* downTime= */ 0,
            /* eventTime= */ SystemClock.uptimeMillis(),
            /* action= */ MotionEvent.ACTION_MOVE,
            /* x= */ this.displayWidth,
            /* y= */ this.displayHeight,
            /* metaState= */ 0);

    new Handler(Looper.getMainLooper())
        .post(() -> uiAutomation.injectInputEvent(androidEvent, true));
  }

  private static class SimulatorFrameCallback implements Choreographer.FrameCallback {
    @Override
    public void doFrame(long frameTimeNanos) {
      Choreographer.getInstance().postFrameCallback(this);
    }
  }
}
