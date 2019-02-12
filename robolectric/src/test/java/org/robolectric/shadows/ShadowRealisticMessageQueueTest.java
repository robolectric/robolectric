package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowRealisticMessageQueue.ReflectorMessageQueue;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowRealisticMessageQueueTest {
  private MessageQueue queue;
  private ShadowRealisticMessageQueue shadowQueue;

  @Before
  public void setUp() throws Exception {
    queue = ReflectionHelpers.callConstructor(MessageQueue.class, from(boolean.class, true));
    shadowQueue = Shadow.extract(queue);
  }

  @Test
  public void isIdle_initial() {
    assertThat(shadowQueue.isIdle()).isTrue();
  }

  @Test
  public void isIdle_withMsg() {
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    reflector(ReflectorMessageQueue.class, queue).enqueueMessage(msg, 0);
    assertThat(shadowQueue.isIdle()).isFalse();
  }

  @Test
  public void next_withMsg() {
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    reflector(ReflectorMessageQueue.class, queue).enqueueMessage(msg, 0);
    Message actual = shadowQueue.getNext();
    assertThat(actual).isNotNull();
  }

  @Test
  public void next_blocks() throws InterruptedException {
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    NextThread t = NextThread.startSync(queue);
    Thread.sleep(10);
    // assume blocked
    assertThat(t.isAlive()).isTrue();
    reflector(ReflectorMessageQueue.class, queue).enqueueMessage(msg, 0);
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
    assertThat(reflector(ReflectorMessageQueue.class, mainQueue).getMessages()).isNull();
    Message msg = Message.obtain();
    msg.setTarget(new Handler());
    reflector(ReflectorMessageQueue.class, mainQueue).enqueueMessage(msg, 0);
  }

  private static class NextThread extends Thread {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final MessageQueue messageQueue;

    private NextThread(MessageQueue messageQueue) {
      this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
      latch.countDown();
      reflector(ReflectorMessageQueue.class, messageQueue).next();
    }

    public static NextThread startSync(MessageQueue m) throws InterruptedException {
      NextThread t = new NextThread(m);
      t.start();
      t.latch.await();
      return t;
    }
  }
}
