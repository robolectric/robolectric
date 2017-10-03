package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.Scheduler;

@RunWith(RobolectricTestRunner.class)
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
    assertThat(shadowQueue.getHead()).as("getHead()").isSameAs(testMessage);
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
    assertThat(shadowQueue.getHead()).as("head").isSameAs(testMessage);
  }

  @Test
  public void enqueueMessage_returnsTrue() {
    assertThat(enqueueMessage(testMessage, 100)).as("retval").isTrue();
  }

  @Test
  public void enqueueMessage_setsWhen() {
    enqueueMessage(testMessage, 123);
    assertThat(testMessage.getWhen()).as("when").isEqualTo(123);
  }
  
  @Test
  public void enqueueMessage_returnsFalse_whenQuitting() {
    setField(queue, quitField, true);
    assertThat(enqueueMessage(testMessage, 1)).as("enqueueMessage()").isFalse();
  }

  @Test
  public void enqueueMessage_doesntSchedule_whenQuitting() {
    setField(queue, quitField, true);
    enqueueMessage(testMessage, 1);
    assertThat(scheduler.size()).as("scheduler_size").isEqualTo(0);
  }
  
  @Test
  public void enqueuedMessage_isSentToHandler() {
    enqueueMessage(testMessage, 200);
    scheduler.advanceTo(199);
    assertThat(handler.handled).as("handled:before").isEmpty();
    scheduler.advanceTo(200);
    assertThat(handler.handled).as("handled:after").containsExactly(testMessage);
  }
  
  @Test
  public void removedMessage_isNotSentToHandler() {
    enqueueMessage(testMessage, 200);
    assertThat(scheduler.size()).as("scheduler size:before").isEqualTo(1);
    removeMessages(handler, testMessage.what, null);
    scheduler.advanceToLastPostedRunnable();
    assertThat(scheduler.size()).as("scheduler size:after").isEqualTo(0);
    assertThat(handler.handled).as("handled").isEmpty();
  }

  @Test
  public void enqueueMessage_withZeroWhen_postsAtFront() {
    enqueueMessage(testMessage, 0);
    Message m2 = handler.obtainMessage(2);
    enqueueMessage(m2, 0);
    scheduler.advanceToLastPostedRunnable();
    assertThat(handler.handled).as("handled").containsExactly(m2, testMessage);
  }
  
  @Test
  public void dispatchedMessage_isMarkedInUse_andRecycled() {
    Handler handler = new Handler(looper) {
      @Override
      public void handleMessage(Message msg) {
        boolean inUse = callInstanceMethod(msg, "isInUse");
        assertThat(inUse).as(msg.what + ":inUse").isTrue();
        Message next = getField(msg, "next");
        assertThat(next).as(msg.what + ":next").isNull();
      }
    };
    Message msg = handler.obtainMessage(1);
    enqueueMessage(msg, 200);
    Message msg2 = handler.obtainMessage(2);
    enqueueMessage(msg2, 205);
    scheduler.advanceToNextPostedRunnable();
    
    // Check that it's been properly recycled.
    assertThat(msg.what).as("msg.what").isZero();
    
    scheduler.advanceToNextPostedRunnable();

    assertThat(msg2.what).as("msg2.what").isZero();
  }
  
  @Test 
  public void reset_shouldClearMessageQueue() {
    Message msg  = handler.obtainMessage(1234);
    Message msg2 = handler.obtainMessage(5678);
    handler.sendMessage(msg);
    handler.sendMessage(msg2);
    assertThat(handler.hasMessages(1234)).as("before-1234").isTrue();
    assertThat(handler.hasMessages(5678)).as("before-5678").isTrue();
    shadowQueue.reset();
    assertThat(handler.hasMessages(1234)).as("after-1234").isFalse();
    assertThat(handler.hasMessages(5678)).as("after-5678").isFalse();
  }
}
