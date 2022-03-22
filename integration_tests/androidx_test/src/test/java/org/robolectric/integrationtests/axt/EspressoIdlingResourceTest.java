package org.robolectric.integrationtests.axt;

import static androidx.test.espresso.Espresso.onIdle;
import static com.google.common.truth.Truth.assertThat;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test Espresso IdlingResource support. */
@RunWith(AndroidJUnit4.class)
public final class EspressoIdlingResourceTest {
  private final IdlingRegistry idlingRegistry = IdlingRegistry.getInstance();

  private ExecutorService executor;

  @Before
  public void setup() {
    executor = Executors.newSingleThreadExecutor();
  }

  @After
  public void teardown() {
    for (IdlingResource resource : idlingRegistry.getResources()) {
      idlingRegistry.unregister(resource);
    }
    for (Looper looper : idlingRegistry.getLoopers()) {
      idlingRegistry.unregisterLooperAsIdlingResource(looper);
    }
    executor.shutdown();
  }

  @Test
  public void onIdle_idlingResourceIsIdle_doesntBlock() {
    AtomicBoolean didCheckIdle = new AtomicBoolean();
    idlingRegistry.register(
        new NamedIdleResource("Test", /* isIdle= */ true) {
          @Override
          public boolean isIdleNow() {
            didCheckIdle.set(true);
            return super.isIdleNow();
          }
        });

    onIdle();

    assertThat(didCheckIdle.get()).isTrue();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  public void onIdle_postToMainThread() {
    idlingRegistry.register(
        new NamedIdleResource("Test", /* isIdle= */ false) {
          boolean submitted;

          @Override
          public boolean isIdleNow() {
            if (!submitted) {
              submitted = true;
              executor.submit(this::postToMainLooper);
            }
            return super.isIdleNow();
          }

          void postToMainLooper() {
            new Handler(Looper.getMainLooper()).post(() -> setIdle(true));
          }
        });

    onIdle();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  public void onIdle_cooperativeResources() {
    NamedIdleResource a = new NamedIdleResource("A", /* isIdle= */ true);
    NamedIdleResource b = new NamedIdleResource("B", /* isIdle= */ false);
    NamedIdleResource c = new NamedIdleResource("C", /* isIdle= */ false);
    idlingRegistry.register(a, b, c);
    executor.submit(
        () -> {
          a.setIdle(false);
          b.setIdle(true);
          c.setIdle(false);
          executor.submit(
              () -> {
                a.setIdle(true);
                b.setIdle(false);
                c.setIdle(false);
                executor.submit(
                    () -> {
                      a.setIdle(true);
                      b.setIdle(true);
                      c.setIdle(true);
                    });
              });
        });

    onIdle();

    assertThat(a.isIdleNow()).isTrue();
    assertThat(b.isIdleNow()).isTrue();
    assertThat(c.isIdleNow()).isTrue();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  public void onIdle_looperIsIdle() throws Exception {
    HandlerThread handlerThread = new HandlerThread("Test");
    try {
      handlerThread.start();
      Handler handler = new Handler(handlerThread.getLooper());
      CountDownLatch handlerStarted = new CountDownLatch(1);
      CountDownLatch releaseHandler = new CountDownLatch(1);
      handler.post(
          () -> {
            handlerStarted.countDown();
            try {
              releaseHandler.await();
            } catch (InterruptedException e) {
              // ignore
            }
          });
      handlerStarted.await();
      idlingRegistry.registerLooperAsIdlingResource(handlerThread.getLooper());

      executor.submit(releaseHandler::countDown);
      onIdle();

      // onIdle should have blocked on the looper waiting on the release latch
      assertThat(releaseHandler.getCount()).isEqualTo(0);
    } finally {
      handlerThread.quit();
    }
  }

  private static class NamedIdleResource implements IdlingResource {
    final String name;
    final AtomicBoolean isIdle;
    ResourceCallback callback;

    NamedIdleResource(String name, boolean isIdle) {
      this.name = name;
      this.isIdle = new AtomicBoolean(isIdle);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean isIdleNow() {
      return isIdle.get();
    }

    void setIdle(boolean isIdle) {
      this.isIdle.set(isIdle);
      if (isIdle && callback != null) {
        callback.onTransitionToIdle();
      }
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
      this.callback = callback;
    }
  }
}
