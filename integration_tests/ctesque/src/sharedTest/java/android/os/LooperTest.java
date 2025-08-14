package android.os;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;

/** Tests to verify INSTRUMENTATION_TEST mode Looper behaves like a looping Looper. */
@RunWith(AndroidJUnit4.class)
public class LooperTest {

  @Test
  @LooperMode(Mode.INSTRUMENTATION_TEST)
  public void postAndWait() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    new Handler(Looper.getMainLooper()).post(latch::countDown);
    assertThat(latch.await(1, SECONDS)).isTrue();
  }

  @Test
  @LooperMode(Mode.INSTRUMENTATION_TEST)
  public void isIdle() throws InterruptedException {
    HandlerThread ht = new HandlerThread("isIdle_timing");
    ht.start();

    Looper looper = ht.getLooper();
    Handler handler = new Handler(looper);

    CountDownLatch blockObtained = new CountDownLatch(1);
    CountDownLatch block = new CountDownLatch(1);
    // post a message that will block looper
    handler.post(
        () -> {
          try {
            blockObtained.countDown();
            block.await();
          } catch (InterruptedException e) {
            // ignore
          }
        });

    try {
      blockObtained.await();
      assertWithMessage("Looper is not idle after blocking")
          .that(looper.getQueue().isIdle())
          .isTrue();
      handler.post(() -> {});
      assertWithMessage("Looper is idle after posting a message!")
          .that(looper.getQueue().isIdle())
          .isFalse();
    } finally {
      block.countDown();
      ht.quit();
      ht.join();
    }
  }
}
