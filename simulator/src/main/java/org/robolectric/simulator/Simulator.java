package org.robolectric.simulator;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.app.Activity;
import android.app.Application;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
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
import java.awt.GraphicsEnvironment;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.pluginapi.perf.Metadata;
import org.robolectric.pluginapi.perf.PerfStatsReporter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowChoreographer;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPausedLooper;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.shadows.ShadowView;
import org.robolectric.simulator.pluginapi.RemoteControl;
import org.robolectric.simulator.pluginapi.ScreenRecorder;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.PerfStatsPublisher;
import org.robolectric.util.inject.Injector;

/** The main entry point for the Robolectric Simulator for use in existing Robolectric tests. */
@Beta
public final class Simulator {

  private boolean headless = false;
  private RemoteControl remoteControl = null;

  private final Class<? extends Activity> activityClassToLaunch;
  private Injector injector;
  private PerfStatsCollector perfStatsCollector;
  private PerfStatsPublisher perfStatsPublisher;
  private ScreenUpdateHandler screenUpdateHandler;

  public Simulator() {
    this(null);
  }

  public Simulator(Class<? extends Activity> activityClassToLaunch) {
    this.activityClassToLaunch = activityClassToLaunch;
    this.headless = GraphicsEnvironment.isHeadless();
    this.injector = new Injector.Builder(Looper.class.getClassLoader()).build();
  }

  public void start() {
    Preconditions.checkState(ShadowView.useRealGraphics());
    Preconditions.checkState(ShadowLooper.looperMode() != Mode.LEGACY);
    System.setProperty("java.awt.headless", headless ? "true" : "false");
    ShadowView.setUseRealViewAnimations(true);
    ShadowView.setUseRealDrawTraversals(true);
    ShadowChoreographer.setPaused(true);
    ShadowChoreographer.setFrameDelay(Duration.ofMillis(15));
    ShadowLog.setCaptureLogsEnabled(false);

    setupPerfStats();

    Application application = RuntimeEnvironment.getApplication();
    DisplayManager displayManager = application.getSystemService(DisplayManager.class);
    Display display = displayManager.getDisplay(Display.DEFAULT_DISPLAY);
    final int apiLevel = getApiLevel();
    final int width = (int) display.getWidth();
    final int height = (int) display.getHeight();

    startUi(apiLevel, width, height);

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
    postMotionEvent(width, height);

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
      remoteControl.onCycle(SystemClock.uptimeNanos());
    }
  }

  private void startUi(int apiLevel, int width, int height) {
    FrameListener swingFrameListener = new SwingFrameListener(headless, apiLevel, width, height);

    ScreenRecorderFrameListener screenRecorderFrameListener = null;
    if (Boolean.parseBoolean(System.getProperty("robolectric.recordVideo", "false"))) {
      screenRecorderFrameListener =
          new ScreenRecorderFrameListener(
              injector.getInstance(ScreenRecorder.class), width, height);
    }
    screenUpdateHandler =
        ScreenUpdateHandler.create(screenRecorderFrameListener, swingFrameListener);
    if (screenUpdateHandler != null) {
      screenUpdateHandler.requestStartUi();
    }
  }

  private void postMotionEvent(int width, int height) {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    MotionEvent androidEvent =
        MotionEvent.obtain(
            /* downTime= */ 0,
            /* eventTime= */ SystemClock.uptimeMillis(),
            /* action= */ MotionEvent.ACTION_MOVE,
            /* x= */ width,
            /* y= */ height,
            /* metaState= */ 0);

    new Handler(Looper.getMainLooper())
        .post(() -> uiAutomation.injectInputEvent(androidEvent, true));
  }

  private void connectRemoteControl() {
    // The default RemoteControl is a no-op stub.
    remoteControl = injector.getInstance(RemoteControl.class);
    remoteControl.connect(
        InstrumentationRegistry.getInstrumentation()
            .getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES),
        Looper.getMainLooper());
  }


  private void setupPerfStats() {
    perfStatsCollector = PerfStatsCollector.getInstance();
    perfStatsCollector.putMetadata(new Metadata(RuntimeEnvironment.getApiLevel()));
    List<PerfStatsReporter> perfStatsReporters =
        Arrays.asList(injector.getInstance(PerfStatsReporter[].class));
    perfStatsPublisher = PerfStatsPublisher.getInstance();
    perfStatsPublisher.addReporters(perfStatsReporters);
    perfStatsPublisher.doFinalReportOnShutdown(
        () -> {
          // Reuse the shutdown hook to logMemoryAndCpu, otherwise output of perfstats can collide
          // with
          // logMemoryAndCpu if they both log to System.err
          logMemoryAndCpu();
          System.err.println();
          // there is no test runner reporting the data, so report first here before the finalReport
          // runs
          perfStatsPublisher.report(perfStatsCollector);
        });

    if (perfStatsReporters.isEmpty()) {
      // there won't be a perf stats shutdown hook - add our own here to log memory and CPU
      Runtime.getRuntime().addShutdownHook(new Thread(Simulator::logMemoryAndCpu));
    }
  }

  private static void logMemoryAndCpu() {
    System.err.println();
    System.err.printf(
        "Java heap info: totalMemory %dm, maxMemory %dm%n",
        (Runtime.getRuntime().totalMemory() / 1024 / 1024),
        (Runtime.getRuntime().maxMemory() / 1024 / 1024));
    ProcessHandle.current()
        .info()
        .totalCpuDuration()
        .ifPresent(
            duration -> {
              System.err.printf(
                  "Java process total CPU time: %.1fs%n", duration.toMillis() / 1000.0);
            });
  }

  private static class SimulatorFrameCallback implements Choreographer.FrameCallback {
    @Override
    public void doFrame(long frameTimeNanos) {
      Choreographer.getInstance().postFrameCallback(this);
    }
  }
}
