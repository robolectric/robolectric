package org.robolectric.simulator;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.app.Activity;
import android.app.Application;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.view.Display;
import android.view.MotionEvent;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.robolectric.simulator.pluginapi.RemoteControl;
import org.robolectric.simulator.pluginapi.ScreenRecorder;
import org.robolectric.util.inject.Injector;

/** The main entry point for the Robolectric Simulator for use in existing Robolectric tests. */
@Beta
public final class Simulator {

  private SimulatorFrame simulatorFrame;
  private boolean headless = false;
  private float displayWidth;
  private float displayHeight;
  private ScreenRecorder screenRecorder = null;
  private final Semaphore writingFrame = new Semaphore(1);

  private final Class<? extends Activity> activityClassToLaunch;

  public Simulator() {
    this(null);
  }

  public Simulator(Class<? extends Activity> activityClassToLaunch) {
    this.activityClassToLaunch = activityClassToLaunch;
    this.headless = GraphicsEnvironment.isHeadless();
  }

  public void start() {
    Preconditions.checkState(ShadowView.useRealGraphics());
    Preconditions.checkState(ShadowLooper.looperMode() != Mode.LEGACY);
    System.setProperty("java.awt.headless", headless ? "true" : "false");
    ShadowView.setUseRealViewAnimations(true);
    ShadowChoreographer.setPaused(true);
    ShadowChoreographer.setFrameDelay(Duration.ofMillis(15));

    if (this.activityClassToLaunch != null) {
      System.err.println("Launching " + this.activityClassToLaunch.getName());
      if (Boolean.parseBoolean(
          System.getProperty("robolectric.useContextStartActivity", "false"))) {
        Context context = RuntimeEnvironment.getApplication().getApplicationContext();
        Intent intent = new Intent(context, this.activityClassToLaunch);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
      } else {
        Robolectric.setupActivity(this.activityClassToLaunch);
      }
    }
    // Inject an off-screen motion event to avoid a blank screen when the simulator first starts.
    postMotionEvent();

    startUi();
    startScreenRecorder();
    captureScreen();
    connectRemoteControl();
    loop();
  }

  private void loop() {
    ShadowPausedLooper shadowLooper = Shadow.extract(Looper.myLooper());
    shadowLooper.idle();
    Choreographer.getInstance().postFrameCallback(new SimulatorFrameCallback());
    AtomicBoolean running = new AtomicBoolean(true);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> running.set(false)));
    while (running.get()) {
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
    if (headless) {
      return;
    }
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
    if (!headless) {
      SwingUtilities.invokeLater(() -> simulatorFrame.getCanvas().drawBitmap(bitmap));
    }
    writingFrame.acquireUninterruptibly();
    if (screenRecorder != null) {
      screenRecorder.recordFrame(bitmap);
    }
    writingFrame.release();
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

  private void connectRemoteControl() {
    // The default RemoteControl is a no-op stub.
    Injector injector = new Injector.Builder(Looper.class.getClassLoader()).build();
    RemoteControl remoteControl = injector.getInstance(RemoteControl.class);
    remoteControl.connect(
        InstrumentationRegistry.getInstrumentation()
            .getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES),
        Looper.getMainLooper());
  }

  private Path getVideoPath() throws IOException {
    if (System.getProperty("robolectric.videoPath") != null) {
      Path videoPath = Path.of(System.getProperty("robolectric.videoPath"));
      Files.createDirectories(videoPath.getParent());
      return videoPath;
    } else {
      return Files.createTempFile(
          "robolectric", System.getProperty("robolectric.videoExtension", "webm"));
    }
  }

  void startScreenRecorder() {
    if (!Boolean.parseBoolean(System.getProperty("robolectric.recordVideo", "false"))) {
      return;
    }
    Path videoPath;
    try {
      videoPath = getVideoPath();
    } catch (IOException e) {
      Log.i("Simulator", "Failed to get a path for screen recording!", e);
      return;
    }
    // Use the class loader for an Android class.
    Injector injector = new Injector.Builder(Looper.class.getClassLoader()).build();
    screenRecorder = injector.getInstance(ScreenRecorder.class);
    screenRecorder.start(
        videoPath,
        (int) this.displayWidth,
        (int) this.displayHeight,
        new ScreenRecorder.FrameRate(24, 1));

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  // Make sure that we don't stop the screen recorder in the middle of a write!
                  writingFrame.acquireUninterruptibly();
                  screenRecorder.stop();
                  screenRecorder = null;
                  writingFrame.release();
                }));
  }

  private static class SimulatorFrameCallback implements Choreographer.FrameCallback {
    @Override
    public void doFrame(long frameTimeNanos) {
      Choreographer.getInstance().postFrameCallback(this);
    }
  }
}
