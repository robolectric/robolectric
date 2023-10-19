package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.shadow.api.Shadow;

@LooperMode(Mode.INSTRUMENTATION_TEST)
@RunWith(RobolectricTestRunner.class)
public class ShadowInstrumentationTestLooperTest {

  @Test
  @Config(minSdk = 18)
  public void testThreadIsNotMainThread() {
    assertFalse(Looper.getMainLooper().isCurrentThread());
  }

  @Test
  public void idle() throws InterruptedException {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());

    AtomicBoolean hasRun = new AtomicBoolean(false);
    mainHandler.post(() -> hasRun.set(true));
    shadowMainLooper.idle();
    assertTrue(hasRun.get());
  }

  @Test
  public void pauseMainLooper() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());

    shadowMainLooper.pause();
    AtomicBoolean hasRun = new AtomicBoolean(false);
    mainHandler.post(() -> hasRun.set(true));
    assertFalse(hasRun.get());
    shadowMainLooper.idle();
    assertTrue(hasRun.get());
  }

  @Test
  public void unpauseMainLooper() throws InterruptedException {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());

    shadowMainLooper.pause();
    CountDownLatch hasRun = new CountDownLatch(1);
    mainHandler.post(hasRun::countDown);
    assertEquals(1, hasRun.getCount());
    shadowMainLooper.unPause();
    assertTrue(hasRun.await(2, SECONDS));
  }

  @Test
  public void idleFor() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());

    AtomicBoolean hasRun = new AtomicBoolean(false);
    mainHandler.postDelayed(() -> hasRun.set(true), 99);
    assertFalse(hasRun.get());
    shadowMainLooper.idleFor(Duration.ofMillis(100));
    assertTrue(hasRun.get());
  }

  @Test
  public void exceptionOnMainThreadPropagated() {
    ShadowLooper shadowMainLooper = shadowOf(Looper.getMainLooper());
    Handler mainHandler = new Handler(Looper.getMainLooper());

    mainHandler.post(
        () -> {
          throw new RuntimeException("Exception should be propagated!");
        });
    assertThrows(RuntimeException.class, () -> shadowMainLooper.idle());

    // Restore main looper and main thread to avoid error at tear down
    ShadowPausedLooper.resetLoopers();
  }

  @Test
  public void backgroundLooperCrash() throws InterruptedException {
    HandlerThread ht = new HandlerThread("backgroundLooperCrash");
    ht.start();
    Handler handler = new Handler(ht.getLooper());
    handler.post(
        () -> {
          throw new RuntimeException();
        });
    ht.join();

    assertThrows(IllegalStateException.class, () -> handler.post(() -> {}));
  }

  @Test
  public void mainThreadDies_resetRestartsLooper() {
    ShadowLooper shadowLooper = shadowOf(Looper.getMainLooper());
    Handler handler = new Handler(Looper.getMainLooper());
    AtomicBoolean didRun = new AtomicBoolean();

    handler.post(
        () -> {
          throw new RuntimeException();
        });
    RuntimeException exception = null;
    try {
      shadowLooper.idle();
    } catch (RuntimeException e) {
      exception = e;
    }
    Preconditions.checkNotNull(exception);
    ShadowPausedLooper.resetLoopers();
    handler.post(
        () -> {
          didRun.set(true);
        });
    shadowLooper.idle();

    assertThat(didRun.get()).isTrue();
  }

  @Test
  public void postSync_runsOnlyToTheRunnable() {
    ShadowPausedLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    shadowLooper.setPaused(true);
    AtomicBoolean firstTaskRan = new AtomicBoolean();
    AtomicBoolean secondTaskRan = new AtomicBoolean();
    AtomicBoolean thirdTaskRan = new AtomicBoolean();

    new Handler(Looper.getMainLooper()).post(() -> firstTaskRan.set(true));
    shadowLooper.postSync(
        () -> {
          new Handler(Looper.getMainLooper()).post(() -> thirdTaskRan.set(true));
          secondTaskRan.set(true);
        });

    assertThat(firstTaskRan.get()).isTrue();
    assertThat(secondTaskRan.get()).isTrue();
    assertThat(thirdTaskRan.get()).isFalse();
  }

  @Test
  public void postSync_exceptionIsThrown() {
    ShadowPausedLooper shadowLooper = Shadow.extract(Looper.getMainLooper());

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              throw new RuntimeException();
            });

    assertThrows(RuntimeException.class, () -> shadowLooper.postSync(() -> {}));

    // Restore main looper and main thread to avoid error at tear down
    ShadowPausedLooper.resetLoopers();
  }
}
