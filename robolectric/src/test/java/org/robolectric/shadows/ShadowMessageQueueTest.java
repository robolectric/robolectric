package org.robolectric.shadows;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.Scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.*;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowMessageQueueTest {
  private MessageQueue queue;
  private ShadowMessageQueue shadowQueue;
  private Message testMessage;
  private TestHandler handler;
  private Scheduler scheduler;
  
  private static class TestHandler extends Handler {
    public List<Message> handled = new ArrayList<>();
    
    @Override
    public void handleMessage(Message msg) {
      handled.add(msg);
    }
  }
  
  @Before
  public void setUp() throws Exception {
    handler = new TestHandler();
    scheduler = shadowOf(handler.getLooper()).getScheduler();
    scheduler.pause();
    queue = callConstructor(MessageQueue.class, from(boolean.class, true));
    shadowQueue = shadowOf(queue);
    testMessage = handler.obtainMessage();
  }

  private static void shouldAssert(String method, ClassParameter<?>... params) {
    boolean ran = false;
    try {
      callStaticMethod(MessageQueue.class, method, params);
      ran = true;
    } catch (Throwable t) {
      assertThat(t).as(method).isInstanceOf(AssertionError.class);
    }
    assertThat(ran).as(method).overridingErrorMessage("Should have asserted but no exception was thrown").isFalse();
  }
  
  @Test
  @Config(emulateSdk=19)
  public void nativePollOnce_shouldAssert_19() {
    shouldAssert("nativePollOnce", from(int.class, 1), from(int.class, 2));
  }
  
  @Test
  @Config(emulateSdk=19)
  public void nativeWake_shouldAssert_19() {
    shouldAssert("nativeWake", from(int.class, 1));
  }
  
  @Test
  @Config(emulateSdk=21)
  public void nativePollOnce_shouldAssert_21() {
    shouldAssert("nativePollOnce", from(long.class, 1), from(int.class, 2));
  }
  
  @Test
  @Config(emulateSdk=21)
  public void nativeWake_shouldAssert_21() {
    shouldAssert("nativeWake", from(long.class, 1));
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

  public void enqueueMessage_setsWhen() {
    enqueueMessage(testMessage, 123);
    assertThat(testMessage.getWhen()).as("when").isEqualTo(123);
  }
  
  @Test
  public void enqueueMessage_returnsFalse_whenQuitting() {
    setField(queue, "mQuitting", true);
    assertThat(enqueueMessage(testMessage, 1)).as("enqueueMessage()").isFalse();
  }
  
  @Test
  public void enqueueMessage_doesntSchedule_whenQuitting() {
    setField(queue, "mQuitting", true);
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
  @Config(emulateSdk = 19)
  public void dispatchedMessage_isMarkedInUse_andRecycled_19() {
    dispatchedMessage_isMarkedInUse_andRecycled();
  }

  @Test
  @Config(emulateSdk = 21)
  public void dispatchedMessage_isMarkedInUse_andRecycled_21() {
    dispatchedMessage_isMarkedInUse_andRecycled();
  }

  private void dispatchedMessage_isMarkedInUse_andRecycled() {
    Handler handler = new Handler() {
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
    shadowOf(handler.getLooper().getQueue()).reset();
    assertThat(handler.hasMessages(1234)).as("after-1234").isFalse();
    assertThat(handler.hasMessages(5678)).as("after-5678").isFalse();
  }
}
