package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertThat;

import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLog.LogItem;

@RunWith(AndroidJUnit4.class)
public class SimulatorUncaughtExceptionHandlerTest {

  private Thread.UncaughtExceptionHandler originalHandler;
  private final AtomicInteger exitCount = new AtomicInteger();

  @Before
  public void setUp() {
    originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(null);
  }

  @After
  public void tearDown() {
    Thread.setDefaultUncaughtExceptionHandler(originalHandler);
  }

  @Test
  public void installIfNone_noHandler_installsHandler() {
    SimulatorUncaughtExceptionHandler.installIfNone(exitCount::incrementAndGet);

    assertThat(Thread.getDefaultUncaughtExceptionHandler())
        .isInstanceOf(SimulatorUncaughtExceptionHandler.class);
  }

  @Test
  public void installIfNone_existingHandler_keepsIt() {
    Thread.UncaughtExceptionHandler existing = (thread, throwable) -> {};
    Thread.setDefaultUncaughtExceptionHandler(existing);

    SimulatorUncaughtExceptionHandler.installIfNone(exitCount::incrementAndGet);

    assertThat(Thread.getDefaultUncaughtExceptionHandler()).isSameInstanceAs(existing);
  }

  @Test
  public void uncaughtException_logsFatalExceptionAndExits() throws Exception {
    SimulatorUncaughtExceptionHandler.installIfNone(exitCount::incrementAndGet);
    RuntimeException crash = new RuntimeException("boom");
    Thread thread =
        new Thread(
            () -> {
              throw crash;
            },
            "worker");

    thread.start();
    thread.join();

    List<LogItem> logs = ShadowLog.getLogsForTag("AndroidRuntime");
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).type).isEqualTo(Log.ERROR);
    assertThat(logs.get(0).msg).isEqualTo("FATAL EXCEPTION: worker");
    assertThat(logs.get(0).throwable).isSameInstanceAs(crash);
    assertThat(exitCount.get()).isEqualTo(1);
  }
}
