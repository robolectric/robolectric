package org.robolectric.shadows;

import android.os.Looper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.robolectric.util.FailureListener;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class BackgroundLooperUnitTest {
  @Rule public final Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

  @Test
  public void backgroundThreadsWithActiveLoopersShouldBeReleasedAfterTest() throws Exception {
    assertThat(FailureListener.runTests(TestWithBackgroundThreads.class)).isEmpty();
  }

  public static class TestWithBackgroundThreads {
    private static Thread thread;

    @Test
    public void first() throws Exception {
      if (thread != null) thread.join();
      thread = startBackgroundThread();
    }

    @Test
    public void second() throws Exception {
      if (thread != null) thread.join();
      thread = startBackgroundThread();
    }

    private Thread startBackgroundThread() {
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          Looper.prepare();
          Looper.loop();
        }
      });
      thread.start();
      return thread;
    }
  }
}
