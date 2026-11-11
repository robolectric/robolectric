package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLog.LogItem;

@RunWith(AndroidJUnit4.class)
public class AndroidUncaughtExceptionHandlerTest {

  private final Thread worker = new Thread("worker");
  private final RuntimeException crash = new RuntimeException("boom");

  @Test
  public void uncaughtException_logsFatalException() {
    new AndroidUncaughtExceptionHandler((thread, throwable) -> {}).uncaughtException(worker, crash);

    List<LogItem> logs = ShadowLog.getLogsForTag("AndroidRuntime");
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).type).isEqualTo(Log.ERROR);
    assertThat(logs.get(0).msg).isEqualTo("FATAL EXCEPTION: worker");
    assertThat(logs.get(0).throwable).isSameInstanceAs(crash);
  }

  @Test
  public void uncaughtException_handsTheCrashToThePreviousHandler() {
    List<Throwable> handled = new ArrayList<>();

    new AndroidUncaughtExceptionHandler((thread, throwable) -> handled.add(throwable))
        .uncaughtException(worker, crash);

    assertThat(handled).containsExactly(crash);
  }

  @Test
  public void uncaughtException_noPreviousHandler_printsTheCrash() {
    PrintStream originalErr = System.err;
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    System.setErr(new PrintStream(err, true, UTF_8));
    try {
      new AndroidUncaughtExceptionHandler(null).uncaughtException(worker, crash);
    } finally {
      System.setErr(originalErr);
    }

    assertThat(err.toString(UTF_8))
        .startsWith("Exception in thread \"worker\" java.lang.RuntimeException: boom");
  }
}
