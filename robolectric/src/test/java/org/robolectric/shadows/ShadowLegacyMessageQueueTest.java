package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;
import static org.robolectric.util.ReflectionHelpers.callConstructor;
import static org.robolectric.util.ReflectionHelpers.callInstanceMethod;
import static org.robolectric.util.ReflectionHelpers.setField;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowMessage.MessageReflector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.Scheduler;

/** Unit tests for {@link ShadowLegacyMessageQueue}. */
@RunWith(AndroidJUnit4.class)
@LooperMode(Mode.LEGACY)
public class ShadowLegacyMessageQueueTest {
  private Looper looper;
  private MessageQueue queue;
  private ShadowLegacyMessageQueue shadowQueue;
  private Message testMessage;
  private TestHandler handler;
  private Scheduler scheduler;
  private String quitField;
  
  private static class TestHandler extends Handler {
    public List<Message> handled = new ArrayList<>();
    
    public TestHandler(Looper looper) {
      super(looper);
    }
    
    @Override
    public void handleMessage(Message msg) {
      handled.add(msg);
    }
  }
  
  private static Looper newLooper() {
    return newLooper(true);
  }
  
  private static Looper newLooper(boolean canQuit) {
    return callConstructor(Looper.class, ClassParameter.from(boolean.class, canQuit));
  }
  
  @Before
  public void setUp() throws Exception {
    // Queues and loopers are closely linked; can't easily test one without the other.
    looper = newLooper();
    handler = new TestHandler(looper);
    queue = looper.getQueue();
    shadowQueue = Shadow.extract(queue);
    scheduler = shadowQueue.getScheduler();
    scheduler.pause();
    testMessage = handler.obtainMessage();
    quitField = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? "mQuitting" : "mQuiting";
  }

  @Test
  public void test_setGetHead() {
    shadowQueue.setHead(testMessage);
    assertWithMessage("getHead()").that(shadowQueue.getHead()).isSameInstanceAs(testMessage);
  }

  private boolean enqueueMessage(Message msg, long when) {
    return callInstanceMethod(
        queue,
        "enqueueMessage",
        ClassParameter.from(Message.class, msg),
        ClassParameter.from(long.class, when));
  }

  private void removeMessages(Handler handler, int what, Object token) {
    callInstanceMethod(
        queue,
        "removeMessages",
        ClassParameter.from(Handler.class, handler),
        ClassParameter.from(int.class, what),
        ClassParameter.from(Object.class, token));
  }
  
  @Test
  public void enqueueMessage_setsHead() {
    enqueueMessage(testMessage, 100);
    assertWithMessage("head").that(shadowQueue.getHead()).isSameInstanceAs(testMessage);
  }

  @Test
  public void enqueueMessage_returnsTrue() {
    assertWithMessage("retval").that(enqueueMessage(testMessage, 100)).isTrue();
  }

  @Test
  public void enqueueMessage_setsWhen() {
    enqueueMessage(testMessage, 123);
    assertWithMessage("when").that(testMessage.getWhen()).isEqualTo(123);
  }
  
  @Test
  public void enqueueMessage_returnsFalse_whenQuitting() {
    setField(queue, quitField, true);
    assertWithMessage("enqueueMessage()").that(enqueueMessage(testMessage, 1)).isFalse();
  }

  @Test
  public void enqueueMessage_doesntSchedule_whenQuitting() {
    setField(queue, quitField, true);
    enqueueMessage(testMessage, 1);
    assertWithMessage("scheduler_size").that(scheduler.size()).isEqualTo(0);
  }
  
  @Test
  public void enqueuedMessage_isSentToHandler() {
    enqueueMessage(testMessage, 200);
    scheduler.advanceTo(199);
    assertWithMessage("handled:before").that(handler.handled).isEmpty();
    scheduler.advanceTo(200);
    assertWithMessage("handled:after").that(handler.handled).containsExactly(testMessage);
  }
  
  @Test
  public void removedMessage_isNotSentToHandler() {
    enqueueMessage(testMessage, 200);
    assertWithMessage("scheduler size:before").that(scheduler.size()).isEqualTo(1);
    removeMessages(handler, testMessage.what, null);
    scheduler.advanceToLastPostedRunnable();
    assertWithMessage("scheduler size:after").that(scheduler.size()).isEqualTo(0);
    assertWithMessage("handled").that(handler.handled).isEmpty();
  }

  @Test
  public void enqueueMessage_withZeroWhen_postsAtFront() {
    enqueueMessage(testMessage, 0);
    Message m2 = handler.obtainMessage(2);
    enqueueMessage(m2, 0);
    scheduler.advanceToLastPostedRunnable();
    assertWithMessage("handled").that(handler.handled).containsExactly(m2, testMessage);
  }
  
  @Test
  public void dispatchedMessage_isMarkedInUse_andRecycled() {
    Handler handler =
        new Handler(looper) {
          @Override
          public void handleMessage(Message msg) {
            boolean inUse = callInstanceMethod(msg, "isInUse");
            assertWithMessage(msg.what + ":inUse").that(inUse).isTrue();
            Message next = reflector(MessageReflector.class, msg).getNext();
            assertWithMessage(msg.what + ":next").that(next).isNull();
          }
        };
    Message msg = handler.obtainMessage(1);
    enqueueMessage(msg, 200);
    Message msg2 = handler.obtainMessage(2);
    enqueueMessage(msg2, 205);
    scheduler.advanceToNextPostedRunnable();

    // Check that it's been properly recycled.
    assertWithMessage("msg.what").that(msg.what).isEqualTo(0);

    scheduler.advanceToNextPostedRunnable();

    assertWithMessage("msg2.what").that(msg2.what).isEqualTo(0);
  }
  
  @Test 
  public void reset_shouldClearMessageQueue() {
    Message msg  = handler.obtainMessage(1234);
    Message msg2 = handler.obtainMessage(5678);
    handler.sendMessage(msg);
    handler.sendMessage(msg2);
    assertWithMessage("before-1234").that(handler.hasMessages(1234)).isTrue();
    assertWithMessage("before-5678").that(handler.hasMessages(5678)).isTrue();
    shadowQueue.reset();
    assertWithMessage("after-1234").that(handler.hasMessages(1234)).isFalse();
    assertWithMessage("after-5678").that(handler.hasMessages(5678)).isFalse();
  }

  @Test
  public void postAndRemoveSyncBarrierToken() {
    int token = postSyncBarrier(queue);
    removeSyncBarrier(queue, token);
  }

  @Test
  // TODO(b/74402484): enable once workaround is removed
  @Ignore
  public void removeInvalidSyncBarrierToken() {
    try {
      removeSyncBarrier(queue, 99);
      fail("Expected exception when sync barrier not present on MessageQueue");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void postAndRemoveSyncBarrierToken_messageBefore() {
    enqueueMessage(testMessage, SystemClock.uptimeMillis());
    int token = postSyncBarrier(queue);
    removeSyncBarrier(queue, token);

    assertThat(shadowQueue.getHead()).isEqualTo(testMessage);
  }

  @Test
  public void postAndRemoveSyncBarrierToken_messageBeforeConsumed() {
    enqueueMessage(testMessage, SystemClock.uptimeMillis());
    int token = postSyncBarrier(queue);
    scheduler.advanceToLastPostedRunnable();
    removeSyncBarrier(queue, token);
    assertThat(shadowQueue.getHead()).isNull();
    assertWithMessage("handled:after").that(handler.handled).containsExactly(testMessage);
  }

  @Test
  public void postAndRemoveSyncBarrierToken_messageAfter() {
    enqueueMessage(testMessage, SystemClock.uptimeMillis() + 100);
    int token = postSyncBarrier(queue);
    removeSyncBarrier(queue, token);

    assertThat(shadowQueue.getHead()).isEqualTo(testMessage);
    scheduler.advanceToLastPostedRunnable();
    assertThat(shadowQueue.getHead()).isNull();
    assertWithMessage("handled:after").that(handler.handled).containsExactly(testMessage);
  }

  @Test
  public void postAndRemoveSyncBarrierToken_syncBefore() {
    int token = postSyncBarrier(queue);
    enqueueMessage(testMessage, SystemClock.uptimeMillis());
    scheduler.advanceToLastPostedRunnable();
    removeSyncBarrier(queue, token);
    assertThat(shadowQueue.getHead()).isNull();
    assertWithMessage("handled:after").that(handler.handled).containsExactly(testMessage);
  }

  private static void removeSyncBarrier(MessageQueue queue, int token) {
    ReflectionHelpers.callInstanceMethod(
        MessageQueue.class, queue, "removeSyncBarrier", ClassParameter.from(int.class, token));
  }

  private static int postSyncBarrier(MessageQueue queue) {
    if (RuntimeEnvironment.getApiLevel() >= M) {
      return queue.postSyncBarrier();
    } else {
      return ReflectionHelpers.callInstanceMethod(
          MessageQueue.class,
          queue,
          "enqueueSyncBarrier",
          ClassParameter.from(long.class, SystemClock.uptimeMillis()));
    }
  }
}
