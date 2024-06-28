package android.os;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

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
    assertTrue(latch.await(1, SECONDS));
  }
}
