package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static com.google.common.truth.Truth.assertThat;

import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/** Integration test for Choreographer in RUNNING mode. */
@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.RUNNING)
@Config(minSdk = CINNAMON_BUN)
public class ShadowRunningChoreographerTest {

  @Test
  public void postFrameCallback_runsCallback() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              Choreographer.getInstance().postFrameCallback(frameTimeNanos -> latch.countDown());
            });

    boolean completed = latch.await(1, TimeUnit.SECONDS);
    assertThat(completed).isTrue();
  }
}
