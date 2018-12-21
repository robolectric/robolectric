package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.ReflectionHelpers.callConstructor;
import static org.robolectric.util.ReflectionHelpers.callInstanceMethod;
import static org.robolectric.util.ReflectionHelpers.getField;
import static org.robolectric.util.ReflectionHelpers.setField;

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
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
public class ShadowMessageQueueTest {
  private Looper looper;
  private MessageQueue queue;
  private ShadowMessageQueue shadowQueue;
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
    return callConstructor(Looper.class, from(boolean.class, canQuit));
  }
  
  @Before
  public void setUp() throws Exception {
    // Queues and loopers are closely linked; can't easily test one without the other.
    looper = newLooper();
    handler = new TestHandler(looper);
    queue = looper.getQueue(); 
    shadowQueue = shadowOf(queue);
    scheduler = shadowQueue.getScheduler();
    scheduler.pause();
    testMessage = handler.obtainMessage();
    quitField = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? "mQuitting" : "mQuiting";
  }

  @Test
  public void test_setGetHead() {
    shadowQueue.setHead(testMessage);
    assertThat(shadowQueue.getHead()).named("getHead()").isSameAs(testMessage);
  }

  private boolean enqueueMessage(Message msg, long when) {
    return callInstanceMethod(queue, "enqueueMessage",
        from(Message.class, msg),
        from(long.class, when)
        );    
  }

  private void removeMessages(Handler handler, int what, Object token) {
    callInstanceMethod(queue, "removeMessages",
        from(Handler.class, handler),
        from(int.class, what),
        from(Object.class, token)
    );
  }
  
  @Test
  public void enqueueMessage_setsHead() {
    enqueueMessage(testMessage, 100);
    assertThat(shadowQueue.getHead()).named("head").isSameAs(testMessage);
  }

  @Test
  public void enqueueMessage_returnsTrue() {
    assertThat(enqueueMessage(testMessage, 100)).named("retval").isTrue();
  }

  @Test
  public void enqueueMessage_setsWhen() {
    enqueueMessage(testMessage, 123);
    assertThat(testMessage.getWhen()).named("when").isEqualTo(123);
  }
  
  @Test
  public void enqueueMessage_returnsFalse_whenQuitting() {
    setField(queue, quitField, true);
    assertThat(enqueueMessage(testMessage, 1)).named("enqueueMessage()").isFalse();
  }

  @Test
  public void enqueueMessage_doesntSchedule_whenQuitting() {
    setField(queue, quitField, true);
    enqueueMessage(testMessage, 1);
    assertThat(scheduler.size()).named("scheduler_size").isEqualTo(0);
  }
  
  @Test
  public void enqueuedMessage_isSentToHandler() {
    enqueueMessage(testMessage, 200);
    scheduler.advanceTo(199);
    assertThat(handler.handled).named("handled:before").isEmpty();
    scheduler.advanceTo(200);
    assertThat(handler.handled).named("handled:after").containsExactly(testMessage);
  }
  
  @Test
  public void removedMessage_isNotSentToHandler() {
    enqueueMessage(testMessage, 200);
    assertThat(scheduler.size()).named("scheduler size:before").isEqualTo(1);
    removeMessages(handler, testMessage.what, null);
    scheduler.advanceToLastPostedRunnable();
    assertThat(scheduler.size()).named("scheduler size:after").isEqualTo(0);
    assertThat(handler.handled).named("handled").isEmpty();
  }

  @Test
  public void enqueueMessage_withZeroWhen_postsAtFront() {
    enqueueMessage(testMessage, 0);
    Message m2 = handler.obtainMessage(2);
    enqueueMessage(m2, 0);
    scheduler.advanceToLastPostedRunnable();
    assertThat(handler.handled).named("handled").containsExactly(m2, testMessage);
  }
  
  @Test
  public void dispatchedMessage_isMarkedInUse_andRecycled() {
    Handler handler = new Handler(looper) {
      @Override
      public void handleMessage(Message msg) {
        boolean inUse = callInstanceMethod(msg, "isInUse");
        assertThat(inUse).named(msg.what + ":inUse").isTrue();
        Message next = getField(msg, "next");
        assertThat(next).named(msg.what + ":next").isNull();
      }
    };
    Message msg = handler.obtainMessage(1);
    enqueueMessage(msg, 200);
    Message msg2 = handler.obtainMessage(2);
    enqueueMessage(msg2, 205);
    scheduler.advanceToNextPostedRunnable();
    
    // Check that it's been properly recycled.
    assertThat(msg.what).named("msg.what").isEqualTo(0);
    
    scheduler.advanceToNextPostedRunnable();

    assertThat(msg2.what).named("msg2.what").isEqualTo(0);
  }
  
  @Test 
  public void reset_shouldClearMessageQueue() {
    Message msg  = handler.obtainMessage(1234);
    Message msg2 = handler.obtainMessage(5678);
    handler.sendMessage(msg);
    handler.sendMessage(msg2);
    assertThat(handler.hasMessages(1234)).named("before-1234").isTrue();
    assertThat(handler.hasMessages(5678)).named("before-5678").isTrue();
    shadowQueue.reset();
    assertThat(handler.hasMessages(1234)).named("after-1234").isFalse();
    assertThat(handler.hasMessages(5678)).named("after-5678").isFalse();
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
    assertThat(handler.handled).named("handled:after").containsExactly(testMessage);
  }

  @Test
  public void postAndRemoveSyncBarrierToken_messageAfter() {
    enqueueMessage(testMessage, SystemClock.uptimeMillis() + 100);
    int token = postSyncBarrier(queue);
    removeSyncBarrier(queue, token);

    assertThat(shadowQueue.getHead()).isEqualTo(testMessage);
    scheduler.advanceToLastPostedRunnable();
    assertThat(shadowQueue.getHead()).isNull();
    assertThat(handler.handled).named("handled:after").containsExactly(testMessage);
  }

  @Test
  public void postAndRemoveSyncBarrierToken_syncBefore() {
    int token = postSyncBarrier(queue);
    enqueueMessage(testMessage, SystemClock.uptimeMillis());
    scheduler.advanceToLastPostedRunnable();
    removeSyncBarrier(queue, token);
    assertThat(shadowQueue.getHead()).isNull();
    assertThat(handler.handled).named("handled:after").containsExactly(testMessage);
  }

  private static void removeSyncBarrier(MessageQueue queue, int token) {
    ReflectionHelpers.callInstanceMethod(
        MessageQueue.class, queue, "removeSyncBarrier", from(int.class, token));
  }

  private static int postSyncBarrier(MessageQueue queue) {
    if (RuntimeEnvironment.getApiLevel() >= M) {
      return queue.postSyncBarrier();
    } else {
      return ReflectionHelpers.callInstanceMethod(
          MessageQueue.class,
          queue,
          "enqueueSyncBarrier",
          from(long.class, SystemClock.uptimeMillis()));
    }
  }
}
