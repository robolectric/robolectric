package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowRealisticMessageQueueTest {
  private MessageQueue queue;
  private ShadowRealisticMessageQueue shadowQueue;

  @Before
  public void setUp() throws Exception {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.PAUSED);

    queue = ReflectionHelpers.callConstructor(MessageQueue.class, from(boolean.class, true));
    shadowQueue = Shadow.extract(queue);
  }

  @After
  public void tearDown() {
    if (shadowQueue != null) {
      shadowQueue.quit();
    }
  }

  @Test
  public void isIdle_initial() {
    assertThat(shadowQueue.isIdle()).isTrue();
  }

  @Test
  public void isIdle_withMsg() {
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    shadowQueue.doEnqueueMessage(msg, 0);
    assertThat(shadowQueue.isIdle()).isFalse();
  }

  @Test
  public void next_withMsg() {
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    shadowQueue.doEnqueueMessage(msg, 0);
    Message actual = shadowQueue.getNext();
    assertThat(actual).isNotNull();
  }

  @Test
  public void next_blocks() throws InterruptedException {
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    NextThread t = NextThread.startSync(shadowQueue);
    shadowQueue.doEnqueueMessage(msg, 0);
    t.join();
  }

  @Test
  public void next_releasedOnClockIncrement() throws InterruptedException {
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    shadowQueue.doEnqueueMessage(msg, TimeUnit.MINUTES.toMillis(10));
    NextThread t = NextThread.startSync(shadowQueue);
    ShadowSystemClock.advanceBy(Duration.ofMinutes(10));
    t.join();
  }

  @Test
  public void reset_clearsMsg1() {
    assertMainQueueEmptyAndAdd();
  }

  @Test
  public void reset_clearsMsg2() {
    assertMainQueueEmptyAndAdd();
  }

  private void assertMainQueueEmptyAndAdd() {
    MessageQueue mainQueue = Looper.getMainLooper().getQueue();
    ShadowRealisticMessageQueue shadowRealisticMessageQueue = Shadow.extract(mainQueue);
    assertThat(shadowRealisticMessageQueue.getMessages()).isNull();
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    shadowRealisticMessageQueue.doEnqueueMessage(msg, 0);
  }

  private static class NextThread extends Thread {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final ShadowRealisticMessageQueue shadowQueue;

    private NextThread(ShadowRealisticMessageQueue shadowQueue) {
      this.shadowQueue = shadowQueue;
    }

    @Override
    public void run() {
      latch.countDown();
      shadowQueue.getNext();
    }

    public static NextThread startSync(ShadowRealisticMessageQueue shadowQueue)
        throws InterruptedException {
      NextThread t = new NextThread(shadowQueue);
      t.start();
      t.latch.await();
      while (!shadowQueue.isPolling()) {
        Thread.yield();
      }
      assertThat(t.isAlive()).isTrue();
      return t;
    }
  }
}
